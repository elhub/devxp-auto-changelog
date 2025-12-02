package no.elhub.devxp.autochangelog.jira

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import no.elhub.devxp.autochangelog.config.Configuration
import no.elhub.devxp.autochangelog.extensions.description
import no.elhub.devxp.autochangelog.extensions.title
import no.elhub.devxp.autochangelog.util.EnvironmentProvider
import org.eclipse.jgit.revwalk.RevCommit
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

data class JiraIssue(
    val key: String,
    val title: String,
    val description: String?,
    val url: String
)

class JiraConnectionException(message: String, cause: Throwable? = null) : Exception(message, cause)
class JiraAuthenticationException(message: String) : Exception(message)

class JiraIssueExtractor(private val httpClient: HttpClient, private val environmentProvider: EnvironmentProvider) {
    private val jiraBaseUrl = Configuration.JIRA_BASE_URL
    private val jiraIssuesUrl = Configuration.JIRA_ISSUES_URL

    // Cache to avoid fetching the same issue multiple times
    private val issueCache = ConcurrentHashMap<String, JiraIssue>()

    // JIRA issue pattern (e.g., PROJECT-123)
    val jiraRegex = "([A-Z][A-Z0-9_]+-[0-9]+)".toRegex()

    // HTTP client for JIRA API requests with timeout

    private var credentials: Pair<String, String>? = null

    /**
     * Initialize the Jira client with credentials.
     * If credentials are not provided, attempts to read from environment variables.
     * If environment variables are not set, returns false.
     */
    fun initialize(username: String? = null, token: String? = null): Boolean {
        val envUsername = environmentProvider.getEnv("JIRA_USERNAME") ?: environmentProvider.getEnv("JIRA_EMAIL")
        val envToken = environmentProvider.getEnv("JIRA_TOKEN") ?: environmentProvider.getEnv("JIRA_API_TOKEN")

        // Log if environment variables are found
        if (envUsername != null) {
            println("Found JIRA_USERNAME/JIRA_EMAIL in environment variables")
        }
        if (envToken != null) {
            println("Found JIRA_TOKEN/JIRA_API_TOKEN in environment variables")
        }

        credentials = when {
            // Use provided credentials if available
            username != null && token != null -> username to token

            // Otherwise try to read from environment variables
            envUsername != null && envToken != null -> envUsername to envToken

            // No credentials available
            else -> {
                System.err.println("Jira credentials not available. Set JIRA_USERNAME and JIRA_TOKEN environment variables to use Jira integration.")
                return false
            }
        }

        // Test the credentials
        return try {
            testConnection()
            println("Successfully authenticated with Jira")
            true
        } catch (e: JiraAuthenticationException) {
            System.err.println(e.message)
            credentials = null
            false
        } catch (e: Exception) {
            System.err.println("Failed to connect to Jira: ${e.message}")
            credentials = null
            false
        }
    }

