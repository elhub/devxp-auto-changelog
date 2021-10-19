package no.elhub.tools.autochangelog.io

import no.elhub.tools.autochangelog.project.Version
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

private val defaultContent = sequence {
    val lines = listOf(
        "# Changelog",
        "",
        "All notable changes to this project will be documented in this file.",
        "",
        "The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),",
        lastDescriptionLine
    )
    yieldAll(lines)
}

private const val lastDescriptionLine =
    "and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html)."
