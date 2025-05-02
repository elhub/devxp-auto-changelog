package no.elhub.devxp.autochangelog.jira

import kotlinx.serialization.Serializable
import no.elhub.devxp.autochangelog.project.ChangelogEntry
import no.elhub.devxp.autochangelog.project.Version
import no.elhub.devxp.autochangelog.serializers.LocalDateSerializer
import java.time.LocalDate

/**
 * Extends the ChangelogEntry to include Jira issue details.
 * This is used for JSON serialization when Jira integration is enabled.
 */
@Serializable
data class JiraChangelogEntry(
    val release: Release?,
    val added: List<JiraEntry> = emptyList(),
    val changed: List<JiraEntry> = emptyList(),
    val fixed: List<JiraEntry> = emptyList(),
    val breakingChange: List<JiraEntry> = emptyList(),
    val other: List<JiraEntry> = emptyList()
) {
    @Serializable
    data class Release(
        val version: Version,
        @Serializable(with = LocalDateSerializer::class)
        val date: LocalDate
    )

    @Serializable
    data class JiraEntry(
        val text: String,
        val jira_issues: List<JiraIssueInfo> = emptyList()
    )

    @Serializable
    data class JiraIssueInfo(
        val key: String,
        val title: String,
        val description: String?,
        val url: String
    )

    companion object {
        /**
         * Convert a standard ChangelogEntry to a JiraChangelogEntry with associated Jira issues.
         */
        fun fromChangelogEntry(entry: ChangelogEntry, jiraEnabled: Boolean): JiraChangelogEntry {
            if (!jiraEnabled || !JiraIssueExtractor.isInitialized()) {
                // If Jira is not enabled, create a simple JiraChangelogEntry without issue details
                return JiraChangelogEntry(
                    release = entry.release?.let {
                        Release(it.version, it.date)
                    },
                    added = entry.added.map { JiraEntry(it) },
                    changed = entry.changed.map { JiraEntry(it) },
                    fixed = entry.fixed.map { JiraEntry(it) },
                    breakingChange = entry.breakingChange.map { JiraEntry(it) },
                    other = entry.other.map { JiraEntry(it) }
                )
            }

            // Helper function to extract and add Jira issues
            fun String.withJiraIssues(): JiraEntry {
                val jiraRegex = JiraIssueExtractor.jiraRegex
                val issueKeys = jiraRegex.findAll(this).map { it.value }.distinct().toList()
                val jiraIssues = issueKeys.mapNotNull { JiraIssueExtractor.fetchJiraIssue(it) }
                    .map { JiraIssueInfo(it.key, it.title, it.description, it.url) }

                return JiraEntry(this, jiraIssues)
            }

            return JiraChangelogEntry(
                release = entry.release?.let {
                    Release(it.version, it.date)
                },
                added = entry.added.map { it.withJiraIssues() },
                changed = entry.changed.map { it.withJiraIssues() },
                fixed = entry.fixed.map { it.withJiraIssues() },
                breakingChange = entry.breakingChange.map { it.withJiraIssues() },
                other = entry.other.map { it.withJiraIssues() }
            )
        }
    }
}
