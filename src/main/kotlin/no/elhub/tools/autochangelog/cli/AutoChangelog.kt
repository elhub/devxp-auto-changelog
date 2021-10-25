package no.elhub.tools.autochangelog.cli

import no.elhub.tools.autochangelog.io.ChangelogWriter
import no.elhub.tools.autochangelog.project.GitRepo
import org.eclipse.jgit.api.Git
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@CommandLine.Command(
    name = "auto-changelog",
    mixinStandardHelpOptions = true,
    description = ["auto-changelog ."],
    optionListHeading = "@|bold %nOptions|@:%n",
    versionProvider = ManifestVersionProvider::class,
    sortOptions = false
)
object AutoChangelog : Callable<Int> {

    @CommandLine.Option(
        names = ["-d", "--dir-path"],
        required = false,
        description = ["Path to directory with git repository.", "Defaults to '.'"],
        defaultValue = "."
    )
    private var repoPath: String = "."

    @CommandLine.Option(
        names = ["-f", "--file-name"],
        required = false,
        description = ["Output file name.", "Defaults to 'CHANGELOG.md'"],
        defaultValue = "CHANGELOG.md"
    )
    private var outputFileName: String = "CHANGELOG.md"

    @CommandLine.Option(
        names = ["-o", "--output-dir"],
        required = false,
        description = ["Output directory path where changelog file will be written.", "Defaults to '.'"],
        defaultValue = "."
    )
    private var outputDir: String = "."

    override fun call(): Int {
        val git = Git.open(File(repoPath))
        val repo = GitRepo(git)
        val changelist = repo.createChangelist(repo.constructLog())
        File("$outputDir/$outputFileName").writer().use {
            it.write(ChangelogWriter().writeToString(changelist))
            it.flush()
        }
        return 0
    }
}

object ManifestVersionProvider : CommandLine.IVersionProvider {

    @Throws(Exception::class)
    override fun getVersion(): Array<String> {
        return arrayOf(CommandLine::class.java.`package`.implementationVersion.toString())
    }

}

@Suppress("SpreadOperator")
fun main(args: Array<String>): Unit = exitProcess(
    CommandLine(AutoChangelog).execute(*args)
)
