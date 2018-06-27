package com.github.blueboxware.tocme

import com.github.blueboxware.tocme.util.Mode
import com.github.blueboxware.tocme.util.TocMeGradleOptions
import com.github.blueboxware.tocme.util.TocMeOptionsImpl
import com.github.blueboxware.tocme.util.Variant
import com.vladsch.flexmark.ext.toc.internal.TocLevelsOptionParser
import com.vladsch.flexmark.ext.toc.internal.TocOptions
import com.vladsch.flexmark.util.options.ParsedOptionStatus
import com.vladsch.flexmark.util.sequence.CharSubSequence
import groovy.lang.Closure
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import java.io.File


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
open class TocMeExtension(
        private val project: Project,
        private val defaultOptions: TocMeOptionsImpl
): TocMeOptions by defaultOptions {

  @Suppress("unused")
  constructor(project: Project): this(project, TocMeOptionsImpl(null))

  private val registeredDocs = mutableListOf<Pair<File, TocMeGradleOptions>>()

  @JvmOverloads
  @Suppress("MemberVisibilityCanBePrivate")
  fun doc(inputFile: File, configurationClosure: Closure<in TocMeGradleOptions>? = null) =
          TocMeGradleOptions(project, this).let { options ->
            ConfigureUtil.configure(configurationClosure, options)
            registeredDocs.add(inputFile to options)
          }

  @Suppress("MemberVisibilityCanBePrivate")
  fun doc(inputFile: File, configurationClosure: TocMeGradleOptions.() -> Unit) =
          TocMeGradleOptions(project, this).let { options ->
            options.apply(configurationClosure)
            registeredDocs.add(inputFile to options)
          }

  @JvmOverloads
  @Suppress("unused")
  fun doc(inputFile: String, configurationClosure: Closure<in TocMeGradleOptions>? = null) =
          doc(project.file(inputFile), configurationClosure)

  @Suppress("unused")
  fun doc(inputFile: String, configurationClosure: TocMeGradleOptions.() -> Unit) =
          doc(project.file(inputFile), configurationClosure)

  @Suppress("MemberVisibilityCanBePrivate")
  fun docs(vararg inputFiles: File) =
          inputFiles.forEach { doc(it) }

  @Suppress("unused")
  fun docs(vararg inputFiles: String) =
          docs(*(inputFiles.map { project.file(it) }.toTypedArray()))

  fun getDocs(): List<Pair<File, TocMeGradleOptions>> = registeredDocs

  internal fun getOutputFiles() =
          registeredDocs.flatMap { (inputFile, gradleOptions) ->
            gradleOptions.outputFiles.map { it.key }.toMutableList().apply {
              add(inputFile)
            }
          }

  internal fun getOptionsAsString() =
          defaultOptions.asString() +
                  TocMeGradleOptions.SEPARATOR +
                  registeredDocs.joinToString(TocMeGradleOptions.SEPARATOR) { (file, options) ->
                    file.hashCode().toString() + TocMeGradleOptions.SEPARATOR + options.asString()
                  }

  open fun levels(str: String): Int {
    TocLevelsOptionParser("levels").parseOption(CharSubSequence.of(str), TocOptions(), null).let { result ->
      result.second.firstOrNull()?.let { parsedOption ->
        parsedOption.messages?.map { it.message }?.forEach {
          project.logger.warn(it)
        }
        if (parsedOption.optionResult != ParsedOptionStatus.ERROR) {
          return result.first.levels
        }
      }
    }

    throw GradleException("Invalid level specification: '$str'")
  }

  @Suppress("unused")
  companion object {

    @JvmField
    val Local: Mode = Mode.Local
    @JvmField
    val Normal: Mode = Mode.Normal
    @JvmField
    val Full: Mode = Mode.Full

    @JvmField
    val Hierarchy = TocOptions.ListType.HIERARCHY
    @JvmField
    val Flat = TocOptions.ListType.FLAT
    @JvmField
    val Reversed = TocOptions.ListType.FLAT_REVERSED
    @JvmField
    val Increasing = TocOptions.ListType.SORTED
    @JvmField
    val Decreasing = TocOptions.ListType.SORTED_REVERSED

    @JvmField
    val Commonmark = Variant.Commonmark
    @JvmField
    val Commonmark26 = Variant.Commonmark26
    @JvmField
    val Commonmark27 = Variant.Commonmark27
    @JvmField
    val Commonmark28 = Variant.Commonmark28
    @JvmField
    val Kramdown = Variant.Kramdown
    @JvmField
    val Markdown = Variant.Markdown
    @JvmField
    val GitHub = Variant.GitHub
    @JvmField
    val GitHubDoc = Variant.GitHubDoc
    @JvmField
    val MultiMarkdown = Variant.MultiMarkdown
    @JvmField
    val Pegdown = Variant.Pegdown
    @JvmField
    val PegdownStrict = Variant.PegdownStrict
    @JvmField
    val GitLab = Variant.GitLab

  }

}