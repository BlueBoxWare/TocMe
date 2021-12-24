import com.github.blueboxware.tocme.TocMeOptions
import com.github.blueboxware.tocme.util.TocMeOptionsImpl
import com.github.blueboxware.tocme.util.Variant
import com.github.blueboxware.tocme.util.insertTocs
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.parser.ParserEmulationProfile
import com.vladsch.flexmark.util.data.MutableDataSet
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withData
import io.kotest.engine.spec.tempfile
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
internal object TestInsertToc: BehaviorSpec({

  val TEST_DATA_DIR = File("src/test/resources")
  val TEST_DATA_FILES_DIR = File(TEST_DATA_DIR, "files/")

  var expectedTestNumber: Int

  data class Test(
    val label: String,
    val input: String,
    val variant: Variant,
    val expected: String
  )

  fun loadTests(filename: String): List<Test> = run {
    expectedTestNumber = 0
    File(TEST_DATA_DIR, filename).readText().split(Regex("=====+\n")).flatMap { test ->
      Regex(
        """(.*)---*\s+(\d+)\s+---+\n(.*)""",
        RegexOption.DOT_MATCHES_ALL
      ).matchEntire(test)?.groupValues?.let { (_, input, nr, expectedOutput) ->
        expectedTestNumber++
        if (nr.toInt() != expectedTestNumber) {
          throw AssertionError("Test number is $nr, should be $expectedTestNumber")
        }
        Variant.values().map {
          Test("$nr ($it)", input.trimEnd('\n'), it, expectedOutput.trimEnd('\n'))
        }

      } ?: throw AssertionError()
    }
  }

  given("a Markdown document with correct toc tags") {

    withData(loadTests("valid.data")) { (label, input, variant, expected) ->

      `when`("inserting a toc: $label") {

        val parserOptions = MutableDataSet()
        variant.setIn(parserOptions)

        val tocMeOptions = TocMeOptionsImpl(null).apply { this.variant = variant }

        val parser = Parser.builder(parserOptions).build()

        val document = parser.parse(input)
        val (result, warnings, error) = document.insertTocs(tocMeOptions, checkCurrentContent = true)

        then("generates no errors") {
          assertNull(error, "Unexpected error: $error")
        }

        then("generates no warnings") {
          assertTrue(warnings.isEmpty(), "Unexpected warning(s): \n" + warnings.joinToString(separator = "\n"))
        }

        then("generates the correct results") {
          ProjectFixture.assertTextEquals(expected, result!!, document = document)
        }

        then("should 'update' without warnings, errors or actual changes") {
          val document2 = parser.parse(result!!)
          val (result2, warnings2, error2) = document2.insertTocs(tocMeOptions, checkCurrentContent = true)
          assertNull(error2, "Failed to update. Unexpected error: $error2")
          assertTrue(
            warnings2.isEmpty(),
            "Failed to update. Unexpected warning(s): \n" + warnings2.joinToString(separator = "\n")
          )
          ProjectFixture.assertTextEquals(result, result2!!)
        }
      }
    }

  }

  given("a Markdown document which should produces warnings") {

    withData(loadTests("warning.data")) { (label, input, variant, expected) ->

      `when`("inserting a toc: $label") {

        val parserOptions = MutableDataSet()
        variant.setIn(parserOptions)

        val tocMeOptions = TocMeOptionsImpl(null).apply { this.variant = variant }

        val parser = Parser.builder(parserOptions).build()

        val document = parser.parse(input)
        val (result, warnings, error) = document.insertTocs(tocMeOptions, checkCurrentContent = true)

        val warningRegex = expected.lines().first().let {
          Regex(
            ".*$it.*",
            RegexOption.IGNORE_CASE
          )
        }

        then("generates no errors") {
          assertNull(error, "Unexpected error: $error")
        }

        then("generates the expected warning") {
          assertTrue(
            warnings.any { it.matches(warningRegex) },
            "No warning which matches '$warningRegex' found.\nWarnings:\n" + warnings.joinToString(separator = "\n")
          )
        }

        then("generates the correct result") {
          expected.lines().drop(1).joinToString(separator = "\n").let {
            ProjectFixture.assertTextEquals(it, result!!)
          }
        }

      }
    }
  }

  given("a Markdown document which should produce an error") {

    withData(loadTests("error.data")) { (label, input, variant, expected) ->

      `when`("inserting a toc: $label") {

        val parserOptions = MutableDataSet()
        variant.setIn(parserOptions)

        val tocMeOptions = TocMeOptionsImpl(null).apply { this.variant = variant }

        val parser = Parser.builder(parserOptions).build()

        val document = parser.parse(input)
        val (_, _, error) = document.insertTocs(tocMeOptions, checkCurrentContent = true)

        val errorRegex = expected.lines().first().let { Regex(".*$it.*", RegexOption.IGNORE_CASE) }

        then("generates an error") {
          assertNotNull(error, "No error")
          assertTrue(error.matches(errorRegex), "Actual error ('$error') doesn't match regex ('$errorRegex')")
        }

      }
    }
  }

  given("a Markdown document from file") {

    val options = MutableDataSet().apply {
      setFrom(ParserEmulationProfile.GITHUB)
    }
    val parser = Parser.builder(options).build()

    TEST_DATA_FILES_DIR.walkTopDown().filter { it.extension == "md" && it.name != "variant_issues.md" }.map {
      Triple(
        it.absolutePath,
        it.readText(),
        File(TEST_DATA_FILES_DIR, it.nameWithoutExtension + ".out").readText()
      )
    }.forEach { (absolutePath, input, expected) ->

      `when`("inserting a toc ($absolutePath)") {

        val document = parser.parse(input)
        val (result, warnings, error) = document.insertTocs(TocMeOptionsImpl(null), checkCurrentContent = true)

        then("generates no errors") {
          assertNull(error, "Unexpected error: $error")
        }

        then("generates no warnings") {
          assertTrue(warnings.isEmpty(), "Unexpected warning(s): \n" + warnings.joinToString(separator = "\n"))
        }

        then("generates the correct results") {
          ProjectFixture.assertTextEquals(expected.trimEnd(), result!!.trimEnd())
        }

        then("should 'update' without warnings, errors or actual changes") {
          val document2 = parser.parse(result!!)
          val (result2, warnings2, error2) = document2.insertTocs(TocMeOptionsImpl(null), checkCurrentContent = true)
          assertNull(error2, "Failure on update. Unexpected error: $error2")
          assertTrue(
            warnings2.isEmpty(),
            "Failure on update. Unexpected warning(s): \n" + warnings2.joinToString(separator = "\n")
          )
          ProjectFixture.assertTextEquals(result, result2!!)
        }

      }
    }

  }

  given("a Markdown document with constructs which are handled different by different markdown variants") {

    Variant.values().map {
      Pair(
        it.name,
        it
      )
    }.forEach { (name, variant) ->

      `when`("inserting a toc with variant $name") {

        val tocmeOptions = TocMeOptionsImpl(null).apply {
          this.variant = variant
        }
        val tempFile = tempfile()

        val (result, warnings, error) =
          insertTocs(
            File(TEST_DATA_FILES_DIR, "variant_issues.md"),
            tempFile,
            tocmeOptions,
            writeChanges = true
          )

        then("generates no errors") {
          assertNull(error, "Unexpected error: $error")
        }

        then("generates no warnings") {
          assertTrue(
            warnings.isEmpty(),
            "Unexpected warning(s): \n" + warnings.joinToString(separator = "\n")
          )
        }

        then("generates the correct result") {

          File(TEST_DATA_FILES_DIR, "variant_issues.$name.out").let { expectedFile ->

            if (!expectedFile.exists()) {
              expectedFile.writeText(result!!)
              throw AssertionError("Expected file '${expectedFile.absolutePath}' does not exist. Creating.")
            }

            ProjectFixture.assertTextEquals(
              expectedFile.readText(),
              result!!
            )

          }
        }
      }
    }

  }

  given("a Markdown document with emojis in headers") {

    listOf("variant_issues", "nerdfonts").map {
      Pair("$it.md", "${it}_no_emojis.out")
    }.forEach { (file, expected) ->

      `when`("inserting a toc while removing emojis: $file") {

        val tempFile = tempfile()

        val tocmeOptions = TocMeOptionsImpl(null).apply {
          removeEmojis = true
        }

        val (_, warnings, error) = insertTocs(
          File(TEST_DATA_FILES_DIR, file),
          tempFile,
          tocmeOptions,
          writeChanges = true
        )

        then("generates no errors") {
          assertNull(error, "Unexpected error: $error")
        }

        then("generates no warnings") {
          assertTrue(warnings.isEmpty(), "Unexpected warning(s): \n" + warnings.joinToString(separator = "\n"))
        }

        then("generates the correct result") {
          ProjectFixture.assertFileEquals(File(TEST_DATA_FILES_DIR, expected), tempFile)
        }

      }
    }
  }

  given("a simple document with variant issues") {

    data class Test(
      val label: String,
      val contents: String,
      val conf: TocMeOptions.() -> Unit,
      val expected: String
    )

    val tests = mutableListOf<Test>()

    fun test(
      id: String,
      doc: String,
      cases: List<Triple<String, TocMeOptions.() -> Unit, String>>,
      trimIndent: Boolean = true,
      insertHeader: Boolean = true
    ) {
      val header = if (insertHeader) "<!--toc-->\n<!--/toc-->\n" else ""
      val contents = header + if (trimIndent) doc.trimIndent() else doc
      cases.forEach { (caseId, conf, expected) ->
        tests.add(Test("$id: $caseId", contents, conf, expected.trimIndent()))
      }
    }

    fun case(id: String, conf: TocMeOptions.() -> Unit, expected: String) = Triple(id, conf, expected)

    test(
      id = "allowSpace",
      doc = """
                # Header1
                #Header2
              """,
      cases = listOf(
        case(
          id = "true",
          conf = { requireSpace = false },
          expected = """
                                <!--toc-->
                                - __[Header1](#header1)__
                                - __[Header2](#header2)__
                                <!--/toc-->
                                # Header1
                                #Header2
                            """
        ),
        case(
          id = "false",
          conf = { requireSpace = true },
          expected = """
                                <!--toc-->
                                - __[Header1](#header1)__
                                <!--/toc-->
                                # Header1
                                #Header2
                            """
        )

      )
    )

    test(
      id = "dupedDashes",
      doc = """
                # ----Header1 ---___--
                # ---__---Header--2--___--
                #   Hea der   3
              """,
      cases = listOf(
        case(
          id = "true",
          conf = {
            dupedDashes = true
            dashChars = "-"
          },
          expected = """
                              <!--toc-->
                              - __[----Header1 ---___--](#----header1-----)__
                              - __[------Header--2--_--](#------header--2----)__
                              - __[Hea der   3](#header3)__
                              <!--/toc-->
                              # ----Header1 ---___--
                              # ---__---Header--2--___--
                              #   Hea der   3
                            """
        ),
        case(
          id = "false",
          conf = {
            dupedDashes = false
            dashChars = "-"
          },
          expected = """
                              <!--toc-->
                              - __[----Header1 ---___--](#-header1-)__
                              - __[------Header--2--_--](#-header-2-)__
                              - __[Hea der   3](#header3)__
                              <!--/toc-->
                              # ----Header1 ---___--
                              # ---__---Header--2--___--
                              #   Hea der   3
                            """
        ),
        case(
          id = "true + dashChars='_- '",
          conf = {
            dupedDashes = true
            dashChars = "_- "
          },
          expected = """
                              <!--toc-->
                              - __[----Header1 ---___--](#----header1---------)__
                              - __[------Header--2--_--](#------header--2-----)__
                              - __[Hea der   3](#hea-der---3)__
                              <!--/toc-->
                              # ----Header1 ---___--
                              # ---__---Header--2--___--
                              #   Hea der   3
                            """
        ),
        case(
          id = "false + dashChars='_- '",
          conf = {
            dupedDashes = false
            dashChars = "_- "
          },
          expected = """
                              <!--toc-->
                              - __[----Header1 ---___--](#-header1-)__
                              - __[------Header--2--_--](#-header-2-)__
                              - __[Hea der   3](#hea-der-3)__
                              <!--/toc-->
                              # ----Header1 ---___--
                              # ---__---Header--2--___--
                              #   Hea der   3
                            """
        )

      )
    )

    test(
      id = "resolveDupes",
      doc = """
                # Header1
                # Header2
                ## Header1
                # Header2
                Header1
                =====
              """,
      cases = listOf(
        case(
          id = "true",
          conf = { resolveDupes = true },
          expected = """
                              <!--toc-->
                              - __[Header1](#header1)__
                              - __[Header2](#header2)__
                                 - __[Header1](#header1-1)__
                              - __[Header2](#header2-1)__
                              - __[Header1](#header1-2)__
                              <!--/toc-->
                              # Header1
                              # Header2
                              ## Header1
                              # Header2
                              Header1
                              =====
                            """
        ),
        case(
          id = "false",
          conf = { resolveDupes = false },
          expected = """
                              <!--toc-->
                              - __[Header1](#header1)__
                              - __[Header2](#header2)__
                                 - __[Header1](#header1)__
                              - __[Header2](#header2)__
                              - __[Header1](#header1)__
                              <!--/toc-->
                              # Header1
                              # Header2
                              ## Header1
                              # Header2
                              Header1
                              =====
                            """
        )

      )
    )

    test(
      id = "allowedChars",
      doc = """
                # Header_+1()*#$@!
                # Header++2++$@!###
                # Header 3 ($)/\
              """,
      cases = listOf(
        case(
          id = "_+#$/ ",
          conf = { allowedChars = "_+#$/ " },
          expected = """
                              <!--toc-->
                              - __[Header_+1()*#${'$'}@!](#header_+1#${'$'})__
                              - __[Header++2++${'$'}@!###](#header++2++${'$'}###)__
                              - __[Header 3 (${'$'})/\](#header 3 ${'$'}/)__
                              <!--/toc-->
                              # Header_+1()*#${'$'}@!
                              # Header++2++${'$'}@!###
                              # Header 3 (${'$'})/\
                            """
        ),
        case(
          id = "<none>",
          conf = { allowedChars = "" },
          expected = """
                              <!--toc-->
                              - __[Header_+1()*#${'$'}@!](#header-1)__
                              - __[Header++2++${'$'}@!###](#header2)__
                              - __[Header 3 (${'$'})/\](#header-3-)__
                              <!--/toc-->
                              # Header_+1()*#${'$'}@!
                              # Header++2++${'$'}@!###
                              # Header 3 (${'$'})/\
                            """
        )

      )
    )

    test(
      id = "allowLeadingSpace",
      trimIndent = false,
      doc = """
    # Header1
  # Header2
 # Header 3
 Header4
 =====
  Header5
   ------
    Header6
     -------
""",
      cases = listOf(
        case(
          id = "true",
          conf = { allowLeadingSpace = true },
          expected = """
                            <!--toc-->
                            - __[Header2](#header2)__
                            - __[Header 3](#header-3)__
                            - __[Header4](#header4)__
                               - __[Header5](#header5)__
                            <!--/toc-->

                                # Header1
                              # Header2
                             # Header 3
                             Header4
                             =====
                              Header5
                               ------
                                Header6
                                 -------

                            """
        ),
        case(
          id = "false",
          conf = { allowLeadingSpace = false },
          expected = """
                              <!--toc-->
                              <!--/toc-->

                                  # Header1
                                # Header2
                               # Header 3
                               Header4
                               =====
                                Header5
                                 ------
                                  Header6
                                   -------

                            """
        )

      )
    )

    test(
      id = "setextMarkerLength",
      doc = """
                Length 1
                =

                Length 1
                -

                Length 2
                ==

                Length 3
                ===

                Length 4
                ====

                Length 2
                --

                Length 3
                ---

                Length 4
                ----

                Length 5
                -----
              """,
      cases = listOf(
        case(
          id = "1",
          conf = { setextMarkerLength = 1 },
          expected = """
                              <!--toc-->
                              - __[Length 1](#length-1)__
                                 - __[Length 1](#length-1-1)__
                              - __[Length 2](#length-2)__
                              - __[Length 3](#length-3)__
                              - __[Length 4](#length-4)__
                                 - __[Length 2](#length-2-1)__
                                 - __[Length 3](#length-3-1)__
                                 - __[Length 4](#length-4-1)__
                                 - __[Length 5](#length-5)__
                              <!--/toc-->
                              Length 1
                              =

                              Length 1
                              -

                              Length 2
                              ==

                              Length 3
                              ===

                              Length 4
                              ====

                              Length 2
                              --

                              Length 3
                              ---

                              Length 4
                              ----

                              Length 5
                              -----
                            """
        ),
        case(
          id = "2",
          conf = { setextMarkerLength = 2 },
          expected = """
                              <!--toc-->
                              - __[Length 2](#length-2)__
                              - __[Length 3](#length-3)__
                              - __[Length 4](#length-4)__
                                 - __[Length 2](#length-2-1)__
                                 - __[Length 3](#length-3-1)__
                                 - __[Length 4](#length-4-1)__
                                 - __[Length 5](#length-5)__
                              <!--/toc-->
                              Length 1
                              =

                              Length 1
                              -

                              Length 2
                              ==

                              Length 3
                              ===

                              Length 4
                              ====

                              Length 2
                              --

                              Length 3
                              ---

                              Length 4
                              ----

                              Length 5
                              -----
                            """
        ),
        case(
          id = "4",
          conf = { setextMarkerLength = 4 },
          expected = """
                              <!--toc-->
                              - __[Length 4](#length-4)__
                                 - __[Length 4](#length-4-1)__
                                 - __[Length 5](#length-5)__
                              <!--/toc-->
                              Length 1
                              =

                              Length 1
                              -

                              Length 2
                              ==

                              Length 3
                              ===

                              Length 4
                              ====

                              Length 2
                              --

                              Length 3
                              ---

                              Length 4
                              ----

                              Length 5
                              -----
                            """
        )

      )
    )

    test(
      id = "emptyHeadingWithoutSpace",
      doc = """
              # Header1
              ##
              ##s
              ###
              ###s
              ####
            """.replace("s", " "),
      cases = listOf(
        case(
          id = "true",
          conf = { emptyHeadingWithoutSpace = true },
          expected = """
                              <!--toc-->
                              - __[Header1](#header1)__
                                 - __[](#)__
                                 - __[](#)__
                                    - __[](#)__
                                    - __[](#)__
                              <!--/toc-->
                              # Header1
                              ##
                              ##s
                              ###
                              ###s
                              ####
                            """.replace("s", " ")
        ),
        case(
          id = "false",
          conf = { emptyHeadingWithoutSpace = false },
          expected = """
                              <!--toc-->
                              - __[Header1](#header1)__
                                 - __[](#)__
                                    - __[](#)__
                              <!--/toc-->
                              # Header1
                              ##
                              ##s
                              ###
                              ###s
                              ####
                            """.replace("s", " ")
        )

      )
    )

    test(
      id = "headingInterruptsItemParagraph",
      doc = """
                # Header1
                - item 1
                - item 2
                  # Header2
                - item 3

                # Header 3
              """,
      cases = listOf(
        case(
          id = "true",
          conf = { headingInterruptsItemParagraph = true },
          expected = """
                              <!--toc-->
                              - __[Header1](#header1)__
                              - __[Header2](#header2)__
                              - __[Header 3](#header-3)__
                              <!--/toc-->
                              # Header1
                              - item 1
                              - item 2
                                # Header2
                              - item 3

                              # Header 3
                            """
        ),
        case(
          id = "false",
          conf = { headingInterruptsItemParagraph = false },
          expected = """
                              <!--toc-->
                              - __[Header1](#header1)__
                              - __[Header 3](#header-3)__
                              <!--/toc-->
                              # Header1
                              - item 1
                              - item 2
                                # Header2
                              - item 3

                              # Header 3
                            """
        )

      )
    )

    test(
      id = "custom tag name",
      doc = """
                <!----   foobar numbered=true--->
                <!--/foobar    --->
                # Header1
                ### Header2
              """,
      insertHeader = false,
      cases = listOf(
        case(
          id = "",
          conf = { tag = "foobar" },
          expected = """
                              <!----   foobar numbered=true--->
                              1. __[Header1](#header1)__
                                    1. __[Header2](#header2)__
                              <!--/foobar    --->
                              # Header1
                              ### Header2
                            """
        )
      )
    )

    withData(tests.associateBy { it.label }) { (name, content, conf, expected) ->

      `when`("inserting a toc: $name") {

        val parentTocmeOptions = TocMeOptionsImpl(null).apply {
          variant = Variant.Commonmark
        }
        conf.let { parentTocmeOptions.it() }

        val tocmeOptions = TocMeOptionsImpl(parentTocmeOptions)

        val parser = Parser.builder(tocmeOptions.toParserOptions()).build()

        val document = parser.parse(content)
        val (result, warnings, error) = document.insertTocs(tocmeOptions, checkCurrentContent = true)

        then("generates no errors") {
          assertNull(error, "Unexpected error: $error")
        }

        then("generates no warnings") {
          assertTrue(warnings.isEmpty(), "Unexpected warning(s): \n" + warnings.joinToString(separator = "\n"))
        }

        then("generates the correct result") {
          ProjectFixture.assertTextEquals(expected, result!!)
        }

      }
    }

  }

})
