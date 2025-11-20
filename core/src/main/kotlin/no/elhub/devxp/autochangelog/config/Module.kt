package no.elhub.devxp.autochangelog.config

import org.koin.dsl.module
import java.net.http.HttpClient
import java.time.Duration

val appModule = module {
    single<HttpClient> {
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()
    }
}
