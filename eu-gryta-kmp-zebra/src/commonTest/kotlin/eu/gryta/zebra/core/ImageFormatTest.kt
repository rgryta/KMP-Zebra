package eu.gryta.zebra.core

import kotlin.test.Test
import kotlin.test.assertTrue

class ImageFormatTest {
    @Test
    fun jpeg_is_a_member() {
        assertTrue(ImageFormat.entries.any { it.name == "JPEG" })
    }
}
