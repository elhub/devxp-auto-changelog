package no.elhub.devxp.autochangelog.jira

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.elhub.devxp.autochangelog.extensions.description
import no.elhub.devxp.autochangelog.extensions.title
import org.eclipse.jgit.revwalk.RevCommit
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class JiraIssueExtractorTest : FunSpec({

    beforeTest {
        startKoin {
            modules(module {
                single<HttpClient> {
                    mockk<HttpClient>().apply {
                        val mockResponse = mockk<HttpResponse<String>>()
                        every { mockResponse.statusCode() } returns 200
                        every { mockResponse.body() } returns """{"key":"PROJ-1","fields":{"summary":"Title","description":"This is a description."}}"""
                        every { send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>()) } returns mockResponse
                    }
                }
            })
        }
    }

    afterTest {
        stopKoin()
    }

    test("jiraRegex matches valid Jira issue keys") {
        val regex = JiraIssueExtractor.jiraRegex
        val matches = regex.findAll("Fixed PROJ-123 and TEST_2-456").map { it.value }.toList()
        matches shouldBe listOf("PROJ-123", "TEST_2-456")
    }

    test("fetchJiraIssue returns null if not initialized") {
        JiraIssueExtractor.reset()
        JiraIssueExtractor.fetchJiraIssue("PROJ-1") shouldBe null
    }

    test("getJiraIssueUrl returns correct URL") {
        JiraIssueExtractor.getJiraIssueUrl("PROJ-1") shouldBe "${no.elhub.devxp.autochangelog.config.Configuration.JIRA_ISSUES_URL}/PROJ-1"
    }

    test("formatJiraIssuesMarkdown formats issues correctly") {
        val issues = listOf(
            JiraIssue("PROJ-1", "Title", "Description", "http://jira/PROJ-1"),
            JiraIssue("PROJ-2", "Title2", null, "http://jira/PROJ-2")
        )
        val markdown = JiraIssueExtractor.formatJiraIssuesMarkdown(issues)
        markdown shouldBe """
            **[PROJ-1](http://jira/PROJ-1)**: Title
              *Description*: Description
            **[PROJ-2](http://jira/PROJ-2)**: Title2
        """.trimIndent()
    }

    test("reset clears credentials and cache") {
        JiraIssueExtractor.initialize("user", "token")
        JiraIssueExtractor.reset()
        JiraIssueExtractor.isInitialized() shouldBe false
    }

    test("fetchJiraIssuesFromCommit returns JiraIssue list for all found keys") {
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
        JiraIssueExtractor.reset()
        JiraIssueExtractor.fetchJiraIssue("PROJ-1") shouldBe null
    }

    test("fetchJiraIssue returns JiraIssue for valid key when initialized") {
        JiraIssueExtractor.initialize("user", "token")
        JiraIssueExtractor.fetchJiraIssue("PROJ-1") shouldBe JiraIssue(
            "PROJ-1",
            "Title",
            "Desc",
            "https://elhub.atlassian.net/browse/PROJ-1"
        )
        unmockkObject(JiraIssueExtractor)
    }

    test("fetchJiraIssue returns null for invalid key") {
        JiraIssueExtractor.initialize("user", "token")
        mockkObject(JiraIssueExtractor)
        every { JiraIssueExtractor.fetchJiraIssue("INVALID") } returns null
        JiraIssueExtractor.fetchJiraIssue("INVALID") shouldBe null
        unmockkObject(JiraIssueExtractor)
    }

})

