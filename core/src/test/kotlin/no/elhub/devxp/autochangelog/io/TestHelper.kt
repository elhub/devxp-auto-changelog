package no.elhub.devxp.autochangelog.io

import java.time.LocalDate
import no.elhub.devxp.autochangelog.project.Changelist
import no.elhub.devxp.autochangelog.project.ChangelogEntry
import no.elhub.devxp.autochangelog.project.SemanticVersion

val singleChangelist = Changelist(
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

val defaultDescription = """
        # Changelog

        All notable changes to this project will be documented in this file.
    """.trimIndent()

val existingContent = """
        ## [1.1.0] - 2019-02-15

        ### Added

        - Danish translation from [@frederikspang](https://github.com/frederikspang).
        - Georgian translation from [@tatocaster](https://github.com/tatocaster).
        - Changelog inconsistency section in Bad Practices

        ### Changed

        - Fixed typos in Italian translation from [@lorenzo-arena](https://github.com/lorenzo-arena).
        - Fixed typos in Indonesian translation from [@ekojs](https://github.com/ekojs).
    """.trimIndent()

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

val singleExpectedJson =  """
    [
        {
            "release": {
                "version": {
                    "type": "no.elhub.devxp.autochangelog.project.SemanticVersion",
                    "major": 42,
                    "minor": 0,
                    "patch": 0,
                    "preReleaseId": null,
                    "preRelease": null
                },
                "date": "2063-04-05"
            },
            "added": [
                "First human warp flight"
            ],
            "changed": [
                "Human interstellar travel"
            ],
            "breakingChange": [
                "First contact with Vulcans"
            ]
        }
    ]
""".trimIndent()

val singleChangeLogTest = singleChangelist to singleExpectedJson

val doubleChangeList = Changelist(
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
        ),
        SemanticVersion(43, 0, 0) to listOf(
            ChangelogEntry(
                release = ChangelogEntry.Release(
                    SemanticVersion(43, 0, 0),
                    LocalDate.parse("2064-04-05")
                ),
                added = listOf("Second human warp flight"),
                changed = listOf("Dog interstellar travel"),
                breakingChange = listOf("First contact with Klingons")
            )
        ),
    )
)

val doubleExpectedJson =  """
    [
        {
            "release": {
                "version": {
                    "type": "no.elhub.devxp.autochangelog.project.SemanticVersion",
                    "major": 42,
                    "minor": 0,
                    "patch": 0,
                    "preReleaseId": null,
                    "preRelease": null
                },
                "date": "2063-04-05"
            },
            "added": [
                "First human warp flight"
            ],
            "changed": [
                "Human interstellar travel"
            ],
            "breakingChange": [
                "First contact with Vulcans"
            ]
        },
        {
            "release": {
                "version": {
                    "type": "no.elhub.devxp.autochangelog.project.SemanticVersion",
                    "major": 43,
                    "minor": 0,
                    "patch": 0,
                    "preReleaseId": null,
                    "preRelease": null
                },
                "date": "2064-04-05"
            },
            "added": [
                "Second human warp flight"
            ],
            "changed": [
                "Dog interstellar travel"
            ],
            "breakingChange": [
                "First contact with Klingons"
            ]
        }
    ]
""".trimIndent()

val doubleChangeLogTest = doubleChangeList to doubleExpectedJson
