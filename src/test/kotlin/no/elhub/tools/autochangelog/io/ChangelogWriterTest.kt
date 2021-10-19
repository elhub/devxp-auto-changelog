package no.elhub.tools.autochangelog.io

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ChangelogWriterTest : DescribeSpec({
    describe("ChangelogWriter") {
        context("write with non-empty original changelog contents") {
            val content = """
                |$defaultDescription${'\n'}
                |$existingContent${'\n'}
            """.trimMargin()
            val writer = ChangelogWriter(content)

            it("should append new content after default description") {
                val s = writer.writeToString(nextReleaseContent)
                s shouldBe """
                    |$defaultDescription${'\n'}
                    |$nextReleaseContent${'\n'}
                    |$existingContent${'\n'}
                """.trimMargin()
            }
        }
        context("write with empty original changelog contents") {
            val writer = ChangelogWriter()

            it("should append new content with default description") {
                val s = writer.writeToString(nextReleaseContent)
                s shouldBe """
                    |$defaultDescription${'\n'}
                    |$nextReleaseContent${'\n'}
                """.trimMargin()
            }
        }
    }
})

private val defaultDescription = """
# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
""".trimIndent()

private val existingContent = """
## [1.1.0] - 2019-02-15

### Added

- Danish translation from [@frederikspang](https://github.com/frederikspang).
- Georgian translation from [@tatocaster](https://github.com/tatocaster).
- Changelog inconsistency section in Bad Practices

### Changed

- Fixed typos in Italian translation from [@lorenzo-arena](https://github.com/lorenzo-arena).
- Fixed typos in Indonesian translation from [@ekojs](https://github.com/ekojs).
""".trimIndent()

private val nextReleaseContent = """
## [42.0.0] - 2063-04-05

### Added

- First human warp flight
""".trimIndent()
