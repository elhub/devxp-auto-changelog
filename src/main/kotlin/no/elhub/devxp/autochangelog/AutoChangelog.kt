import no.elhub.devxp.autochangelog.toGitCommits
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import picocli.CommandLine
import picocli.CommandLine.Command
import kotlin.system.exitProcess
import kotlinx.coroutines.runBlocking
import no.elhub.devxp.autochangelog.JiraClient
import no.elhub.devxp.autochangelog.extractJiraIssuesIdsFromCommits
import no.elhub.devxp.autochangelog.model.GitCommit
import no.elhub.devxp.autochangelog.model.JiraIssue
import no.elhub.devxp.autochangelog.printJiraIssues
import no.elhub.devxp.autochangelog.toGitTags

@Command(
    name = "auto-changelog",
    mixinStandardHelpOptions = true,
    description = ["TODO: WRITE ME"]
)
object AutoChangelog : Runnable {
    @CommandLine.Option(
        names = ["-j", "--json"],
        description = ["Output json"]
    )
    var outputJson: Boolean = false

    override fun run() {
        val repo: Repository = FileRepositoryBuilder()
            .findGitDir()
            .build()

        val git = Git(repo)

        val rawTags = git.tagList().call().toList()
        val tags = toGitTags(rawTags)

        val rawCommits = git.log().call().toList().reversed()
        val commits = toGitCommits(rawCommits, tags)

        val jiraIssueIds = extractJiraIssuesIdsFromCommits(commits)

        val client = JiraClient()

        val jiraMap: Map<JiraIssue, List<GitCommit>>

        runBlocking {
            jiraMap = client.populateJiraMap(jiraIssueIds, client)
        }
        printJiraIssues(jiraMap)
    }
}

fun main(args: Array<String>) {
    val exitCode = CommandLine(AutoChangelog).execute(*args)
    exitProcess(exitCode)
}
