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
import com.vladsch.flexmark.ext.toc.internal.TocOptions
import org.gradle.api.GradleException
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import javax.inject.Inject
import kotlin.reflect.KProperty1

open class TocMeOptionsImpl @Inject constructor(
  private val parent: TocMeOptions?, @get:Internal protected val factory: ObjectFactory
): TocMeOptions() {

  override val tag: Property<String> = factory.property()
  override val style: Property<TocOptions.ListType> = factory.property()
  override val mode: Property<Mode> = factory.property()
  override val bold: Property<Boolean> = factory.property()
  override val numbered: Property<Boolean> = factory.property()
  override val plain: Property<Boolean> = factory.property()
  override val removeEmojis: Property<Boolean> = factory.property()
  override val requireSpace: Property<Boolean> = factory.property()
  override val dupedDashes: Property<Boolean> = factory.property()
  override val resolveDupes: Property<Boolean> = factory.property()
  override val dashChars: Property<String> = factory.property()
  override val allowedChars: Property<String> = factory.property()
  override val allowLeadingSpace: Property<Boolean> = factory.property()
  override val setextMarkerLength: Property<Int> = factory.property()
  override val emptyHeadingWithoutSpace: Property<Boolean> = factory.property()
  override val headingInterruptsItemParagraph: Property<Boolean> = factory.property()
  override val variant: Property<Variant> = factory.property()

  // https://github.com/gradle/gradle/issues/12222#issuecomment-674741780
  override val levels: ListProperty<Int> = factory.listProperty<Int>().convention(null)

  private inline fun <reified T: Any> set(
    options: TocMeOptions, parent: TocMeOptions?, property: KProperty1<TocMeOptions, Property<T>>
  ) = property.get(options).set(
    if (!property.get(this).isPresent && parent != null) {
      property.get(parent)
    } else {
      property.get(this)
    }
  )


  @Input
  override fun createStaticOptions(): StaticOptions {

    val parentOptions = (parent as? TocMeOptionsImpl)?.createStaticOptions() ?: parent
    val options = factory.newInstance<StaticOptions>()

    set(options, parentOptions, TocMeOptions::tag)
    set(options, parentOptions, TocMeOptions::variant)
    set(options, parentOptions, TocMeOptions::style)
    set(options, parentOptions, TocMeOptions::mode)
    set(options, parentOptions, TocMeOptions::bold)
    set(options, parentOptions, TocMeOptions::numbered)
    set(options, parentOptions, TocMeOptions::plain)
    set(options, parentOptions, TocMeOptions::removeEmojis)
    set(options, parentOptions, TocMeOptions::requireSpace)
    set(options, parentOptions, TocMeOptions::resolveDupes)
    set(options, parentOptions, TocMeOptions::dupedDashes)
    set(options, parentOptions, TocMeOptions::allowedChars)
    set(options, parentOptions, TocMeOptions::dashChars)
    set(options, parentOptions, TocMeOptions::allowLeadingSpace)
    set(options, parentOptions, TocMeOptions::setextMarkerLength)
    set(options, parentOptions, TocMeOptions::emptyHeadingWithoutSpace)
    set(options, parentOptions, TocMeOptions::headingInterruptsItemParagraph)

    options.levels.set(
      if (!levels.isPresent && parentOptions != null) {
        parentOptions.levels
      } else {
        levels
      }
    )

    return options
  }

  open fun levels(str: String): Collection<Int> =
    parseLevels(str) ?: throw GradleException("Invalid level specification: '$str'")

}
