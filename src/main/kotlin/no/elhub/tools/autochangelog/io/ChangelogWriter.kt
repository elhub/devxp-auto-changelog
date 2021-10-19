package no.elhub.tools.autochangelog.io

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

    fun writeToString(content: String): String = write(content).toString()

    private fun write(content: String): Writer {
        return initialContent().useLines {
            it.ifEmpty { defaultContent }.fold(StringWriter()) { acc, s ->
                if (s == lastDescriptionLine) {
                    acc.append(s)
                    acc.appendLine()
                    acc.appendLine()
                    acc.append(content)
                    acc.appendLine()
                } else {
                    acc.append(s)
                    acc.appendLine()
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

private const val lastDescriptionLine = "and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html)."
