package no.elhub.devxp.autochangelog

import no.elhub.devxp.autochangelog.model.GitCommit
import no.elhub.devxp.autochangelog.model.GitTag
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import java.time.LocalDateTime
import java.time.ZoneOffset

fun toGitTags(tags: List<Ref>): List<GitTag> {
    return tags.map {
        GitTag(
            name = it.name,
            commitHash = it.objectId.name,
        )
    }
}

fun toGitCommits(rawCommits: List<RevCommit>, tags: List<GitTag>): List<GitCommit> {
    val tagCommits = tags.associate { it.commitHash to it.name }
    return rawCommits.map {
        GitCommit(
            hash = it.name,
            title = it.shortMessage,
            body = it.fullMessage.split('\n').drop(1).joinToString("\n").trim(),
            date = LocalDateTime.ofEpochSecond(it.commitTime.toLong(), 0, ZoneOffset.UTC),
            tag = if (it.name in tagCommits) {
                GitTag(
                    name = tagCommits[it.name]!!,
                    commitHash = it.name,
                )
            } else {
                null
            },
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
