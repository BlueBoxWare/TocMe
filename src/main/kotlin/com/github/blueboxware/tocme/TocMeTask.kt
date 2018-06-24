package com.github.blueboxware.tocme

import com.github.blueboxware.tocme.util.backupFiles
import com.github.blueboxware.tocme.util.insertTocs
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.SkipWhenEmpty
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
open class TocMeTask: DefaultTask() {

  init {
    description = "Creates and/or updates TOCs in markdown documents"
  }

  internal lateinit var tocMeExtension: TocMeExtension

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

    val relativeInputFile = inputFile.relativeToOrSelf(project.rootDir)

    val (result, warnings, error) = insertTocs(inputFile, outputFile, options)

    if (error != null) {
      throw GradleException(relativeInputFile.path + ": " + error)
    }

    if (result != null) {
      didWork = true
    }

    warnings.forEach { msg ->
      logger.warn(relativeInputFile.path + ": " + msg)
    }
  }

  @OutputFiles
  @SkipWhenEmpty
  @Suppress("MemberVisibilityCanBePrivate")
  fun getOutputFiles() = tocMeExtension.getOutputFiles()

  @Input
  private fun getOptionsAsString() = tocMeExtension.getOptionsAsString()

}