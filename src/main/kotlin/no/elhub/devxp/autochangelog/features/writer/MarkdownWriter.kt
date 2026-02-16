package no.elhub.devxp.autochangelog.features.writer

import no.elhub.devxp.autochangelog.features.git.GitCommit
import no.elhub.devxp.autochangelog.features.jira.JiraIssue
import java.io.File
import java.time.LocalDate.now

const val JIRA_PREFIX = "https://elhub.atlassian.net/browse/"

fun formatMarkdown(jiraIssues: Map<JiraIssue, List<GitCommit>>, strikethrough: Boolean = false): String {
    val markdown = buildString {
        appendLine("Generated at ${now()}")

        val noJiraEntry = jiraIssues.entries.filter { it.key.key == "NO-JIRA" }

        jiraIssues
            .filterNot { it.key.key == "NO-JIRA" }
            .forEach { (jiraIssue, commits) ->
                if (strikethrough && jiraIssue.status == "Done") {
                    appendLine("## ~~[${jiraIssue.key}]($JIRA_PREFIX${jiraIssue.key})~~: ${jiraIssue.title}")
                } else {
                    appendLine("## [${jiraIssue.key}]($JIRA_PREFIX${jiraIssue.key}): ${jiraIssue.title}")
                }
                appendLine(jiraIssue.body)
                appendLine("### Related Commits")
                commits.forEach { commit ->
                    appendLine("- `${commit.hash}`: ${commit.title}")
                }
                appendLine()
            }

        if (noJiraEntry.isNotEmpty()) {
            appendLine("## Commits without associated JIRA issues")
            noJiraEntry.first().value.forEach { commit ->
                appendLine("- `${commit.hash}`: ${commit.title}")
            }
        }
    }

    return markdown
}

fun formatCommitMarkdown(commitsMap: Map<GitCommit, List<JiraIssue>>, strikethrough: Boolean = false): String {
    val markdown = buildString {
        appendLine("Generated at ${now()}")

        commitsMap.forEach { (commit, jiraIssues) ->
            if (commit.tags.isNotEmpty()) {
                appendLine("# ${commit.tags.joinToString(", ") { it.name }}")
            }
            appendLine("## `${commit.hash}` **${commit.title}**")
            if (jiraIssues.first().key != "NO-JIRA") {
                jiraIssues.forEach { jiraIssue ->
                    if (strikethrough && jiraIssue.status == "Done") {
                        appendLine("- ~~[${jiraIssue.key}]($JIRA_PREFIX${jiraIssue.key})~~: ${jiraIssue.title}")
                    } else {
                        appendLine("- [${jiraIssue.key}]($JIRA_PREFIX${jiraIssue.key}): ${jiraIssue.title}")
                    }
                }
            }
            appendLine()
        }
    }.trimEnd().plus("\n") // Ensure exactly one newline

    return markdown
}

fun writeMarkdownToFile(markdown: String, filePath: String) {
    val file = File(filePath)
    file.parentFile?.mkdirs()
    file.writeText(markdown)
}
