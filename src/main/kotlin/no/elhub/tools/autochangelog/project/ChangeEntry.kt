package no.elhub.tools.autochangelog.project

import no.elhub.tools.autochangelog.config.Configuration.includeJiraLinks
import no.elhub.tools.autochangelog.config.Configuration.jiraIssuesPatternString
import no.elhub.tools.autochangelog.config.Configuration.jiraIssuesUrl
import no.elhub.tools.autochangelog.git.GitMessage
import no.elhub.tools.autochangelog.git.TitleKeyword
import no.elhub.tools.autochangelog.git.titleKeyword
import java.time.LocalDate

data class ChangelogEntry(
    val release: Release?,
    val added: List<String> = emptyList(),
    val changed: List<String> = emptyList(),
    val fixed: List<String> = emptyList(),
    val breakingChange: List<String> = emptyList(),
    val other: List<String> = emptyList()
) {
    data class Release(
        val version: Version,
        val date: LocalDate
    )

    class Builder {
        private var release: Release? = null
        val added: MutableList<String> = mutableListOf()
        val changed: MutableList<String> = mutableListOf()
        val fixed: MutableList<String> = mutableListOf()
        val breakingChange: MutableList<String> = mutableListOf()
        val other: MutableList<String> = mutableListOf()

        fun withRelease(release: Release): Builder {
            return apply { this.release = release }
        }

        fun withMessage(gitMessage: GitMessage): Builder {
            val msg = gitMessage.description.firstOrNull { it.startsWith(jiraIssuesPatternString) }?.let { jiraIds ->
                val title = gitMessage.title
                val jiraIssues = jiraIds.replace(jiraIssuesPatternString, "")
                    .split(", ")
                    .joinToString(", ") { if (includeJiraLinks) "[$it]($jiraIssuesUrl/$it)" else it }
                "[ $jiraIssues ] $title"
            } ?: gitMessage.title

            when (gitMessage.titleKeyword) {
                TitleKeyword.ADD -> this.added.add(msg)
                TitleKeyword.BREAKING_CHANGE -> this.breakingChange.add(msg)
                TitleKeyword.CHANGE -> this.changed.add(msg)
                TitleKeyword.FIX -> this.fixed.add(msg)
                TitleKeyword.OTHER -> this.other.add(msg)
            }

            return this
        }

        fun build() = ChangelogEntry(
            release = release,
            added = added,
            changed = changed,
            fixed = fixed,
            breakingChange = breakingChange,
            other = other
        )
    }
}
