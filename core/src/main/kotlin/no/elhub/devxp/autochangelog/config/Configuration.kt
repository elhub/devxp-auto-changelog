package no.elhub.devxp.autochangelog.config

object Configuration {
    // TODO make configurable
    /**
     * Includes only commits with linked jira issues
     */
    const val INCLUDE_ONLY_WITH_JIRA: Boolean = false

    // TODO this needs to be configurable
    const val GIT_REMOTE_NAME: String = "origin"

    // TODO this needs to be configurable
    const val GIT_DEFAULT_BRANCH_NAME: String = "main"
}
