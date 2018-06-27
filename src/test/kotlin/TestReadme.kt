
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.data_driven.data
import org.jetbrains.spek.data_driven.on
import java.io.File

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
object TestReadme: Spek({

  lateinit var fixture: ProjectFixture

  beforeEachTest {
    fixture = ProjectFixture()
    File(fixture.project.rootDir, "doc").mkdir()
    listOf(
            "README.md",
            "doc/reference.md",
            "doc/intro.md",
            "doc/notes.md",
            "notes.in.md",
            "notes.md",
            "notes.src.md"
    ).forEach {
      fixture.createFile(it, "")
    }
  }

  afterEachTest {
    fixture.destroy()
  }

  val tests = File("README.md").readText().let { readmeTxt ->
    Regex("""```gradle(.*?)```""", RegexOption.DOT_MATCHES_ALL).findAll(readmeTxt).map { it.groupValues[1] }.map { content ->
      data<String, String?>(content, expected = null)
    }
  }.toList().toTypedArray()

  given("a fragment from the README") {

    on("building (%s)", with = *tests) { content, expected ->

      fixture.buildFile(content)
      val output = fixture.build()

      println(output.output)

      it("should succeed") {
        fixture.assertBuildSuccess()
      }

    }

  }

})