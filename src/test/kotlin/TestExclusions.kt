import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withData
import io.kotest.engine.spec.tempdir

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

@Suppress("unused")
internal object TestExclusions: BehaviorSpec({

  lateinit var fixture: ProjectFixture

  beforeContainer {
    fixture = ProjectFixture(tempdir(), useConfigurationCache = false)
    fixture.addFile("files/gdx.md")
  }

  given("a spec with excluded file") {

    withData(
      Pair("SCCS", true),
      Pair("files/.git/test", true),
      Pair("test/test~", true),
      Pair("files/gdx.md", false)
    ) { (filename, excluded) ->

      `when`("building ($filename)") {

        fixture.buildFile(
          """
        tocme {
          doc(file("$filename"))
        }
      """.trimIndent()
        )
        fixture.build(shouldFail = excluded)

        then("should fail with an error") {
          if (excluded) {
            fixture.assertOutputContains("GradleException: The file '$filename' won't be backed up")
          }
        }

      }

    }

  }

})

