package no.elhub.tools.autochangelog.io

import no.elhub.tools.autochangelog.project.Version
import java.time.LocalDate

data class ChangelogEntry private constructor(
    val release: Pair<Version, LocalDate>,
    val added: List<String>,
    val changed: List<String>,
    val fixed: List<String>
) {

    class Builder {
        lateinit var release: Pair<Version, LocalDate>
        lateinit var added: List<String>
        lateinit var changed: List<String>
        lateinit var fixed: List<String>

        companion object {
            fun create(block: Builder.() -> Unit): ChangelogEntry = Builder().apply(block).build()
        }

        private fun build() = ChangelogEntry(
            release = release,
            added = added,
            changed = changed,
            fixed = fixed
        )
    }
}
