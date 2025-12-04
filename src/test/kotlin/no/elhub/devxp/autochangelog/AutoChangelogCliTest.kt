package no.elhub.devxp.autochangelog

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.HttpClient
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import org.eclipse.jgit.api.InitCommand
import picocli.CommandLine
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText
import no.elhub.devxp.autochangelog.features.jira.JiraClient
import no.elhub.devxp.autochangelog.features.jira.JiraIssue

class AutoChangelogCliTest : FunSpec({
    val mockHttpClient = mockk<HttpClient>()
    val mockJiraClient = spyk(JiraClient(mockHttpClient))
    coEvery { mockJiraClient.getIssueById(any()) } returns JiraIssue(
        key = "TEST-123",
        title = "Mocked JIRA Issue",
        body = "This is a mocked JIRA issue for testing purposes.",
    )

    val cmd = CommandLine(AutoChangelog(mockJiraClient))
    val outputChangelogFile = File("CHANGELOG.md")

    // Helper functions for test setup
    fun createNonGitDirectory(): Path {
        val tempDir = createTempDirectory("not-a-git-repo")
        tempDir.toFile().deleteOnExit()
        return tempDir
    }

    data class TestCommit(
        val fileName: String,
        val content: String,
        val message: String,
        val tags: List<String> = emptyList()
    )

    fun createRepositoryFromCommits(name: String, commits: List<TestCommit>): Path {
        val tempDir = createTempDirectory(name)
        InitCommand().setDirectory(tempDir.toFile()).call().use { git ->
            commits.forEach { commit ->
                val f = tempDir.resolve(name)
                f.writeText(commit.content)
                git.add().addFilepattern(name).call()
                val c = git.commit().setMessage(commit.message).call()
                if (commit.tags.isNotEmpty()) {
                    commit.tags.forEach { tag ->
                        git
                            .tag()
                            .setName(tag)
                            .setObjectId(c)
                            .call()
                    }
                }
            }
        }
        return tempDir
    }

    // Cleanup after tests
    afterEach {
        if (outputChangelogFile.exists()) {
            outputChangelogFile.delete()
        }
    }

    afterSpec {
        if (outputChangelogFile.exists()) {
            outputChangelogFile.delete()
        }
    }

    context("AutoChangelog application") {

        test("should fail when run in a non-git directory") {
            val nonGitDir = createNonGitDirectory()
            val exitCode = cmd.execute("--working-dir", nonGitDir.toString())
            exitCode shouldBe 1
            outputChangelogFile.exists() shouldBe false
        }

        test("should have a help option on -h") {
            cmd.execute("-h") shouldBe 0
        }

        test("should generate changelog for basic git repository") {
            val commits = listOf(
                TestCommit(
                    fileName = "README.md",
                    content = "# Test Repository",
                    message = "Initial commit",
                ),
                TestCommit(
                    fileName = "Main.kt",
                    content = "fun main() { println(\"Hello, World!\") }",
                    message = "Add main function",
                )

            )
            val gitRepo = createRepositoryFromCommits("basic-git-repo", commits)
            val exitCode = cmd.execute("--working-dir", gitRepo.toString())
            exitCode shouldBe 0
            outputChangelogFile.exists() shouldBe true
            val content = outputChangelogFile.readText()
            content shouldContain "Initial commit"
            content shouldContain "Add main function"
        }
    }
})
