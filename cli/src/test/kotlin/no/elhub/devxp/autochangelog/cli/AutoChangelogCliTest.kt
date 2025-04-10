package no.elhub.devxp.autochangelog.cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.eclipse.jgit.api.InitCommand
import picocli.CommandLine
import java.io.File
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText

@OptIn(ExperimentalPathApi::class)
class AutoChangelogCliTest : FunSpec({
    // Shared test resources
    val cmd = CommandLine(AutoChangelog)
    val outputChangelogFile = File("CHANGELOG.md")

    // Helper functions for test setup
    fun createNonGitDirectory(): Path {
        val tempDir = createTempDirectory("not-a-git-repo")
        tempDir.toFile().deleteOnExit()
        return tempDir
    }

    fun createGitRepository(name: String): Path {
        val tempDir = createTempDirectory(name)
        tempDir.toFile().deleteOnExit()

        InitCommand().setDirectory(tempDir.toFile()).call().use { git ->
            val readmeFile = tempDir.resolve("README.md")
            readmeFile.writeText("# Test Repository")
            git.add().addFilepattern("README.md").call()
            git.commit().setMessage("Initial commit").call()
        }

        return tempDir
    }

    fun createGitRepositoryWithConfig(): Path {
        val tempDir = createTempDirectory("git-repo-with-config")
        tempDir.toFile().deleteOnExit()

        InitCommand().setDirectory(tempDir.toFile()).call().use { git ->
            val readmeFile = tempDir.resolve("README.md")
            readmeFile.writeText("# Test Repository")

            val configFile = tempDir.resolve("changelog-config.yml")
            configFile.writeText("version: 1.0.0")

            git.add().addFilepattern("README.md").call()
            git.add().addFilepattern("changelog-config.yml").call()
            git.commit().setMessage("Initial commit with config").call()
        }

        return tempDir
    }

    fun createOutputDirectory(): Path {
        val outputDir = createTempDirectory("output-dir")
        outputDir.toFile().deleteOnExit()
        return outputDir
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
        test("should exit with an error when run without arguments") {
            val nonGitDir = createNonGitDirectory()
            cmd.execute(
                "--dir-path",
                nonGitDir.toString()
            ) shouldNotBe 0
        }

        test("should have a help option on -h") {
            cmd.execute("-h") shouldBe 0
        }

        test("should have a help option on --help") {
            cmd.execute("--help") shouldBe 0
        }

        test("should exit with an error when not in a Git repository") {
            val nonGitDir = createNonGitDirectory()
            cmd.execute(
                "--dir-path",
                nonGitDir.toString()
            ) shouldNotBe 0
        }

        test("should succeed when run in a Git repository") {
            val gitRepo = createGitRepository("git-repo")
            val outputDir = createOutputDirectory()

            cmd.execute(
                "--dir-path",
                gitRepo.toString(),
                "--output-dir",
                outputDir.toString()
            ) shouldBe 0
        }

        test("should fail if the .git directory is missing") {
            val nonGitDir = createNonGitDirectory()
            cmd.execute(
                "--dir-path",
                nonGitDir.toString()
            ) shouldNotBe 0
        }

        test("should read changelog configuration from the root of the Git repository") {
            val gitRepoWithConfig = createGitRepositoryWithConfig()
            val outputDir = createOutputDirectory()

            cmd.execute(
                "--dir-path",
                gitRepoWithConfig.toString(),
                "--output-dir",
                outputDir.toString()
            ) shouldBe 0
        }
    }
})
