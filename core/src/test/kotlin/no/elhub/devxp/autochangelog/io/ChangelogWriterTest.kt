package no.elhub.devxp.autochangelog.io

import io.kotest.assertions.json.shouldBeValidJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.elhub.devxp.autochangelog.jira.JiraIssueExtractor
import no.elhub.devxp.autochangelog.jiraIssueExtractor
import no.elhub.devxp.autochangelog.project.defaultContent
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class ChangelogWriterTest : FunSpec({

    fun startKoin(): JiraIssueExtractor {
        val jiraIssueExtractor = jiraIssueExtractor()
        org.koin.core.context.startKoin {
            modules(
                module {
                    single {
                        jiraIssueExtractor
                    }
                }
            )
        }
        return jiraIssueExtractor
    }

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

    context("writeToJsonWithDateTime") {
        val writer = ChangelogWriter()
        val dateTime = "2024-06-10" to "12:34:56"

        test("should wrap JSON with date and time when dateTime is provided") {
            val json = writer.writeToJsonWithDateTime(singleChangelist, dateTime)
            json.shouldBeValidJson()
            json shouldBe """
                {
                    "date": "2024-06-10",
                    "time": "12:34:56",
                    "entries": [
                        {
                            "added": [
                                "Option to delete system32"
                            ],
                            "changed": [
                                "All the functionality"
                            ],
                            "breakingChange": [
                                "Broke all APIs"
                            ]
                        }
                    ]
                }
            """.trimIndent() + "\n"
        }

        xtest("should fallback to writeToJson when dateTime is null") {
            val json = writer.writeToJsonWithDateTime(singleChangelist, null)
            json.shouldBeValidJson()
            json shouldBe writer.writeToJson(singleChangelist)
        }

        xtest("should wrap JSON with date and time and include Jira details if enabled") {
            startKoin().initialize("user", "token")
            try {
                val writerWithJira = ChangelogWriter(includeJiraDetails = true)
                val json = writerWithJira.writeToJsonWithDateTime(singleChangelist, dateTime)
                json.shouldBeValidJson()
                json shouldBe """
                {
                    "date": "2024-06-10",
                    "time": "12:34:56",
                    "entries": [
                        {
                            "added": [
                                "Option to delete system32"
                            ],
                            "changed": [
                                "All the functionality"
                            ],
                            "breakingChange": [
                                "Broke all APIs"
                            ]
                        }
                    ]
                }
                """.trimIndent() + "\n"
            } finally {
                stopKoin()
            }
        }

        test("should handle multiple changelog entries with dateTime") {
            val json = writer.writeToJsonWithDateTime(doubleChangeList, dateTime)
            json.shouldBeValidJson()
        }

        test("should handle empty changelist with dateTime") {
            val emptyChangelist = singleChangelist.copy(changes = mapOf())
            val json = writer.writeToJsonWithDateTime(emptyChangelist, dateTime)
            json.shouldBeValidJson()
            json shouldBe """
                {
                    "date": "2024-06-10",
                    "time": "12:34:56",
                    "entries": []
                }
            """.trimIndent() + "\n"
        }

        test("should properly format different date and time values") {
            val differentDateTime = "2023-12-31" to "23:59:59"
            val json = writer.writeToJsonWithDateTime(singleChangelist, differentDateTime)
            json.shouldBeValidJson()
            json shouldBe """
                {
                    "date": "2023-12-31",
                    "time": "23:59:59",
                    "entries": [
                        {
                            "added": [
                                "Option to delete system32"
                            ],
                            "changed": [
                                "All the functionality"
                            ],
                            "breakingChange": [
                                "Broke all APIs"
                            ]
                        }
                    ]
                }
            """.trimIndent() + "\n"
        }

        test("should handle changelist with all change types and dateTime") {
            val allTypesChangelist = doubleChangeList
            val json = writer.writeToJsonWithDateTime(allTypesChangelist, dateTime)
            json.shouldBeValidJson()
        }
    }
})
