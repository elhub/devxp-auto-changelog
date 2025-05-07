package no.elhub.devxp.autochangelog.io

import no.elhub.devxp.autochangelog.project.Changelist
import no.elhub.devxp.autochangelog.project.ChangelogEntry
import no.elhub.devxp.autochangelog.project.SemanticVersion
import java.time.LocalDate

val singleChangelist = Changelist(
    mapOf(
        SemanticVersion(42, 0, 0) to listOf(
            ChangelogEntry(
                release = ChangelogEntry.Release(
                    SemanticVersion(42, 0, 0),
                    LocalDate.parse("2063-04-05")
                ),
                added = listOf("Option to delete system32"),
                changed = listOf("All the functionality"),
                breakingChange = listOf("Broke all APIs")
            )
        )
    )
)

val singleExpectedMd = """
    |## [42.0.0] - 2063-04-05
    |
    |### Added
    |
    |- Option to delete system32
    |
    |### Breaking Change
    |
    |- Broke all APIs
    |
    |### Changed
    |
    |- All the functionality
""".trimMargin()

val singleExpectedJson = """
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
                "Option to delete system32"
            ],
            "changed": [
                "All the functionality"
            ],
            "breakingChange": [
                "Broke all APIs"
            ]
        }
    ]

""".trimIndent()

val doubleChangeList = Changelist(
    mapOf(
        SemanticVersion(42, 0, 0) to listOf(
            ChangelogEntry(
                release = ChangelogEntry.Release(
                    SemanticVersion(42, 0, 0),
                    LocalDate.parse("2063-04-05")
                ),
                added = listOf("Option to delete system32"),
                changed = listOf("All the functionality"),
                breakingChange = listOf("Broke all APIs")
            )
        ),
        SemanticVersion(43, 0, 0) to listOf(
            ChangelogEntry(
                release = ChangelogEntry.Release(
                    SemanticVersion(43, 0, 0),
                    LocalDate.parse("2064-04-05")
                ),
                added = listOf("More and better vulnerabilities"),
                changed = listOf("Plaintext all passwords"),
                breakingChange = listOf("Removed everyone's favorite function")
            )
        ),
    )
)

val doubleExpectedMd = """
    |## [43.0.0] - 2064-04-05
    |
    |### Added
    |
    |- More and better vulnerabilities
    |
    |### Breaking Change
    |
    |- Removed everyone's favorite function
    |
    |### Changed
    |
    |- Plaintext all passwords
    |
    |## [42.0.0] - 2063-04-05
    |
    |### Added
    |
    |- Option to delete system32
    |
    |### Breaking Change
    |
    |- Broke all APIs
    |
    |### Changed
    |
    |- All the functionality
""".trimMargin()

val doubleExpectedJson = """
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
                "Option to delete system32"
            ],
            "changed": [
                "All the functionality"
            ],
            "breakingChange": [
                "Broke all APIs"
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
                "More and better vulnerabilities"
            ],
            "changed": [
                "Plaintext all passwords"
            ],
            "breakingChange": [
                "Removed everyone's favorite function"
            ]
        }
    ]

""".trimIndent()

val singleChangelogTestMd = TestCase(
    name = "Single changelog produces expected markdown",
    changelist = singleChangelist,
    expected = singleExpectedMd
)
val doubleChangeLogTestMd = TestCase(
    name = "Double changelog produces expected markdown",
    changelist = doubleChangeList,
    expected = doubleExpectedMd
)
val singleChangelogTestJson = TestCase(
    name = "Single changelog produces expected json",
    changelist = singleChangelist,
    expected = singleExpectedJson
)
val doubleChangeLogTestJson = TestCase(
    name = "Double changelog produces expected json",
    changelist = doubleChangeList,
    expected = doubleExpectedJson
)

data class TestCase(
    val name: String,
    val changelist: Changelist,
    val expected: String,
)
