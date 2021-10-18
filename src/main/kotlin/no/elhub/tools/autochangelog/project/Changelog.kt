package no.elhub.tools.autochangelog.project

data class Changelog(
    val lastRelease: Version
) {

    class Builder internal constructor() {
        private var lastRelease: Version = Version(0, 0, 0)

        fun withLastRelease(lastRelease: Version): Builder {
            this.lastRelease = lastRelease
            return this
        }

        fun build(): Changelog = Changelog(
            lastRelease = this.lastRelease
        )

    }

}
