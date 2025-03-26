package no.elhub.devxp.autochangelog.io

import io.kotest.assertions.json.shouldBeValidJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.elhub.devxp.autochangelog.project.defaultContent

class ChangelogWriterTest : FunSpec({
    context("write with non-empty original changelog contents") {
        val defaultDescription = """
                # Changelog

                All notable changes to this project will be documented in this file.
            """.trimIndent()

        val existingContent = """
                ## [1.1.0] - 2024-02-15

                ### Added

                - 15 new features

                ### Changed

                - All the code
            """.trimIndent()

        val writer = ChangelogWriter(start = defaultDescription, end = existingContent)

        test("should append new content after default description") {
            val s = writer.writeToString(singleChangelist)
            s shouldBe """
                    |$defaultDescription
                    |
                    |$singleExpectedMd
                    |
                    |$existingContent
                """.trimMargin()
        }
    }

    context("write with empty original changelog contents") {
        val writer = ChangelogWriter()

        listOf(singleChangelogTestMd, doubleChangeLogTestMd).map {
            test(it.name) {
                val s = writer.writeToString(it.changelist)
                s shouldBe """
                        |${defaultContent.joinToString("\n")}
                        |
                        |${it.expected}
                    """.trimMargin()
            }
        }

        test("writeToJson() should produce valid json") {
            val s = writer.writeToJson(singleChangelist)
            s.shouldBeValidJson()
        }

        listOf(singleChangelogTestJson, doubleChangeLogTestJson).map {
            test(it.name) {
                writer.writeToJson(it.changelist) shouldBe it.expected
            }
        }
    }
})
