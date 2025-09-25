package com.github.blueboxware.tocme.util

import com.github.blueboxware.tocme.TocMeOptions
import com.vladsch.flexmark.ext.toc.internal.TocOptions
import com.vladsch.flexmark.html.HtmlRenderer.*
import com.vladsch.flexmark.parser.Parser.*
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.data.MutableDataSet
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class StaticOptions: TocMeOptions() {

  abstract override val tag: Property<String>

  abstract override val style: Property<TocOptions.ListType>

  abstract override val mode: Property<Mode>

  abstract override val levels: ListProperty<Int>

  abstract override val bold: Property<Boolean>

  abstract override val numbered: Property<Boolean>

  abstract override val plain: Property<Boolean>

  abstract override val removeEmojis: Property<Boolean>

  abstract override val requireSpace: Property<Boolean>

  abstract override val dupedDashes: Property<Boolean>

  abstract override val resolveDupes: Property<Boolean>

  abstract override val dashChars: Property<String>

  abstract override val allowedChars: Property<String>

  abstract override val allowLeadingSpace: Property<Boolean>

  abstract override val setextMarkerLength: Property<Int>

  abstract override val emptyHeadingWithoutSpace: Property<Boolean>

  abstract override val headingInterruptsItemParagraph: Property<Boolean>

  abstract override val variant: Property<Variant>

  override fun createStaticOptions(): StaticOptions = this

  fun toParserOptions(): DataHolder = MutableDataSet().apply {
    if (variant.isPresent) {
      variant.get().setIn(this)
    } else {
      Variant.GitHub.setIn(this)
    }

    requireSpace.getOrNull()?.let {
      set(HEADING_NO_ATX_SPACE, !it)
    }

    allowLeadingSpace.getOrNull()?.let {
      set(HEADING_NO_LEAD_SPACE, !it)
    }

    setextMarkerLength.getOrNull()?.let {
      set(HEADING_SETEXT_MARKER_LENGTH, it)
    }

    emptyHeadingWithoutSpace.getOrNull()?.let {
      set(HEADING_NO_EMPTY_HEADING_WITHOUT_SPACE, !it)
    }

    headingInterruptsItemParagraph.getOrNull()?.let {
      set(HEADING_CAN_INTERRUPT_ITEM_PARAGRAPH, it)
    }

    dupedDashes.getOrNull()?.let {
      set(HEADER_ID_GENERATOR_NO_DUPED_DASHES, !it)
    }

    resolveDupes.getOrNull()?.let {
      set(HEADER_ID_GENERATOR_RESOLVE_DUPES, it)
    }

    dashChars.getOrNull()?.let {
      set(HEADER_ID_GENERATOR_TO_DASH_CHARS, it)
    }

    allowedChars.getOrNull()?.let {
      set(HEADER_ID_GENERATOR_NON_DASH_CHARS, it)
    }
  }
}