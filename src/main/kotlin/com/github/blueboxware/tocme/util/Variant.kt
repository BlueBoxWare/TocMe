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

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.ParserEmulationProfile
import com.vladsch.flexmark.util.data.MutableDataHolder

enum class Variant(private val emulationProfile: ParserEmulationProfile) {
  Commonmark(ParserEmulationProfile.COMMONMARK),
  Commonmark26(ParserEmulationProfile.COMMONMARK_0_26),
  Commonmark27(ParserEmulationProfile.COMMONMARK_0_27),
  Commonmark28(ParserEmulationProfile.COMMONMARK_0_28),
  Kramdown(ParserEmulationProfile.KRAMDOWN),
  Markdown(ParserEmulationProfile.MARKDOWN),
  GitHubDoc(ParserEmulationProfile.GITHUB_DOC),
  GitHub(ParserEmulationProfile.GITHUB),
  MultiMarkdown(ParserEmulationProfile.MULTI_MARKDOWN),
  Pegdown(ParserEmulationProfile.PEGDOWN),
  PegdownStrict(ParserEmulationProfile.PEGDOWN_STRICT),
  GitLab(ParserEmulationProfile.GITHUB);

  fun setIn(dataHolder: MutableDataHolder) {
    emulationProfile.setIn(dataHolder)

    if (this == GitLab) {
      dataHolder.set(HtmlRenderer.HEADER_ID_GENERATOR_NO_DUPED_DASHES, true)
    }
  }

}