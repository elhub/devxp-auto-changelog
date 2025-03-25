package no.elhub.devxp.autochangelog.io

import no.elhub.devxp.autochangelog.config.Configuration
import no.elhub.devxp.autochangelog.extensions.linesAfter
import no.elhub.devxp.autochangelog.extensions.linesUntil
import no.elhub.devxp.autochangelog.project.Changelist
import no.elhub.devxp.autochangelog.project.GitRepo
import no.elhub.devxp.autochangelog.project.Unreleased
import no.elhub.devxp.autochangelog.project.defaultContent
import no.elhub.devxp.autochangelog.project.releaseHeaderRegex
import java.io.StringWriter
import java.io.Writer
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlinx.serialization.json.Json

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

    fun writeToJson(changelist: Changelist): String {
        val newChangeList = changelist.changes.values.flatten()
        val json = Json {
            prettyPrint = true
            allowStructuredMapKeys = true
        }
        return json.encodeToString(newChangeList)
    }

    private fun write(changelist: Changelist): Writer {
        return start()
            .ifEmpty { defaultContent }
            .plus("")
            .plus(changelist.toChangelogLines())
            .plus(end())
            .fold(StringWriter()) { acc, s ->
                acc.appendLine(s)
                acc
            }
    }

    fun generateCompareUrl(changelist: Changelist, repo: GitRepo): String {
        val versions = changelist.changes.entries.drop(changelist.changes.size - 2).map { it.key }
        val first = versions.first()
        val last = versions.last()

        val remoteUri = repo.git.remoteList().call()
            .firstOrNull { it.name == Configuration.GIT_REMOTE_NAME }
            ?.urIs
            ?.first()

        val s = remoteUri.toString()
            .replace(regex = Regex("""\.git$"""), "")
            .replaceBefore("github.com/", "")
            .replaceBefore("code.elhub.cloud/", "")

        val end = if (last == Unreleased) Configuration.GIT_DEFAULT_BRANCH_NAME else "v${last}"
        val compareString = if (s.startsWith("github.com")) {
            "https://$s/compare/v$first...$end"
        } else if (end == Configuration.GIT_DEFAULT_BRANCH_NAME) {
            "https://$s/compare/commits?targetBranch=refs%2Ftags%2Fv$first"
        } else {
            "https://$s/compare/commits?targetBranch=refs%2Ftags%2Fv$first&sourceBranch=refs%2Ftags%2F$end"
        }

        return compareString
    }

    private fun Changelist.toChangelogLines(): List<String> = this.changes.entries.reversed().map { (k, v) ->
        val additions = v.flatMap { it.added.map { s -> s } }
        val breakingChanges = v.flatMap { it.breakingChange.map { s -> s } }
        val changes = v.flatMap { it.changed.map { s -> s } }
        val fixes = v.flatMap { it.fixed.map { s -> s } }
        val unknown = v.flatMap { it.other.map { s -> s } }

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
            |
        """.trimMargin()
    }
}
