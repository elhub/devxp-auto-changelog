package no.elhub.devxp.autochangelog.project

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class VersionTest : FunSpec({

    context("A semantic version string") {

        test("0.1.0 should return major version 0, minor version 1, patch version 0 and prerelease null") {
            val sut = SemanticVersion("0.1.0")
            sut.major shouldBe 0
            sut.minor shouldBe 1
            sut.patch shouldBe 0
            sut.preRelease.shouldBeNull()
        }

        test("1.2.3 should return major version 1, minor version 2, patch version 3 and prerelease null") {
            val sut = SemanticVersion("1.2.3")
            sut.major shouldBe 1
            sut.minor shouldBe 2
            sut.patch shouldBe 3
            sut.preRelease.shouldBeNull()
        }

        test("1.2.3-RC.4 should return major version 1, minor version 2, patch version 3 and prerelease 4") {
            val sut = SemanticVersion("1.2.3-RC.4")
            sut.major shouldBe 1
            sut.minor shouldBe 2
            sut.patch shouldBe 3
            sut.preRelease shouldBe 4
        }

        test("1.2 should throw an illegal argument exception") {
            shouldThrow<IllegalArgumentException> {
                SemanticVersion("1.2")
            }
        }

        test("1.B.3 should throw an illegal argument exception") {
            shouldThrow<IllegalArgumentException> {
                SemanticVersion("1.B.3")
            }
        }
    }

    context("Two list of versions") {
        val v1 = SemanticVersion("0.1.0")
        val v2 = SemanticVersion("1.2.3")
        val v3 = SemanticVersion("1.2.4-RC.3")
        val v4 = SemanticVersion("1.2.4-RC.5")
        val v5 = SemanticVersion("1.2.4")
        val v6 = SemanticVersion("2.1.0")

        test("should sort into the correct semantic order") {
            listOf(v4, v2, v1, v5, v3, v6).sorted() shouldBe listOf(v1, v2, v3, v4, v5, v6)
        }
    }
})
