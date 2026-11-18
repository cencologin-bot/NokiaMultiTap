package com.example.nokiamultitap

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MultiTapCodecTest {
    @Test
    fun encodesWordsWithSeparator() {
        assertEquals("44 666 555 2 / 6 88 66 3 666", MultiTapCodec.encode("hola mundo"))
    }

    @Test
    fun normalizesAccents() {
        assertEquals("2 66 2", MultiTapCodec.encode("ánã"))
    }

    @Test
    fun decodesRoundTrip() {
        val result = MultiTapCodec.decode(MultiTapCodec.encode("hola mundo"))
        assertEquals("hola mundo".uppercase(), result.text.uppercase())
        assertTrue(result.invalidTokens.isEmpty())
    }

    @Test
    fun reportsInvalidTokens() {
        val result = MultiTapCodec.decode("44 99999 2")
        assertEquals(listOf("99999"), result.invalidTokens)
    }

    @Test
    fun abbreviatesLongCodes() {
        val short = MultiTapCodec.abbreviate("44 666 555 2 / 6 88 66 3 666", 10)
        assertTrue(short.endsWith("…"))
        assertEquals(3, MultiTapCodec.tapCount("44 2"))
    }
}
