package no.elhub.devxp.autochangelog.project

import kotlinx.serialization.Serializable
import no.elhub.devxp.autochangelog.git.GitMessage
import no.elhub.devxp.autochangelog.git.TitleKeyword
import no.elhub.devxp.autochangelog.git.titleKeyword
import no.elhub.devxp.autochangelog.serializers.LocalDateSerializer
import java.time.LocalDate

@Serializable
data class ChangelogEntry(
    val release: Release?,
    val added: List<String> = emptyList(),
    val changed: List<String> = emptyList(),
    val fixed: List<String> = emptyList(),
    val breakingChange: List<String> = emptyList(),
    val other: List<String> = emptyList()
) {
    @Serializable
    data class Release(
        val version: Version,
        @Serializable(with = LocalDateSerializer::class)
        val date: LocalDate
    )

    class Builder {
        private var release: Release? = null
        val added: MutableList<String> = mutableListOf()
        val changed: MutableList<String> = mutableListOf()
        val fixed: MutableList<String> = mutableListOf()
        val breakingChange: MutableList<String> = mutableListOf()
        val other: MutableList<String> = mutableListOf()

        fun withRelease(release: Release): Builder = apply { this.release = release }

        fun withMessage(gitMessage: GitMessage): Builder {
            val msg = gitMessage.title

            when (gitMessage.titleKeyword) {
                TitleKeyword.ADD -> this.added.add(msg)
                TitleKeyword.BREAKING_CHANGE -> this.breakingChange.add(msg)
                TitleKeyword.CHANGE -> this.changed.add(msg)
                TitleKeyword.FIX -> this.fixed.add(msg)
                TitleKeyword.RELEASE -> { /* do not include release commits in the changelog */ }
                TitleKeyword.OTHER -> this.other.add(msg)
            }

            return this
        }

        fun build() = ChangelogEntry(
            release = release,
            added = added,
            changed = changed,
            fixed = fixed,
            breakingChange = breakingChange,
            other = other
        )
    }
}
