package no.elhub.devxp.autochangelog.features.writer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.time.LocalDate
import no.elhub.devxp.autochangelog.features.git.GitCommit
import no.elhub.devxp.autochangelog.features.jira.JiraIssue

class JsonWriterTest: FunSpec({

    test("formatJson correctly formats json content") {
        val commit1 = GitCommit(
            hash = "abc123",
            title = "Implement feature X",
            body = "This commit implements feature X.\n\nRelated to ABC-101 and PROJ-ABC.",
            date = LocalDate.of(2020, 1, 1),
            tags = emptyList(),
            jiraIssues = listOf("ABC-101", "ABC-102")
        )

        val commit2 = GitCommit(
            hash = "def456",
            title = "Fix bug Y",
            body = "Fixes bug Y reported in XYZ-202.",
            date = LocalDate.of(2020, 1, 2),
            tags = emptyList(),
            jiraIssues = listOf("ABC-101")
        )

        val commit3 = GitCommit(
            hash = "ghi789",
            title = "Update documentation",
            body = "Updates the documentation. No related issue.",
            date = LocalDate.of(2020, 1, 3),
            tags = emptyList(),
            jiraIssues = emptyList()
        )

        val issue1 = JiraIssue(
            key = "ABC-101",
            title = "Implement feature X",
            body = "This is a description for ABC-101."
        )
        val issue2 = JiraIssue(
            key = "ABC-102",
            title = "Fix bug Y",
            body = "This is a description for ABC-102."
        )
        val noIssue = JiraIssue(
            key = "NO-JIRA",
            title = "No Jira Issue",
            body = "Commits without linked Jira issues."
        )

        val myMap = mapOf(
            issue1 to listOf(commit1, commit2),
            issue2 to listOf(commit1),
            noIssue to listOf(commit3)
        )

        val formattedJson = formatJson(myMap)

        formattedJson shouldBe  """
            {
                "generatedAt": "${LocalDate.now()}",
                "issues": [
                    {
                        "key": "ABC-101",
                        "title": "Implement feature X",
                        "body": "This is a description for ABC-101.",
                        "commits": [
                            {
                                "hash": "abc123",
                                "title": "Implement feature X",
                                "body": "This commit implements feature X.\n\nRelated to ABC-101 and PROJ-ABC.",
                                "date": "2020-01-01",
                                "tags": [],
                                "jiraIssues": [
                                    "ABC-101",
                                    "ABC-102"
                                ]
                            },
                            {
                                "hash": "def456",
                                "title": "Fix bug Y",
                                "body": "Fixes bug Y reported in XYZ-202.",
                                "date": "2020-01-02",
                                "tags": [],
                                "jiraIssues": [
                                    "ABC-101"
                                ]
                            }
                        ]
                    },
                    {
                        "key": "ABC-102",
                        "title": "Fix bug Y",
                        "body": "This is a description for ABC-102.",
                        "commits": [
                            {
                                "hash": "abc123",
                                "title": "Implement feature X",
                                "body": "This commit implements feature X.\n\nRelated to ABC-101 and PROJ-ABC.",
                                "date": "2020-01-01",
                                "tags": [],
                                "jiraIssues": [
                                    "ABC-101",
                                    "ABC-102"
                                ]
                            }
                        ]
                    }
                ],
                "commitsWithoutJira": [
                    {
                        "hash": "ghi789",
                        "title": "Update documentation",
                        "body": "Updates the documentation. No related issue.",
                        "date": "2020-01-03",
                        "tags": [],
                        "jiraIssues": []
                    }
                ]
            }
        """.trimIndent()
    }

    context("writeJsonToFile") {
        val changelogFile = File("CHANGELOG.md")

        beforeEach {
            if (changelogFile.exists()) {
                changelogFile.delete()
            }
        }
        afterEach {
            if (changelogFile.exists()) {
                changelogFile.delete()
            }
        }

        test("Can write json content to a file") {
            val jsonContent = """
                {
                "generatedAt": "${'$'}{LocalDate.now()}",
                "issues": [
                    {
                        "key": "ABC-101",
                        "title": "Implement feature X",
                        "body": "This is a description for ABC-101.",
                        "commits": [
                            {
                                "hash": "abc123",
                                "title": "Implement feature X",
                                "body": "This commit implements feature X.\n\nRelated to ABC-101 and PROJ-ABC.",
                                "date": "2020-01-01",
                                "tags": [],
                                "jiraIssues": [
                                    "ABC-101",
                                    "ABC-102"
                                ]
                            },
                            {
                                "hash": "def456",
                                "title": "Fix bug Y",
                                "body": "Fixes bug Y reported in XYZ-202.",
                                "date": "2020-01-02",
                                "tags": [],
                                "jiraIssues": [
                                    "ABC-101"
                                ]
                            }
                        ]
                    }
                ],
                "commitsWithoutJira": [
                    {
                        "hash": "ghi789",
                        "title": "Update documentation",
                        "body": "Updates the documentation. No related issue.",
                        "date": "2020-01-03",
                        "tags": [],
                        "jiraIssues": []
                    }
                ]
            }
            """.trimIndent()
            writeJsonToFile(jsonContent, changelogFile.path)
            changelogFile.exists() shouldBe true
            val fileContent = changelogFile.readText()
            fileContent shouldBe jsonContent
        }
    }
})
