package com.github.blueboxware.tocme.tasks

import com.github.blueboxware.tocme.TocMeOptions
import com.github.blueboxware.tocme.util.backupFiles
import com.github.blueboxware.tocme.util.insertTocs
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
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
open class InsertTocsTask: TocMeTask() {

  init {
    description = "Creates and/or updates TOCs in markdown documents"
  }

  @TaskAction
  fun generateTocs() {

    project.backupFiles(name, getOutputFiles())

    didWork = false

    tocMeExtension.getDocs().forEach { (inputFile, options) ->
      if (options.outputFiles.isEmpty()) {
        doInsertTocs(inputFile, inputFile, options)
      } else {
        options.outputFiles.forEach { (outputFile, options) ->
          doInsertTocs(inputFile, outputFile, options)
        }
      }
    }

  }

  private fun doInsertTocs(inputFile: File, outputFile: File, options: TocMeOptions) {

    val relativeInputPath = inputFile.relativeToOrSelf(project.rootDir).path

    val (result, warnings, error) = insertTocs(inputFile, outputFile, options, writeChanges = true)

    if (error != null) {
      throw GradleException("$relativeInputPath: $error")
    }

    if (result != null) {
      didWork = true
    }

    warnings.forEach { msg ->
      logger.warn("$relativeInputPath: $msg")
    }
  }

}