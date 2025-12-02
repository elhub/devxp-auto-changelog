package no.elhub.devxp.autochangelog

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
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
            body shouldContain ("This commit updates the README file")
            jiraIssues.size shouldBe 0
        }
    }

    test("toGitTags converts refs to git tags") {
        val commit1 = fakeRevCommit("Initial commit")
        val tagRef = fakeTagRef("v1.0.0", commit1)

        val gitTags = toGitTags(listOf(tagRef))
        gitTags.size shouldBe 1

        val gitTag = gitTags.first()
        assertSoftly(gitTag) {
            name shouldBe "v1.0.0"
            commitHash shouldBe commit1.name
        }
    }

    test("getCommitsBetweenTags extracts commits between two tags") {
        val commit1 = fakeRevCommit("First commit", commitTime = 1600000001)
        val commit2 = fakeRevCommit("Second commit", commitTime = 1600000002)
        val commit3 = fakeRevCommit("Third commit", commitTime = 1600000003)
        val commit4 = fakeRevCommit("Fourth commit", commitTime = 1600000004)

        val tag = fakeTagRef("v1.0.0", commit1)
        val tag2 = fakeTagRef("v2.0.0", commit4)

        val gitTags = toGitTags(listOf(tag, tag2))



        val commits = toGitCommits(
            listOf(commit1, commit2, commit3, commit4),
            gitTags
        )

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

fun fakeTagRef(tagName: String, commit: RevCommit): Ref {
    return object : Ref {
        override fun getName() = tagName
        override fun getObjectId(): ObjectId = commit.id
        override fun getPeeledObjectId(): ObjectId? = null
        override fun getStorage() = Ref.Storage.LOOSE
        override fun isSymbolic() = false
        override fun isPeeled() = false
        override fun getLeaf() = this
        override fun getTarget() = this
    }
}
