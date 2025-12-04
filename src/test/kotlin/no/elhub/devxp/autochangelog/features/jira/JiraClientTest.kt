package no.elhub.devxp.autochangelog.features.jira

import io.kotest.core.spec.style.FunSpec
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
import java.time.LocalDate
import kotlinx.serialization.json.Json
import no.elhub.devxp.autochangelog.createMockResponse
import no.elhub.devxp.autochangelog.features.git.GitCommit

class JiraClientTest : FunSpec({
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
                body = "Dummy Body"
            ),
            status = HttpStatusCode.OK,
            headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
        )
    }

    val httpClient = HttpClient(mockEngine) {
        install(ContentNegotiation) { json(json) }
    }
    val client = JiraClient(httpClient)

    test("getIssueById returns correct JiraIssue base on API response") {
        val result = client.getIssueById("GOG-100")
        result.key shouldBe "GOG-100"
        result.title shouldBe "Dummy Title"
        result.body shouldBe "Dummy Body"
    }

    test("populateJiraMap should populate JiraIssue details in the map") {
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
            jiraIssues = listOf("ABC-101")
        )

        val commit3 = GitCommit(
            hash = "ghi789",
            title = "Update documentation",
            body = "Updates the documentation. No related issue.",
            date = LocalDate.of(2020, 1, 3),
            tags = emptyList(),
            jiraIssues = emptyList()
        )

        val myMap = mapOf(
            "ABC-101" to listOf(commit1, commit2),
            "ABC-102" to listOf(commit1),
            "NO-JIRA" to listOf(commit3)
        )

        val populatedMap = client.populateJiraMap(myMap)
        populatedMap.size shouldBe 3

        populatedMap.keys.map { it.key }.toSet() shouldBe setOf(
            "ABC-101",
            "ABC-102",
            "NO-JIRA"
        )

        populatedMap shouldContainExactly mapOf(
            JiraIssue("ABC-101", "Dummy Title", "Dummy Body") to listOf(commit1, commit2),
            JiraIssue("ABC-102", "Dummy Title", "Dummy Body") to listOf(commit1),
            JiraIssue("NO-JIRA", "Commits not associated with any JIRA issues", "") to listOf(commit3)
        )
    }
})
