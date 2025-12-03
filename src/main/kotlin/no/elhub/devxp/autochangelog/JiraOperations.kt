package no.elhub.devxp.autochangelog

import no.elhub.devxp.autochangelog.model.GitCommit
import no.elhub.devxp.autochangelog.model.JiraIssue

fun extractJiraIssuesIdsFromCommits(commits: List<GitCommit>): Map<String, List<GitCommit>> {
    val jiraIdToCommitMap = mutableMapOf<String, MutableList<GitCommit>>()
    commits.forEach {
        if (it.jiraIssues.isNotEmpty()) {
            it.jiraIssues.forEach { jiraIssue ->
                jiraIdToCommitMap
                    .getOrPut(jiraIssue) { mutableListOf() }
                    .add(it)

            }
        } else {
            jiraIdToCommitMap.getOrPut("No Issue") { mutableListOf() }
                .add(it)
        }
    }
    return jiraIdToCommitMap.toMap()
}

suspend fun populateJiraMap(jiraMap: Map<String, List<GitCommit>>, client: JiraClient): Map<JiraIssue, List<GitCommit>> {
    return jiraMap.mapKeys { (jiraIssueId, _) ->
        if (jiraIssueId == "No Issue") {
            JiraIssue(
                key = "NO-JIRA",
                title = "Commits not associated with any JIRA issues",
                body = ""
            )
        } else
            client.getIssueById(jiraIssueId)
    }
}


// Temporary function for local testing
fun printJiraIssues(jiraIssues: Map<JiraIssue, List<GitCommit>>) {
    val noJiraEntry = jiraIssues.entries.filter { it.key.key == "NO-JIRA" }
    jiraIssues
        .filterNot { it.key.key == "NO-JIRA" }
        .forEach { (jiraIssue, commits) ->
            println("Jira Issue: ${jiraIssue.key}")
            println("Title: ${jiraIssue.title}")
            println("Body: ${jiraIssue.body}")
            println("Related Commits:")

            commits.forEach { commit ->
                println("- ${commit.hash}: ${commit.title}")
            }
            println()
        }
    if (noJiraEntry.isNotEmpty()) {
        println("Commits without associated JIRA issues:")
        noJiraEntry.first().value.forEach { commit ->
            println("- ${commit.hash}: ${commit.title}")
        }
    }
}
