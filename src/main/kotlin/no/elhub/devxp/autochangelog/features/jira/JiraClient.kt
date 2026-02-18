package no.elhub.devxp.autochangelog.features.jira

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.http
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import no.elhub.devxp.autochangelog.features.git.GitCommit
import java.util.Base64
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

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
        engine {
            System.getenv("HTTPS_PROXY")?.let { proxyUrl ->
                proxy = ProxyBuilder.http(proxyUrl)
            }
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

    /**
     * Populates a map of JIRA ID [String]s to [GitCommit]s with full [JiraIssue] details.
     *
     * @param jiraMap A map where the key is the JIRA ID [String] and the value is a list of [GitCommit]s associated with that issue.
     * @return A map where the key is a [JiraIssue] and the value is a list of [GitCommit]s associated with that issue.
     */
    suspend fun getIssueDetails(
        jiraMap: Map<String, List<GitCommit>>,
    ): Map<JiraIssue, List<GitCommit>> {
        val noJiraIssue = JiraIssue(
            key = "NO-JIRA",
            title = "Commits not associated with any JIRA issues",
            body = "",
            status = ""
        )
        return coroutineScope {
            jiraMap.map { (jiraIssueId, commits) ->
                async {
                    val issue = if (jiraIssueId == "NO-JIRA") {
                        noJiraIssue
                    } else {
                        getIssueById(jiraIssueId) ?: noJiraIssue
                    }
                    issue to commits
                }
            }.awaitAll().toMap()
        }
    }

    /**
     * Fetches a JIRA issue by its ID.
     *
     * @param issueId The ID of the JIRA issue to fetch.
     * @return A [JiraIssue] object containing the details of the fetched issue.
     */
    suspend fun getIssueById(issueId: String): JiraIssue? {
        val response = internalClient.get("issue/$issueId?fields=summary,description,status")
        if (response.status != HttpStatusCode.OK) {
            println("WARN: Failed to fetch details for JIRA issue '$issueId'. HTTP status: ${response.status}")
            return null
        }
        val apiResponse = response.body() as JiraApiResponse
        return apiResponse.toJiraIssue()
    }
}