    /**
     * Test the connection to Jira using the current credentials.
     */
    private fun testConnection() {
        if (credentials == null) {
            throw JiraAuthenticationException("Jira credentials not set")
        }

        val (username, token) = credentials!!
        val encodedAuth = Base64.getEncoder().encodeToString("$username:$token".toByteArray())

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$jiraBaseUrl/rest/api/3/myself"))
            .header("Authorization", "Basic $encodedAuth")
            .header("Accept", "application/json")
            .GET()
            .build()

        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) {
                if (response.statusCode() == 401) {
                    System.err.println("Authentication error: Your Jira credentials are invalid.")
                    System.err.println("Please check the following:")
                    System.err.println(" - Ensure your token is valid and not expired")
                    System.err.println(" - Check that your username is correct")
                    System.err.println(" - Verify the Jira instance URL ($jiraBaseUrl) is correct")
                    throw JiraAuthenticationException("Failed to authenticate with Jira (HTTP 401 Unauthorized)")
                } else {
                    throw JiraAuthenticationException("Failed to authenticate with Jira (HTTP ${response.statusCode()})")
                }
            }
        } catch (e: Exception) {
            if (e is JiraAuthenticationException) throw e
            throw JiraConnectionException("Error connecting to Jira", e)
        }
    }

    /**
     * Extract all Jira issues from a commit message.
     */
    fun extractJiraIssuesFromCommit(commit: RevCommit): List<String> {
        val issueKeys = mutableListOf<String>()

        // Extract from title
        jiraRegex.findAll(commit.title).forEach { issueKeys.add(it.value) }

        // Extract from description
        commit.description.forEach { line ->
            jiraRegex.findAll(line).forEach { issueKeys.add(it.value) }
        }

        return issueKeys.distinct()
    }

    /**
     * Extract Jira issue from a commit and fetch its details.
     * Returns the first found issue or null if none found.
     */
    fun extractJiraIssueFromCommit(commit: RevCommit): String? {
        val jiraIssue = jiraRegex.find(commit.title) ?: commit.description
            .asSequence()
            .map { jiraRegex.find(it) }
            .firstOrNull { it != null }

        return jiraIssue?.value
    }

    /**
     * Fetch a single Jira issue by its key.
     */
    fun fetchJiraIssue(issueKey: String): JiraIssue? {
        if (credentials == null) {
            return null
        }

        // Check cache first
        if (issueCache.containsKey(issueKey)) {
            return issueCache[issueKey]
        }

        val (username, token) = credentials!!
        val encodedAuth = Base64.getEncoder().encodeToString("$username:$token".toByteArray())

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$jiraBaseUrl/rest/api/3/issue/$issueKey?fields=summary,description"))
            .header("Authorization", "Basic $encodedAuth")
            .header("Accept", "application/json")
            .GET()
            .build()

        return try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            when (response.statusCode()) {
                200 -> {
                    val jsonParser = Json { ignoreUnknownKeys = true }
                    val jsonObject = jsonParser.parseToJsonElement(response.body()).jsonObject
                    val fields = jsonObject["fields"]?.jsonObject
                    val summary = fields?.get("summary")?.jsonPrimitive?.content
                    val description = fields?.get("description")?.jsonPrimitive?.contentOrNull

                    if (summary == null) {
                        println("Warning: Could not parse title for Jira issue $issueKey")
                        null
                    } else {
                        val issue = JiraIssue(
                            key = issueKey,
                            title = summary,
                            description = description,
                            url = "$jiraIssuesUrl/$issueKey"
                        )
                        issueCache[issueKey] = issue
                        issue
                    }
                }

                401, 403 -> {
                    System.err.println("Error: Authentication failed for Jira API (HTTP ${response.statusCode()})")
                    null
                }

                404 -> {
                    System.err.println("Warning: Jira issue $issueKey not found")
                    null
                }

                else -> {
                    System.err.println("Error: Failed to fetch Jira issue $issueKey (HTTP ${response.statusCode()})")
                    null
                }
            }
        } catch (e: Exception) {
            System.err.println("Error fetching Jira issue $issueKey: ${e.message}")
            null
        }
    }

    /**
     * Fetch details for a batch of Jira issues.
     */
    fun fetchJiraIssues(issueKeys: List<String>): Map<String, JiraIssue> =
        issueKeys.mapNotNull { key -> fetchJiraIssue(key)?.let { key to it } }.toMap()

    /**
     * Format Jira issues as a Markdown string.
     */
    fun formatJiraIssuesMarkdown(issues: List<JiraIssue>): String {
        if (issues.isEmpty()) return ""

        return issues.joinToString("\n") { issue ->
            "**[${issue.key}](${issue.url})**: ${issue.title}" +
                (
                    issue.description?.let { "\n  *Description*: ${it.take(100)}${if (it.length > 100) "..." else ""}" }
                        ?: ""
                    )
        }
    }

    /**
     * Check if the extractor is initialized with valid credentials.
     */
    fun isInitialized(): Boolean = credentials != null
}
