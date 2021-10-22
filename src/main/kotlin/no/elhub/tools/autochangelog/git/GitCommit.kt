package no.elhub.tools.autochangelog.git

import no.elhub.tools.autochangelog.project.SemanticVersion
import org.eclipse.jgit.lib.ObjectId

/**
 * Represents a git commit with a [message],
 * and a possible [version] tag associated with this commit.
 */
data class GitCommit(
    val message: GitMessage,
    val objectId: ObjectId,
    val version: SemanticVersion? = null
)
