package com.github.blueboxware.tocme.util

import com.vladsch.flexmark.ast.HtmlCommentBlock


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
internal class Tag(
        val container: HtmlCommentBlock,
        val tag: String,
        val isEndTag: Boolean,
        val args: String,
        val startOffset: Int,
        val endOffset: Int
) {

  val isStartTag = !isEndTag

  fun lineNumber() = container.document.getLineNumber(startOffset) + 1

  companion object {
    private val CONTENT_REGEX = Regex("""\s*(/?)([^\s]*)\s*(.*)""")

    fun createTag(container: HtmlCommentBlock, content: String, localStartOffset: Int, localEndOffset: Int): Tag? =
            CONTENT_REGEX.matchEntire(content)?.groupValues?.let { groupValues ->
              return Tag(
                      container,
                      groupValues[2],
                      !groupValues[1].isEmpty(),
                      groupValues[3],
                      container.startOffset + localStartOffset,
                      container.startOffset + localEndOffset
              )
            }
  }


}
