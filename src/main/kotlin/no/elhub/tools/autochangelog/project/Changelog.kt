package no.elhub.tools.autochangelog.project

data class Changelog(
    val lastRelease: Version?
) {

    class Builder internal constructor() {
        private var lastRelease: Version? = null

        fun withLastRelease(lastRelease: Version): Builder {
            return this.also { it.lastRelease = lastRelease }
        }

        fun build(): Changelog = Changelog(
            lastRelease = this.lastRelease
        )

    }

}
