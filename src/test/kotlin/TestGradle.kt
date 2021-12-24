import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.BehaviorSpec
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
@OptIn(ExperimentalKotest::class)
@Suppress("unused")
internal object TestGradle: BehaviorSpec({

  lateinit var fixture: ProjectFixture

  beforeContainer {
    fixture = ProjectFixture(tempdir())
    fixture.addFile("files/gdx.md")
    fixture.addFile("files/nerdfonts.md")
    fixture.addFile("files/variant_issues.md")
    fixture.addFile("files/react.md")
  }

  given("no spec") {

    beforeContainer {
      fixture.buildFile("")
    }

    `when`("checking") {

      fixture.buildCheck()

      then("should report no source") {
        fixture.assertBuildNoSourceAfter33()
      }

    }

    `when`("building") {

      fixture.build()

      then("should report no source") {
        fixture.assertBuildNoSourceAfter33()
      }

    }

  }

  given("an empty spec") {

    beforeContainer {
      fixture.buildFile(
        """
        tocme {
        }
      """.trimIndent()
      )
    }

    `when`("building") {

      fixture.build()

      then("should report no source") {
        fixture.assertBuildNoSourceAfter33()
      }

    }

    `when`("building") {

      fixture.build()

      then("should report no source") {
        fixture.assertBuildNoSourceAfter33()
      }

    }

  }

  given("a trivial build file") {

    beforeContainer {
      fixture.buildFile(
        """
        tocme {
          doc(file("files/gdx.md"))
        }
      """.trimIndent()
      )
    }

    `when`("checking") {

      fixture.buildCheck(shouldFail = true)

      then("should report the TOC is out of date") {
        fixture.assertCheckOutputFileIsOutOfDate("gdx.md")
      }

      then("should have not made any changes") {
        fixture.assertFileEquals("files/gdx.md", "files/gdx.md")
      }

    }

    `when`("checking twice") {

      fixture.buildCheck(shouldFail = true)
      fixture.buildCheck(shouldFail = true)

      then("should report the TOC is out of date") {
        fixture.assertCheckOutputFileIsOutOfDate("gdx.md")
      }

      then("should have not made any changes") {
        fixture.assertFileEquals("files/gdx.md", "files/gdx.md")
      }

    }

    `when`("building") {

      fixture.build()

      then("should insert the tocs correctly") {
        fixture.assertBuildSuccess()
        fixture.assertFileEquals("files/gdx.out", "files/gdx.md")
      }

      then("should create a backup") {
        fixture.project.file(fixture.backupDir).listFiles().first().relativeToOrSelf(fixture.project.rootDir).let {
          fixture.assertFileEquals("files/gdx.md", it.path + "/gdx.md")
        }
      }

    }

    `when`("checking after building") {
      fixture.build()
      fixture.buildCheck()

      then("should not report the TOC is out of date") {
        fixture.assertCheckOutputNothingIsOutOfDate()
      }
    }

    `when`("building twice") {

      fixture.build()
      fixture.build()

      then("should be up-to-date the second time") {
        fixture.assertBuildUpToDate()
      }

    }

    `when`("checking after building and changing the input doc (unchanged TOC)") {

      fixture.build()
      fixture.file("files/gdx.md").let { file ->
        file.writeText(file.readText().replace("Table of Contents", "TOC"))
      }
      fixture.buildCheck()

      then("should not report the TOC is out of date") {
        fixture.assertCheckOutputNothingIsOutOfDate()
      }

    }

    `when`("checking after building and changing the input doc (changed TOC)") {

      fixture.build()
      fixture.file("files/gdx.md").let { file ->
        file.writeText(file.readText().replace("Getting started", "Getting stopped"))
      }
      fixture.buildCheck(shouldFail = true)

      then("should report the TOC is out of date") {
        fixture.assertCheckOutputFileIsOutOfDate("gdx.md")
      }

    }

    `when`("building twice and changing the input doc in between") {

      fixture.build()
      fixture.file("files/gdx.md").let { file ->
        file.writeText(file.readText().replace("Table of Contents", "TOC"))
      }
      fixture.build()

      then("should build again") {
        fixture.assertBuildSuccess()
      }

    }

    `when`("checking after building and changing the default settings") {

      fixture.build()
      fixture.replaceInBuildFile("tocme {", "tocme {\nbold=false\n")
      fixture.buildCheck(shouldFail = true)

      then("should report the TOC is out of date") {
        fixture.assertCheckOutputFileIsOutOfDate("gdx.md")
      }

    }

    `when`("building twice and changing default settings in between") {

      fixture.build()
      fixture.replaceInBuildFile("tocme {", "tocme {\nbold=false\n")
      fixture.build()

      then("should build again") {
        fixture.assertBuildSuccess()
      }

      then("should create the correct output") {
        fixture.assertFileEquals("files/gdx.default_nobold.out", "files/gdx.md")
      }

    }

    `when`("checking after building and changing the input file's default settings") {

      fixture.build()
      fixture.replaceInBuildFile("))", """)) { numbered=true }""")
      fixture.buildCheck(shouldFail = true)

      then("should report the TOC is out of date") {
        fixture.assertCheckOutputFileIsOutOfDate("gdx.md")
      }

    }

    `when`("building twice and changing the input file's default settings in between") {

      fixture.build()
      fixture.replaceInBuildFile("))", """)) { numbered=true }""")
      fixture.build()

      then("should build again") {
        fixture.assertBuildSuccess()
      }

      then("should create the correct output") {
        fixture.assertFileEquals("files/gdx.default_numbered.out", "files/gdx.md")
      }

    }

    `when`("building twice and adding an output file in between") {

      fixture.build()
      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) }""")
      fixture.build()

      then("should build again") {
        fixture.assertBuildSuccess()
      }

      then("should create the correct output") {
        fixture.assertFileEquals("files/gdx.out", "out.md")
      }

    }

    `when`("building twice and adding output file default settings in between") {

      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) }""")
      fixture.build()
      fixture.replaceInBuildFile("""out.md"))""", """out.md")) { bold = false }""")
      fixture.build()

      then("should build again") {
        fixture.assertBuildSuccess()
      }

      then("should create the correct output") {
        fixture.assertFileEquals("files/gdx.default_nobold.out", "out.md")
      }

    }

    `when`("building twice and changing output file default settings in between") {

      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) { bold = false } }""")
      fixture.build()
      fixture.replaceInBuildFile("false", "true")
      fixture.build()

      then("should build again") {
        fixture.assertBuildSuccess()
      }

      then("should create the correct output") {
        fixture.assertFileEquals("files/gdx.out", "out.md")
      }

    }

    `when`("building twice and changing the input file in between (1)") {

      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) { bold = false } }""")
      fixture.build()
      fixture.replaceInBuildFile("gdx.md", "nerdfonts.md")
      fixture.build()

      then("should build again") {
        fixture.assertBuildSuccess()
      }

      then("should create the correct output") {
        fixture.assertFileEquals("files/nerdfonts.default_nobold.out", "out.md")
      }

    }

    `when`("building twice and changing the input file in between (2)") {

      fixture.build()
      fixture.replaceInBuildFile("doc(file(\"files/gdx.md\"))", "docs(\"files/nerdfonts.md\", \"files/gdx.md\")")
      fixture.build()

      then("should build again") {
        fixture.assertBuildSuccess()
      }

      then("should create the correct output") {
        fixture.assertFileEquals("files/gdx.out", "files/gdx.md")
      }

    }

    `when`("building twice and changing the output file in between") {

      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) { bold = false } }""")
      fixture.build()
      fixture.replaceInBuildFile("out.md", "out2.md")
      fixture.build()

      then("should build again") {
        fixture.assertBuildSuccess()
      }

      then("should create the correct output") {
        fixture.assertFileEquals("files/gdx.default_nobold.out", "out2.md")
      }

    }

    `when`("building twice and adding an output file in between") {

      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) { bold = false } }""")
      fixture.build()
      fixture.replaceInBuildFile("output", "outputs(\"out4.md\", \"out3.md\")\noutput")
      fixture.build()

      then("should build again") {
        fixture.assertBuildSuccess()
      }

      then("should create the correct outputs") {
        fixture.assertFileEquals("files/gdx.default_nobold.out", "out.md")
        fixture.assertFileEquals("files/gdx.out", "out3.md")
      }

    }

    `when`("building twice and changing the default variant in between") {

      fixture.build()
      fixture.replaceInBuildFile("tocme {", "tocme {\nvariant = Markdown")
      fixture.build()

      then("should build again") {
        fixture.assertBuildSuccess()
      }

      then("should create the correct output") {
        fixture.assertFileEquals("files/gdx.out", "files/gdx.md")
      }

    }

  }


})

