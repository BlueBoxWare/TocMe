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
internal object TestGradle: BehaviorSpec({

  lateinit var fixture: ProjectFixture

  beforeContainer {
    fixture = ProjectFixture(tempdir())
    fixture.addFile("files/gdx.md")
    fixture.addFile("files/nerdfonts.md")
    fixture.addFile("files/variant_issues.md")
    fixture.addFile("files/react.md")
    fixture.addFile("files/simple.md")
  }

  Given("no spec") {

    beforeContainer {
      fixture.buildFile("")
    }

    When("checking") {

      fixture.buildCheck()

      Then("should report no source") {
        fixture.assertBuildNoSourceAfter33()
      }

    }

    When("building") {

      fixture.build()

      Then("should report no source") {
        fixture.assertBuildNoSourceAfter33()
      }

    }

  }

  Given("an empty spec") {

    beforeContainer {
      fixture.buildFile(
        """
        tocme {
        }
      """.trimIndent()
      )
    }

    When("building") {

      fixture.build()

      Then("should report no source") {
        fixture.assertBuildNoSourceAfter33()
      }

    }

    When("checking") {

      fixture.buildCheck()

      Then("should report no source") {
        fixture.assertBuildNoSourceAfter33()
      }

    }

  }

  Given("a build file") {

    beforeContainer {
      fixture.buildFile(
        """
        tocme {
          doc(file("files/gdx.md"))
        }
      """.trimIndent()
      )
    }

    When("checking") {

      fixture.buildCheck(shouldFail = true)

      Then("should report the TOC is out of date") {
        fixture.assertCheckOutputFileIsOutOfDate("gdx.md")
      }

      Then("should have not made any changes") {
        fixture.assertFileEquals("files/gdx.md", "files/gdx.md")
      }

    }

    When("checking twice") {

      fixture.buildCheck(shouldFail = true)
      fixture.buildCheck(shouldFail = true)

      Then("should report the TOC is out of date") {
        fixture.assertCheckOutputFileIsOutOfDate("gdx.md")
      }

      Then("should have not made any changes") {
        fixture.assertFileEquals("files/gdx.md", "files/gdx.md")
      }

    }

    When("building") {

      fixture.build()

      Then("should insert the tocs correctly") {
        fixture.assertBuildSuccess()
        fixture.assertFileEquals("files/gdx.out", "files/gdx.md")
      }

      Then("should create a backup") {
        fixture.assertBackupMade("files/gdx.md")
      }

    }

    When("checking after building") {
      fixture.build()
      fixture.buildCheck()

      Then("should not report the TOC is out of date") {
        fixture.assertCheckOutputNothingIsOutOfDate()
      }
    }

    When("building twice") {

      fixture.build()
      fixture.build()

      Then("should be up-to-date the second time") {
        fixture.assertBuildUpToDate()
      }

      Then("should use configuration cache") {
        fixture.assertConfigurationCacheUsed()
      }

    }

    When("checking after building and changing the input doc (unchanged TOC)") {

      fixture.build()
      fixture.file("files/gdx.md").let { file ->
        file.writeText(file.readText().replace("Table of Contents", "TOC"))
      }
      fixture.buildCheck()

      Then("should not report the TOC is out of date") {
        fixture.assertCheckOutputNothingIsOutOfDate()
      }

    }

    When("checking after building and changing the input doc (changed TOC)") {

      fixture.build()
      fixture.file("files/gdx.md").let { file ->
        file.writeText(file.readText().replace("Getting started", "Getting stopped"))
      }
      fixture.buildCheck(shouldFail = true)

      Then("should report the TOC is out of date") {
        fixture.assertCheckOutputFileIsOutOfDate("gdx.md")
      }

    }

    When("building twice and changing the input doc in between") {

      fixture.build()
      fixture.file("files/gdx.md").let { file ->
        file.writeText(file.readText().replace("Table of Contents", "TOC"))
      }
      fixture.build()

      Then("should build again") {
        fixture.assertBuildSuccess()
      }

    }

    When("checking after building and changing the default settings") {

      fixture.build()
      fixture.replaceInBuildFile("tocme {", "tocme {\nbold=false\n")
      fixture.buildCheck(shouldFail = true)

      Then("should report the TOC is out of date") {
        fixture.assertCheckOutputFileIsOutOfDate("gdx.md")
      }

    }

    When("building twice and changing default settings in between (2)") {

      fixture.build()
      fixture.replaceInBuildFile("tocme {", "tocme {\nbold=false\n")
      fixture.build()

      Then("should build again") {
        fixture.assertBuildSuccess()
      }

      Then("should create the correct output") {
        fixture.assertFileEquals("files/gdx.default_nobold.out", "files/gdx.md")
      }

    }

    When("checking after building and changing the input file's default settings") {

      fixture.build()
      fixture.replaceInBuildFile("))", """)) { numbered=true }""")
      fixture.buildCheck(shouldFail = true)

      Then("should report the TOC is out of date") {
        fixture.assertCheckOutputFileIsOutOfDate("gdx.md")
      }

    }

    When("building twice and changing the input file's default settings in between") {

      fixture.build()
      fixture.replaceInBuildFile("))", """)) { numbered=true }""")
      fixture.build()

      Then("should build again") {
        fixture.assertBuildSuccess()
      }

      Then("should create the correct output") {
        fixture.assertFileEquals("files/gdx.default_numbered.out", "files/gdx.md")
      }

    }

    When("building twice and adding an output file in between") {

      fixture.build()
      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) }""")
      fixture.build()

      Then("should build again") {
        fixture.assertBuildSuccess()
      }

      Then("should create the correct output") {
        fixture.assertFileEquals("files/gdx.out", "out.md")
      }

    }

    When("building twice and adding output file default settings in between") {

      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) }""")
      fixture.build()
      fixture.replaceInBuildFile("""out.md"))""", """out.md")) { bold = false }""")
      fixture.build()

      Then("should build again") {
        fixture.assertBuildSuccess()
      }

      Then("should create the correct output") {
        fixture.assertFileEquals("files/gdx.default_nobold.out", "out.md")
      }

    }

    When("building twice and changing output file default settings in between") {

      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) { bold = false } }""")
      fixture.build()
      fixture.replaceInBuildFile("false", "true")
      fixture.build()

      Then("should build again") {
        fixture.assertBuildSuccess()
      }

      Then("should create the correct output") {
        fixture.assertFileEquals("files/gdx.out", "out.md")
      }

    }

    When("building twice and changing the input file in between (1)") {

      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) { bold = false } }""")
      fixture.build()
      fixture.replaceInBuildFile("gdx.md", "nerdfonts.md")
      fixture.build()

      Then("should build again") {
        fixture.assertBuildSuccess()
      }

      Then("should create the correct output") {
        fixture.assertFileEquals("files/nerdfonts.default_nobold.out", "out.md")
      }

      Then("should have made backups") {
        fixture.assertBackupMade("files/gdx.md")
        fixture.assertBackupMade("files/nerdfonts.md")
      }

    }

    When("building twice and changing the input file in between (2)") {

      fixture.build()
      fixture.replaceInBuildFile("doc(file(\"files/gdx.md\"))", "docs(\"files/nerdfonts.md\", \"files/gdx.md\")")
      fixture.build()

      Then("should build again") {
        fixture.assertBuildSuccess()
      }

      Then("should create the correct output") {
        fixture.assertFileEquals("files/gdx.out", "files/gdx.md")
      }

      Then("should have made backups") {
        fixture.assertBackupMade("files/gdx.md")
        fixture.assertBackupMade("files/nerdfonts.md")
      }

    }

    When("building twice and changing the output file in between") {

      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) { bold = false } }""")
      fixture.build()
      fixture.replaceInBuildFile("out.md", "out2.md")
      fixture.build()

      Then("should build again") {
        fixture.assertBuildSuccess()
      }

      Then("should create the correct output") {
        fixture.assertFileEquals("files/gdx.default_nobold.out", "out2.md")
      }

    }

    When("building twice and adding an output file in between (2)") {

      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) { bold = false } }""")
      fixture.build()
      fixture.replaceInBuildFile("output", "outputs(\"out4.md\", \"out3.md\")\noutput")
      fixture.build()

      Then("should build again") {
        fixture.assertBuildSuccess()
      }

      Then("should create the correct outputs") {
        fixture.assertFileEquals("files/gdx.default_nobold.out", "out.md")
        fixture.assertFileEquals("files/gdx.out", "out3.md")
      }

    }

    When("building twice and changing the default variant in between") {

      fixture.build()
      fixture.replaceInBuildFile("tocme {", "tocme {\nvariant = Markdown")
      fixture.build()

      Then("should build again") {
        fixture.assertBuildSuccess()
      }

      Then("should create the correct output") {
        fixture.assertFileEquals("files/gdx.out", "files/gdx.md")
      }

    }

  }

  Given("an output spec") {

    beforeContainer {
      fixture.buildFile(
        """
        tocme {
          doc(file("files/simple.md")) { output(file("out.md")) { bold = false } }

        }
      """.trimIndent()
      )
    }

    When("building") {

      fixture.build()

      Then("should insert the tocs correctly") {
        fixture.assertBuildSuccess()
        fixture.assertFileEquals("files/simple_nobold.out", "out.md")
      }
    }

  }

  Given("an input spec") {

    beforeContainer {
      fixture.buildFile(
        """
        tocme {
          doc(file("files/simple.md")) { 
          bold = false
          output(file("out.md"))  }

        }
      """.trimIndent()
      )
    }

    When("building") {

      fixture.build()

      Then("should insert the tocs correctly") {
        fixture.assertBuildSuccess()
        fixture.assertFileEquals("files/simple_nobold.out", "out.md")
      }
    }

  }

  Given("an input and ouput spec, input before") {

    beforeContainer {
      fixture.buildFile(
        """
        tocme {
          doc(file("files/simple.md")) { 
          bold = false
          output(file("out.md")) { bold = true } }

        }
      """.trimIndent()
      )
    }

    When("building") {

      fixture.build()

      Then("should insert the tocs correctly") {
        fixture.assertBuildSuccess()
        fixture.assertFileEquals("files/simple_bold.out", "out.md")
      }
    }

  }

  Given("an input and output spec, input after") {

    beforeContainer {
      fixture.buildFile(
        """
        tocme {
          doc(file("files/simple.md")) { 
           output(file("out.md")) { bold = false }
           bold = true
          }

        }
      """.trimIndent()
      )
    }

    When("building") {

      fixture.build()

      Then("should insert the tocs correctly") {
        fixture.assertBuildSuccess()
        fixture.assertFileEquals("files/simple_nobold.out", "out.md")
      }
    }

    When("building twice and removing the output file in between") {

      fixture.build()
      fixture.file("out.md").delete()
      fixture.build()

      Then("should use the build cache") {
        fixture.assertBuildFromCache()
      }

      Then("should use the configuration cache") {
        fixture.assertConfigurationCacheUsed()
      }

    }

  }

})

