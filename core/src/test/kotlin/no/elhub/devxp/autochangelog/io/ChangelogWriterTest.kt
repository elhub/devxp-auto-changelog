package no.elhub.devxp.autochangelog.io

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import no.elhub.devxp.autochangelog.project.Changelist
import no.elhub.devxp.autochangelog.project.ChangelogEntry
import no.elhub.devxp.autochangelog.project.SemanticVersion
import no.elhub.devxp.autochangelog.project.defaultContent
import java.time.LocalDate

class ChangelogWriterTest : DescribeSpec({
    describe("ChangelogWriter") {

        context("write with non-empty original changelog contents") {
            val writer = ChangelogWriter(start = existingDescription, end = existingContent)

            it("should append new content after default description") {
                val s = writer.writeToString(changelist)
                s shouldBe """
                    |$existingDescription
                    |
                    |$expectedChangelogContent
                    |
                    |$existingContent
                """.trimMargin()
            }
        }

        context("write with empty original changelog contents") {
            val writer = ChangelogWriter()

            it("should append new content to default description") {
                val s = writer.writeToString(changelist)
                s shouldBe """
                    |${defaultContent.joinToString("\n")}
                    |
                    |$expectedChangelogContent
                """.trimMargin()
            }
        }

        context("write using existing changelog file") {
        }
    }
})

private val existingDescription = """
# Changelog

All notable changes to this project will be documented in this file.
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

private val changelist = Changelist(
    mapOf(
        SemanticVersion(42, 0, 0) to listOf(
            ChangelogEntry(
                release = ChangelogEntry.Release(
                    SemanticVersion(42, 0, 0),
                    LocalDate.parse("2063-04-05")
                ),
                added = listOf("First human warp flight"),
                changed = listOf("Human interstellar travel"),
                breakingChange = listOf("First contact with Vulcans")
            )
        )
    )
)

val expectedChangelogContent = """
    |## [42.0.0] - 2063-04-05
    |
    |### Added
    |
    |- First human warp flight
    |
    |### Breaking Change
    |
    |- First contact with Vulcans
    |
    |### Changed
    |
    |- Human interstellar travel
""".trimMargin()
