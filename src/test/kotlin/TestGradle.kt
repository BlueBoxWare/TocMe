import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertTrue

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

object TestGradle: Spek({

  lateinit var fixture: ProjectFixture

  beforeEachTest {
    fixture = ProjectFixture()
    fixture.addFile("files/gdx.md")
    fixture.addFile("files/nerdfonts.md")
    fixture.addFile("files/variant_issues.md")
    fixture.addFile("files/react.md")
  }

  afterEachTest {
    fixture.destroy()
  }

  given("a trivial build file") {

    beforeEachTest {
      fixture.buildFile("""
        tocme {
          doc(file("files/gdx.md"))
        }
      """.trimIndent())
    }

    on("building") {

      fixture.build()

      it("should insert the tocs correctly") {
        fixture.assertBuildSuccess()
        fixture.assertFileEquals("files/gdx.out", "files/gdx.md")
      }

      it("should create a backup") {
        fixture.assertFileEquals("files/gdx.md", fixture.backupDir + "gdx.md")
      }

    }

    on("building twice") {

      fixture.build()
      fixture.build()

      it("should be up to date the second time") {
        fixture.assertBuildUpToDate()
      }

    }

    on("building twice and changing the input doc in between") {

      fixture.build()
      fixture.file("files/gdx.md").let { file ->
        file.writeText(file.readText().replace("Table of Contents", "TOC"))
      }
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

    }

    on("building twice and changing default settings in between") {

      fixture.build()
      fixture.replaceInBuildFile("tocme {", "tocme {\nbold=false\n")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

      it("should create the correct output") {
        fixture.assertFileEquals("files/gdx.default_nobold.out", "files/gdx.md")
      }

    }

    on("building twice and changing the input file's default settings in between") {

      fixture.build()
      fixture.replaceInBuildFile("))", """)) { numbered=true }""")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

      it("should create the correct output") {
        fixture.assertFileEquals("files/gdx.default_numbered.out", "files/gdx.md")
      }

    }

    on("building twice and adding an output file in between") {

      fixture.build()
      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) }""")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

      it("should create the correct output") {
        fixture.assertFileEquals("files/gdx.out", "out.md")
      }

    }

    on("building twice and adding output file default settings in between") {

      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) }""")
      fixture.build()
      fixture.replaceInBuildFile("""out.md"))""", """out.md")) { bold = false }""")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

      it("should create the correct output") {
        fixture.assertFileEquals("files/gdx.default_nobold.out", "out.md")
      }

    }

    on("building twice and changing output file default settings in between") {

      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) { bold = false } }""")
      fixture.build()
      fixture.replaceInBuildFile("false", "true")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

      it("should create the correct output") {
        fixture.assertFileEquals("files/gdx.out", "out.md")
      }

    }

    on("building twice and changing the input file in between (1)") {

      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) { bold = false } }""")
      fixture.build()
      fixture.replaceInBuildFile("gdx.md", "nerdfonts.md")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

      it("should create the correct output") {
        fixture.assertFileEquals("files/nerdfonts.default_nobold.out", "out.md")
      }

    }

    on("building twice and changing the input file in between (2)") {

      fixture.build()
      fixture.replaceInBuildFile("doc(file(\"files/gdx.md\"))", "docs(\"files/nerdfonts.md\", \"files/gdx.md\")")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

      it("should create the correct output") {
        fixture.assertFileEquals("files/gdx.out", "files/gdx.md")
      }

    }

    on("building twice and changing the output file in between") {

      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) { bold = false } }""")
      fixture.build()
      fixture.replaceInBuildFile("out.md", "out2.md")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

      it("should create the correct output") {
        fixture.assertFileEquals("files/gdx.default_nobold.out", "out2.md")
      }

    }

    on("building twice and adding an output file in between") {

      fixture.replaceInBuildFile("))", """)) { output(file("out.md")) { bold = false } }""")
      fixture.build()
      fixture.replaceInBuildFile("output", """outputs("out4.md", "out3.md")\noutput""")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

      it("should create the correct outputs") {
        fixture.assertFileEquals("files/gdx.default_nobold.out", "out.md")
        fixture.assertFileEquals("files/gdx.default_nobold.out", "out3.md")
      }

    }

    on("building twice and changing the default variant in between") {

      fixture.build()
      fixture.replaceInBuildFile("tocme {", "tocme {\nvariant = Markdown")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

      it("should create the correct output") {
        fixture.assertFileEquals("files/gdx.out", "files/gdx.md")
      }

    }

  }

  given("a spec with a file with emojis in headers") {

    beforeEachTest {
      fixture.buildFile("""
        tocme {
          doc(file("files/nerdfonts.md")) {
            output("nerdfonts.out") {

            }
            output("nerdfonts2.out") {
               removeEmojis = true
            }
          }
        }
      """.trimIndent())
    }

    on("building") {

      fixture.build()

      it("should create the correct outputs") {
        fixture.assertFileEquals("files/nerdfonts.out", "nerdfonts.out")
        fixture.assertFileEquals("files/nerdfonts.without_emojis.out", "nerdfonts2.out")
      }

    }

    on("building twice and changing emoji setting in between") {

      fixture.build()
      fixture.replaceInBuildFile("true", "false")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

      it("should create the correct outputs") {
        fixture.assertFileEquals("files/nerdfonts.out", "nerdfonts.out")
        fixture.assertFileEquals("files/nerdfonts.out", "nerdfonts2.out")
      }

    }

    on("building twice and changing the default emoji setting in between") {

      fixture.build()
      fixture.replaceInBuildFile("tocme {", "tocme {\nremoveEmojis = true")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

      it("should create the correct outputs") {
        fixture.assertFileEquals("files/nerdfonts.without_emojis.out", "nerdfonts.out")
        fixture.assertFileEquals("files/nerdfonts.without_emojis.out", "nerdfonts2.out")
      }

    }

  }

  given("a spec with multiple variants") {

    beforeEachTest {
      fixture.buildFile("""

        tocme {

          doc(file("files/variant_issues.md")) {

            variant = Pegdown

            output(file("pegdown.out"))

            output("github.out") {
              variant = GitHub
            }

            output("commonmark.out") {
              variant = Commonmark
            }

            output("kramdown.out") {
              variant = Kramdown
            }

          }

        }

      """)
    }

    on("building") {

      fixture.build()

      it("should create the correct outputs") {
        fixture.assertFileEquals("files/variant_issues.Pegdown.out", "pegdown.out")
        fixture.assertFileEquals("files/variant_issues.GitHub.out", "github.out")
        fixture.assertFileEquals("files/variant_issues.Commonmark.out", "commonmark.out")
        fixture.assertFileEquals("files/variant_issues.Kramdown.out", "kramdown.out")
      }

    }

    on("building twice and changing an output variant in between") {

      fixture.build()
      fixture.replaceInBuildFile("Kramdown", "MultiMarkdown")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

      it("should create the correct output") {
        fixture.assertFileEquals("files/variant_issues.MultiMarkdown.out", "kramdown.out")
      }

    }

  }

  given("a more complicated build file") {

    beforeEachTest {
      fixture.buildFile("""
        tocme {
          bold = true

          doc(file("files/gdx.md")) {

            bold = true

            output("gdx.out") {
              numbered = false
            }

            output("gdx_numbered.out") {
              numbered = true
              bold = true
            }

            output("gdx_flat.out") {
              style = "flat"
            }

          }

          doc("files/nerdfonts.md") {

            style = "sorted_reversed"
            plain = true

            output("nerdfonts_reversed_plain.out")

          }

          doc("files/variant_issues.md")

          doc("files/react.md") {
            style = "flat_reversed"
            mode = "local"

            output("react.default_flat_reversed_local.out")

            output("react.default_sorted_full.out") {
              style = "sorted"
              mode = "full"
            }

          }

        }

      """)
    }

    on("building") {

      fixture.build()

      it("should succeed") {
        fixture.assertBuildSuccess()
      }

      it("should create backups") {
        fixture.assertFileEquals("files/gdx.md", fixture.backupDir + "gdx.md")
        fixture.assertFileEquals("files/variant_issues.md", fixture.backupDir + "variant_issues.md")
        fixture.assertFileEquals("files/react.md", fixture.backupDir + "react.md")
      }

      it("should create the correct outputs") {
        fixture.assertFileEquals("files/gdx.out", "gdx.out")
        fixture.assertFileEquals("files/gdx.default_numbered.out", "gdx_numbered.out")
        fixture.assertFileEquals("files/gdx.default_flat.out", "gdx_flat.out")
        fixture.assertFileEquals("files/variant_issues.GitHub.out", "files/variant_issues.md")
        fixture.assertFileEquals("files/react.default_flat_reversed_local.out", "react.default_flat_reversed_local.out")
        fixture.assertFileEquals("files/react.default_sorted_full.out", "react.default_sorted_full.out")
      }

    }

    on("building twice") {

      fixture.build()
      fixture.build()

      it("should be up to date the second time") {
        fixture.assertBuildUpToDate()
      }

    }

    on("building twice and changing an option in between") {

      fixture.build()
      fixture.replaceInBuildFile("numbered = false", "numbered = true")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

      it("should create backups") {
        listOf(
                "gdx.md",
                "gdx.out",
                "gdx_numbered.out",
                "gdx_flat.out",
                "nerdfonts_reversed_plain.out",
                "variant_issues.md",
                "react.default_flat_reversed_local.out",
                "react.default_sorted_full.out"
        ).map { fixture.backupDir + it }.toTypedArray().let {
          fixture.assertFilesExist(*it)
        }
      }

    }

    on("building twice and changing an output file in between") {

      fixture.build()
      fixture.replaceInBuildFile("gdx_numbered.out", "gdx_numbered_foo.out")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

    }

  }

  given("a spec with DSL shortcuts") {
    beforeEachTest {
      fixture.buildFile("""
        tocme {
          style = Flat
          mode = Local
          variant = Commonmark

          doc(file("files/gdx.md")) {

            style = Hierarchy
            mode = Normal
            variant = Commonmark28

            output("gdx.out") {
              style = Reversed
              mode = Full
              variant = Kramdown
            }

            output("gdx_numbered.out") {
              style = Increasing
              variant = Markdown
            }

            output("gdx_flat.out") {
              style = Decreasing
              variant = GitHub
            }

            output("foo.bar") {
              variant = GitHubDoc
              variant = MultiMarkdown
              variant = Pegdown
              variant = PegdownStrict
              variant = GitLab
            }

          }

        }

      """)
    }

    on("building") {

      fixture.build()

      it("should build successfully") {
        fixture.assertBuildSuccess()
      }

    }

  }

  given("a spec with advanced options") {

    beforeEachTest {
      fixture.buildFile("""
        tocme {
          variant = Commonmark
          setextMarkerLength = 2

          doc(file("files/variant_issues.md")) {
            output("out1.md")
            output("out2.md") {
              setextMarkerLength = 1
            }
          }

          doc(file("files/react.md")) {
            allowedChars = ",’"
          }

        }

      """)
    }

    on("building") {

      fixture.build()

      it("creates the correct outputs") {
        fixture.assertFileEquals("files/variant_issues.Commonmark.setextMarkerLength2.out", "out1.md")
        fixture.assertFileEquals("files/variant_issues.Commonmark.setextMarkerLength1.out", "out2.md")
        fixture.assertFileEquals("files/react.withCommaAndApostrophe.out", "files/react.md")
      }

    }

    on("building twice") {

      fixture.build()
      fixture.build()

      it("should be up to date the second time") {
        fixture.assertBuildUpToDate()
      }

    }

    on("building twice and changing a default setting in between") {

      fixture.build()
      fixture.replaceInBuildFile("setextMarkerLength = 2", "setextMarkerLength = 4")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

      it("should create the correct outputs") {
        fixture.assertFileEquals("files/variant_issues.Commonmark.setextMarkerLength4.out", "out1.md")
        fixture.assertFileEquals("files/variant_issues.Commonmark.setextMarkerLength1.out", "out2.md")
      }

    }

    on("building twice and changing an output file setting in between") {

      fixture.build()
      fixture.replaceInBuildFile("setextMarkerLength = 1", "setextMarkerLength = 4")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

      it("should create the correct outputs") {
        fixture.assertFileEquals("files/variant_issues.Commonmark.setextMarkerLength2.out", "out1.md")
        fixture.assertFileEquals("files/variant_issues.Commonmark.setextMarkerLength4.out", "out2.md")
      }

    }

    on("building twice and changing an input file setting in between") {

      fixture.build()
      fixture.replaceInBuildFile(",’", ",")
      fixture.build()

      it("should build again") {
        fixture.assertBuildSuccess()
      }

      it("should create the correct outputs") {
        fixture.assertFileEquals("files/react.withComma.out", "files/react.md")
      }

    }

  }

  given("a doc with an error") {

    beforeEachTest {

      fixture.createFile("input.md", """
        <!-- toc -->
        <!-- toc -->
        <!-- /toc -->
      """.trimIndent())

      fixture.buildFile("""
        tocme {
          doc("input.md")
        }
      """.trimIndent())
    }

    on("building") {

      val error = try {
        fixture.build()
        null
      } catch (e: Exception) {
        e.message
      }

      it("should exit with an error") {
        assertTrue(error?.contains(Regex("""input\.md.*opening toc tag.*line 2.*on line 1.*wasn't closed""")) == true, error)
      }

    }

  }

  given("a doc which should produce warnings") {

    beforeEachTest {
      fixture.createFile("input.md", """
        <!-- toc foo=bar-->
        <!-- /toc -->
        <!---- toc style=boo --->
        <!--- /toc-->
        <!-- toc -->
        a
        <!--/toc-->
        # Header 1
        Header 2
        ------
      """.trimIndent())

      fixture.buildFile("""
        tocme {
          doc("input.md")
        }
      """.trimIndent())
    }

    on("building") {

      fixture.build()

      it("should produce the expected warnings") {
        fixture.assertOutputContains("input.md: line 1: unknown option: 'foo'")
        fixture.assertOutputContains("line 3: invalid argument for parameter style: 'boo'")
        fixture.assertOutputContains("current content between the toc tags on line 5 and line 7")
      }

      it("should produce the expected output") {
        ProjectFixture.assertTextEquals("""
          <!-- toc foo=bar-->
          - __[Header 1](#header-1)__
            - __[Header 2](#header-2)__
          <!-- /toc -->
          <!---- toc style=boo --->
          - __[Header 1](#header-1)__
            - __[Header 2](#header-2)__
          <!--- /toc-->
          <!-- toc -->
          a
          <!--/toc-->
          # Header 1
          Header 2
          ------
        """.trimIndent(), fixture.project.file("input.md").readText())
      }

    }

  }

  given("a spec with level specifications") {

    beforeEachTest {
      fixture.buildFile("""
        tocme {

          levels = levels("1-3,4")

          doc(file("files/variant_issues.md")) {
            levels = levels("1,2")
            output("out1.md")
            output("out2.md") {
              levels = levels("1-2,3")
            }
          }

          doc(file("files/react.md")) {

          }

        }
      """.trimIndent())
    }

    on("building") {

      fixture.build()

      it("should create the correct outputs") {
        fixture.assertFileEquals("files/variant_issues.levels1-2.out", "out1.md")
        fixture.assertFileEquals("files/variant_issues.out", "out2.md")
        fixture.assertFileEquals("files/react.levels1-4.out", "files/react.md")
      }

    }

  }

})