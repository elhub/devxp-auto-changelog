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

    // Jira related configuration
    const val JIRA_BASE_URL: String = "https://elhub.atlassian.net"
    const val JIRA_ISSUES_URL: String = "$JIRA_BASE_URL/browse"
    const val JIRA_API_PATH: String = "/rest/api/3"
}
