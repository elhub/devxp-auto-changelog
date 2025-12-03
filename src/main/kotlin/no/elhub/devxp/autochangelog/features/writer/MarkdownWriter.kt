package no.elhub.devxp.autochangelog.features.writer

import java.io.File
import java.time.LocalDate.now
import no.elhub.devxp.autochangelog.features.git.GitCommit
import no.elhub.devxp.autochangelog.features.jira.JiraIssue


fun formatMarkdown(jiraIssues: Map<JiraIssue, List<GitCommit>>): String {
    val markdown = buildString {
        appendLine("Generated at ${now()}")

        val noJiraEntry = jiraIssues.entries.filter { it.key.key == "NO-JIRA" }

        jiraIssues
            .filterNot { it.key.key == "NO-JIRA" }
            .forEach { (jiraIssue, commits) ->
                appendLine("## ${jiraIssue.key}: ${jiraIssue.title}")
                appendLine()
                appendLine(jiraIssue.body)
                appendLine()
                appendLine("### Related Commits")
                appendLine()
                commits.forEach { commit ->
                    appendLine("- `${commit.hash}`: ${commit.title}")
                }
                appendLine()
            }

        if (noJiraEntry.isNotEmpty()) {
            appendLine("## Commits without associated JIRA issues")
            appendLine()
            noJiraEntry.first().value.forEach { commit ->
                appendLine("- `${commit.hash}`: ${commit.title}")
            }
        }
    }

    return markdown
}

fun writeMarkdownToFile(markdown: String, filePath: String) {
    val file = File(filePath)
    file.writeText(markdown)
}
