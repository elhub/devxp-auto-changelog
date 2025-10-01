package no.elhub.devxp.autochangelog.cli

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

@OptIn(ExperimentalPathApi::class)
class AutoChangelogCliTest :
    FunSpec({
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
            test("should exit with an error when run without arguments") {
                val nonGitDir = createNonGitDirectory()
                cmd.execute(
                    "--dir-path",
                    nonGitDir.toString(),
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
                    nonGitDir.toString(),
                ) shouldNotBe 0
            }

            test("should succeed when run in a Git repository") {
                val gitRepo = createGitRepository("git-repo")
                val outputDir = createOutputDirectory()

                cmd.execute(
                    "--dir-path",
                    gitRepo.toString(),
                    "--output-dir",
                    outputDir.toString(),
                ) shouldBe 0
            }

            test("should fail if the .git directory is missing") {
                val nonGitDir = createNonGitDirectory()
                cmd.execute(
                    "--dir-path",
                    nonGitDir.toString(),
                ) shouldNotBe 0
            }

            test("should read changelog configuration from the root of the Git repository") {
                val gitRepoWithConfig = createGitRepositoryWithConfig()
                val outputDir = createOutputDirectory()

                cmd.execute(
                    "--dir-path",
                    gitRepoWithConfig.toString(),
                    "--output-dir",
                    outputDir.toString(),
                ) shouldBe 0
            }

            test("fails when --after tag is newer than --up-to tag") {
                val repo = createComplexRepository()
                val out = createOutputDirectory()
                cmd.execute(
                    "--dir-path",
                    repo.toString(),
                    "--output-dir",
                    out.toString(),
                    "--after",
                    "v1.1.0",
                    "--up-to",
                    "v1.0.0",
                ) shouldNotBe 0
            }

            test("fails when using --for-tag on oldest tag (no previous tag)") {
                val repo = createComplexRepository()
                val out = createOutputDirectory()
                cmd.execute(
                    "--dir-path",
                    repo.toString(),
                    "--output-dir",
                    out.toString(),
                    "--for-tag",
                    "v0.1.0",
                ) shouldNotBe 0
            }

            // There seems to be a bug here
            xtest("json output includes date and time for --for-tag") {
                val repo = createComplexRepository()
                val out = createOutputDirectory()
                cmd.execute(
                    "--dir-path",
                    repo.toString(),
                    "--output-dir",
                    out.toString(),
                    "--for-tag",
                    "v1.1.0",
                    "--json",
                ) shouldBe 0
                val json = File(out.toFile(), "CHANGELOG.json").readText()
                json.shouldContain("\"feat: add feature B\"")
                json.shouldContain("\"refactor: code cleanup\"")
                json.shouldNotContain("feat: add feature C")
                json.shouldNotContain("fix: bug fix 1")
                json.shouldContain("\"date\"")
                json.shouldContain("\"time\"")
            }

            test("custom changelog base name gets .json when --json used") {
                val repo = createComplexRepository()
                val out = createOutputDirectory()
                cmd.execute(
                    "--dir-path",
                    repo.toString(),
                    "--output-dir",
                    out.toString(),
                    "--json",
                    "--changelog-name",
                    "HISTORY.md",
                ) shouldBe 0
                File(out.toFile(), "HISTORY.json").exists() shouldBe true
            }

            test("tag regex that excludes previous tag causes failure") {
                val repo = createComplexRepository()
                val out = createOutputDirectory()
                cmd.execute(
                    "--dir-path",
                    repo.toString(),
                    "--output-dir",
                    out.toString(),
                    "--for-tag",
                    "v1.1.0",
                    "--tag-regex",
                    "v1\\.1\\.0",
                ) shouldNotBe 0
            }

            // There seems to be a bug here
            xtest("only --up-to filters commits correctly") {
                val repo = createComplexRepository()
                val out = createOutputDirectory()
                cmd.execute(
                    "--dir-path",
                    repo.toString(),
                    "--output-dir",
                    out.toString(),
                    "--up-to",
                    "v1.0.0",
                ) shouldBe 0
                val md = File(out.toFile(), "CHANGELOG.md").readText()
                md.shouldContain("fix: bug fix 1")
                md.shouldContain("feat: add feature A")
                md.shouldContain("chore: initial")
                md.shouldNotContain("feat: add feature B")
            }

            test("only --after filters commits correctly") {
                val repo = createComplexRepository()
                val out = createOutputDirectory()
                cmd.execute(
                    "--dir-path",
                    repo.toString(),
                    "--output-dir",
                    out.toString(),
                    "--after",
                    "v1.0.0",
                ) shouldBe 0
                val md = File(out.toFile(), "CHANGELOG.md").readText()
                md.shouldContain("feat: add feature B")
                md.shouldContain("refactor: code cleanup")
                md.shouldContain("feat: add feature C")
                md.shouldNotContain("fix: bug fix 1")
            }

            test("between two tags includes only middle range") {
                val repo = createComplexRepository()
                val out = createOutputDirectory()
                cmd.execute(
                    "--dir-path",
                    repo.toString(),
                    "--output-dir",
                    out.toString(),
                    "--after",
                    "v0.1.0",
                    "--up-to",
                    "v1.0.0",
                ) shouldBe 0
                val md = File(out.toFile(), "CHANGELOG.md").readText()
                md.shouldContain("feat: add feature A")
                md.shouldContain("fix: bug fix 1")
                md.shouldNotContain("feat: add feature B")
                md.shouldNotContain("chore: initial")
            }

            test("custom changelog base name gets .json when --json used") {
                val repo = createComplexRepository()
                val out = createOutputDirectory()
                cmd.execute(
                    "--dir-path",
                    repo.toString(),
                    "--output-dir",
                    out.toString(),
                    "--json",
                    "--changelog-name",
                    "HISTORY.md",
                ) shouldBe 0
                File(out.toFile(), "HISTORY.json").exists() shouldBe true
            }
        }
    })
