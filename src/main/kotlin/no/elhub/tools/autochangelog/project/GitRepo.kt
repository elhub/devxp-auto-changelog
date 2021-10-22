package no.elhub.tools.autochangelog.project

import no.elhub.tools.autochangelog.extensions.description
import no.elhub.tools.autochangelog.extensions.title
import no.elhub.tools.autochangelog.git.GitCommit
import no.elhub.tools.autochangelog.git.GitLog
import no.elhub.tools.autochangelog.git.GitMessage
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit

/**
 * Represents a generic git repository.
 *
 * Serves mainly as a wrapper around [git] functionality.
 */
class GitRepo(private val git: Git) {

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
        } else if (end != null) log.not(end)
        return log.call().asSequence()
    }

    /**
     * Returns a [Ref] from this [versionTag]
     * or null if the ref is not found in tha tag list of this [git] object.
     */
    fun findTagRef(versionTag: Version): Ref? = git.tagList().call().firstOrNull {
        it.name == "refs/tags/v$versionTag"
    }

    /**
     * Returns the git tag id for the [versionTag] value.
     */
    fun findCommitId(versionTag: Version): ObjectId? = findTagRef(versionTag)?.let {
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
    fun constructLog(start: ObjectId? = null, end: ObjectId? = null, predicate: (RevCommit) -> Boolean = { true }): GitLog {
        val versionedCommits: List<Pair<String, ObjectId>> = git.tagList().call().map {
            val name = it.name.replace("refs/tags/v", "")
            git.repository.refDatabase.peel(it).peeledObjectId?.let { id -> name to id }
                ?: (name to it.objectId)
        }

        val commits = log(start = start, end = end).fold(mutableListOf<GitCommit>()) { acc, commit ->
            if (predicate(commit)) {
                val v = versionedCommits.firstOrNull { it.second == commit.id }?.first
                val c = GitCommit(
                    message = GitMessage(
                        title = commit.title,
                        description = commit.description
                    ),
                    objectId = commit.id,
                    version = v?.let { Version(it) }
                )
                acc.add(c)
            }

            acc
        }

        return GitLog(commits)
    }
}
