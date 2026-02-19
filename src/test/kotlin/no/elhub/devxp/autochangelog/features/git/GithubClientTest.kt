package no.elhub.devxp.autochangelog.features.git

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.OverrideMode
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import no.elhub.devxp.autochangelog.createMockResponse
import no.elhub.devxp.autochangelog.features.git.GitCommit
import org.eclipse.jgit.api.Git
import java.time.LocalDateTime

class GithubClientTest : FunSpec({
    val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    val mockEngine = MockEngine {
        val issueId = it.url.encodedPath.substringAfterLast("/").substringBefore("?")

        respond(
            content = createMockResponse(
                key = issueId,
                title = "Dummy Title",
                body = "Dummy Body",
                status = "In Progress"
            ),
            status = HttpStatusCode.OK,
            headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
        )
    }

    val httpClient = HttpClient(mockEngine) {
        install(ContentNegotiation) { json(json) }
    }

    val client = GithubClient(httpClient)

    test("github client cannot be initiated without env vars being present") {
        withEnvironment(
            mapOf(
                "GITHUB_TOKEN" to null,
            ),
            mode = OverrideMode.SetOrOverride
        ) {
            shouldThrow<IllegalStateException> { GithubClient() }
        }
    }

    test("github client can be initiated when env vars are present") {
        withEnvironment(
            mapOf(
                "GITHUB_TOKEN" to "dummy-token"
            ),
            mode = OverrideMode.SetOrOverride
        ) {
            shouldNotThrowAny { GithubClient() }
        }
    }

    test("getRepoInfo returns correct owner and repo from HTTPS URL") {
        val mockGit = mockk<Git>()
        every { mockGit.repository.config.getString("remote", "origin", "url") } returns "https://github.com/org/repo-name.git"
        val result = client.getRepoInfo(mockGit)
        result shouldBe Pair("org", "repo-name")
    }

    test("getRepoInfo returns null for invalid URL") {
        val mockGit = mockk<Git>()
        every { mockGit.repository.config.getString("remote", "origin", "url") } returns "https://notgithub.com/invalid/url"
        val result = client.getRepoInfo(mockGit)
        result shouldBe null
    }

    test("getPrDescription returns empty string on non-OK response") {
        val errorEngine = MockEngine { respond("{}", HttpStatusCode.NotFound) }
        val errorClient = GithubClient(HttpClient(errorEngine))
        val result = errorClient.getPrDescription("owner", "repo", "sha")
        result shouldBe ""
    }

    test("getPrDescription returns PR body when present") {
        val prBody = "This is a PR body"
        val engine = MockEngine {
            respond(
                content = """[{"body":"$prBody"}]""",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
            )
        }
        val prClient = GithubClient(
            HttpClient(engine) {
                install(ContentNegotiation) { json(json) }
            }
        )
        val result = prClient.getPrDescription("owner", "repo", "sha")
        result shouldBe prBody
    }

    test("populateJiraIssuesFromDescription adds Jira issues from PR description") {
        val mockGit = mockk<Git>()
        every { mockGit.repository.config.getString("remote", "origin", "url") } returns "https://github.com/org/repo.git"

        val commit = GitCommit(
            hash = "sha123",
            title = "Some commit",
            body = "Some body once told me",
            commitTime = LocalDateTime.now(),
            tags = emptyList(),
            jiraIssues = emptyList()
        )
        val commits = listOf(commit)

        val client = spyk(GithubClient(httpClient))
        coEvery { client.getPrDescription("org", "repo", "sha123") } returns "Implements JIRA-123 and JIRA-456"

        client.populateJiraIssuesFromDescription(mockGit, commits)

        commit.jiraIssues shouldContainExactly listOf("JIRA-123", "JIRA-456")
    }
})
