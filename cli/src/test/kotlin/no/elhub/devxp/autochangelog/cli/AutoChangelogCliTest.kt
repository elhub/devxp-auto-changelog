package no.elhub.devxp.autochangelog.cli

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.elhub.devxp.autochangelog.cli.AutoChangelog
import picocli.CommandLine
import kotlin.io.path.ExperimentalPathApi

@OptIn(ExperimentalPathApi::class)
class AutoChangelogCliTest : DescribeSpec({
    val cmd = CommandLine(AutoChangelog)

    describe("AutoChangelog application") {

        it("should exit with an error when run without arguments") {
            cmd.execute() shouldNotBe 0
        }

        it("should have a help option on -h and --help") {
            cmd.execute("-h") shouldBe 0
            cmd.execute("--help") shouldBe 0
        }
    }
})
