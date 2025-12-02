package no.elhub.devxp.autochangelog

import AutoChangelog
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.eclipse.jgit.api.InitCommand
import picocli.CommandLine
import java.io.File
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText

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

    fun createComplexRepository(): Path {
        val tempDir = createTempDirectory("complex-extra")
        InitCommand().setDirectory(tempDir.toFile()).call().use { git ->
            fun commit(
                name: String,
                content: String,
                msg: String,
                tag: String? = null,
            ) {
                val f = tempDir.resolve(name)
                f.writeText(content)
                git.add().addFilepattern(name).call()
                val c = git.commit().setMessage(msg).call()
                if (tag != null) {
                    git
                        .tag()
                        .setName(tag)
                        .setObjectId(c)
                        .call()
                }
            }
            commit("a.txt", "a", "chore: initial", "v0.1.0")
            commit("b.txt", "b", "feat: add feature A")
            commit("c.txt", "c", "fix: bug fix 1", "v1.0.0")
            commit("d.txt", "d", "feat: add feature B")
            commit("e.txt", "e", "refactor: code cleanup", "v1.1.0")
            commit("f.txt", "f", "feat: add feature C")
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

        test("should have a help option on -h") {
            cmd.execute("-h") shouldBe 0
        }
    }
})
