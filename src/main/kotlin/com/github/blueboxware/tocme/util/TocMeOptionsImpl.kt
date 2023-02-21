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
import com.vladsch.flexmark.html.HtmlRenderer.*
import com.vladsch.flexmark.parser.Parser.*
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.data.MutableDataSet
import org.gradle.api.GradleException
import javax.inject.Inject

open class TocMeOptionsImpl @Inject constructor(private val parent: TocMeOptions?): TocMeOptions {

  override var tag: String? = null

  override var variant: Variant? = null

  override var style: TocOptions.ListType? = null

  override var mode: Mode? = null

  override var levels: Collection<Int>? = null

  override var bold: Boolean? = null
  override var numbered: Boolean? = null
  override var plain: Boolean? = null

  override var removeEmojis: Boolean? = null

  override var requireSpace: Boolean? = null
  override var dupedDashes: Boolean? = null
  override var resolveDupes: Boolean? = null
  override var dashChars: String? = null
  override var allowedChars: String? = null
  override var allowLeadingSpace: Boolean? = null
  override var setextMarkerLength: Int? = null
  override var emptyHeadingWithoutSpace: Boolean? = null
  override var headingInterruptsItemParagraph: Boolean? = null

  override fun parent(): TocMeOptions? = parent

  override fun tag(): String = tag ?: parent?.tag() ?: TOC_TAG

  override fun variant(): Variant = variant ?: parent?.variant() ?: Variant.GitHub

  override fun style(): TocOptions.ListType = style ?: parent?.style() ?: TocOptions.ListType.HIERARCHY

  override fun mode(): Mode = mode ?: parent?.mode() ?: Mode.Normal

  override fun levels(): Collection<Int> = levels ?: parent?.levels() ?: setOf(1, 2, 3)

  open fun levels(str: String): Collection<Int> =
    parseLevels(str) ?: throw GradleException("Invalid level specification: '$str'")

  override fun bold(): Boolean = bold ?: parent?.bold() ?: true
  override fun numbered(): Boolean = numbered ?: parent?.numbered() ?: false
  override fun plain(): Boolean = plain ?: parent?.plain() ?: false

  override fun removeEmojis(): Boolean = removeEmojis ?: parent?.removeEmojis() ?: false

  override fun requireSpace(): Boolean? = requireSpace ?: parent?.requireSpace()
  override fun dupedDashes(): Boolean? = dupedDashes ?: parent?.dupedDashes()
  override fun resolveDupes(): Boolean? = resolveDupes ?: parent?.resolveDupes()
  override fun dashChars(): String? = dashChars ?: parent?.dashChars()
  override fun allowedChars(): String? = allowedChars ?: parent?.allowedChars
  override fun allowLeadingSpace(): Boolean? = allowLeadingSpace ?: parent?.allowLeadingSpace()
  override fun setextMarkerLength(): Int? = setextMarkerLength ?: parent?.setextMarkerLength()
  override fun emptyHeadingWithoutSpace(): Boolean? = emptyHeadingWithoutSpace ?: parent?.emptyHeadingWithoutSpace()
  override fun headingInterruptsItemParagraph(): Boolean? =
    headingInterruptsItemParagraph ?: parent?.headingInterruptsItemParagraph()

  override fun toParserOptions(): DataHolder =
    MutableDataSet().apply {
      variant().setIn(this)

      requireSpace()?.let {
        set(HEADING_NO_ATX_SPACE, !it)
      }

      allowLeadingSpace()?.let {
        set(HEADING_NO_LEAD_SPACE, !it)
      }

      setextMarkerLength()?.let {
        set(HEADING_SETEXT_MARKER_LENGTH, it)
      }

      emptyHeadingWithoutSpace()?.let {
        set(HEADING_NO_EMPTY_HEADING_WITHOUT_SPACE, !it)
      }

      headingInterruptsItemParagraph()?.let {
        set(HEADING_CAN_INTERRUPT_ITEM_PARAGRAPH, it)
      }

      dupedDashes()?.let {
        set(HEADER_ID_GENERATOR_NO_DUPED_DASHES, !it)
      }

      resolveDupes()?.let {
        set(HEADER_ID_GENERATOR_RESOLVE_DUPES, it)
      }

      dashChars()?.let {
        set(HEADER_ID_GENERATOR_TO_DASH_CHARS, it)
      }

      allowedChars()?.let {
        set(HEADER_ID_GENERATOR_NON_DASH_CHARS, it)
      }
    }

  open fun asString(): String =
    variant.asString() +
            plain.asString() +
            levels.toString() +
            numbered.asString() +
            style.asString() +
            bold.asString() +
            mode.asString() +
            removeEmojis.asString() +
            requireSpace.asString() +
            dupedDashes.asString() +
            resolveDupes.asString() +
            dashChars.asString() +
            allowLeadingSpace.asString() +
            allowedChars.asString() +
            setextMarkerLength.asString() +
            emptyHeadingWithoutSpace.asString() +
            headingInterruptsItemParagraph.asString()
}
