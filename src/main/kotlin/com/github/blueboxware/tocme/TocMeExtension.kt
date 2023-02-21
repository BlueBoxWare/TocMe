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
package com.github.blueboxware.tocme

import com.github.blueboxware.tocme.util.*
import com.vladsch.flexmark.ext.toc.internal.TocOptions
import org.gradle.api.Action
import org.gradle.api.Project
import java.io.File

abstract class TocMeExtension(
  private val project: Project,
  defaultOptions: TocMeOptionsImpl
): TocMeOptionsImpl(defaultOptions) {

  @Suppress("unused")
  constructor(project: Project): this(project, TocMeOptionsImpl(null))

  private val registeredDocs = mutableListOf<Pair<File, TocMeGradleOptions>>()

  @JvmOverloads
  fun doc(inputFile: File, action: Action<TocMeGradleOptions>? = null) {
    project.objects.newInstance(TocMeGradleOptions::class.java, project, this).let { options ->
      action?.execute(options)
      registeredDocs.add(inputFile to options)
    }
  }

  @JvmOverloads
  fun doc(inputFile: String, action: Action<TocMeGradleOptions>? = null) =
    doc(project.file(inputFile), action)

  fun docs(vararg inputFiles: File) =
    inputFiles.forEach { doc(it) }

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
    super.asString() +
            TocMeGradleOptions.SEPARATOR +
            registeredDocs.joinToString(TocMeGradleOptions.SEPARATOR) { (file, options) ->
              file.hashCode().toString() + TocMeGradleOptions.SEPARATOR + options.asString()
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
