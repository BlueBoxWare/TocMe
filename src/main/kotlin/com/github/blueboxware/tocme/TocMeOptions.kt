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

import com.github.blueboxware.tocme.util.Mode
import com.github.blueboxware.tocme.util.StaticOptions
import com.github.blueboxware.tocme.util.Variant
import com.vladsch.flexmark.ext.toc.internal.TocOptions
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal

@Suppress("EmptyMethod")
abstract class TocMeOptions {

  @get:Internal
  abstract val tag: Property<String>

  @get:Internal
  abstract val variant: Property<Variant>

  @get:Internal
  abstract val style: Property<TocOptions.ListType>

  @get:Internal
  abstract val mode: Property<Mode>

  @get:Internal
  abstract val levels: ListProperty<Int>

  @get:Internal
  abstract val dashChars: Property<String>

  @get:Internal
  abstract val allowedChars: Property<String>

  @get:Internal
  abstract val setextMarkerLength: Property<Int>

  @get:Internal
  abstract val bold: Property<Boolean>

  @get:Internal
  abstract val numbered: Property<Boolean>

  @get:Internal
  abstract val plain: Property<Boolean>

  @get:Internal
  abstract val removeEmojis: Property<Boolean>

  @get:Internal
  abstract val requireSpace: Property<Boolean>

  @get:Internal
  abstract val dupedDashes: Property<Boolean>

  @get:Internal
  abstract val resolveDupes: Property<Boolean>

  @get:Internal
  abstract val allowLeadingSpace: Property<Boolean>

  @get:Internal
  abstract val emptyHeadingWithoutSpace: Property<Boolean>

  @get:Internal
  abstract val headingInterruptsItemParagraph: Property<Boolean>

  abstract fun createStaticOptions(): StaticOptions
}