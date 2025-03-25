package no.elhub.devxp.autochangelog.project

import java.time.ZoneId
import no.elhub.devxp.autochangelog.extensions.description
import no.elhub.devxp.autochangelog.extensions.title
import no.elhub.devxp.autochangelog.git.GitCommit
import no.elhub.devxp.autochangelog.git.GitLog
import no.elhub.devxp.autochangelog.git.GitMessage
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit

/**
 * Represents a generic git repository.
 *
 * Serves mainly as a wrapper around [git] functionality.
 */
class GitRepo(val git: Git) {

    val tags: () -> List<Ref> = { git.tagList().call() }

    /**
     * Returns the log from this [git] object as sequence of commits.
     */
    fun log(): Sequence<RevCommit> = git.log().call().asSequence()

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
     */
    fun constructLog(
        predicate: (RevCommit) -> Boolean = { true }
    ): GitLog {
        val versionedCommits: List<Pair<String, ObjectId>> = git.tagList().call()
            .map {
                val name = it.name.replace("refs/tags/v", "")
                git.repository.refDatabase.peel(it).peeledObjectId?.let { id -> name to id }
                    ?: (name to it.objectId)
            }
            .filter { (v, _) -> v.matches(versionPattern.toRegex()) }

        val commits = log().fold(mutableListOf<GitCommit>()) { acc, commit ->
            if (predicate(commit)) {
                val v = versionedCommits.firstOrNull { it.second == commit.id }?.first
                val c = GitCommit(
                    message = GitMessage(
                        title = commit.title,
                        description = commit.description
                    ),
                    objectId = commit.id,
                    date = commit.authorIdent.`when`.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    version = v?.let { SemanticVersion(it) }
                )
                acc.add(c)
            }

            acc
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
