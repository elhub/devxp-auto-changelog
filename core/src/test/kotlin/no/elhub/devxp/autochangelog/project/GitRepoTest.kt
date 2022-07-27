package no.elhub.devxp.autochangelog.project

import io.github.serpro69.kfaker.Faker
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import no.elhub.devxp.autochangelog.extensions.delete
import no.elhub.devxp.autochangelog.extensions.description
import no.elhub.devxp.autochangelog.git.GitCommit
import no.elhub.devxp.autochangelog.git.GitMessage
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

class GitRepoTest : DescribeSpec({
    describe("TestRepository") {
        val repo = GitRepo(TestRepository.git)
        context("git log") {

            it("should return git ref for a given version") {
                repo.findTagRef(SemanticVersion(1, 1, 0))?.name shouldBe "refs/tags/v1.1.0"
            }

            it("should find a commit for a given ref") {
                val c = repo.findCommitId(SemanticVersion(1, 1, 0))?.let {
                    repo.findCommit(it)
                }
                c?.shortMessage shouldStartWith "Merge pull request #197"
            }

            it("should find a parent for a given ref") {
                val c = repo.findCommitId(SemanticVersion(1, 1, 0))?.let {
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

                git.addCommit(path, "Third 1 commit")
                git.addCommit(path, "Third 2 commit")
                git.addCommit(path, "Third 3 commit")
                git.addCommit(path, "Third commit\n\nRelease version 0.2.0")
                git.tag().setAnnotated(false).setName("v0.2.0").setForceUpdate(true).call()

                git.addCommit(path, "Fourth 1 commit")
                git.addCommit(path, "Fourth 2 commit")
                git.addCommit(path, "Fourth 3 commit")
                git.addCommit(path, "Fourth commit\n\nRelease version 0.3.0")
                git.tag().setAnnotated(true).setName("v0.3.0").setForceUpdate(true).call()

                git.addCommit(path, "Fifth commit\n\nAdd temp5.txt file")

                git.addCommit(path, "Sixth commit")
            }

            afterEach { path.delete() }

            it("should return a constructed GitLog out of a sequence of commits") {
                GitRepo(git).constructLog().commits shouldHaveSize 12
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
                        LocalDate.now(),
                        SemanticVersion("0.5.0")
                    )
                )
            }

            it("should return a constructed log with the 'start' commit") {
                val commit = git.addCommit(path, "Seventh commit")
                git.tag().setAnnotated(true).setName("v0.4.0").setForceUpdate(true).call()
                git.addCommit(path, "Eighth commit\n\nRelease version 0.5.0")
                git.tag().setAnnotated(true).setName("v0.5.0").setForceUpdate(true).call()
                GitRepo(git).constructLog(start = commit.id).commits shouldHaveSize 13
            }

            it("should return a constructed log consisting of commits with linked JIRA Issues") {
                val seventh = git.addCommit(path, "Seventh commit\n\nCommit description\n\n$jiraIssues")
                git.tag().setAnnotated(false).setName("v0.4.0").setForceUpdate(true).call()
                val eighth = git.addCommit(path, "Eighth commit\n\nRelease version 0.5.0\n\n$jiraIssues")
                git.tag().setAnnotated(true).setName("v0.5.0").setForceUpdate(true).call()
                GitRepo(git).constructLog { it.description.contains(jiraIssues) }.commits shouldContainExactly listOf(
                    GitCommit(
                        GitMessage("Eighth commit", listOf("Release version 0.5.0", jiraIssues)),
                        eighth.id,
                        LocalDate.now(),
                        SemanticVersion("0.5.0")
                    ),
                    GitCommit(
                        GitMessage("Seventh commit", listOf("Commit description", jiraIssues)),
                        seventh.id,
                        LocalDate.now(),
                        SemanticVersion("0.4.0")
                    )
                )
            }

            it("should return a reversed changelist from the log of commits") {
                val r = GitRepo(git)
                val cl = r.createChangelist(r.constructLog())
                cl.changes.entries shouldContainExactly changelist.changes.entries.reversed()
            }

            it("should not have UNRELEASED section in the changelog if HEAD is at git tag with a preceding tag") {
                git.tag().setAnnotated(false).setName("v0.4.0").setForceUpdate(true).call()
                git.addCommit(path, "Test commit")
                git.tag().setAnnotated(false).setName("v0.5.0").setForceUpdate(true).call()
                val r = GitRepo(git)
                val cl = r.createChangelist(r.constructLog())
                cl.changes[Unreleased] shouldBe null
            }
            it("should ignore non-semver compliant tags") {
                git.tag().setAnnotated(false).setName("foobar").setForceUpdate(true).call()
                val r = GitRepo(git)
                val cl = r.createChangelist(r.constructLog())
                cl.changes.entries shouldContainExactly changelist.changes.entries.reversed()
            }
        }
    }
})

