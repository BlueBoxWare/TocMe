import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withData
import io.kotest.engine.spec.tempdir
import java.io.File

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
internal object TestReadme: BehaviorSpec({

  lateinit var fixture: ProjectFixture

  fun loadTests(type: String) = File("README.md.src").readText().let { readmeTxt ->
    Regex("""```$type(.*?)```""", RegexOption.DOT_MATCHES_ALL).findAll(readmeTxt).map { it.groupValues[1] }
  }

  Given("a Gradle fragment from the README") {

    withData(
      nameFn = { "Kotlin: ${it.first}, ${it.second}" },
      loadTests("gradle").flatMap { listOf(true to it, false to it) }) { (useKotlin, content) ->

      beforeContainer {
        fixture = ProjectFixture(tempdir(), useKotlin = useKotlin)
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

      When("building") {

        fixture.buildFile(content)
        fixture.build()

        Then("should succeed") {
          fixture.assertBuildSuccess()
        }

      }

    }
  }

  Given("a MarkDown fragment from the README") {

    beforeContainer {
      fixture.buildFile(
        """
        tocme {
          doc("test.md")
        }
      """.trimIndent()
      )
    }

    withData(loadTests("markdown")) { content ->

      When("building ($content)") {

        fixture.createFile("test.md", content)
        fixture.build()

        Then("should succeed") {
          fixture.assertBuildSuccess()
        }

        Then("should generate no warnings") {
          fixture.assertOutputContainsNot(Regex("^test.md:", RegexOption.MULTILINE))
        }

      }
    }
  }

})
