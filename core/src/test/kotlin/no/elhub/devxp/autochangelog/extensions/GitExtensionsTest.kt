package no.elhub.devxp.autochangelog.extensions

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import no.elhub.devxp.autochangelog.project.GitRepo
import no.elhub.devxp.autochangelog.project.TestRepository
import org.eclipse.jgit.lib.ObjectId

class GitExtensionsTest : DescribeSpec({
    val git = GitRepo(TestRepository.git)

    it("should return the title of the commit") {
        val commit = git.findCommit(ObjectId.fromString("18438f2d14e84046e7e3c375ad23b4ed3a97bd0d"))
        commit?.title shouldBe "Add Orchid support to elhub-gradle"
    }

    it("should return the detailed description of the commit") {
        val commit = git.findCommit(ObjectId.fromString("18438f2d14e84046e7e3c375ad23b4ed3a97bd0d"))
        commit?.description shouldBe """
            Summary: - [patch] update
            Test Plan: ./gradlew test
            Reviewers: O5 elhub/devtools, sergei.prodanov
            Reviewed By: O5 elhub/devtools, sergei.prodanov
            Subscribers: sergei.prodanov
            Differential Revision: https://phabricator.elhub.cloud/D2181
        """.trimIndent().split("\n").map { it.trim() }
    }

    it("should return an empty list for commits that only have titles") {
        val commit = git.findCommit(ObjectId.fromString("e7ea01c9f8cec694d27ce11d2c81d4cadf9a3093"))
        commit?.description shouldBe emptyList()
    }
})