private fun Git.addCommit(repoPath: Path, message: String): RevCommit {
    repoPath.resolve("${faker.random.randomString(10)}.txt").toFile().createNewFile()
    add().addFilepattern(".").call()
    return commit().setMessage(message).call()
}

private val faker = Faker()

private const val jiraIssues = "JIRA Issues: TD-1872"

private val changelist = Changelist(
    mapOf(
        Unreleased to listOf(
            ChangelogEntry(
                release = null,
                added = emptyList(),
                changed = emptyList(),
                fixed = emptyList(),
                breakingChange = emptyList(),
                other = listOf("Sixth commit")
            ),
            ChangelogEntry(
                release = null,
                added = emptyList(),
                changed = emptyList(),
                fixed = emptyList(),
                breakingChange = emptyList(),
                other = listOf("Fifth commit")
            )
        ),
        SemanticVersion("0.3.0") to listOf(
            ChangelogEntry(
                release = ChangelogEntry.Release(
                    version = SemanticVersion("0.3.0"),
                    date = LocalDate.now()
                ),
                added = emptyList(),
                changed = emptyList(),
                fixed = emptyList(),
                breakingChange = emptyList(),
                other = listOf("Fourth commit")
            ),
            ChangelogEntry(
                release = null,
                added = emptyList(),
                changed = emptyList(),
                fixed = emptyList(),
                breakingChange = emptyList(),
                other = listOf("Fourth 3 commit")
            ),
            ChangelogEntry(
                release = null,
                added = emptyList(),
                changed = emptyList(),
                fixed = emptyList(),
                breakingChange = emptyList(),
                other = listOf("Fourth 2 commit")
            ),
            ChangelogEntry(
                release = null,
                added = emptyList(),
                changed = emptyList(),
                fixed = emptyList(),
                breakingChange = emptyList(),
                other = listOf("Fourth 1 commit")
            )
        ),
        SemanticVersion("0.2.0") to listOf(
            ChangelogEntry(
                release = ChangelogEntry.Release(
                    version = SemanticVersion("0.2.0"),
                    date = LocalDate.now()
                ),
                added = emptyList(),
                changed = emptyList(),
                fixed = emptyList(),
                breakingChange = emptyList(),
                other = listOf("Third commit")
            ),
            ChangelogEntry(
                release = null,
                added = emptyList(),
                changed = emptyList(),
                fixed = emptyList(),
                breakingChange = emptyList(),
                other = listOf("Third 3 commit")
            ),
            ChangelogEntry(
                release = null,
                added = emptyList(),
                changed = emptyList(),
                fixed = emptyList(),
                breakingChange = emptyList(),
                other = listOf("Third 2 commit")
            ),
            ChangelogEntry(
                release = null,
                added = emptyList(),
                changed = emptyList(),
                fixed = emptyList(),
                breakingChange = emptyList(),
                other = listOf("Third 1 commit")
            )
        ),
        SemanticVersion("0.1.0") to listOf(
            ChangelogEntry(
                release = ChangelogEntry.Release(
                    version = SemanticVersion("0.1.0"),
                    date = LocalDate.now()
                ),
                added = emptyList(),
                changed = emptyList(),
                fixed = emptyList(),
                breakingChange = emptyList(),
                other = listOf("Second commit")
            ),
            ChangelogEntry(
                release = null,
                added = emptyList(),
                changed = emptyList(),
                fixed = emptyList(),
                breakingChange = emptyList(),
                other = listOf("Initial Commit")
            )
        )
    )
)
