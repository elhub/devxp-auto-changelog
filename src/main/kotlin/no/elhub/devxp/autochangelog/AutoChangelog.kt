package no.elhub.devxp.autochangelog

import kotlin.system.exitProcess
import kotlinx.coroutines.runBlocking
import no.elhub.devxp.autochangelog.features.git.GitCommit
import no.elhub.devxp.autochangelog.features.git.getCommitsBetweenTags
import no.elhub.devxp.autochangelog.features.git.toGitCommits
import no.elhub.devxp.autochangelog.features.git.toGitTags
import no.elhub.devxp.autochangelog.features.jira.JiraClient
import no.elhub.devxp.autochangelog.features.jira.JiraIssue
import no.elhub.devxp.autochangelog.features.jira.extractJiraIssuesIdsFromCommits
import no.elhub.devxp.autochangelog.features.writer.formatMarkdown
import no.elhub.devxp.autochangelog.features.writer.writeMarkdownToFile
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import picocli.CommandLine
import picocli.CommandLine.Command

@Command(
    name = "auto-changelog",
    mixinStandardHelpOptions = true,
    description = ["TODO: WRITE ME"]
)
object AutoChangelog : Runnable {
    @CommandLine.Option(
        names = ["--from-tag", "--from"],
        required = true,
        description = ["The tag to start the changelog from (exclusive)"]
    )
    var fromTagName: String? = null

    @CommandLine.Option(
        names = ["--to-tag", "--to"],
        required = true,
        description = ["The tag to end the changelog at (inclusive)"]
    )
    var toTagName: String? = null

    @CommandLine.Option(
        names = ["-o", "--output-file-path"],
        required = false,
        description = ["The output file path for the changelog file"]
    )
    var outputFilePath: String = "."

    override fun run() {
        val repo: Repository = FileRepositoryBuilder()
            .findGitDir()
            .build()

        val git = Git(repo)

        val rawTags = git.tagList().call().toList()
        val tags = toGitTags(rawTags)

        val fromTag = tags.firstOrNull() { it.name == fromTagName }
        require(fromTag != null) { "Tag '$fromTagName' does not exist." }

        val toTag = tags.firstOrNull() { it.name == toTagName }
        require(toTag != null) { "Tag '$toTagName' does not exist." }

        val rawCommits = git.log().call().toList().reversed()
        val commits = toGitCommits(rawCommits, tags)

        val relevantCommits = getCommitsBetweenTags(commits, fromTag, toTag)

        val jiraIssueIds = extractJiraIssuesIdsFromCommits(relevantCommits)

        val client = JiraClient()

        val jiraMap: Map<JiraIssue, List<GitCommit>>

        runBlocking {
            jiraMap = client.populateJiraMap(jiraIssueIds, client)
        }

        val md = formatMarkdown(jiraMap)
        writeMarkdownToFile(md, "$outputFilePath/CHANGELOG [${fromTag.name}-${toTag.name}].md")
    }
}

fun main(args: Array<String>) {
    val exitCode = CommandLine(AutoChangelog).execute(*args)
    exitProcess(exitCode)
}
