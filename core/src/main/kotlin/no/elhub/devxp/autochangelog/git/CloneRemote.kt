package no.elhub.devxp.autochangelog.git

import java.io.File

fun bareClone(repoUrl: String, targetDirectory: String) {
    val directory = File(targetDirectory)
    if (!directory.exists()) {
        directory.mkdirs()

        // Do a bare clone of the repository
        val process = ProcessBuilder("git", "clone", "--bare", "--filter=blob:none", repoUrl, targetDirectory)
            .redirectErrorStream(true)
            .start()
        if (process.waitFor() != 0) {
            error(process.errorStream.bufferedReader().readText())
        }
    }
}
