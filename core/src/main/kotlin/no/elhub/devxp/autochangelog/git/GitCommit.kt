package no.elhub.devxp.autochangelog.git

import no.elhub.devxp.autochangelog.project.SemanticVersion
import org.eclipse.jgit.lib.ObjectId
import java.time.LocalDate

/**
 * Represents a git commit with a [message],
 * and a possible [version] tag associated with this commit.
 */
data class GitCommit(
    val message: GitMessage,
    val objectId: ObjectId,
    val date: LocalDate,
    val version: SemanticVersion? = null
)
