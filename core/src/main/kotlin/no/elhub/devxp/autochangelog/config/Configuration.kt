package no.elhub.devxp.autochangelog.config

object Configuration {

    // TODO make configurable
    private const val JIRA_URL = "https://elhub.atlassian.net/jira"

    const val JIRA_ISSUES_URL = "$JIRA_URL/browse"

    // TODO make configurable
    /**
     * Includes only commits with linked jira issues
     */
    const val INCLUDE_ONLY_WITH_JIRA: Boolean = true

    /**
     * Adds a link for jira issues in the resulting markdown
     */
    const val INCLUDE_JIRA_LINKS: Boolean = true

    const val JIRA_ISSUES_PATTERN_STRING: String = "Issue ID(s): "

    // TODO this needs to be configurable
    const val GIT_REMOTE_NAME: String = "origin"

    // TODO this needs to be configurable
    const val GIT_DEFAULT_BRANCH_NAME: String = "main"
}
