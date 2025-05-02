package no.elhub.devxp.autochangelog.project

import java.util.regex.Pattern
import kotlin.experimental.ExperimentalTypeInference

/**
 * A class representation of a changelog.
 *
 * @property lastRelease the last released [SemanticVersion] in the changelog
 * @property lines a [Sequence] of lines in the changelog
 */
data class Changelog(
    val lastRelease: SemanticVersion?,
    val lines: Sequence<String>
) {

    /**
     * Builder implementation for this [Changelog] class
     */
    class Builder internal constructor() {
        internal var lastRelease: SemanticVersion? = null
            private set
        private var lines: Sequence<String> = emptySequence()

        /**
         * Sets this [Builder.lastRelease] to [lastRelease]
         * and returns an instance of this [Builder]
         */
        fun withLastRelease(lastRelease: SemanticVersion): Builder = this.also { it.lastRelease = lastRelease }

        /**
         * Builds the sequence lazily from the [block] function, yielding results one by one,
         * sets the resulting sequence to this [Builder.lines],
         * and returns an instance of this [Builder]
         */
        @OptIn(ExperimentalTypeInference::class)
        fun withLines(block: suspend SequenceScope<String>.() -> Unit): Builder = this.also { it.lines = Sequence { iterator(block) } }

        /**
         * Builds the [Changelog] from this [Builder]
         */
        fun build(): Changelog = Changelog(
            lastRelease = this.lastRelease,
            lines = lines
        )
    }
}

val releaseHeaderRegex: Pattern = Pattern.compile(
    """^## \[(${versionPattern.pattern().drop(1)})] - \d{4}-\d{2}-\d{2}"""
)

val defaultContent = sequence {
    val lines = listOf(
        "# Changelog",
        "",
        "All notable changes to this project will be documented in this file.",
        "",
        "The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),",
        LAST_DESCRIPTION_LINE
    )
    yieldAll(lines)
}

const val LAST_DESCRIPTION_LINE =
    "and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html)."
