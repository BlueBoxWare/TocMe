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
package com.github.blueboxware.tocme.tasks

import com.github.blueboxware.tocme.TocMeOptions
import com.github.blueboxware.tocme.TocMePlugin
import com.github.blueboxware.tocme.util.insertTocs
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CheckTocsTask: TocMeTask() {

  init {
    description = "Checks if TOCs are up to date"

    outputs.upToDateWhen { getOutputFiles().isEmpty() }
  }

  @TaskAction
  fun checkTocs() {

    didWork = false

    tocMeExtension.getDocs().forEach { (inputFile, options) ->
      if (options.outputFiles.isEmpty()) {
        doCheckTocs(inputFile, inputFile, options)
      } else {
        options.outputFiles.forEach { (outputFile, options) ->
          doCheckTocs(inputFile, outputFile, options)
        }
      }
    }

  }

  private fun doCheckTocs(inputFile: File, outputFile: File, options: TocMeOptions) {

    didWork = true

    val relativeInputPath = inputFile.relativeToOrSelf(project.rootDir).path

    val (result, warnings, error) = insertTocs(inputFile, outputFile, options)

    if (error != null) {
      logger.error("$relativeInputPath: $error")
      return
    }

    warnings.forEach { msg ->
      logger.warn("$relativeInputPath: $msg")
    }

    if (!outputFile.exists() || outputFile.readText() != result) {
      val relativeOutputPath = outputFile.relativeToOrSelf(project.rootDir).path
      logger.warn("$relativeOutputPath: ${TocMePlugin.OUT_OF_DATE_MSG}")
    }

  }

}