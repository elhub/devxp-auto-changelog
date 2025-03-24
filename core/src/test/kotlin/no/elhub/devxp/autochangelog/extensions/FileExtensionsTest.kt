package no.elhub.devxp.autochangelog.extensions

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.sequences.shouldContainExactly
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi

@OptIn(ExperimentalPathApi::class)
class FileExtensionsTest : FunSpec({
    lateinit var file: File

    beforeEach { file = Files.createTempFile("test", "me").toFile() }

    afterEach { file.delete() }

    context("linesAfter") {
        test("should return lines after the predicate is met") {
            val text = """
                one
                two
                three
            """.trimIndent()
            file.writeText(text)
            file.linesAfter { it == "two" } shouldContainExactly sequence { yieldAll(listOf("two", "three")) }
        }

        test("should return empty sequence if the predicate is not met") {
            val text = """
                one
                two
                three
            """.trimIndent()
            file.writeText(text)
            file.linesAfter { it == "none" } shouldContainExactly emptySequence()
        }

        test("should return empty sequence for an empty file") {
            val text = ""
            file.writeText(text)
            file.linesAfter { it == "none" } shouldContainExactly emptySequence()
        }

        test("should throw an exception if the file is a directory") {
            val f = Files.createTempDirectory("test").toFile()
            assertThrows<IllegalArgumentException> { f.linesAfter { it == "none" } }
            f.delete()
        }
    }

    context("takeLines") {
        test("should return lines before the predicate is met") {
            val text = """
                one
                two
                three
                two
                three
            """.trimIndent()
            file.writeText(text)
            file.linesUntil { it == "three" } shouldContainExactly listOf("one", "two")
        }

        test("should return all contents as sequence if the predicate is not met") {
            val text = """
                one
                two
                three
            """.trimIndent()
            file.writeText(text)
            file.linesUntil { it == "none" } shouldContainExactly listOf("one", "two", "three")
        }

        test("should return empty sequence for an empty file") {
            val text = ""
            file.writeText(text)
            file.linesUntil { it == "none" } shouldContainExactly emptyList()
        }

        test("should throw an exception if the file is a directory") {
            val f = Files.createTempDirectory("test").toFile()
            assertThrows<IllegalArgumentException> { f.linesUntil { it == "none" } }
            f.delete()
        }
    }
})
