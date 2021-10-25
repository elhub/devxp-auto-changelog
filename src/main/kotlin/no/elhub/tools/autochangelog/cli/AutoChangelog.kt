package no.elhub.tools.autochangelog.cli

import no.elhub.tools.autochangelog.io.ChangelogReader
import no.elhub.tools.autochangelog.io.ChangelogWriter
import no.elhub.tools.autochangelog.project.GitRepo
import org.eclipse.jgit.api.Git
import picocli.CommandLine
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.Callable
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.exists
import kotlin.system.exitProcess

@ExperimentalPathApi
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
        description = ["Path to directory with git repository.", "Defaults to '.'"]
    )
    private var repoPath: String = "."

    @CommandLine.Option(
        names = ["-n", "--changelog-name"],
        required = false,
        description = ["Input changelog file name.", "Defaults to 'CHANGELOG.md'"]
    )
    private var inputFileName: String = "CHANGELOG.md"

    @CommandLine.Option(
        names = ["-o", "--output-dir"],
        required = false,
        description = ["Output directory path to which changelog file will be written.", "Defaults to '.'"]
    )
    private var outputDir: String = "."

    @CommandLine.Option(
        names = ["-f", "--file-name"],
        required = false,
        description = ["Output file name.", "Defaults to 'CHANGELOG.md'"]
    )
    private var outputFileName: String = "CHANGELOG.md"

    override fun call(): Int {
        val git = Git.open(File(repoPath))
        val repo = GitRepo(git)
        val changelogFile = Paths.get(repoPath).resolve(inputFileName)
        val content = if (changelogFile.exists()) {
            val lastRelease = ChangelogReader(changelogFile).getLastRelease()
            val end = lastRelease?.let { repo.findCommitId(it) }
            val changelist = repo.createChangelist(repo.constructLog(end = end))
            ChangelogWriter(changelogFile).writeToString(changelist)
        } else {
            val changelist = repo.createChangelist(repo.constructLog())
            ChangelogWriter().writeToString(changelist)
        }

        File("$outputDir/$outputFileName")
            .apply { if (!exists()) createNewFile() }
            .writer().use {
                it.write(content)
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

@OptIn(ExperimentalPathApi::class)
@Suppress("SpreadOperator")
fun main(args: Array<String>): Unit = exitProcess(
    CommandLine(AutoChangelog).execute(*args)
)
