package no.elhub.devxp.autochangelog.jira

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.elhub.devxp.autochangelog.jiraIssueExtractor
import no.elhub.devxp.autochangelog.mockEnvironmentProvider

class JiraIssueExtractorTest : FunSpec({

    context("JiraIssueExtractor.jiraRegex") {

        test("it matches valid Jira issue keys") {
            val regex = jiraIssueExtractor().jiraRegex
            val matches = regex.findAll("Fixed PROJ-123 and TEST_2-456").map { it.value }.toList()
            matches shouldBe listOf("PROJ-123", "TEST_2-456")
        }

        test("it does not match invalid Jira issue keys") {
            val regex = jiraIssueExtractor().jiraRegex
            val matches = regex.findAll("This is not a key: PROJ123 or PROJ- or -123").map { it.value }.toList()
            matches shouldBe emptyList()
        }
    }

    context("Initializing the Jira client") {
        test("isInitialized returns false before initialization") {
            jiraIssueExtractor().isInitialized() shouldBe false
        }

        test("isInitialized returns true after initialization") {
            val jTest = jiraIssueExtractor()
            jTest.initialize("user", "token")
            jTest.isInitialized() shouldBe true
        }

        test("should fetch credentials from environment variables if not initialized explicitly") {
            val jTest = jiraIssueExtractor(
                environmentProvider = mockEnvironmentProvider(
                    username = "envUser",
                    token = "envToken"
                )
            )
            jTest.initialize(null, null)
            jTest.isInitialized() shouldBe true
        }

        test("should fail if no credentials are provided") {
            val jTest = jiraIssueExtractor(
                environmentProvider = mockEnvironmentProvider(
                    username = null,
                    token = null
                )
            )
            jTest.initialize(null, null)
            jTest.isInitialized() shouldBe false
        }
    }

    context("JiraIssueExtractor methods when not initialized") {
        test("fetchJiraIssue returns null if not initialized") {
            val jTest = jiraIssueExtractor()
            jTest.fetchJiraIssue("PROJ-1") shouldBe null
        }
    }

    /*
    test("getJiraIssueUrl returns correct URL") {
        val jTest = jiraIssueExtractor()
        jTest.initialize("user", "token")
        jTest.getJiraIssueUrl("PROJ-1") shouldBe "${no.elhub.devxp.autochangelog.config.Configuration.JIRA_ISSUES_URL}/PROJ-1"
    }

    test("formatJiraIssuesMarkdown formats issues correctly") {
        val jTest = jiraIssueExtractor()
        jTest.initialize("user", "token")
        val issues = listOf(
            JiraIssue("PROJ-1", "Title", "Description", "http://jira/PROJ-1"),
            JiraIssue("PROJ-2", "Title2", null, "http://jira/PROJ-2")
        )
        val markdown = jTest.formatJiraIssuesMarkdown(issues)
        markdown shouldBe """
     **[PROJ-1](http://jira/PROJ-1)**: Title
     *Description*: Description
     **[PROJ-2](http://jira/PROJ-2)**: Title2
        """.trimIndent()
    }

    test("reset clears credentials and cache") {
        val jTest = jiraIssueExtractor()
        jTest.initialize("user", "token")
        jTest.reset()
        jTest.isInitialized() shouldBe false
    }

    xtest("fetchJiraIssuesFromCommit returns JiraIssue list for all found keys") {
        val jTest = jiraIssueExtractor()
        jTest.initialize("user", "token")
        val commit = mockk<RevCommit>()
        every { commit.title } returns "PROJ-1"
        every { commit.description } returns listOf("Some text", "PROJ-2 in desc")

        mockkObject(JiraIssueExtractor)
        every { JiraIssueExtractor.extractJiraIssuesFromCommit(commit) } returns listOf("PROJ-1", "PROJ-2")
        every { JiraIssueExtractor.fetchJiraIssue("PROJ-1") } returns JiraIssue("PROJ-1", "Title1", "Desc1", "url1")
        every { JiraIssueExtractor.fetchJiraIssue("PROJ-2") } returns JiraIssue("PROJ-2", "Title2", "Desc2", "url2")

        val issues = JiraIssueExtractor.fetchJiraIssuesForCommit(commit)
        issues shouldBe listOf(
            JiraIssue("PROJ-1", "Title1", "Desc1", "url1"),
            JiraIssue("PROJ-2", "Title2", "Desc2", "url2")
        )

        unmockkObject(JiraIssueExtractor)
    }

    test("fetchJiraIssue returns null if not initialized") {
        val jTest = jiraIssueExtractor()
        jTest.fetchJiraIssue("PROJ-1") shouldBe null
    }

    test("fetchJiraIssue returns JiraIssue for valid key when initialized") {
        val jTest = jiraIssueExtractor()
        jTest.initialize("user", "token")
        jTest.fetchJiraIssue("PROJ-1") shouldBe JiraIssue(
            "PROJ-1",
            "Title",
            "This is a description.",
            "https://elhub.atlassian.net/browse/PROJ-1"
        )
    }

    xtest("fetchJiraIssue returns null for invalid key") {
        val jTest = jiraIssueExtractor()
        jTest.initialize("user", "token")
        mockkObject(JiraIssueExtractor)
        every { JiraIssueExtractor.fetchJiraIssue("INVALID") } returns null
        JiraIssueExtractor.fetchJiraIssue("INVALID") shouldBe null
        unmockkObject(JiraIssueExtractor)
    }
     */
})
