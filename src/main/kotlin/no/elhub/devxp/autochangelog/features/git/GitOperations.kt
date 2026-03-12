package no.elhub.devxp.autochangelog.features.git

import no.elhub.devxp.autochangelog.Logger
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
    return toGitTags(rawTags, git.repository)
}

fun toGitTags(tags: List<Ref>, repo: Repository): List<GitTag> = tags.map {
    val peeled = repo.refDatabase.peel(it)
    val commitId = peeled.peeledObjectId ?: peeled.objectId

    GitTag(
        name = it.name.removePrefix("refs/tags/"),
        commitHash = commitId.name.take(7)
    )
}

fun getRelevantCommits(git: Git, from: GitTag?, to: GitTag?, tags: List<GitTag>): List<GitCommit> {
    val rawCommits = git.log().call().toList().reversed()
    Logger.debug("Got ${rawCommits.size} commits from git log.")
    val gitCommits = toGitCommits(rawCommits, tags)
    Logger.debug("Created ${gitCommits.size} GitCommit objects from raw commits.")
    return getCommitsBetweenTags(gitCommits, from, to)
}

fun getRelevantCommitsBetweenCommits(git: Git, from: String?, to: String?): List<GitCommit> {
    val rawCommits = git.log().call().toList().reversed()
    Logger.debug("Got ${rawCommits.size} commits from git log.")
    val gitCommits = toGitCommits(rawCommits, emptyList())
    Logger.debug("Created ${gitCommits.size} GitCommit objects from raw commits.")
    val fromCommit = from?.let { hash -> gitCommits.firstOrNull { it.hash.startsWith(hash) } }
    val toCommit = to?.let { hash -> gitCommits.firstOrNull { it.hash.startsWith(hash) } }
    return getCommitsBetweenCommits(gitCommits, fromCommit, toCommit)
}

fun toGitCommits(rawCommits: List<RevCommit>, tags: List<GitTag>): List<GitCommit> {
    val tagCommits = tags.groupBy { it.commitHash }
    return rawCommits.map {
        GitCommit(
            hash = it.name.take(7),
            title = it.shortMessage,
            body = it.fullMessage.split('\n').drop(1).joinToString("\n").trim(),
            commitTime = Instant.ofEpochSecond(it.commitTime.toLong())
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime(),
            tags = tagCommits[it.name.take(7)] ?: emptyList(),
            jiraIssues = extractJiraIssues(it.fullMessage)
        )
    }
}

fun extractJiraIssues(message: String): List<String> {
    val regex = Regex("""[A-Z][A-Z0-9]+-\d+""")
    return regex.findAll(message).map { it.value }.toList().distinct()
}

fun getCommitsBetweenCommits(
    commits: List<GitCommit>,
    fromCommit: GitCommit?,
    toCommit: GitCommit?
): List<GitCommit> {
    val startIndex = fromCommit?.let { commit ->
        commits.indexOfFirst { it.hash == commit.hash } + 1
    } ?: 0
    val endIndex = toCommit?.let {
        commits.indexOfFirst { it.hash == toCommit.hash } + 1
    } ?: commits.size

    require(startIndex <= endIndex) { "The 'from' commit cannot be newer than the 'to' commit." }
    Logger.debug("Finding commits between indices $startIndex and $endIndex (exclusive) in the list of ${commits.size} commits.")
    val commits = commits.subList(startIndex, endIndex)
    Logger.debug("Commit titles included in range: \n ${commits.joinToString("\n") { it.title }}")
    return commits
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

    Logger.debug("Finding commits between indices $startIndex and $endIndex (exclusive) in the list of ${commits.size} commits.")
    require(startIndex <= endIndex) { "The 'from' cannot be newer than the 'to' tag." }
    val commits = commits.subList(startIndex, endIndex)
    Logger.debug("Commit titles included in range: \n ${commits.joinToString("\n") { it.title }}")
    return commits
}

fun extractJiraIssuesIdsFromCommits(commits: List<GitCommit>): Map<String, List<GitCommit>> {
    Logger.debug("Extracting JIRA issue IDs from ${commits.size} commits...")
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

fun extractCurrentAndPreviousTag(tags: List<GitTag>, forTag: String?, tagRegex: String?): Pair<GitTag?, GitTag?> {
    val current = tags.firstOrNull { it.name == forTag }
    require(current != null) { "Tag '$forTag' not found in repository." }

    val candidateTags = tags.takeWhile { it != current }
    val previous = tagRegex?.let { pattern ->
        candidateTags.lastOrNull { Regex(pattern).matches(it.name) }
    } ?: candidateTags.lastOrNull()
    return previous to current
}
