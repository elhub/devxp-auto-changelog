package no.elhub.devxp.autochangelog.extensions

import java.io.File
import java.io.FileInputStream

/**
 * Returns a sequence of lines from this [File] receiver
 * after (and including) the line that satisfies the [predicate] function.
 *
 * @throws IllegalArgumentException if this file is not a *normal* file
 */
fun File.linesAfter(predicate: (String) -> Boolean): Sequence<String> {
    require(isFile) { "File '${this.path}' is not a regular file" }
    return sequence {
        FileInputStream(this@linesAfter).use { fis ->
            fis.bufferedReader().useLines {
                var add = false
                it.forEach { s ->
                    if (!add && predicate(s)) add = true
                    if (add) yield(s)
                }
            }
        }
    }
}

/**
 * Returns a list containing first lines from this [File] receiver
 * that satisfy the given [predicate] function.
 *
 * @throws IllegalArgumentException if this file is not a *normal* file
 */
fun File.linesUntil(predicate: (String) -> Boolean): List<String> {
    require(isFile) { "File '${this.path}' is not a regular file" }
    return FileInputStream(this@linesUntil).use { fis ->
        fis.bufferedReader().useLines {
            it.takeWhile { s -> !predicate(s) }.toList()
        }
    }
}
