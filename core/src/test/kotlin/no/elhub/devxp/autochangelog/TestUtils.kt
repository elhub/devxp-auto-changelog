package no.elhub.devxp.autochangelog

import io.mockk.every
import io.mockk.mockk
import no.elhub.devxp.autochangelog.jira.JiraIssueExtractor
import no.elhub.devxp.autochangelog.util.EnvironmentProvider
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun mockHttpClient(
    httpStatusCode: Int = 200,
    responseBody: String = """{"key":"PROJ-1","fields":{"summary":"Title","description":"This is a description."}}""",
): HttpClient {
    val mockHttpClient = mockk<HttpClient>()
    val mockResponse = mockk<HttpResponse<String>>()
    every { mockResponse.statusCode() } returns httpStatusCode
    every { mockResponse.body() } returns responseBody
    every { mockHttpClient.send(any<HttpRequest>(), any<HttpResponse.BodyHandler<String>>()) } returns mockResponse
    return mockHttpClient
}

fun mockEnvironmentProvider(
    username: String? = null,
    token: String? = null
): EnvironmentProvider {
    val mockEnvironmentProvider = mockk<EnvironmentProvider>()
    every { mockEnvironmentProvider.getEnv("JIRA_USERNAME") } returns username
    every { mockEnvironmentProvider.getEnv("JIRA_EMAIL") } returns null
    every { mockEnvironmentProvider.getEnv("JIRA_TOKEN") } returns token
    every { mockEnvironmentProvider.getEnv("JIRA_API_TOKEN") } returns null
    return mockEnvironmentProvider
}

fun jiraIssueExtractor(
    httpClient: HttpClient = mockHttpClient(),
    environmentProvider: EnvironmentProvider = mockEnvironmentProvider(),
) = JiraIssueExtractor(
    httpClient,
    environmentProvider
)
