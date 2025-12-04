package no.elhub.devxp.autochangelog

import kotlinx.coroutines.runBlocking
import no.elhub.devxp.autochangelog.features.git.GitCommit
import no.elhub.devxp.autochangelog.features.git.extractJiraIssuesIdsFromCommits
import no.elhub.devxp.autochangelog.features.git.getRelevantCommits
import no.elhub.devxp.autochangelog.features.git.getTagsFromRepo
import no.elhub.devxp.autochangelog.features.git.initRepository
import no.elhub.devxp.autochangelog.features.jira.JiraClient
import no.elhub.devxp.autochangelog.features.jira.JiraIssue
import no.elhub.devxp.autochangelog.features.writer.formatJson
import no.elhub.devxp.autochangelog.features.writer.formatMarkdown
import no.elhub.devxp.autochangelog.features.writer.writeJsonToFile
import no.elhub.devxp.autochangelog.features.writer.writeMarkdownToFile
import picocli.CommandLine
import picocli.CommandLine.Command
import kotlin.system.exitProcess

@Command(
    name = "auto-changelog",
    mixinStandardHelpOptions = true,
    description = ["Generates changelogs based on JIRA issues for a given repository."]
)
class AutoChangelog(private val client: JiraClient) : Runnable {
    @CommandLine.Option(
        names = ["--working-dir", "-w"],
        required = false,
        description = ["The directory in which to run the changelog (mainly for testing)"]
    )
    var workingDir: String = "."

    @CommandLine.Option(
        names = ["--from-tag", "--from"],
        required = false,
        description = ["The tag to start the changelog from (exclusive)"]
    )
    var fromTagName: String? = null

    @CommandLine.Option(
        names = ["--to-tag", "--to"],
        required = false,
        description = ["The tag to end the changelog at (inclusive)"]
    )
    var toTagName: String? = null

    @CommandLine.Option(
        names = ["-j", "-json"],
        required = false,
        description = ["Whether to output changelog in JSON format instead of Markdown"]
    )
    var json: Boolean = false

    override fun run() {
        val gitRepository = initRepository(workingDir)

        val tags = getTagsFromRepo(gitRepository)

        val maybeFromTag = tags.firstOrNull { it.name == fromTagName }
        val maybeToTag = tags.firstOrNull { it.name == toTagName }

        val relevantCommits = getRelevantCommits(gitRepository, maybeFromTag, maybeToTag, tags)

        // Create a mapping of JIRA issue IDs to commits
        val jiraIssueIds = extractJiraIssuesIdsFromCommits(relevantCommits)

        // Populate the map with actual JIRA issue details
        val jiraMap: Map<JiraIssue, List<GitCommit>>
        runBlocking {
            jiraMap = client.populateJiraMap(jiraIssueIds)
        }

        val changelogName = "CHANGELOG"
        val changeLogFileSuffix = if (maybeFromTag != null || maybeToTag != null) {
            " [${maybeFromTag?.name ?: ""}-${maybeToTag?.name ?: ""}]"
        } else {
            ""
        }

        if (json) {
            val jsonContent = formatJson(jiraMap)
            writeJsonToFile(jsonContent, "$changelogName$changeLogFileSuffix.json")
        } else {
            val markdownContent = formatMarkdown(jiraMap)
            writeMarkdownToFile(markdownContent, "$changelogName$changeLogFileSuffix.md")
        }
    }
}

fun main(args: Array<String>) {
    val exitCode = CommandLine(AutoChangelog(JiraClient())).execute(*args)
    exitProcess(exitCode)
}
