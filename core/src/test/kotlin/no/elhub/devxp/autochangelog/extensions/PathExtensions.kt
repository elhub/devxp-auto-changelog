package no.elhub.devxp.autochangelog.extensions

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.isDirectory

/**
 * Walks `this` [Path] and deletes all the contents.
 * If `this` receiver [Path] is not a directory - deletes the file itself.
 */
@OptIn(ExperimentalPathApi::class)
fun Path.delete() {
    if (this.isDirectory()) {
        Files.walkFileTree(
            this,
            object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes?): FileVisitResult {
                    Files.delete(file)
                    return FileVisitResult.CONTINUE
                }

                override fun postVisitDirectory(dir: Path, e: IOException?): FileVisitResult = if (e == null) {
                    Files.delete(dir)
                    FileVisitResult.CONTINUE
                } else {
                    // directory iteration failed
                    throw e
                }
            }
        )
    } else {
        Files.delete(this)
    }
}
