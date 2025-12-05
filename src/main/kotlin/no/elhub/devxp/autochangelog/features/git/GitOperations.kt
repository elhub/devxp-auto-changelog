package no.elhub.devxp.autochangelog.features.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File
import java.time.Instant
import java.time.ZoneOffset

fun initRepository(workingDir: String): Git {
    val workingDirectory = File(workingDir)
    require(workingDirectory.exists()) { "Working directory $workingDirectory does not exist." }
    require(workingDirectory.isDirectory) { "Working directory $workingDirectory is not a directory." }
    require(workingDirectory.resolve(".git").exists()) { "Working directory $workingDirectory does not contain a .git directory." }

    // Initialize Git repository
    val repo: Repository = FileRepositoryBuilder()
        .setWorkTree(workingDirectory)
        .build()

    return Git(repo)
}

fun getTagsFromRepo(git: Git): List<GitTag> {
    val rawTags = git.tagList().call().toList()
    return toGitTags(rawTags)
}

fun toGitTags(tags: List<Ref>): List<GitTag> = tags.map {
    val commitId = it.peeledObjectId ?: it.objectId
    GitTag(
        name = it.name.replace("refs/tags/", ""),
        commitHash = commitId.name
    )
}

fun getRelevantCommits(git: Git, from: GitTag?, to: GitTag?, tags: List<GitTag>): List<GitCommit> {
    val rawCommits = git.log().call().toList().reversed()
    val gitCommits = toGitCommits(rawCommits, tags)
    return getCommitsBetweenTags(gitCommits, from, to)
}

fun toGitCommits(rawCommits: List<RevCommit>, tags: List<GitTag>): List<GitCommit> {
    val tagCommits = tags.groupBy { it.commitHash }
    return rawCommits.map {
        GitCommit(
            hash = it.name,
            title = it.shortMessage,
            body = it.fullMessage.split('\n').drop(1).joinToString("\n").trim(),
            commitTime = Instant.ofEpochSecond(it.commitTime.toLong())
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime(),
            tags = tagCommits[it.name] ?: emptyList(),
            jiraIssues = extractJiraIssues(it.fullMessage)
        )
    }
}

private fun extractJiraIssues(message: String): List<String> {
    val regex = Regex("""[A-Z][A-Z0-9]+-\d+""")
    return regex.findAll(message).map { it.value }.toList()
}

fun getCommitsBetweenTags(
    commits: List<GitCommit>,
    fromTag: GitTag?,
    toTag: GitTag?
): List<GitCommit> {
    val startIndex = fromTag?.let { tag ->
        commits.indexOfFirst { it.hash == tag.commitHash } + 1
    } ?: 0

    val endIndex = toTag?.let { tag ->
        commits.indexOfFirst { it.hash == tag.commitHash } + 1
    } ?: commits.size

    require(startIndex <= endIndex) { "The 'from' cannot be newer than the 'to' tag." }
    return commits.subList(startIndex, endIndex)
}

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
