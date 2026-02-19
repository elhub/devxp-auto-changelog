package no.elhub.devxp.autochangelog

import kotlinx.coroutines.runBlocking
import no.elhub.devxp.autochangelog.features.git.GitCommit
import no.elhub.devxp.autochangelog.features.git.GithubClient
import no.elhub.devxp.autochangelog.features.git.extractCurrentAndPreviousTag
import no.elhub.devxp.autochangelog.features.git.extractJiraIssuesIdsFromCommits
import no.elhub.devxp.autochangelog.features.git.getRelevantCommits
import no.elhub.devxp.autochangelog.features.git.getTagsFromRepo
import no.elhub.devxp.autochangelog.features.git.initRepository
import no.elhub.devxp.autochangelog.features.jira.JiraClient
import no.elhub.devxp.autochangelog.features.jira.JiraIssue
import no.elhub.devxp.autochangelog.features.writer.formatCommitJson
import no.elhub.devxp.autochangelog.features.writer.formatCommitMarkdown
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
class AutoChangelog(private val jiraClient: JiraClient, private val githubClient: GithubClient) : Runnable {
    @CommandLine.Option(
        names = ["--working-dir", "-w"],
        required = false,
        description = ["The directory in which to run the changelog (mainly for testing)"]
    )
    var workingDir: String = "."

    @CommandLine.Option(
        names = ["--changelog-name"],
        required = false,
        description = ["Overrides the name of the generated changelog. Relevant file extension is still added."]
    )
    var customChangelogName: String? = null

    @CommandLine.Option(
        names = ["--for-tag", "--for"],
        required = false,
        description = ["Generate a changelog for a specific tag, comparing it to the previous tag"]
    )
    var forTagName: String? = null

    @CommandLine.Option(
        names = ["--tag-regex"],
        required = false,
        description = ["Regex to compare tags when looking for the previous tag. Only used if --for-tag is set."]
    )
    var tagRegex: String? = null

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
        names = ["-j", "--json"],
        required = false,
        description = ["Whether to output changelog in JSON format instead of Markdown"]
    )
    var json: Boolean = false

    @CommandLine.Option(
        names = ["--group-by-commit"],
        required = false,
        description = ["Whether to group the changelog entries by commit instead of JIRA issue"]
    )
    var commitGrouping: Boolean = false

    @CommandLine.Option(
        names = ["--strikethrough-done-issues"],
        required = false,
        description = ["Whether to strikethough JIRA issues with status 'Done' in the generated changelog (only applies to Markdown output)"]
    )
    var strikethrough: Boolean = false

    @CommandLine.Option(
        names = ["--include-pr-description-issues"],
        required = false,
        description = [
            "Whether to include JIRA issues found in PR descriptions in addition to those found in commit messages.",
            "This may result in more complete changelogs, but also significantly increases the time it takes to generate the changelog,",
            "especially for repositories with many commits and PRs",
            "Use with --for-tag to limit the number of commits/PRs that need to be processed."
        ]
    )
    var includeDescriptionJira: Boolean = false

    override fun run() {
        val gitRepository = initRepository(workingDir)

        val tags = getTagsFromRepo(gitRepository)

        val (maybeFromTag, maybeToTag) =
            if (forTagName != null) {
                extractCurrentAndPreviousTag(tags, forTagName, tagRegex)
            } else {
                tags.firstOrNull { it.name == fromTagName } to tags.firstOrNull { it.name == toTagName }
            }
        println("Generating changelog from '${maybeFromTag?.name ?: "START"}' to '${maybeToTag?.name ?: "END"}'")

        val relevantCommits = getRelevantCommits(gitRepository, maybeFromTag, maybeToTag, tags)
        println("Found ${relevantCommits.size} relevant commits.")

        if (includeDescriptionJira) {
            println("Populating JIRA issue details for the relevant commits based on commit messages and PR descriptions...")
            runBlocking {
                githubClient.populateJiraIssuesFromDescription(gitRepository, relevantCommits)
            }
        }

        // Create a mapping of JIRA issue IDs to commits
        val jiraIssueIds = extractJiraIssuesIdsFromCommits(relevantCommits)

        // Populate the map with actual JIRA issue details
        val jiraMap: Map<JiraIssue, List<GitCommit>>
        runBlocking {
            jiraMap = jiraClient.getIssueDetails(jiraIssueIds)
        }

        val changelogName = customChangelogName ?: run {
            val tagsSuffix = if (maybeFromTag != null || maybeToTag != null) {
                " [${maybeFromTag?.name ?: ""}-${maybeToTag?.name ?: ""}]"
            } else {
                ""
            }
            "CHANGELOG$tagsSuffix"
        }

        if (commitGrouping) {
            // Reverse the mapping to be GitCommit -> List<JiraIssue>
            val commitMap = jiraMap
                .flatMap { (key, values) -> values.map { it to key } }
                .groupBy({ it.first }, { it.second })
                .toSortedMap((compareByDescending { it.commitTime }))

            if (json) {
                val jsonContent = formatCommitJson(commitMap)
                writeJsonToFile(jsonContent, "$changelogName.json")
            } else {
                val markdownContent = formatCommitMarkdown(commitMap, strikethrough)
                writeMarkdownToFile(markdownContent, "$changelogName.md")
            }
        } else {
            if (json) {
                val jsonContent = formatJson(jiraMap)
                writeJsonToFile(jsonContent, "$changelogName.json")
            } else {
                val markdownContent = formatMarkdown(jiraMap, strikethrough)
                writeMarkdownToFile(markdownContent, "$changelogName.md")
            }
        }
    }
}

fun main(args: Array<String>) {
    val exitCode = CommandLine(AutoChangelog(JiraClient(), GithubClient())).execute(*args)
    exitProcess(exitCode)
}
