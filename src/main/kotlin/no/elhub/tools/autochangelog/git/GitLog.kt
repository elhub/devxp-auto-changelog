package no.elhub.tools.autochangelog.git

/**
 * Represents a git log consisting of [commits] starting from most recent commit
 */
data class GitLog(val commits: List<GitCommit>)
