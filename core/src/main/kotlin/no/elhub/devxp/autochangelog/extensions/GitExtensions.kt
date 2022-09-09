package no.elhub.devxp.autochangelog.extensions

import org.eclipse.jgit.revwalk.RevCommit

/**
 * @property title the title of this git commit
 */
val RevCommit.title: String
    get() = this.shortMessage

/**
 * @property description the message for this git commit without the first line (title)
 *
 * The returned list only contains non-empty lines from the full commit message minus the title (first line of the commit)
 */
val RevCommit.description: List<String>
    get() = this.fullMessage.split("\n").tail().mapNotNull {
        it.trim().ifEmpty { null }
    }
