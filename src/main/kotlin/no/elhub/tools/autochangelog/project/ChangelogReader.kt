package no.elhub.tools.autochangelog.project

import java.nio.file.Path
import java.util.regex.Pattern
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.bufferedReader

class ChangelogReader(private val changelogPath: Path) {
    private val releaseHeaderRegex = Pattern.compile(
        """
        ^## \[($versionPattern)] - \d{4}-\d{2}-\d{2}
    """.trimIndent()
    )

    @OptIn(ExperimentalPathApi::class)
    fun read(): Changelog {
        val builder = Changelog.Builder()
        changelogPath.bufferedReader().useLines {
            it.forEach { s ->
                val matcher = releaseHeaderRegex.matcher(s)
                if (matcher.matches()) {
                    builder.withLastRelease(Version(matcher.group(1).toString()))
                    return@useLines
                }
            }
        }
        return builder.build()
    }
}
