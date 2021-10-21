package no.elhub.tools.autochangelog.project

import io.github.serpro69.kfaker.Faker
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import no.elhub.tools.autochangelog.extensions.delete
import no.elhub.tools.autochangelog.git.GitCommit
import no.elhub.tools.autochangelog.git.GitMessage
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import java.nio.file.Files
import java.nio.file.Path

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
                val c = repo.findCommitId(Version(1, 1, 0))?.let {
                    repo.findParent(it)
                }
                c?.shortMessage shouldBe "Bump Ruby version to 2.4"
            }

            it("should return a log of commit ranges") {
                val end = ObjectId.fromString("5f06962bf9a484a35a3a37c24f3933a9168e90ff")
                val start = ObjectId.fromString("c25aae9ec630c546999a5cd62740639746efbc13")

                repo.log(start, end).toList() shouldHaveSize 7
            }

            it("should return entire log") {
                repo.log().toList() shouldHaveSize 495
            }
        }

        context("constructing GitLog") {
            lateinit var git: Git
            val path = Files.createTempDirectory("test-git")

            beforeEach {
                git = Git.init().setDirectory(path.toFile()).call()

                git.addCommit(path, "Initial Commit")

                git.addCommit(path, "Second commit\n\nRelease version 0.1.0")
                git.tag().setAnnotated(true).setName("v0.1.0").setForceUpdate(true).call()

                git.addCommit(path, "Third commit\n\nRelease version 0.2.0")
                git.tag().setAnnotated(false).setName("v0.2.0").setForceUpdate(true).call()

                git.addCommit(path, "Fourth commit\n\nRelease version 0.3.0")
                git.tag().setAnnotated(true).setName("v0.3.0").setForceUpdate(true).call()

                git.addCommit(path, "Fifth commit\n\nAdd temp5.txt file")

                git.addCommit(path, "Sixth commit")
            }

            afterEach { path.delete() }

            it("should return a constructed GitLog out of a sequence of commits") {
                GitRepo(git).constructLog().commits shouldHaveSize 6
            }

            it("should return a constructed log with the 'end' commit") {
                val commit = git.addCommit(path, "Seventh commit")
                git.tag().setAnnotated(true).setName("v0.4.0").setForceUpdate(true).call()
                val lastCommit = git.addCommit(path, "Eighth commit\n\nRelease version 0.5.0")
                git.tag().setAnnotated(true).setName("v0.5.0").setForceUpdate(true).call()
                GitRepo(git).constructLog(end = commit.id).commits shouldContainExactly listOf(
                    GitCommit(
                        GitMessage("Eighth commit", listOf("Release version 0.5.0")),
                        lastCommit.id,
                        Version("0.5.0")
                    )
                )
            }

            it("should return a constructed log with the 'start' commit") {
                val commit = git.addCommit(path, "Seventh commit")
                git.tag().setAnnotated(true).setName("v0.4.0").setForceUpdate(true).call()
                git.addCommit(path, "Eighth commit\n\nRelease version 0.5.0")
                git.tag().setAnnotated(true).setName("v0.5.0").setForceUpdate(true).call()
                GitRepo(git).constructLog(start = commit.id).commits shouldHaveSize 7
            }
        }
    }
})

private fun Git.addCommit(repoPath: Path, message: String): RevCommit {
    repoPath.resolve("${faker.random.randomAlphanumeric(10)}.txt").toFile().createNewFile()
    add().addFilepattern(".").call()
    return commit().setMessage(message).call()
}

private val faker = Faker()
