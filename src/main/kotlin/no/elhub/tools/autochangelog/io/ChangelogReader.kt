package no.elhub.tools.autochangelog.io

import no.elhub.tools.autochangelog.project.Changelog
import no.elhub.tools.autochangelog.project.Version
import no.elhub.tools.autochangelog.project.versionPattern
import java.io.BufferedReader
import java.io.Reader
import java.nio.file.Path
import java.util.regex.Pattern
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.bufferedReader

class ChangelogReader {
    private val releaseHeaderRegex = Pattern.compile("""^## \[($versionPattern)] - \d{4}-\d{2}-\d{2}""")
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
        val builder = Changelog.Builder()
        changelogContent.useLines {
            it.forEach { s ->
                val matcher = releaseHeaderRegex.matcher(s)
                if (matcher.matches()) {
                    builder.withLastRelease(Version(matcher.group(1).toString()))
                    return@useLines
                }
            }
        }
        return builder.build()
    }
}
