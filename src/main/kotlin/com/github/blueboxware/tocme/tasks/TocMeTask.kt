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

import com.github.blueboxware.tocme.TocMeExtension
import com.github.blueboxware.tocme.TocMePlugin
import org.gradle.api.DefaultTask
import org.gradle.api.internal.tasks.TaskExecutionOutcome
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.api.tasks.*

abstract class TocMeTask: DefaultTask() {

  init {
    group = TocMePlugin.TASK_GROUP
  }

  @get:[Internal]
  internal lateinit var tocMeExtension: TocMeExtension

  @OutputFiles
  fun getOutputFiles() = tocMeExtension.getOutputFiles()

  @Input
  internal fun getOptionsAsString() = tocMeExtension.getOptionsAsString()

  @Internal
  override fun getState(): TaskStateInternal {
    val state = super.getState()
    if (state.outcome != null && getOutputFiles().isEmpty()) {
      state.outcome = TaskExecutionOutcome.NO_SOURCE
    }
    return state
  }

}
