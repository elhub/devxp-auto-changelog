package no.elhub.devxp.autochangelog.project

/**
 * Holds releases (released [Version]s) and [ChangelogEntry]s associated with those releases.
 *
 * @property changes changelist entries represented as a map of [Version]s to a list of [ChangelogEntry]s
 */
data class Changelist(
    val changes: Map<Version, List<ChangelogEntry>>
)
