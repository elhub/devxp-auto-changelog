package no.elhub.devxp.autochangelog.extensions

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CollectionExtensionsTest : FunSpec({
    test("should return the 'tail' of the list") {
        val list = listOf(0, 1, 2, 3, 4, 5, 6)
        list.tail() shouldBe listOf(1, 2, 3, 4, 5, 6)
    }
    test("should return an empty tail for an empty list") {
        val list = emptyList<Int>()
        list.tail() shouldBe emptyList()
    }
    test("should return an empty tail for a list of size 1") {
        val list = listOf(42)
        list.tail() shouldBe emptyList()
    }
})
