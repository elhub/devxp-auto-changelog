package no.elhub.tools.autochangelog.project

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ChangelogReaderTest : DescribeSpec({
    val changelog = ChangelogReader(TestRepository.changelogPath).read()

    it("should return latest released version from the changelog file") {
        val version: Version = changelog.lastRelease
        assertSoftly {
            version.major shouldBe 1
            version.minor shouldBe 1
            version.patch shouldBe 0
        }
    }
})
