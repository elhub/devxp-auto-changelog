package no.elhub.tools.autochangelog.config

object Configuration {

    // TODO make configurable
    private const val jiraUrl = "https://jira.elhub.cloud"

    const val jiraIssuesUrl = "$jiraUrl/browse"

    // TODO make configurable
    /**
     * Includes only commits with linked jira issues
     */
    const val includeOnlyWithJira: Boolean = true

    /**
     * Adds a link for jira issues in the resulting markdown
     */
    const val includeJiraLinks: Boolean = true

    const val jiraIssuesPatternString: String = "JIRA Issues: "
}
