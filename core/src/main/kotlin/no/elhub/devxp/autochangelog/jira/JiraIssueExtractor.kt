package no.elhub.devxp.autochangelog.jira

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Base64
import java.util.regex.Pattern
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import no.elhub.devxp.autochangelog.extensions.description
import no.elhub.devxp.autochangelog.extensions.title
import org.eclipse.jgit.revwalk.RevCommit

object JiraIssueExtractor {
    private const val JIRA_BASE_URL: String = "https://elhub.atlassian.net"
    private val JIRA_EMAIL: String = System.getenv("JIRA_EMAIL")
    private val JIRA_API_TOKEN: String = System.getenv("JIRA_API_TOKEN")
    private val AUTH_STRING = "$JIRA_EMAIL:$JIRA_API_TOKEN"
    private val encodedAuth = Base64.getEncoder().encodeToString(AUTH_STRING.toByteArray())

    // JIRA issue pattern (e.g., PROJECT-123)
    private val jiraRegex = "([A-Z][A-Z0-9_]+-[0-9]+)".toRegex()

    // HTTP client for JIRA API requests
    private val httpClient = HttpClient.newBuilder().build()

    fun extractJiraIssueFromCommit(commit: RevCommit): String? {
        val jiraIssue = jiraRegex.find(commit.title) ?: jiraRegex.find(commit.description.joinToString(" "))
        return fetchJiraIssueTitle(jiraIssue?.value)
    }

    private fun fetchJiraIssueTitle(issue: String?): String? {
        if (issue == null) return null
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$JIRA_BASE_URL/rest/api/3/issue/$issue?fields=summary"))
            .header("Authorization", "Basic $encodedAuth")
            .header("Accept", "application/json")
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 200) {
            val jsonParser = Json { ignoreUnknownKeys = true }
            val jsonObject = jsonParser.parseToJsonElement(response.body()).jsonObject
            val fields = jsonObject["fields"]?.jsonObject
            val summary = fields?.get("summary")?.jsonPrimitive?.content

            require(summary != null) { "No title found!" }
            return "$issue: $summary"

        } else {
            error("Error: HTTP ${response.statusCode()}")
        }
    }
}
