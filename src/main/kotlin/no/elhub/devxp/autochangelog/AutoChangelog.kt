package no.elhub.devxp.autochangelog

import java.io.File
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
        names = ["-o", "--output-file-path"],
        required = false,
        description = ["The output file path for the changelog file"]
    )
    var outputFilePath: String = "."

    override fun run() {
        val workingDirectory = File(workingDir)
        require(workingDirectory.exists()) { "Working directory $workingDirectory does not exist." }
        require(workingDirectory.isDirectory) { "Working directory $workingDirectory is not a directory." }
        require(workingDirectory.resolve(".git").exists()) { "Working directory $workingDirectory does not contain a .git directory." }

        val repo: Repository = FileRepositoryBuilder()
            .setWorkTree(workingDirectory)
            .build()

        val git = Git(repo)

        val rawTags = git.tagList().call().toList()
        val tags = toGitTags(rawTags)

        val maybeFromTag = tags.firstOrNull() { it.name == fromTagName }
        val maybeToTag = tags.firstOrNull() { it.name == toTagName }

        val rawCommits = git.log().call().toList().reversed()
        val commits = toGitCommits(rawCommits, tags)

        val relevantCommits = getCommitsBetweenTags(commits, maybeFromTag, maybeToTag)

        val jiraIssueIds = extractJiraIssuesIdsFromCommits(relevantCommits)

        val client = JiraClient()

        val jiraMap: Map<JiraIssue, List<GitCommit>>

        runBlocking {
            jiraMap = client.populateJiraMap(jiraIssueIds, client)
        }

        val md = formatMarkdown(jiraMap)

        val changeLogSuffix = if (maybeFromTag != null || maybeToTag != null) {
            " [${maybeFromTag?.name ?: ""}-${maybeToTag?.name ?: ""}]"
        } else {
            ""
        }
        writeMarkdownToFile(md, "$outputFilePath/CHANGELOG$changeLogSuffix.md")
    }
}

fun main(args: Array<String>) {
    val exitCode = CommandLine(AutoChangelog).execute(*args)
    exitProcess(exitCode)
}
