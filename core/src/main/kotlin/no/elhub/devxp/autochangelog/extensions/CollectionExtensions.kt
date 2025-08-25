package no.elhub.devxp.autochangelog.extensions

/**
 * Returns this list of [T] element minus the `head` element.
 */
fun <T> List<T>.tail(): List<T> = if (this.isEmpty()) emptyList() else this.takeLast(size - 1)
