package no.elhub.devxp.autochangelog

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.maps.shouldHaveKeys
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import no.elhub.devxp.autochangelog.model.GitCommit
import no.elhub.devxp.autochangelog.model.JiraIssue

class JiraIssueTest : FunSpec({
    context("no.elhub.devxp.autochangelog.extractJiraIssuesIdsFromCommits") {
        val commit1 = GitCommit(
            hash = "abc123",
            title = "Implement feature X",
            body = "This commit implements feature X.\n\nRelated to ABC-101 and PROJ-ABC.",
            date = LocalDateTime.of(2020, 1, 1, 1, 1),
            tags = emptyList(),
            jiraIssues = listOf("ABC-101", "ABC-102")
        )

        val commit2 = GitCommit(
            hash = "def456",
            title = "Fix bug Y",
            body = "Fixes bug Y reported in XYZ-202.",
            date = LocalDateTime.of(2020, 1, 2, 1, 1),
            tags = emptyList(),
            jiraIssues = listOf("XYZ-202")
        )

        val commit3 = GitCommit(
            hash = "ghi789",
            title = "Update documentation",
            body = "Updates the documentation. No related issue.",
            date = LocalDateTime.of(2020, 1, 3, 1, 1),
            tags = emptyList(),
            jiraIssues = emptyList()
        )

        val commit4 = GitCommit(
            hash = "jkl012",
            title = "Refactor codebase",
            body = "Refactors the codebase for better readability. See ABC-101 for details.",
            date = LocalDateTime.of(2020, 1, 4, 1, 1),
            tags = emptyList(),
            jiraIssues = listOf("ABC-101")
        )


        test("correctly extracts issues") {
            val commits = listOf(commit1, commit2, commit3, commit4)
            val result = extractJiraIssuesIdsFromCommits(commits)
            result.size shouldBe 3
            result.shouldHaveKeys("ABC-101", "ABC-102", "XYZ-202")
            assertSoftly(result) {
                this["ABC-101"] shouldBe listOf(commit1, commit4)
                this["ABC-102"] shouldBe listOf(commit1)
                this["XYZ-202"] shouldBe listOf(commit2)
            }
        }
    }

    context("no.elhub.devxp.autochangelog.getJiraIssuesById") {

        val commit1 = GitCommit(
            hash = "abc123",
            title = "Implement feature X",
            body = "This commit implements feature X.\n\nRelated to ABC-101 and PROJ-ABC.",
            date = LocalDateTime.of(2020, 1, 1, 1, 1),
            tags = emptyList(),
            jiraIssues = listOf("ABC-101", "ABC-102")
        )

        val commit2 = GitCommit(
            hash = "def456",
            title = "Fix bug Y",
            body = "Fixes bug Y reported in XYZ-202.",
            date = LocalDateTime.of(2020, 1, 2, 1, 1),
            tags = emptyList(),
            jiraIssues = listOf("XYZ-202")
        )

        val idMap = extractJiraIssuesIdsFromCommits(listOf(commit1, commit2))

        test("does stuff") {
            val result = populateJiraMap(idMap)
            result.size shouldBe 3
            result.keys.map { it.key }.toSet() shouldBe setOf("ABC-101", "ABC-102", "XYZ-202")
            val expectedKey = JiraIssue("ABC-102", "Not a real title", "Not a real body")
            result.keys shouldContain expectedKey
            result[expectedKey] shouldBe listOf(commit1)
        }
    }
})
