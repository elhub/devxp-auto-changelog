import no.elhub.devxp.autochangelog.toGitCommits
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import picocli.CommandLine
import picocli.CommandLine.Command
import kotlin.system.exitProcess

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

        val tags = git.tagList().call().toList()

        val rawCommits = git.log().call().toList()
        val commits = toGitCommits(rawCommits, tags)

        commits
            .filter { it.jiraIssues.isNotEmpty() }
            .take(3)
            .forEach {
                println("Hash: ${it.hash}")
                println("Title: ${it.title}")
                println("Date: ${it.date}")
                println("Tag: ${it.tag?.name ?: "No Tag"}")
                println("JiraIssues: ${it.jiraIssues.joinToString(", ")}")
                println("-----")
            }
    }
}

fun main(args: Array<String>) {
    val exitCode = CommandLine(AutoChangelog).execute(*args)
    exitProcess(exitCode)
}
