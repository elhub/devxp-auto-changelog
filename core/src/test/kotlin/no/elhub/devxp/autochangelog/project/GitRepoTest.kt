package no.elhub.devxp.autochangelog.project

import io.github.serpro69.kfaker.Faker
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import no.elhub.devxp.autochangelog.extensions.delete
import no.elhub.devxp.autochangelog.extensions.description
import no.elhub.devxp.autochangelog.git.GitCommit
import no.elhub.devxp.autochangelog.git.GitMessage
import no.elhub.devxp.autochangelog.io.ChangelogWriter
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

class GitRepoTest : FunSpec({
    context("TestRepository") {
        val repo = GitRepo(TestRepository.git)
        context("git log") {

            test("should return git ref for a given version") {
                repo.findTagRef(SemanticVersion(0, 3, 6))?.name shouldBe "refs/tags/v0.3.6"
            }

            test("should find a commit for a given ref") {
                val c = repo.findCommitId(SemanticVersion(0, 4, 0))?.let {
                    repo.findCommit(it)
                }
                c?.shortMessage shouldStartWith "Fix errors in publishing configuration"
            }

            test("should find a parent for a given ref") {
                val c = repo.findCommitId(SemanticVersion(0, 4, 0))?.let {
                    repo.findParent(it)
                }
                c?.shortMessage shouldBe "Merge pull request #19 from elhub/feat/add-renovate-config"
            }


            test("should return entire log") {
                repo.log().toList() shouldHaveSize 115
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

            test("should return a constructed GitLog out of a sequence of commits") {
                GitRepo(git).constructLog().commits shouldHaveSize 12
            }

            test("should return a constructed log consisting of commits with linked JIRA Issues") {
                val seventh = git.addCommit(path, "Seventh commit\n\nCommit description\n\n$JIRA_ISSUES")
                git.tag().setAnnotated(false).setName("v0.4.0").setForceUpdate(true).call()
                val eighth = git.addCommit(path, "Eighth commit\n\nRelease version 0.5.0\n\n$JIRA_ISSUES")
                git.tag().setAnnotated(true).setName("v0.5.0").setForceUpdate(true).call()
                GitRepo(git).constructLog { it.description.contains(JIRA_ISSUES) }.commits shouldContainExactly listOf(
                    GitCommit(
                        GitMessage("Eighth commit", listOf("Release version 0.5.0", JIRA_ISSUES)),
                        eighth.id,
                        LocalDate.now(),
                        SemanticVersion("0.5.0")
                    ),
                    GitCommit(
                        GitMessage("Seventh commit", listOf("Commit description", JIRA_ISSUES)),
                        seventh.id,
                        LocalDate.now(),
                        SemanticVersion("0.4.0")
                    )
                )
            }

            test("should return a reversed changelist from the log of commits") {
                val r = GitRepo(git)
                val cl = r.createChangelist(r.constructLog())
                cl.changes.values shouldContainExactly changelist.changes.values.reversed()
            }

            test("should not have UNRELEASED section in the changelog if HEAD is at git tag with a preceding tag") {
                git.tag().setAnnotated(false).setName("v0.4.0").setForceUpdate(true).call()
                git.addCommit(path, "Test commit")
                git.tag().setAnnotated(false).setName("v0.5.0").setForceUpdate(true).call()
                val r = GitRepo(git)
                val cl = r.createChangelist(r.constructLog())
                cl.changes[Unreleased] shouldBe null
            }

            test("should ignore non-semver compliant tags") {
                git.tag().setAnnotated(false).setName("foobar").setForceUpdate(true).call()
                val r = GitRepo(git)
                val cl = r.createChangelist(r.constructLog())
                cl.changes.values shouldContainExactly changelist.changes.values.reversed()
            }
        }

        test("should generate compare url") {
            val cl = repo.createChangelist(repo.constructLog())
            val writer = ChangelogWriter()
            writer.generateCompareUrl(cl, repo) shouldBe "https://github.com/elhub/devxp-auto-changelog/compare/v0.5.0...main"
        }
    }
})

private fun Git.addCommit(repoPath: Path, message: String): RevCommit {
    repoPath.resolve("${faker.random.randomString(10)}.txt").toFile().createNewFile()
    add().addFilepattern(".").call()
    return commit().setMessage(message).call()
}

private val faker = Faker()

private const val JIRA_ISSUES = "JIRA Issues: TD-1872"

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
