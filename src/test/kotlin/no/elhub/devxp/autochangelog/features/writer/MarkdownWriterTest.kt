package no.elhub.devxp.autochangelog.features.writer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.elhub.devxp.autochangelog.features.git.GitCommit
import no.elhub.devxp.autochangelog.features.jira.JiraIssue
import java.io.File
import java.time.LocalDate

class MarkdownWriterTest : FunSpec({

    test("formatMarkdown correctly formats markdown content") {
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

        val formattedMarkDownWithoutDate = formatMarkdown(myMap).substringAfter("\n")

        formattedMarkDownWithoutDate shouldBe """
            ## ABC-101: Implement feature X

            This is a description for ABC-101.

            ### Related Commits

            - `abc123`: Implement feature X
            - `def456`: Fix bug Y

            ## ABC-102: Fix bug Y

            This is a description for ABC-102.

            ### Related Commits

            - `abc123`: Implement feature X

            ## Commits without associated JIRA issues

            - `ghi789`: Update documentation

        """.trimIndent()
    }

    context("writeMarkdownToFile") {
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

        test("Can write markdown content to a file") {
            val markdownContent = """
                Generated at 2050-06-01
                ## [1.0.0] - 2024-01-01
                - Initial release
                ### Related Commits
                - `abc123`: Initial commit
            """.trimIndent()
            writeMarkdownToFile(markdownContent, changelogFile.path)
            changelogFile.exists() shouldBe true
            val fileContent = changelogFile.readText()
            fileContent shouldBe markdownContent
        }
    }
})
