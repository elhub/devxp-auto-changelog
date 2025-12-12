package no.elhub.devxp.autochangelog

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.ktor.client.HttpClient
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import no.elhub.devxp.autochangelog.features.jira.JiraClient
import no.elhub.devxp.autochangelog.features.jira.JiraIssue
import picocli.CommandLine
import java.io.File

class AutoChangelogCliTest : FunSpec({

    // Mock JiraClient to avoid real HTTP calls
    val mockHttpClient = mockk<HttpClient>()
    val mockJiraClient = spyk(JiraClient(mockHttpClient))

    coEvery { mockJiraClient.getIssueById(any()) } returns JiraIssue(
        key = "TEST-123",
        title = "Mocked JIRA Issue",
        body = "This is a mocked JIRA issue for testing purposes.",
    )

    val cmd = CommandLine(AutoChangelog(mockJiraClient))
    val outputChangelogFile = File("CHANGELOG.md")

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
            assertSoftly {
                content shouldContain "Initial commit"
                content shouldContain "Add main function"
            }
        }

        test("should generate changelog with mocked JIRA issues") {
            val commits = listOf(
                TestCommit(
                    fileName = "Feature.kt",
                    content = "fun feature() { println(\"Feature!\") }",
                    message = "Implement feature TEST-123",
                )
            )
            val gitRepo = createRepositoryFromCommits("jira-git-repo", commits)
            val exitCode = cmd.execute("--working-dir", gitRepo.toString())
            exitCode shouldBe 0
            outputChangelogFile.exists() shouldBe true
            val content = outputChangelogFile.readText()
            assertSoftly {
                content shouldContain "Mocked JIRA Issue"
                content shouldContain "Implement feature TEST-123"
            }
        }

        test("should generate changelog with no JIRA issues") {
            val commits = listOf(
                TestCommit(
                    fileName = "NoJira.kt",
                    content = "fun noJira() { println(\"No JIRA!\") }",
                    message = "Implement no JIRA feature",
                )
            )
            val gitRepo = createRepositoryFromCommits("no-jira-git-repo", commits)
            val exitCode = cmd.execute("--working-dir", gitRepo.toString())
            exitCode shouldBe 0
            outputChangelogFile.exists() shouldBe true
            val content = outputChangelogFile.readText()
            assertSoftly {
                content shouldContain "Commits without associated JIRA issues"
                content shouldContain "Implement no JIRA feature"
            }
        }

        test("should allow --from-tag and --to-tag options") {
            val fromTag = "v1.0.0"
            val toTag = "v1.1.0"
            val changelogFile = File("CHANGELOG [$fromTag-$toTag].md")
            try {
                val commits = listOf(
                    TestCommit(
                        fileName = "Versioned.kt",
                        content = "fun versioned() { println(\"Versioned!\") }",
                        message = "Add versioned feature",
                        tags = listOf("v1.0.0")
                    ),
                    TestCommit(
                        fileName = "Versioned2.kt",
                        content = "fun versioned2() { println(\"Versioned 2!\") }",
                        message = "Add another versioned feature",
                        tags = listOf("v1.1.0")
                    )
                )
                val gitRepo = createRepositoryFromCommits("versioned-git-repo", commits)

                val exitCode = cmd.execute(
                    "--working-dir",
                    gitRepo.toString(),
                    "--from-tag",
                    "v1.0.0",
                    "--to-tag",
                    "v1.1.0"
                )
                exitCode shouldBe 0
                changelogFile.exists() shouldBe true
                val content = changelogFile.readText()
                assertSoftly {
                    content shouldContain "Add another versioned feature"
                    content shouldNotContain "Add versioned feature"
                }
            } finally {
                if (changelogFile.exists()) {
                    changelogFile.delete()
                }
            }
        }

        test("should allow only --to-tag option") {
            val toTag = "v2.0.0"
            val changelogFile = File("CHANGELOG [-$toTag].md")
            try {
                val commits = listOf(
                    TestCommit(
                        fileName = "PreToTag.kt",
                        content = "fun preToTag() { println(\"Pre To Tag!\") }",
                        message = "Add pre to-tag feature",
                    ),
                    TestCommit(
                        fileName = "OtherTag.kt",
                        content = "fun preToTag() { println(\"Pre To Tag 2!\") }",
                        message = "Add first tag",
                        tags = listOf("v1.0.0")
                    ),
                    TestCommit(
                        fileName = "ToTag.kt",
                        content = "fun toTag() { println(\"To Tag!\") }",
                        message = "Add to-tag feature",
                        tags = listOf("v2.0.0")
                    )
                )
                val gitRepo = createRepositoryFromCommits("to-tag-git-repo", commits)

                val exitCode = cmd.execute(
                    "--working-dir",
                    gitRepo.toString(),
                    "--to-tag",
                    "v2.0.0"
                )
                exitCode shouldBe 0
                changelogFile.exists() shouldBe true
                val content = changelogFile.readText()
                assertSoftly {
                    content shouldContain "Add pre to-tag feature"
                    content shouldContain "Add first tag"
                    content shouldContain "Add to-tag feature"
                }
            } finally {
                if (changelogFile.exists()) {
                    changelogFile.delete()
                }
            }
        }

        test("should allow only --from-tag option") {
            val fromTag = "v1.0.0"
            val changelogFile = File("CHANGELOG [$fromTag-].md")
            try {
                val commits = listOf(
                    TestCommit(
                        fileName = "FromTag.kt",
                        content = "fun fromTag() { println(\"From Tag!\") }",
                        message = "Add from-tag feature",
                        tags = listOf("v1.0.0")
                    ),
                    TestCommit(
                        fileName = "PostFromTag.kt",
                        content = "fun postFromTag() { println(\"Post From Tag!\") }",
                        message = "Add post from-tag feature",
                    )
                )
                val gitRepo = createRepositoryFromCommits("from-tag-git-repo", commits)

                val exitCode = cmd.execute(
                    "--working-dir",
                    gitRepo.toString(),
                    "--from-tag",
                    "v1.0.0"
                )
                exitCode shouldBe 0
                changelogFile.exists() shouldBe true
                val content = changelogFile.readText()
                content shouldContain "Add post from-tag feature"
                content shouldNotContain "Add from-tag feature"
            } finally {
                if (changelogFile.exists()) {
                    changelogFile.delete()
                }
            }
        }

        test("Should create changelog with custom name when '--changelog-name' is provided") {
            val customName = "MY_CUSTOM_CHANGELOG"
            val commits = listOf(
                TestCommit(
                    fileName = "CustomName.kt",
                    content = "fun customName() { println(\"Custom Name!\") }",
                    message = "Add custom name feature",
                )
            )
            val gitRepo = createRepositoryFromCommits("custom-name-git-repo", commits)
            val exitCode = cmd.execute(
                "--working-dir",
                gitRepo.toString(),
                "--changelog-name",
                customName
            )
            exitCode shouldBe 0
            val customChangelogFile = File("$customName.md")
            try {
                customChangelogFile.exists() shouldBe true
                val content = customChangelogFile.readText()
                content shouldContain "Add custom name feature"
            } finally {
                if (customChangelogFile.exists()) {
                    customChangelogFile.delete()
                }
            }
        }

        test("should group on commits if '--group-by-commit' flag is set ") {
            val commits = listOf(
                TestCommit(
                    fileName = "NoJira.kt",
                    content = "fun noJira() { println(\"No JIRA!\") }",
                    message = "Implement no JIRA feature",
                ),
                TestCommit(
                    fileName = "SomeJira.kt",
                    content = "fun someJira() { println(\"I'm JIRAing here!\") }",
                    message = "Implement TDX-123",
                ),
                TestCommit(
                    fileName = "README.md",
                    content = "# Just a readme",
                    message = "Add README",
                ),
            )
            val gitRepo = createRepositoryFromCommits("git-repo", commits)
            val exitCode = cmd.execute("--working-dir", gitRepo.toString(), "--group-by-commit")
            exitCode shouldBe 0
            outputChangelogFile.exists() shouldBe true
            val content = outputChangelogFile.readText()
            assertSoftly {
                content shouldContain "Implement TDX-123"
                content shouldContain "Implement no JIRA feature"
                content shouldContain "Add README"
                content.shouldContain("- TEST-123: Mocked JIRA Issue")
            }
        }
    }
})
