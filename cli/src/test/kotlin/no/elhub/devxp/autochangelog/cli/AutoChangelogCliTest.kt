package no.elhub.devxp.autochangelog.cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.elhub.devxp.autochangelog.cli.AutoChangelog
import picocli.CommandLine
import kotlin.io.path.ExperimentalPathApi

@OptIn(ExperimentalPathApi::class)
class AutoChangelogCliTest : FunSpec({
    val cmd = CommandLine(AutoChangelog)

    context("AutoChangelog application") {

        test("should exit with an error when run without arguments") {
            cmd.execute() shouldNotBe 0
        }

        test("should have a help option on -h and --help") {
            cmd.execute("-h") shouldBe 0
            cmd.execute("--help") shouldBe 0
        }
    }
})
