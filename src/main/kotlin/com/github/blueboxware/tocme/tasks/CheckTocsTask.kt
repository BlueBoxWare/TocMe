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
import org.gradle.api.GradleException
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

@CacheableTask
abstract class CheckTocsTask @Inject constructor(
  private val projectLayout: ProjectLayout,
  private val objectFactory: ObjectFactory
): TocMeTask() {

  init {
    description = "Checks if TOCs are up-to-date"

    outputs.upToDateWhen { tocMeExtension.get().getOutputFiles(alwaysIncludeInputFile = false).isEmpty() }
  }

  @TaskAction
  fun checkTocs() {

    var outOfDate = false
    val extension = tocMeExtension.get()

    didWork = false

    extension.registeredDocs.get().forEach { (inputFile, options) ->
      if (options.outputFiles.get().isEmpty()) {
        outOfDate = doCheckTocs(inputFile, inputFile, options, objectFactory) || outOfDate
      } else {
        options.outputFiles.get().forEach { (outputFile, options) ->
          outOfDate = doCheckTocs(inputFile, outputFile, options, objectFactory) || outOfDate
        }
      }
    }

    if (outOfDate) {
      throw GradleException("One or more Table of Contents are out of date. Run the ${TocMePlugin.INSERT_TOCS_TASK} task to update.")
    }

  }

  private fun doCheckTocs(
    inputFile: File,
    outputFile: File,
    options: TocMeOptions,
    objectFactory: ObjectFactory
  ): Boolean {

    didWork = true

    val projectDir = projectLayout.projectDirectory.asFile
    val relativeInputPath = inputFile.relativeToOrSelf(projectDir).path

    val (result, warnings, error) = insertTocs(inputFile, outputFile, options.createStaticOptions(), objectFactory)

    if (error != null) {
      logger.error("$relativeInputPath: $error")
      return false
    }

    warnings.forEach { msg ->
      logger.warn("$relativeInputPath: $msg")
    }

    if (!outputFile.exists() || outputFile.readText() != result) {
      val relativeOutputPath = outputFile.relativeToOrSelf(projectDir).path
      logger.warn("$relativeOutputPath: ${TocMePlugin.OUT_OF_DATE_MSG}")
      return true
    }

    return false

  }

}
