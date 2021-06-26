/*
 * Copyright 2021 Blue Box Ware
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

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.data_driven.data
import org.jetbrains.spek.data_driven.on

object TestGradleExclusions: Spek({

  lateinit var fixture: ProjectFixture

  beforeEachTest {
    fixture = ProjectFixture()
    fixture.addFile("files/gdx.md")
  }

  afterEachTest {
    fixture.destroy()
  }

  given("a spec with excluded file") {

    val tests = arrayOf(
      data("SCCS", true),
      data("files/.git/test", true),
      data("test/test~", true),
      data("files/gdx.md", false)
    )

    on("building (%s)", with = *tests) { filename: String, excluded: Boolean ->

      fixture.buildFile(
        """
        tocme {
          doc(file("$filename"))
        }
      """.trimIndent()
      )
      val result = fixture.build(shouldFail = excluded)

      it("should fail with an error") {
        if (excluded) {
          fixture.assertOutputContains("GradleException: The file '$filename' won't be backed up")
        }
      }

    }

  }
})