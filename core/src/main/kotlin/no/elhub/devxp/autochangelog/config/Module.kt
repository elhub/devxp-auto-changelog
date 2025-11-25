package no.elhub.devxp.autochangelog.config

import no.elhub.devxp.autochangelog.jira.JiraIssueExtractor
import no.elhub.devxp.autochangelog.util.SystemEnvironmentProvider
import org.koin.dsl.module
import java.net.http.HttpClient
import java.time.Duration

val appModule = module {
    single {
        JiraIssueExtractor(
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
            SystemEnvironmentProvider(),
        )
    }
}
