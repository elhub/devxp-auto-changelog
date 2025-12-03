package no.elhub.devxp.autochangelog.features.git

import java.time.LocalDate
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit

fun toGitTags(tags: List<Ref>): List<GitTag> = tags.map {
    val commitId = it.peeledObjectId ?: it.objectId
    GitTag(
        name = it.name.replace("refs/tags/", ""),
        commitHash = commitId.name
    )
}

fun toGitCommits(rawCommits: List<RevCommit>, tags: List<GitTag>): List<GitCommit> {
    val tagCommits = tags.groupBy { it.commitHash }
    return rawCommits.map {
        GitCommit(
            hash = it.name,
            title = it.shortMessage,
            body = it.fullMessage.split('\n').drop(1).joinToString("\n").trim(),
            date = LocalDate.ofEpochDay(it.commitTime.toLong()),
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
    fromTag: GitTag,
    toTag: GitTag
): List<GitCommit> {
    val fromCommit = commits.first { it.hash == fromTag.commitHash }
    val toCommit = commits.first { it.hash == toTag.commitHash }

    val startIndex = commits.indexOf(fromCommit) + 1
    val endIndex = commits.indexOf(toCommit) + 1

    require(startIndex < endIndex) { "The 'from' tag must be older than the 'to' tag." }
    return commits.subList(startIndex, endIndex)
}
