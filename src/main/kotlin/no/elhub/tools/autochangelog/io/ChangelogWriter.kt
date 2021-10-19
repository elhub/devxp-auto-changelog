package no.elhub.tools.autochangelog.io

import no.elhub.tools.autochangelog.project.Version
import no.elhub.tools.autochangelog.project.defaultContent
import no.elhub.tools.autochangelog.project.lastDescriptionLine
import java.io.BufferedReader
import java.io.StringReader
import java.io.StringWriter
import java.io.Writer
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.bufferedReader

class ChangelogWriter {
    private val initialContent: () -> BufferedReader

    @OptIn(ExperimentalPathApi::class)
    constructor(changelogPath: Path) {
        initialContent = { changelogPath.bufferedReader() }
    }

    constructor(changelogContent: String = "") {
        initialContent = { BufferedReader(StringReader(changelogContent)) }
    }

    /**
     * Appends/prepends new changelog [content]s to a default/existing changelog and returns
     * the new changelog as String value.
     *
     * The following contract is used when writing the contents:
     * * `if version != null` - prepends the [content]s before the version header line
     * * `else` - appends the [content]s after the default description text
     *
     * @param content - new content to write
     * @param version - last released version in the static changelog file
     */
    fun writeToString(content: String, version: Version? = null): String = write(content, version).toString()

    private fun write(content: String, version: Version? = null): Writer {
        return initialContent().useLines {
            it.ifEmpty { defaultContent }.fold(StringWriter()) { acc, s ->
                when {
                    // prepend content before the last release
                    version != null && s.startsWith("## [$version]") -> {
                        acc.appendLine(content)
                        acc.appendLine()
                        acc.appendLine(s)
                    }
                    // append content after description text
                    version == null && s == lastDescriptionLine -> {
                        acc.appendLine(s)
                        acc.appendLine()
                        acc.appendLine(content)
                    }
                    else -> acc.appendLine(s)
                }
                acc
            }
        }
    }
}

