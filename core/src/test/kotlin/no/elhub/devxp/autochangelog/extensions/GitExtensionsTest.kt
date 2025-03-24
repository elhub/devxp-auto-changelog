package no.elhub.devxp.autochangelog.extensions

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.elhub.devxp.autochangelog.project.GitRepo
import no.elhub.devxp.autochangelog.project.TestRepository
import org.eclipse.jgit.lib.ObjectId

class GitExtensionsTest : FunSpec({
    val git = GitRepo(TestRepository.git)

    test("should return the title of the commit") {
        val commit = git.findCommit(ObjectId.fromString("f13fe0137c9c239cfc66da4fc1a238a2dabf586b"))
        commit?.title shouldBe "Bump devxp-build-config version for tc upgrade"
    }

    test("should return the detailed description of the commit") {
        val commit = git.findCommit(ObjectId.fromString("f13fe0137c9c239cfc66da4fc1a238a2dabf586b"))
        commit?.description shouldBe """
            ## 🔗 Issue ID(s): TDX-811
            ## 📋 Checklist
            * ✅ Lint checks passed on local machine.
            * ✅ Unit tests passed on local machine.
        """.trimIndent().split("\n").map { it.trim() }
    }

    test("should return an empty list for commits that only have titles") {
        val commit = git.findCommit(ObjectId.fromString("aa711042e58828bd4259eee9edde42d0d539272f"))
        commit?.description shouldBe emptyList()
    }
})
