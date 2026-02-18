package no.elhub.devxp.autochangelog.features.git

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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.eclipse.jgit.api.Git

class GithubClient(
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

        val token = System.getenv("GITHUB_TOKEN")

        check(!token.isNullOrBlank()) { "GITHUB_TOKEN environment variable is not set." }

        defaultRequest {
            url("https://api.github.com/")
            header("Authorization", "Bearer $token")
            header("Accept", "application/json")
        }
    }

    suspend fun populateJiraIssuesFromDescription(git: Git, commits: List<GitCommit>) {
        val (owner, repo) = getRepoInfo(git) ?: error("Could not determine repository information from Git configuration.")

        val semaphore = Semaphore(10)
        coroutineScope {
            commits.map { commit ->
                async {
                    semaphore.withPermit {
                        val description = getPrDescription(owner, repo, commit.hash)
                        commit.jiraIssues = commit.jiraIssues.plus(extractJiraIssues(description)).distinct()
                    }
                }
            }.awaitAll()
        }
    }

    fun getRepoInfo(git: Git): Pair<String, String>? {
        val url = git.repository.config.getString("remote", "origin", "url") ?: return null
        val httpsRegex = Regex("""https://github\.com/([^/]+)/([^/.]+)(\.git)?""")
        return when {
            httpsRegex.matches(url) -> {
                val match = httpsRegex.find(url)!!
                match.groupValues[1] to match.groupValues[2]
            }

            else -> null
        }
    }

    suspend fun getPrDescription(owner: String, repo: String, sha: String): String {
        val response = internalClient.get("repos/$owner/$repo/commits/$sha/pulls") {
            header("Accept", "application/vnd.github.v3+json")
        }
        if (response.status != HttpStatusCode.OK) {
            return ""
        }
        val json = response.body<List<JsonObject>>()
        val descriptionBody = json.firstOrNull()?.get("body")?.jsonPrimitive?.content ?: ""
        return descriptionBody
    }
}
