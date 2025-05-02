package no.elhub.devxp.autochangelog.project

import no.elhub.devxp.autochangelog.config.Configuration.INCLUDE_ONLY_WITH_JIRA
import no.elhub.devxp.autochangelog.extensions.description
import no.elhub.devxp.autochangelog.extensions.title
import no.elhub.devxp.autochangelog.git.GitCommit
import no.elhub.devxp.autochangelog.git.GitLog
import no.elhub.devxp.autochangelog.git.GitMessage
import no.elhub.devxp.autochangelog.jira.JiraIssueExtractor.extractJiraIssueFromCommit
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import java.time.ZoneId

/**
 * Represents a generic git repository.
 *
 * Serves mainly as a wrapper around [git] functionality.
 */
class GitRepo(val git: Git) {

    val tags: () -> List<Ref> = { git.tagList().call() }

    /**
     * Returns the log from this [git] object as sequence of commits.
     *
     * If both [start] and [end] are provided, uses the range `since..until`.
     *
     * @param start the commit to start graph traversal from
     * @param end same as `--not start` or `start^`
     */
    fun log(start: ObjectId? = null, end: ObjectId? = null): Sequence<RevCommit> {
        val log = git.log()
        if (start != null) {
            if (end != null) log.addRange(start, end) else log.add(start)
        } else if (end != null) {
            log.not(end)
        }
        return log.call().asSequence()
    }

    /**
     * Returns a [Ref] from this [versionTag]
     * or null if the ref is not found in tha tag list of this [git] object.
     */
    fun findTagRef(versionTag: SemanticVersion): Ref? = git.tagList().call().firstOrNull {
        it.name == "refs/tags/v$versionTag"
    }

    /**
     * Returns the git tag id for the [versionTag] value.
     */
    fun findCommitId(versionTag: SemanticVersion): ObjectId? = findTagRef(versionTag)?.let {
        git.repository.refDatabase.peel(it).peeledObjectId ?: it.objectId
    }

    fun findParent(objectId: ObjectId) = findCommit(objectId)?.getParent(0)

    fun findCommit(objectId: ObjectId): RevCommit? = log().firstOrNull { it == objectId }

    /**
     * Constructs the log of [GitCommit]s for this [GitRepo] and returns as an instance of [GitLog]
     *
     * If both [start] and [end] are provided, uses the range `since..until` for the git log.
     *
     * @param start the commit to start git log graph traversal from
     * @param end same as `--not start` or `start^`
     */
    fun constructLog(
        start: ObjectId? = null,
        end: ObjectId? = null,
        predicate: (RevCommit) -> Boolean = { true }
    ): GitLog {
        val versionedCommits: List<Pair<String, ObjectId>> = git.tagList().call()
            .map {
                val name = it.name.replace("refs/tags/v", "")
                git.repository.refDatabase.peel(it).peeledObjectId?.let { id -> name to id }
                    ?: (name to it.objectId)
            }
            .filter { (v, _) -> v.matches(versionPattern.toRegex()) }

        val commits = mutableListOf<GitCommit>()
        for (commit in log(start = start, end = end)) {
            if (predicate(commit)) {
                val jiraId = if (INCLUDE_ONLY_WITH_JIRA) extractJiraIssueFromCommit(commit) else null
                val v = versionedCommits.firstOrNull { it.second == commit.id }?.first

                if (!INCLUDE_ONLY_WITH_JIRA || commits.none { it.message.title == jiraId }) {
                    val c = GitCommit(
                        message = GitMessage(
                            title = jiraId ?: commit.title,
                            description = commit.description
                        ),
                        objectId = commit.id,
                        date = commit.authorIdent.`when`.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        version = v?.let { SemanticVersion(it) }
                    )
                    commits.add(c)
                }
            }
        }

        return GitLog(commits)
    }

    /**
     * Creates a [Changelist] for from the [gitLog] log of commits in reversed chronological order.
     */
    fun createChangelist(gitLog: GitLog): Changelist {
        val map = gitLog.commits.foldRight(linkedMapOf<Version, List<ChangelogEntry>>()) { commit, acc ->
            val builder = ChangelogEntry.Builder()
            builder.withMessage(commit.message)
            commit.version?.let { v ->
                builder.withRelease(ChangelogEntry.Release(v, commit.date))
                acc[v] = mutableListOf(builder.build()).also { new ->
                    acc[Unreleased]?.let { existing -> new.addAll(existing) }
                    acc.remove(Unreleased)
                }
            } ?: run {
                acc.merge(Unreleased, listOf(builder.build())) { existing, new ->
                    new + existing
                }
            }

            acc
        }

        return Changelist(map)
    }
}
