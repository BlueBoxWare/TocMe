package com.github.blueboxware.tocme

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.GradleVersion

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
class TocMePlugin: Plugin<Project> {

  override fun apply(project: Project) {

    if (GradleVersion.current() < GradleVersion.version("3.0")) {
      throw GradleException("The com.github.blueboxware.tocme plugin requires Gradle version 3.0 or higher")
    }

    val tocmeExtension = project.extensions.create(TOCME_EXTENSION_NAME, TocMeExtension::class.java, project)
    project.tasks.create(INSERT_TOCS_TASK, TocMeTask::class.java) {
      it.tocMeExtension = tocmeExtension
    }

  }

  companion object {
    const val TOCME_EXTENSION_NAME = "tocme"
    const val INSERT_TOCS_TASK = "insertTocs"
  }

}