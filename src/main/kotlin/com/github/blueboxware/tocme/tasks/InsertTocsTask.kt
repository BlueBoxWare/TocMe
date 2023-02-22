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
import com.github.blueboxware.tocme.util.BACKUP_DIR
import com.github.blueboxware.tocme.util.insertTocs
import com.github.blueboxware.tocme.util.uniquifyFileNames
import org.apache.tools.ant.DirectoryScanner
import org.gradle.api.GradleException
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.ProjectLayout
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

open class InsertTocsTask @Inject constructor(
  private val projectLayout: ProjectLayout,
  private val fileOperations: FileOperations
): TocMeTask() {

  init {
    description = "Creates and/or updates TOCs in Markdown documents"
  }

  @TaskAction
  fun generateTocs() {

    backupFiles(name, getOutputFiles())

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

    val relativeInputPath = inputFile.relativeToOrSelf(projectLayout.projectDirectory.asFile).path

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


  private fun backupFiles(subdir: String, files: Collection<File>) {
    checkInclusion(files)
    uniquifyFileNames(files).let { fileMap ->

      val projectDir = projectLayout.projectDirectory.asFile
      val buildDir = projectLayout.buildDirectory.asFile.get()

      val timeStamp = System.currentTimeMillis()

      val backupParentDir = "$buildDir/$BACKUP_DIR$subdir/"
      val backupdir = fileOperations.file("$backupParentDir$timeStamp/")

      fileOperations.file(backupParentDir)
        .listFiles()
        ?.mapNotNull { file -> file.name.toLongOrNull()?.let { it to file } }
        ?.sortedByDescending { it.first }
        ?.drop(TocMePlugin.NR_OF_BACKUPS)
        ?.forEach {
          logger.info("Removing old backups from " + it.second.relativeToOrSelf(projectDir).path)
          fileOperations.delete(it.second)
        }

      fileOperations.copy { copySpec ->
        copySpec.duplicatesStrategy = DuplicatesStrategy.FAIL
        copySpec.into(backupdir)
        fileMap.forEach { (file, name) ->
          if (file.exists()) {
            logger.lifecycle(
              "Backing up ${file.relativeToOrSelf(projectDir).path} to " + backupdir.relativeToOrSelf(
                projectDir
              ).path + File.separator + name
            )
            copySpec.from(file.parentFile) {
              it.include(file.name).rename { name }
            }
          }
        }
      }
    }
  }

  private fun checkInclusion(files: Collection<File>) {
    for (file in files) {
      for (pattern in DirectoryScanner.getDefaultExcludes()) {
        if (DirectoryScanner.match(pattern, file.path)) {
          throw GradleException("The file '${file.relativeToOrSelf(projectLayout.projectDirectory.asFile)}' won't be backed up before changing because it is excluded by DirectoryScanner default rule '$pattern' (https://github.com/gradle/gradle/issues/2985). Please use a different filename.")
        }
      }
    }
  }

}
