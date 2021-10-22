package no.elhub.tools.autochangelog.project

import java.time.LocalDate

data class ChangelogEntry(
    val release: Release?,
    val added: List<String>,
    val changed: List<String>,
    val fixed: List<String>,
    val breakingChange: List<String>,
    val unknown: List<String>
) {
    data class Release(
        val version: Version,
        val date: LocalDate
    )

    class Builder {
        private var release: Release? = null
        val added: MutableList<String> = mutableListOf()
        val changed: MutableList<String> = mutableListOf()
        val fixed: MutableList<String> = mutableListOf()
        val breakingChange: MutableList<String> = mutableListOf()
        val unknown: MutableList<String> = mutableListOf()

        fun withRelease(release: Release): Builder {
            return apply { this.release = release }
        }

        fun build() = ChangelogEntry(
            release = release,
            added = added,
            changed = changed,
            fixed = fixed,
            breakingChange = breakingChange,
            unknown = unknown
        )
    }
}
