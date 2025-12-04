package no.elhub.devxp.autochangelog.features.jira

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import no.elhub.devxp.autochangelog.features.git.GitCommit
import java.util.Base64

class JiraClient(
    client: HttpClient? = null,
) {
    private val internalClient = client ?: HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                }
            )
        }
        val username = System.getenv("JIRA_USERNAME")
        val token = System.getenv("JIRA_API_TOKEN")

        check(!username.isNullOrBlank()) { "JIRA_USERNAME environment variable is not set." }
        check(!token.isNullOrBlank()) { "JIRA_API_TOKEN environment variable is not set." }

        val encodedAuth = Base64.getEncoder().encodeToString("$username:$token".toByteArray())

        defaultRequest {
            url("https://elhub.atlassian.net/rest/api/3/")
            header("Authorization", "Basic $encodedAuth")
            header("Accept", "application/json")
        }
    }

    suspend fun populateJiraMap(
        jiraMap: Map<String, List<GitCommit>>,
    ): Map<JiraIssue, List<GitCommit>> = jiraMap.mapKeys { (jiraIssueId, _) ->
        if (jiraIssueId == "NO-JIRA") {
            JiraIssue(
                key = "NO-JIRA",
                title = "Commits not associated with any JIRA issues",
                body = ""
            )
        } else {
            this.getIssueById(jiraIssueId)
        }
    }

    suspend fun getIssueById(issueId: String): JiraIssue {
        val response = internalClient.get("issue/$issueId?fields=summary,description")
        val apiResponse = response.body() as JiraApiResponse
        return apiResponse.toJiraIssue()
    }
}
