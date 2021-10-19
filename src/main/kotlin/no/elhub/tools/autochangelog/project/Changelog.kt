package no.elhub.tools.autochangelog.project

import kotlin.experimental.ExperimentalTypeInference

/**
 * A class representation of a changelog.
 *
 * @property lastRelease the last released [Version] in the changelog
 * @property lines a [Sequence] of lines in the changelog
 */
data class Changelog(
    val lastRelease: Version?,
    val lines: Sequence<String>
) {

    /**
     * Builder implementation for this [Changelog] class
     */
    class Builder internal constructor() {
        internal var lastRelease: Version? = null
            private set
        private var lines: Sequence<String> = emptySequence()

        /**
         * Sets this [Builder.lastRelease] to [lastRelease]
         * and returns an instance of this [Builder]
         */
        fun withLastRelease(lastRelease: Version): Builder {
            return this.also { it.lastRelease = lastRelease }
        }

        /**
         * Builds the sequence lazily from the [block] function, yielding results one by one,
         * sets the resulting sequence to this [Builder.lines],
         * and returns an instance of this [Builder]
         */
        @OptIn(ExperimentalTypeInference::class)
        fun withLines(block: suspend SequenceScope<String>.() -> Unit): Builder {
            return this.also { it.lines = Sequence { iterator(block) } }
        }

        /**
         * Builds the [Changelog] from this [Builder]
         */
        fun build(): Changelog = Changelog(
            lastRelease = this.lastRelease,
            lines = lines
        )

    }

}
