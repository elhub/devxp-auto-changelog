package no.elhub.devxp.autochangelog.features.git

import java.time.LocalDateTime

data class GitCommit(
    val hash: String,
    val title: String,
    val body: String,
    val commitTime: LocalDateTime,
    val tags: List<GitTag>,
    val jiraIssues: List<String>
)

data class GitTag(
    val name: String,
    val commitHash: String,
)
