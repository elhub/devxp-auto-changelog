package no.elhub.devxp.autochangelog

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Logger {
    private val log: Logger = LoggerFactory.getLogger("AutoChangelog")

    fun info(msg: String) = log.info(msg)
    fun debug(msg: String) = log.debug(msg)
    fun warn(msg: String) = log.warn(msg)
    fun error(msg: String, e: Throwable? = null) = log.error(msg, e)
}
