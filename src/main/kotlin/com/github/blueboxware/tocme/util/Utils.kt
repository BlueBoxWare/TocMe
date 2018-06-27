package com.github.blueboxware.tocme.util

/*
 * Copyright 2018 Blue Box Ware
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.blueboxware.tocme.TocMeOptions
import com.github.blueboxware.tocme.TocMePlugin
import com.vladsch.flexmark.ast.Document
import com.vladsch.flexmark.ast.Heading
import com.vladsch.flexmark.ast.HtmlCommentBlock
import com.vladsch.flexmark.ast.Node
import com.vladsch.flexmark.ast.util.HeadingCollectingVisitor
import com.vladsch.flexmark.ast.util.TextCollectingVisitor
import com.vladsch.flexmark.ext.toc.internal.TocLevelsOptionParser
import com.vladsch.flexmark.ext.toc.internal.TocOptions
import com.vladsch.flexmark.formatter.internal.MarkdownWriter
import com.vladsch.flexmark.html.renderer.HeaderIdGenerator
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.collection.NodeCollectingVisitor
import com.vladsch.flexmark.util.options.ParsedOptionStatus
import com.vladsch.flexmark.util.sequence.BasedSequence
import com.vladsch.flexmark.util.sequence.CharSubSequence
import org.apache.tools.ant.DirectoryScanner
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import java.io.File
import java.lang.Integer.min

const val TOC_TAG = "toc"

private const val OPT_MODE = "mode"
private const val OPT_STYLE = "style"
private const val OPT_NUMBERED = "numbered"
private const val OPT_PLAIN = "plain"
private const val OPT_BOLD = "bold"
private const val OPT_LEVELS = "levels"
private const val OPT_VARIANT = "variant"

private const val MODE_FULL = "full"
private const val MODE_LOCAL = "local"
private const val MODE_NORMAL = "normal"

private const val STYLE_HIERARCHY = "hierarchy"
private const val STYLE_FLAT = "flat"
private const val STYLE_REVERSED = "reversed"
private const val STYLE_INCREASING = "increasing"
private const val STYLE_DECREASING = "decreasing"

private val STYLE_VALUES = listOf(STYLE_HIERARCHY, STYLE_FLAT, STYLE_REVERSED, STYLE_INCREASING, STYLE_DECREASING).joinToString { "'$it'" }
private val MODE_VALUES = listOf(MODE_NORMAL, MODE_FULL, MODE_LOCAL).joinToString { "'$it'" }

private const val INDENT = "  "

private const val BACKUP_DIR = "tocme/backups/"

internal fun insertTocs(
        inputFile: File,
        outputFile: File?,
        options: TocMeOptions,
        writeChanges: Boolean = false
): Triple<String?, List<String>, String?> {

  val parserOptions = options.toParserOptions()

  val document = inputFile.reader().use { reader ->
    Parser.builder(parserOptions).build().parseReader(reader)
  }

  val (result, warnings, error) = document.insertTocs(
          options,
          checkCurrentContent = writeChanges && inputFile == outputFile
  )

  if (writeChanges) {
    result?.let { txt ->
      outputFile?.writeText(txt)
    }
  }

  return Triple(result, warnings, error)

}

internal fun Document.insertTocs(options: TocMeOptions, checkCurrentContent: Boolean): Triple<String?, List<String>, String?> {

  val headings by lazy {
    HeaderIdGenerator().generateIds(this)
    HeadingCollectingVisitor().collectAndGetHeadings(this)
  }

  val str = StringBuilder(chars)
  val warnings = mutableListOf<String>()

  val (tags, tagError) = collectTags(options.tag())

  if (tagError != null) {
    return Triple(null, warnings, tagError)
  }

  tags?.toList()?.sortedBy { it.first.startOffset }?.reversed()?.forEach {

    val startTag = it.first
    val endTag = it.second

    checkBounds(startTag, endTag) { return Triple(null, warnings, "something went horribly wrong") }

    if (checkCurrentContent && !checkCurrentContent(startTag, endTag)) {
      warnings.add("It doesn't look like the current content between the ${options.tag()} tags on line ${startTag.lineNumber()} and line ${endTag.lineNumber()} is a toc. Not making changes here, just in case.")
      return@forEach
    }

    val (tocOptions, optionErrors) = parseArgs(TocMeOptionsImpl(options), startTag.args)

    warnings.addAll(0, optionErrors.map { "line ${startTag.lineNumber()}: $it" })

    var startHeader: Heading? = null
    var endHeader: Heading? = null
    if (tocOptions.isLocal()) {
      getEnclosingHeaders(headings, startTag, endTag)?.let {
        startHeader = it.first
        endHeader = it.second
      }
    }

    val filteredHeadings = filterHeadings(headings, tocOptions, endTag.container, startHeader, endHeader)
    val headerTexts = getHeaderTexts(filteredHeadings, tocOptions)

    val result = renderToc(filteredHeadings, headerTexts, tocOptions)

    str.replace(startTag.endOffset + 1, endTag.startOffset, result)
  }

  return Triple(str.toString(), warnings, null)

}

private fun Document.checkCurrentContent(startTag: Tag, endTag: Tag): Boolean {

  val regex = Regex("""^\s*(?:-|\d+\.)\s.*""")

  innerContent(startTag, endTag)?.let { content ->
    content.lines().forEach { line ->
      if (!line.isBlank() && !line.matches(regex)) {
        return false
      }
    }
    return true
  }

  return false

}

private fun getEnclosingHeaders(headings: List<Heading>, startTag: Tag, endTag: Tag): Pair<Heading, Heading?>? {

  val firstHeader = headings.findLast { it.startOffset < startTag.startOffset }

  firstHeader?.let { first ->
    headings.dropWhile { it.startOffset < endTag.startOffset }.dropWhile { it.level > first.level }.firstOrNull()?.let {
      return Pair(first, it)
    }
  }

  return null

}

private fun filterHeadings(
        headings: List<Heading>,
        options: TocMeOptions,
        endTag: HtmlCommentBlock,
        startHeader: Heading? = null,
        endHeader: Heading? = null
): List<Heading> =
        headings.filter { header ->
          options.isLevelIncluded(header.level)
                  && (options.isFull() || header.startOffset > endTag.startOffset)
                  && (!options.isLocal() || (header.startOffset > startHeader?.startOffset ?: 0 && header.startOffset < endHeader?.startOffset ?: Int.MAX_VALUE))
        }

private fun getHeaderTexts(headings: List<Heading>, options: TocMeOptions): List<String> {

  val isReversed = options.style() == TocOptions.ListType.SORTED_REVERSED || options.style() == TocOptions.ListType.FLAT_REVERSED
  val isSorted = options.style() == TocOptions.ListType.SORTED || options.style() == TocOptions.ListType.SORTED_REVERSED

  val texts = mutableListOf<String>()

  for (header in headings) {

    var text = TextCollectingVisitor().collectAndGetText(header).let {
      if (options.plain()) {
        it
      } else {
        "[$it](#${header.anchorRefId})"
      }
    }

    if (options.removeEmojis()) {
      text = text.removeEmojis()
    }

    if (options.bold()) {
      texts.add("__${text}__")
    } else {
      texts.add(text)
    }

  }

  if (isSorted) {
    texts.sort()
  }
  if (isReversed) {
    texts.reverse()
  }

  return texts
}

private fun parseArgs(options: TocMeOptions, text: String): Pair<TocMeOptions, List<String>> {

  val warnings = mutableListOf<String>()

  Regex("""\b([^\s=]+)\s*(?:=\s*(["'][^"']*["']|[^\s]+))?""")
          .findAll(text)
          .map { it.groupValues.let { it[1] to it[2].trim { it.isWhitespace() || it == '"' || it == '\'' } } }
          .forEach { (key, value) ->

            fun boolean(): Boolean? =
                    when {
                      value.toLowerCase() == "true" -> true
                      value.toLowerCase() == "false" -> false
                      value.isBlank() -> warnings.add("No value specified for option '$key'")
                      else -> warnings.add("Option '$key' should be 'true' or 'false'")
                    }

            when (key) {

              OPT_STYLE -> when (value) {
                STYLE_HIERARCHY -> options.style = TocOptions.ListType.HIERARCHY
                STYLE_FLAT -> options.style = TocOptions.ListType.FLAT
                STYLE_REVERSED -> options.style = TocOptions.ListType.FLAT_REVERSED
                STYLE_INCREASING -> options.style = TocOptions.ListType.SORTED
                STYLE_DECREASING -> options.style = TocOptions.ListType.SORTED_REVERSED
                else -> {
                  warnings.add(
                          if (value.isBlank()) {
                            "Missing argument for parameter $OPT_STYLE"
                          } else {
                            "Invalid argument for parameter $OPT_STYLE: '$value'"
                          } + ". Valid arguments are: $STYLE_VALUES."
                  )
                }
              }

              OPT_NUMBERED -> options.numbered = boolean()
              OPT_PLAIN -> options.plain = boolean()
              OPT_BOLD -> options.bold = boolean()

              OPT_LEVELS -> {
                if (value.isBlank()) {
                  warnings.add("Missing argument for parameter levels")
                } else {

                  TocLevelsOptionParser(OPT_LEVELS).parseOption(CharSubSequence.of(value), TocOptions(), null).let { result ->
                    result.second.firstOrNull()?.let { parsedOption ->
                      parsedOption.messages?.map { it.message }?.forEach {
                        warnings.add(it)
                      }
                      if (parsedOption.optionResult != ParsedOptionStatus.ERROR) {
                        options.levels = result.first.levels
                      }
                    }
                  }

                }
              }

              OPT_MODE -> when (value) {
                MODE_FULL -> options.mode = Mode.Full
                MODE_NORMAL -> options.mode = Mode.Normal
                MODE_LOCAL -> options.mode = Mode.Local
                else -> {
                  warnings.add(
                          if (value.isBlank()) {
                            "Missing argument for parameter $OPT_MODE"
                          } else {
                            "Invalid argument for parameter $OPT_STYLE: '$value'"
                          } + ". Valid arguments are: $MODE_VALUES."
                  )
                }
              }

              OPT_VARIANT -> warnings.add("The option '$OPT_VARIANT' can only be specified in Gradle, not in the '${options.tag()}' tag")

              else -> warnings.add("Unknown option: '$key'")

            }

          }

  return Pair(options, warnings)

}

private fun HtmlCommentBlock.collectTags(tagName: String) =
        Regex("""<!--+\s*(.*?)\s*--+>""").findAll(chars).mapNotNull { match ->
          val start = match.range.start
          val end = match.range.endInclusive + 1
          if (startOffset + start == 0 || document.chars[startOffset + start - 1] == '\n') {
            Tag.createTag(this, match.groupValues[1], start, end)
          } else {
            null
          }
        }.filter { tag ->
          tag.tag == tagName
        }.toList()

private fun Document.collectTags(tagName: String): Pair<Map<Tag, Tag>?, String?> {

  val result = mutableMapOf<Tag, Tag>()

  var startTag: Tag? = null

  val visitor = NodeCollectingVisitor(setOf(HtmlCommentBlock::class.java))
  visitor.collect(this)

  visitor.subClassingBag.items.filterIsInstance<HtmlCommentBlock>().flatMap { it.collectTags(tagName) }.forEach { tag ->

    if (tag.isStartTag) {
      if (startTag != null) {
        return Pair(null, "Opening $tagName tag found on line ${tag.lineNumber()} while previous $tagName tag (on line ${startTag?.lineNumber()}) wasn't closed yet")
      } else {
        startTag = tag
      }
    } else if (tag.isEndTag) {
      if (startTag == null) {
        return Pair(null, "Closing $tagName tag on line ${tag.lineNumber()} does not have a corresponding opening tag")
      }
      startTag?.let {
        result[it] = tag
      }
      startTag = null
    }

  }

  startTag?.let {
    return Pair(null, "Opening $tagName tag on line ${it.lineNumber()} does not have a corresponding closing tag")
  }

  return Pair(result, null)

}

private inline fun checkBounds(start: Tag, end: Tag, orElse: () -> Unit) {
  if (start.endOffset > end.startOffset) {
    assert(false) { "Start must come before end" }
    orElse()
  }
}

private fun Node.innerContent(start: Tag, end: Tag): BasedSequence? {
  checkBounds(start, end) { return null }

  return chars.subSequence(start.endOffset, end.startOffset)
}

private fun renderToc(headings: List<Heading>, headingTexts: List<String>, options: TocMeOptions): String {

  var initLevel = -1
  var lastLevel = -1

  val headingNumbers = MutableList(7) { 0 }
  val openedItems = MutableList(7) { false }

  val writer = MarkdownWriter(StringBuilder()).apply {
    indentPrefix = INDENT
  }

  fun listOpen(level: Int): String {
    openedItems[level] = true
    return if (options.numbered()) {
      val v = ++headingNumbers[level]
      "$v. "
    } else {
      "- "
    }
  }

  if (options.style() == TocOptions.ListType.HIERARCHY) {
    headings.firstOrNull()?.level?.let { firstLevel ->
      headings.minBy { it.level }?.level?.let { minLevel ->
        (minLevel until firstLevel).forEach { writer.indent() }
      }
    }
  }

  for (i in 0 until headings.size) {
    val header = headings[i]
    val headerText = headingTexts[i]
    val headerLevel = if (options.style() == TocOptions.ListType.HIERARCHY) {
      header.level
    } else {
      1
    }

    if (initLevel == -1) {
      initLevel = headerLevel
      lastLevel = headerLevel
    }

    if (lastLevel < headerLevel) {
      for (lv in lastLevel until headerLevel) {
        openedItems[lv + 1] = false
        writer.indent()
      }
    } else if (lastLevel == headerLevel) {
      if (i != 0) {
        writer.line()
      }
    } else {
      for (lv in lastLevel downTo headerLevel + 1) {
        if (openedItems[lv]) {
          headingNumbers[lv] = 0
        }
        writer.unIndent()
      }
      writer.line()
    }

    writer.line().append(listOpen(headerLevel))
    writer.append(headerText)

    lastLevel = headerLevel
  }

  writer.line()

  return writer.text

}

internal fun Project.backupFiles(subdir: String, files: Collection<File>) =
        uniquifyFileNames(files).let { fileMap ->

          // https://github.com/gradle/gradle/issues/2986
          DirectoryScanner.getDefaultExcludes().forEach { DirectoryScanner.removeDefaultExclude(it) }
          DirectoryScanner.addDefaultExclude("_Dummy McDummface_")

          val timeStamp = System.currentTimeMillis()

          val backupParentDir = "$buildDir/$BACKUP_DIR$subdir/"
          val backupdir = file("$backupParentDir$timeStamp/")

          file(backupParentDir)
                  .listFiles()
                  ?.mapNotNull { file -> file.name.toLongOrNull()?.let { it to file } }
                  ?.sortedByDescending { it.first }
                  ?.drop(TocMePlugin.NR_OF_BACKUPS)
                  ?.forEach {
                    logger.info("Removing old backups from " + it.second.relativeToOrSelf(projectDir).path)
                    delete(it.second)
                  }

          project.copy {
            it.duplicatesStrategy = DuplicatesStrategy.FAIL
            it.into(backupdir)
            fileMap.forEach { file, name ->
              if (file.exists()) {
                logger.lifecycle("Backing up ${file.relativeToOrSelf(projectDir).path} to " + backupdir.relativeToOrSelf(projectDir).path + File.separator + name)
                it.from(file.parentFile) {
                  it.include(file.name).rename { name }
                }
              }
            }
          }
        }

internal fun uniquifyFileNames(files: Collection<File>): Map<File, String> {

  val fileMap = mutableMapOf<File, String>()

  files.groupBy { it.name }.forEach { _, sameNameFiles ->
    if (sameNameFiles.size == 1) {
      fileMap[sameNameFiles.first()] = sameNameFiles.first().name
    } else {

      var prefixLength = 1
      var found = false

      loop@ while (!found) {
        sameNameFiles.groupBy {
          it.toPath().toAbsolutePath().let { absolutePath ->
            val nameCount = absolutePath.nameCount
            val filePrefixLength = min(prefixLength, nameCount)
            absolutePath
                    .subpath(nameCount - filePrefixLength, nameCount)
                    .joinToString(".")
                    .replace(Regex("""\.+"""), ".")
          }
        }.let { grouped ->
          if (grouped.size == sameNameFiles.size) {
            grouped.forEach { key, value ->
              fileMap[value.first()] = key
            }
            found = true
          } else {
            prefixLength++
          }
        }
      }

    }
  }

  fileMap.toList().groupBy { it.second }.forEach { _, entries ->
    if (entries.size > 1) {
      var count = 1
      entries.forEach { (file, name) ->
        var newName = "$name.count"
        while (newName in fileMap.values) {
          count++
          newName = "$name.count"
        }
        fileMap[file] = newName
      }
    }
  }

  return fileMap

}

private val EMOJI_REGEX = Regex(""":[\w\d_+-]+:""")
private val SPACES_REGEX = Regex(""" +""")

internal fun String.removeEmojis() =
        EMOJI_REGEX.replace(this, "").let { SPACES_REGEX.replace(it, " ") }

fun Boolean?.asString() = this?.toString()?.take(1) ?: "_"

fun Int?.asString() = this?.toString() ?: "_"

fun String?.asString() = "\"" + (this ?: "") + "\""

fun Variant?.asString() = this?.ordinal?.toString() ?: "_"

fun TocOptions.ListType?.asString() = this?.ordinal?.toString() ?: "_"

fun Mode?.asString() = this?.ordinal?.toString() ?: "_"
