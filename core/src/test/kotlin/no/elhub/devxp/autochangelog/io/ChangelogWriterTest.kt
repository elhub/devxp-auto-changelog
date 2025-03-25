package no.elhub.devxp.autochangelog.io

import io.kotest.assertions.json.shouldBeValidJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.elhub.devxp.autochangelog.project.defaultContent

class ChangelogWriterTest : FunSpec({
    context("ChangelogWriter") {

        context("write with non-empty original changelog contents") {
            val writer = ChangelogWriter(start = defaultDescription, end = existingContent)

            test("should append new content after default description") {
                val s = writer.writeToString(singleChangelist)
                s shouldBe """
                    |$defaultDescription
                    |
                    |$expectedChangelogContent
                    |
                    |$existingContent
                """.trimMargin()
            }
        }

        context("write with empty original changelog contents") {
            val writer = ChangelogWriter()

            test("should append new content to default description") {
                val s = writer.writeToString(singleChangelist)
                s shouldBe """
                    |${defaultContent.joinToString("\n")}
                    |
                    |$expectedChangelogContent
                """.trimMargin()
            }

            test("writeToJson() should produce valid json") {
                val s = writer.writeToJson(singleChangelist)
                s.shouldBeValidJson()
            }

            listOf(singleChangeLogTest, doubleChangeLogTest).map {
                test("should produce valid json") {
                    writer.writeToJson(it.first) shouldBe it.second
                }
            }
        }
    }
})
