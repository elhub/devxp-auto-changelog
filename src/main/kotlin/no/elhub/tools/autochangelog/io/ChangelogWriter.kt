package no.elhub.tools.autochangelog.io

import no.elhub.tools.autochangelog.extensions.linesAfter
import no.elhub.tools.autochangelog.extensions.linesUntil
import no.elhub.tools.autochangelog.project.Changelist
import no.elhub.tools.autochangelog.project.defaultContent
import no.elhub.tools.autochangelog.project.releaseHeaderRegex
import java.io.StringWriter
import java.io.Writer
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi

class ChangelogWriter {
    private val start: () -> Sequence<String>
    private val end: () -> Sequence<String>

    @OptIn(ExperimentalPathApi::class)
    constructor(changelogPath: Path) {
        start = {
            changelogPath.toFile()
                .linesUntil { it.matches(releaseHeaderRegex.toRegex()) }
                .asSequence()
        }
        end = {
            changelogPath.toFile().linesAfter { it.matches(releaseHeaderRegex.toRegex()) }
        }
    }

    constructor(start: String = "", end: String = "") {
        this.start = { if (start != "") start.lineSequence() else emptySequence() }
        this.end = { if (end != "") end.lineSequence() else emptySequence() }
    }

    /**
     * Writes the changelog with this [changelist]
     * and returns the new changelog as String value.
     */
    fun writeToString(changelist: Changelist): String = write(changelist).toString().trim()

    private fun write(changelist: Changelist): Writer {
        return start()
            .ifEmpty { defaultContent }
            .plus("")
            .plus(changelist.toChangelogLines())
            .plus("")
            .plus(end())
            .fold(StringWriter()) { acc, s ->
                acc.appendLine(s)
                acc
            }
    }

    private fun Changelist.toChangelogLines(): List<String> = this.entries.map { (k, v) ->
        val additions = v.flatMap { it.added.map { s -> s } }
        val breakingChanges = v.flatMap { it.breakingChange.map { s -> s } }
        val changes = v.flatMap { it.changed.map { s -> s } }
        val fixes = v.flatMap { it.fixed.map { s -> s } }
        val unknown = v.flatMap { it.unknown.map { s -> s } }

        val sb = StringBuilder()

        if (additions.isNotEmpty()) {
            sb.appendLine("### Added\n")
            sb.appendLine(additions.joinToString("\n") { "- $it" })
            sb.appendLine()
        }
        if (breakingChanges.isNotEmpty()) {
            sb.appendLine("### Breaking Change\n")
            sb.appendLine(breakingChanges.joinToString("\n") { "- $it" })
            sb.appendLine()
        }
        if (changes.isNotEmpty()) {
            sb.appendLine("### Changed\n")
            sb.appendLine(changes.joinToString("\n") { "- $it" })
            sb.appendLine()
        }
        if (fixes.isNotEmpty()) {
            sb.appendLine("### Fixed\n")
            sb.appendLine(fixes.joinToString("\n") { "- $it" })
            sb.appendLine()
        }
        if (unknown.isNotEmpty()) {
            sb.appendLine("### Unknown\n")
            sb.appendLine(unknown.joinToString("\n") { "- $it" })
            sb.appendLine()
        }

        val releaseHeader = v.first().release?.date?.let { "## [$k] - $it" } ?: "## [$k]"

        """
            |$releaseHeader
            |
            |${sb.trim()}
        """.trimMargin()
    }
}
