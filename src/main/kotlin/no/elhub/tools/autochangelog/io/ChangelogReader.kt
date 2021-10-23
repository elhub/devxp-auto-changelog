package no.elhub.tools.autochangelog.io

import no.elhub.tools.autochangelog.project.Changelog
import no.elhub.tools.autochangelog.project.SemanticVersion
import no.elhub.tools.autochangelog.project.releaseHeaderRegex
import java.io.BufferedReader
import java.io.Reader
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.bufferedReader

class ChangelogReader {
    private val changelogContent: BufferedReader

    /**
     * @constructor creates an instance of this [ChangelogReader]
     *
     * @param changelogPath path to the changelog file
     */
    @OptIn(ExperimentalPathApi::class)
    constructor(changelogPath: Path) {
        changelogContent = changelogPath.bufferedReader()
    }

    /**
     * @constructor creates an instance of this [ChangelogReader] with the [reader]
     *
     * @param reader a [Reader] with the changelog contents character stream
     */
    constructor(reader: Reader) {
        this.changelogContent = reader.buffered()
    }

    fun read(): Changelog {
        return with(Changelog.Builder()) {
            val lines = changelogContent.useLines {
                it.takeWhile { s ->
                    val matcher = releaseHeaderRegex.matcher(s)
                    if (matcher.matches() && lastRelease == null) {
                        withLastRelease(SemanticVersion(matcher.group(1).toString()))
                        false
                    } else true
                }.toList()
            }
            withLines { yieldAll(lines) }
            build()
        }
    }
}
