package no.elhub.devxp.autochangelog.features.jira

import no.elhub.devxp.autochangelog.features.git.GitCommit

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
            jiraIdToCommitMap.getOrPut("NO-JIRA") { mutableListOf() }
                .add(it)
        }
    }
    return jiraIdToCommitMap.toMap()
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
