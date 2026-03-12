package no.elhub.devxp.autochangelog

import ch.qos.logback.classic.Level.DEBUG
import ch.qos.logback.classic.Level.INFO
import ch.qos.logback.classic.LoggerContext
import org.slf4j.Logger
import org.slf4j.Logger.ROOT_LOGGER_NAME
import org.slf4j.LoggerFactory

object Logger {
    private val log: Logger = LoggerFactory.getLogger("AutoChangelog")

    fun setLevel(debug: Boolean) {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        context.getLogger("AutoChangelog").level = if (debug) DEBUG else INFO
    }

    fun info(msg: String) = log.info(msg)
    fun debug(msg: String) = log.debug(msg)
    fun warn(msg: String) = log.warn(msg)
    fun error(msg: String, e: Throwable? = null) = log.error(msg, e)
}
