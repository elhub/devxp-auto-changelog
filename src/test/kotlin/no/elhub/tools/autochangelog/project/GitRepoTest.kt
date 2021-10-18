package no.elhub.tools.autochangelog.project

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith

class GitRepoTest : DescribeSpec({
    describe("TestRepository") {
        val repo = GitRepo(TestRepository.git)
        context("git log") {

            it("should return git ref for a given version") {
                repo.findTagRef(Version(1, 1, 0))?.name shouldBe "refs/tags/v1.1.0"
            }

            it("should find a commit for a given ref") {
                val c = repo.findCommitId(Version(1, 1, 0))?.let {
                    repo.findCommit(it)
                }
                c?.shortMessage shouldStartWith "Merge pull request #197"
            }

            it("should find a parent for a given ref") {
                val c = repo.findCommitId(Version(0, 0, 2))?.let {
                    repo.findParent(it)
                }
                c?.shortMessage shouldStartWith "Merge pull request #3"
            }

            it("should return a log of commit ranges") {
                val start = repo.findCommitId(Version(0, 0, 2))?.let {
                    repo.findParent(it)
                }?.toObjectId()
                val end = repo.findCommitId(Version(0, 0, 3))?.let {
                    repo.findCommit(it)
                }?.toObjectId()

                repo.log(start, end).toList() shouldHaveSize 3
            }

            it("should return entire log") {
                repo.log().toList() shouldHaveSize 695
            }
        }
    }
})
