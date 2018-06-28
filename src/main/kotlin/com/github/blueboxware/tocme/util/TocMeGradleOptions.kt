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
package com.github.blueboxware.tocme.util

import com.github.blueboxware.tocme.TocMeOptions
import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.util.ConfigureUtil
import java.io.File

class TocMeGradleOptions(
        val project: Project,
        defaultOptions: TocMeOptions,
        private val inputFileDefaultOptions: TocMeOptionsImpl = TocMeOptionsImpl(defaultOptions)
): TocMeOptions by inputFileDefaultOptions {

  internal val outputFiles: MutableMap<File, TocMeOptions> = mutableMapOf()

  @JvmOverloads
  fun output(outputFile: File, configurationClosure: Closure<in TocMeOptions>? = null) =
          TocMeOptionsImpl(this).let { options ->
            ConfigureUtil.configure(configurationClosure, options)
            outputFiles[outputFile] = options
          }

  fun output(outputFile: File, configurationClosure: TocMeOptions.() -> Unit) =
          TocMeOptionsImpl(this).let { options ->
            options.apply(configurationClosure)
            outputFiles[outputFile] = options
          }

  @JvmOverloads
  @Suppress("unused")
  fun output(outputFile: String, configurationClosure: Closure<in TocMeOptions>? = null) =
          output(project.file(outputFile), configurationClosure)

  fun output(outputFile: String, configurationClosure: TocMeOptions.() -> Unit) =
          output(project.file(outputFile), configurationClosure)

  @Suppress("MemberVisibilityCanBePrivate")
  fun outputs(vararg outputFiles: File) =
          outputFiles.forEach { output(it) }

  @Suppress("unused")
  fun outputs(vararg outputFiles: String) =
          outputs(*(outputFiles.map { project.file(it) }.toTypedArray()))

  @Input
  fun asString(): String =
          inputFileDefaultOptions.asString() +
                  SEPARATOR +
                  outputFiles.map { (file, options) ->
                    file.hashCode().toString() + SEPARATOR + (options as? TocMeOptionsImpl)?.asString()
                  }.joinToString(SEPARATOR)

  companion object {

    internal const val SEPARATOR = ";"

  }
}