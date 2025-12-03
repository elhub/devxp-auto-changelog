package no.elhub.devxp.autochangelog.model

import java.time.LocalDateTime

data class GitCommit(
    val hash: String,
    val title: String,
    val body: String,
    val date: LocalDateTime,
    val tags: List<GitTag>,
    val jiraIssues: List<String>
)

data class GitTag(
    val name: String,
    val commitHash: String,
)
