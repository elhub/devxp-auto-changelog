package no.elhub.tools.autochangelog.project

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit

class GitRepo(private val git: Git) {
    fun log(start: ObjectId? = null, end: ObjectId? = null): Sequence<RevCommit> {
        val log = git.log()
        if (start != null) {
            if (end != null) log.addRange(start, end) else log.add(start)
        } else log.all()
        return log.call().asSequence()
    }

    fun findTagRef(versionTag: Version): Ref? = git.tagList().call().firstOrNull {
        it.name == "refs/tags/v$versionTag"
    }

    fun findCommitId(versionTag: Version): ObjectId? = findTagRef(versionTag)?.peeledObjectId

    fun findParent(objectId: ObjectId) = findCommit(objectId)?.getParent(0)

    fun findCommit(objectId: ObjectId): RevCommit? = log().firstOrNull {
        it == objectId
    }
}
