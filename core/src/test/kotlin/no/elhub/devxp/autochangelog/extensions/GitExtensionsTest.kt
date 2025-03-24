package no.elhub.devxp.autochangelog.extensions

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.elhub.devxp.autochangelog.project.GitRepo
import no.elhub.devxp.autochangelog.project.TestRepository
import org.eclipse.jgit.lib.ObjectId

class GitExtensionsTest : FunSpec({
    val git = GitRepo(TestRepository.git)

    test("should return the title of the commit") {
        val commit = git.findCommit(ObjectId.fromString("a40cacb9c7d2f8996789498494583e78d611b174"))
        commit?.title shouldBe "Bump middleman from 4.3.11 to 4.4.0 (#401)"
    }

    test("should return the detailed description of the commit") {
        val commit = git.findCommit(ObjectId.fromString("a40cacb9c7d2f8996789498494583e78d611b174"))
        commit?.description shouldBe """
            Bumps [middleman](https://github.com/middleman/middleman) from 4.3.11 to 4.4.0.
            - [Release notes](https://github.com/middleman/middleman/releases)
            - [Changelog](https://github.com/middleman/middleman/blob/v4.4.0/CHANGELOG.md)
            - [Commits](https://github.com/middleman/middleman/compare/v4.3.11...v4.4.0)
            ---
            updated-dependencies:
            - dependency-name: middleman
                dependency-type: direct:production
                update-type: version-update:semver-minor
            ...
            Signed-off-by: dependabot[bot] <support@github.com>
            Co-authored-by: dependabot[bot] <49699333+dependabot[bot]@users.noreply.github.com>
        """.trimIndent().split("\n").map { it.trim() }
    }

    test("should return an empty list for commits that only have titles") {
        val commit = git.findCommit(ObjectId.fromString("2ea214c6f40eb002b086475910a0948fbf2e5dac"))
        commit?.description shouldBe emptyList()
    }
})
