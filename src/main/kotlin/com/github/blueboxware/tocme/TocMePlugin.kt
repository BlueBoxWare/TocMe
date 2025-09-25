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

import com.github.blueboxware.tocme.tasks.CheckTocsTask
import com.github.blueboxware.tocme.tasks.InsertTocsTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.util.GradleVersion

class TocMePlugin: Plugin<Project> {

  override fun apply(project: Project) {

    if (GradleVersion.current() < GradleVersion.version("8.3")) {
      throw GradleException("The com.github.blueboxware.tocme plugin requires Gradle version 8.3 or higher")
    }

    val extension = project.extensions.create<TocMeExtension>(TOCME_EXTENSION_NAME)

    project.tasks.register<InsertTocsTask>(INSERT_TOCS_TASK) {
      tocMeExtension.set(extension)
    }

    project.tasks.register<CheckTocsTask>(CHECK_TOCS_TASK) {
      tocMeExtension.set(extension)
    }


    project.tasks.findByName(LifecycleBasePlugin.ASSEMBLE_TASK_NAME)?.let { assembleTask ->
      project.tasks.findByName(CHECK_TOCS_TASK)?.let {
        assembleTask.dependsOn(it)
      }

    }
  }

  companion object {
    const val TOCME_EXTENSION_NAME = "tocme"

    const val INSERT_TOCS_TASK = "insertTocs"
    const val CHECK_TOCS_TASK = "checkTocs"

    const val TASK_GROUP = "documentation"

    const val OUT_OF_DATE_MSG = "Table of Contents is out of date. Run the $INSERT_TOCS_TASK task to update."

    const val NR_OF_BACKUPS = 10
  }

}
