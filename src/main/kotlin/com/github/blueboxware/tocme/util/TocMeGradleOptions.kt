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
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import java.io.File
import javax.inject.Inject

abstract class TocMeGradleOptions @Inject constructor (
  val project: Project,
  defaultOptions: TocMeOptionsImpl,
): TocMeOptionsImpl(defaultOptions) {

  internal val outputFiles: MutableMap<File, TocMeOptions> = mutableMapOf()

  @JvmOverloads
  fun output(outputFile: File, action: Action<TocMeOptionsImpl>? = null) {
    project.objects.newInstance(TocMeOptionsImpl::class.java, this).let { options ->
      action?.execute(options)
      outputFiles[outputFile] = options
    }
  }

  @JvmOverloads
  fun output(outputFile: String, action: Action<TocMeOptionsImpl>? = null) =
    output(project.file(outputFile), action)

  @Suppress("MemberVisibilityCanBePrivate")
  fun outputs(vararg outputFiles: File) =
    outputFiles.forEach { output(it) }

  @Suppress("unused")
  fun outputs(vararg outputFiles: String) =
    outputs(*(outputFiles.map { project.file(it) }.toTypedArray()))

  @Input
  override fun asString(): String =
    super.asString() +
            SEPARATOR +
            outputFiles.map { (file, options) ->
              file.hashCode().toString() + SEPARATOR + (options as? TocMeOptionsImpl)?.asString()
            }.joinToString(SEPARATOR)

  companion object {

    internal const val SEPARATOR = ";"

  }
}
