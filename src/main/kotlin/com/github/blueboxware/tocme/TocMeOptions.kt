package com.github.blueboxware.tocme

import com.github.blueboxware.tocme.util.Mode
import com.github.blueboxware.tocme.util.Variant
import com.vladsch.flexmark.ext.toc.internal.TocOptions
import com.vladsch.flexmark.util.options.DataHolder


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
interface TocMeOptions {

  var tag: String?

  var variant: Variant?

  var style: TocOptions.ListType?

  var mode: Mode?

  var levels: Int?

  var bold: Boolean?
  var numbered: Boolean?
  var plain: Boolean?

  var removeEmojis: Boolean?

  var requireSpace: Boolean?
  var dupedDashes: Boolean?
  var resolveDupes: Boolean?
  var dashChars: String?
  var allowedChars: String?
  var allowLeadingSpace: Boolean?
  var setextMarkerLength: Int?
  var emptyHeadingWithoutSpace: Boolean?
  var headingInterruptsItemParagraph: Boolean?

  fun tag(): String
  fun style(): TocOptions.ListType
  fun mode(): Mode
  fun levels(): Int
  fun bold(): Boolean
  fun numbered(): Boolean
  fun plain(): Boolean
  fun variant(): Variant
  fun removeEmojis(): Boolean

  fun requireSpace(): Boolean?
  fun dupedDashes(): Boolean?
  fun resolveDupes(): Boolean?
  fun dashChars(): String?
  fun allowedChars(): String?
  fun allowLeadingSpace(): Boolean?
  fun setextMarkerLength(): Int?
  fun emptyHeadingWithoutSpace(): Boolean?
  fun headingInterruptsItemParagraph(): Boolean?

  fun isLocal() = mode() == Mode.Local
  fun isFull() = mode() == Mode.Full

  fun isLevelIncluded(level: Int): Boolean

  fun parent(): TocMeOptions?

  fun toParserOptions(): DataHolder

}