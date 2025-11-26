package no.elhub.devxp.autochangelog.util

interface EnvironmentProvider {
    fun getEnv(key: String): String?
}

class SystemEnvironmentProvider : EnvironmentProvider {
    override fun getEnv(key: String): String? = System.getenv(key)
}
