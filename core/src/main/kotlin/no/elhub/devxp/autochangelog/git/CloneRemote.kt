package no.elhub.devxp.autochangelog.git

import java.io.File
import java.io.IOException

fun bareClone(repoUrl: String, targetDirectory: String) {
    val directory = File(targetDirectory)

    try {
        // Do a bare clone of the repository
        val process = ProcessBuilder("git", "clone", "--bare", "--filter=blob:none", repoUrl, targetDirectory)
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            println("Git clone failed with exit code $exitCode")
            println("Error output: $output")
            throw IOException("Failed to clone repository from $repoUrl: $output")
        }
    } catch (e: Exception) {
        println("Error cloning repository: ${e.message}")
        throw IOException("Failed to clone repository from $repoUrl", e)
    }
}
