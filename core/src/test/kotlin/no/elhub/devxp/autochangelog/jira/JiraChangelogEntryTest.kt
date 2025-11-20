package no.elhub.devxp.autochangelog.jira

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.serialization.json.Json
import no.elhub.devxp.autochangelog.project.ChangelogEntry
import no.elhub.devxp.autochangelog.project.SemanticVersion
import no.elhub.devxp.autochangelog.project.Version
import java.time.LocalDate

class JiraChangelogEntryTest : FunSpec({

    test("JiraChangelogEntry serializes and deserializes correctly") {
        val entry = JiraChangelogEntry(
            release = JiraChangelogEntry.Release(SemanticVersion("1.0.0"), LocalDate.of(2024, 6, 1)),
            added = listOf(JiraChangelogEntry.JiraEntry("Added feature", listOf(
                JiraChangelogEntry.JiraIssueInfo("PROJ-1", "Title", "Desc", "http://jira/PROJ-1")
            )))
        )
        val json = Json.encodeToString(JiraChangelogEntry.serializer(), entry)
        val decoded = Json.decodeFromString(JiraChangelogEntry.serializer(), json)
        decoded shouldBe entry
    }

    test("fromChangelogEntry returns simple JiraChangelogEntry when jiraEnabled is false") {
        val changelogEntry = ChangelogEntry(
            release = ChangelogEntry.Release(SemanticVersion("1.0.0"), LocalDate.of(2024, 6, 1)),
            added = listOf("Added feature"),
            changed = listOf("Changed something"),
            fixed = listOf("Fixed bug"),
            breakingChange = listOf("Breaking change"),
            other = listOf("Other note")
        )
        val result = JiraChangelogEntry.fromChangelogEntry(changelogEntry, jiraEnabled = false)
        result.added[0].text shouldBe "Added feature"
        result.added[0].jira_issues shouldBe emptyList()
    }

    test("fromChangelogEntry returns JiraChangelogEntry with Jira issues when jiraEnabled is true and extractor initialized") {
        val changelogEntry = ChangelogEntry(
            release = ChangelogEntry.Release(SemanticVersion("1.0.0"), LocalDate.of(2024, 6, 1)),
            added = listOf("Added PROJ-1"),
            changed = emptyList(),
            fixed = emptyList(),
            breakingChange = emptyList(),
            other = emptyList()
        )
        mockkObject(JiraIssueExtractor)
        every { JiraIssueExtractor.isInitialized() } returns true
        every { JiraIssueExtractor.jiraRegex } returns "([A-Z][A-Z0-9_]+-[0-9]+)".toRegex()
        every { JiraIssueExtractor.fetchJiraIssue("PROJ-1") } returns JiraIssue("PROJ-1", "Title", "Desc", "http://jira/PROJ-1")

        val result = JiraChangelogEntry.fromChangelogEntry(changelogEntry, jiraEnabled = true)
        result.added[0].jira_issues shouldBe listOf(
            JiraChangelogEntry.JiraIssueInfo("PROJ-1", "Title", "Desc", "http://jira/PROJ-1")
        )

        unmockkObject(JiraIssueExtractor)
    }

    test("fromChangelogEntry returns JiraChangelogEntry with empty Jira issues if extractor not initialized") {
        val changelogEntry = ChangelogEntry(
            release = null,
            added = listOf("Added PROJ-2"),
            changed = emptyList(),
            fixed = emptyList(),
            breakingChange = emptyList(),
            other = emptyList()
        )
        mockkObject(JiraIssueExtractor)
        every { JiraIssueExtractor.isInitialized() } returns false

        val result = JiraChangelogEntry.fromChangelogEntry(changelogEntry, jiraEnabled = true)
        result.added[0].jira_issues shouldBe emptyList()

        unmockkObject(JiraIssueExtractor)
    }
})

