package no.elhub.tools.autochangelog.project

/**
 * Holds releases (released [Version]s) and [ChangelogEntry]s associated with those releases.
 *
 * @property entries changelist entries represented as a map of [Version]s to a list of [ChangelogEntry]s
 */
data class Changelist(
    val entries: Map<Version, List<ChangelogEntry>>
)
