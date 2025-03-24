package no.elhub.devxp.autochangelog.io

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.sequences.shouldContainExactly
import io.kotest.matchers.shouldBe
import no.elhub.devxp.autochangelog.project.SemanticVersion
import no.elhub.devxp.autochangelog.project.TestRepository
import java.io.StringReader
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ChangelogReaderTest : FunSpec({
    val changelogContent = """
        # Changelog

        All notable changes to this project will be documented in this file.

        The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
        and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

        ## [Unreleased]

        ### Added

        - Added Dutch translation

        ### Fixed

        - Fixed foldouts in Dutch translation
    """.trimIndent()

    context("Changelog instance") {
        val changelog = ChangelogReader(TestRepository.changelogPath).read()

        test("should return latest released version from the changelog file") {
            val version: SemanticVersion? = changelog.lastRelease
            assertSoftly {
                version?.major shouldBe 1
                version?.minor shouldBe 1
                version?.patch shouldBe 0
            }
        }

        test("should return null version if changelog file does not have releases") {
            val reader = ChangelogReader(StringReader(changelogContent))
            reader.read().lastRelease shouldBe null
        }

        test("should return null version for an empty changelog file") {
            val reader = ChangelogReader(StringReader(""))
            reader.read().lastRelease shouldBe null
        }

        test("should return contents of a changelog") {
            val cl = """
                # Changelog

                All notable changes to this project will be documented in this file.

                The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
                and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

                This text is not ignored because the changelog does not contain a "release header".
            """.trimIndent()
            val lines = ChangelogReader(StringReader(cl)).read().lines
            lines shouldContainExactly cl.splitToSequence("\n")
        }

        test("should trim contents of a changelog after the last release header") {
            val cl = """
                # Changelog

                All notable changes to this project will be documented in this file.

                The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
                and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

                ## [1.1.0] - 2163-13-32

                This text should be ignored.
                And this one as well.
                In fact, even the "release header" should not be included.
            """.trimIndent()
            val lines = ChangelogReader(StringReader(cl)).read().lines
            lines shouldContainExactly cl.splitToSequence("\n").take(7)
        }

        test("should return empty lines sequence for empty changelog contents") {
            val lines = ChangelogReader(StringReader("")).read().lines
            lines shouldContainExactly emptySequence()
        }
    }

    context("Last release in existing changelog") {
        test("should return last release version") {
            val cl = """
                # Changelog

                ## [1.1.0] - 2163-13-32
            """.trimIndent()
            val reader = ChangelogReader(StringReader(cl))
            reader.getLastRelease() shouldBe SemanticVersion(1, 1, 0)
        }

        test("should return null if version is not found") {
            val cl = """
                # Changelog
            """.trimIndent()

            val reader = ChangelogReader(StringReader(cl))
            reader.getLastRelease() shouldBe null
        }
    }
})
