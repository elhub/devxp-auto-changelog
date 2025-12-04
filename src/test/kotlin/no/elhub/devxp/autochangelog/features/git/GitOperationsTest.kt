package no.elhub.devxp.autochangelog.features.git

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.maps.shouldHaveKeys
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.time.LocalDate
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk

class GitOperationsTest : FunSpec({

    test("toGitCommits extracts stuff from commit message") {
        val commit1 = fakeRevCommit(
            """
                Initial commit

                ## 🔗 Issue ID(s): TDX-000
                ## 📋 Checklist
                * ✅ Lint checks passed on local machine.
                * ✅ Unit tests passed on local machine.
            """.trimIndent()
        )

        val gitCommits = toGitCommits(listOf(commit1), emptyList())
        gitCommits.size shouldBe 1

        val initCommit = gitCommits.first()
        assertSoftly(initCommit) {
            title shouldBe "Initial commit"
            body shouldNotContain ("Initial commit")
            body shouldContain ("TDX-000")
            jiraIssues.size shouldBe 1
            jiraIssues.shouldContain("TDX-000")
        }
    }

    test("toGitCommits handles commit with no body") {
        val commit1 = fakeRevCommit("Fix bug in production")

        val gitCommits = toGitCommits(listOf(commit1), emptyList())
        gitCommits.size shouldBe 1

        val fixCommit = gitCommits.first()
        assertSoftly(fixCommit) {
            title shouldBe "Fix bug in production"
            body shouldBe ""
        }
    }

    test("toGitCommits handles commit with body but no issue IDs") {
        val commit1 = fakeRevCommit(
            """
                Update documentation

                This commit does some stuff but no issues are linked.
            """.trimIndent()
        )

        val gitCommits = toGitCommits(listOf(commit1), emptyList())
        gitCommits.size shouldBe 1

        val docCommit = gitCommits.first()
        assertSoftly(docCommit) {
            title shouldBe "Update documentation"
            body shouldContain ("This commit does some stuff but no issues are linked.")
            jiraIssues.size shouldBe 0
        }
    }

    test("toGitCommits supports multiple tags for a commit") {
        val commit1 = fakeRevCommit("Release version 1.0.0")
        val tag1 = fakeTagRef("refs/tags/v1.0.0", commit1)
        val tag2 = fakeTagRef("stable", commit1)

        val gitTags = toGitTags(listOf(tag1, tag2))
        val gitCommits = toGitCommits(listOf(commit1), gitTags)

        gitCommits.size shouldBe 1
        val releaseCommit = gitCommits.first()
        releaseCommit.tags.size shouldBe 2
        releaseCommit.tags.map { it.name } shouldBe listOf("v1.0.0", "stable")
    }

    test("toGitTags converts refs to git tags") {
        val commit1 = fakeRevCommit("Initial commit")
        val tagRef = fakeTagRef("refs/tags/v1.0.0", commit1)

        val gitTags = toGitTags(listOf(tagRef))
        gitTags.size shouldBe 1

        val gitTag = gitTags.first()
        assertSoftly(gitTag) {
            name shouldBe "v1.0.0"
            commitHash shouldBe commit1.name
        }
    }

    context("getCommitsBetweenTags") {
        val commit1 = fakeRevCommit("First commit", commitTime = 1600000001)
        val commit2 = fakeRevCommit("Second commit", commitTime = 1600000002)
        val commit3 = fakeRevCommit("Third commit", commitTime = 1600000003)
        val commit4 = fakeRevCommit("Fourth commit", commitTime = 1600000004)

        val tag = fakeTagRef("refs/tags/v1.0.0", commit1)
        val tag2 = fakeTagRef("refs/tags/v2.0.0", commit4)

        val gitTags = toGitTags(listOf(tag, tag2))

        val commits = toGitCommits(
            listOf(commit1, commit2, commit3, commit4),
            gitTags
        )

        test("extracts commits between two tags") {
            val commitsBetweenTags = getCommitsBetweenTags(
                commits,
                fromTag = gitTags.first(),
                toTag = gitTags.last()
            )

            commitsBetweenTags.size shouldBe 3
            commitsBetweenTags.map { it.title } shouldBe listOf(
                "Second commit",
                "Third commit",
                "Fourth commit",
            )
        }

        test("errors on invalid tag order") {
            shouldThrow<IllegalArgumentException> {
                getCommitsBetweenTags(
                    commits,
                    fromTag = gitTags.last(),
                    toTag = gitTags.first()
                )
            }
        }

        test("can cross tags without issues") {
            val commit5 = fakeRevCommit("Fifth commit", commitTime = 1600000005)
            val commit6 = fakeRevCommit("Sixth commit", commitTime = 1600000006)

            val tag3 = fakeTagRef("v3.0.0", commit6)
            val extendedGitTags = toGitTags(listOf(tag, tag2, tag3))
            val extendedCommits = toGitCommits(
                listOf(commit1, commit2, commit3, commit4, commit5, commit6),
                extendedGitTags
            )
            val commitsBetweenTags = getCommitsBetweenTags(
                extendedCommits,
                fromTag = extendedGitTags[0],
                toTag = extendedGitTags[2]
            )

            commitsBetweenTags.size shouldBe 5
            commitsBetweenTags.map { it.title } shouldBe listOf(
                "Second commit",
                "Third commit",
                "Fourth commit",
                "Fifth commit",
                "Sixth commit",
            )
        }
    }

    context("extractJiraIssuesIdsFromCommits") {
        val commit1 = GitCommit(
            hash = "abc123",
            title = "Implement feature X",
            body = "This commit implements feature X.\n\nRelated to ABC-101 and PROJ-ABC.",
            date = LocalDate.of(2020, 1, 1),
            tags = emptyList(),
            jiraIssues = listOf("ABC-101", "ABC-102")
        )

        val commit2 = GitCommit(
            hash = "def456",
            title = "Fix bug Y",
            body = "Fixes bug Y reported in XYZ-202.",
            date = LocalDate.of(2020, 1, 2),
            tags = emptyList(),
            jiraIssues = listOf("XYZ-202")
        )

        val commit3 = GitCommit(
            hash = "ghi789",
            title = "Update documentation",
            body = "Updates the documentation. No related issue.",
            date = LocalDate.of(2020, 1, 3),
            tags = emptyList(),
            jiraIssues = emptyList()
        )

        val commit4 = GitCommit(
            hash = "jkl012",
            title = "Refactor codebase",
            body = "Refactors the codebase for better readability. See ABC-101 for details.",
            date = LocalDate.of(2020, 1, 4),
            tags = emptyList(),
            jiraIssues = listOf("ABC-101")
        )

        test("correctly extracts issues") {
            val commits = listOf(commit1, commit2, commit3, commit4)
            val result = extractJiraIssuesIdsFromCommits(commits)
            result.size shouldBe 4
            result.shouldHaveKeys("ABC-101", "ABC-102", "XYZ-202", "NO-JIRA")
            assertSoftly(result) {
                this["ABC-101"] shouldBe listOf(commit1, commit4)
                this["ABC-102"] shouldBe listOf(commit1)
                this["XYZ-202"] shouldBe listOf(commit2)
            }
        }
    }
})

fun fakeRevCommit(
    fullMessage: String,
    commitTime: Int = 1609459200
): RevCommit {
    val repo = InMemoryRepository.Builder()
        .setRepositoryDescription(DfsRepositoryDescription("test"))
        .build()
    val inserter = repo.objectDatabase.newInserter()

    val commitContent = buildString {
        append("tree " + ObjectId.zeroId().name() + "\n")
        append("author Test <test@test.com> $commitTime +0000\n")
        append("committer Test <test@test.com> $commitTime +0000\n\n")
        append(fullMessage)
    }

    val commitId = inserter.insert(Constants.OBJ_COMMIT, commitContent.toByteArray())
    inserter.flush()

    return RevWalk(repo).parseCommit(commitId)
}

fun fakeTagRef(tagName: String, commit: RevCommit): Ref = object : Ref {
    override fun getName() = tagName
    override fun getObjectId(): ObjectId = commit.id
    override fun getPeeledObjectId(): ObjectId? = null
    override fun getStorage() = Ref.Storage.LOOSE
    override fun isSymbolic() = false
    override fun isPeeled() = false
    override fun getLeaf() = this
    override fun getTarget() = this
}
