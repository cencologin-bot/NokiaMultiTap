package com.example.nokiamultitap

import java.text.Normalizer
import java.util.Locale

/**
 * Distribución Multi-Tap solicitada:
 * 2 ABC, 3 DEF, 4 GHI, 5 JKL, 6 MNO, 7 PQRS, 8 TUV, 9 WXYZ.
 * 0 representa espacio y 1 se reserva para puntuación.
 *
 * Los acentos se normalizan (Á -> A) y Ñ se normaliza a N porque
 * el mapa solicitado no define una tecla exclusiva para Ñ.
 */
object MultiTapCodec {
    private val encodeMap = mapOf(
        'A' to "2", 'B' to "22", 'C' to "222",
        'D' to "3", 'E' to "33", 'F' to "333",
        'G' to "4", 'H' to "44", 'I' to "444",
        'J' to "5", 'K' to "55", 'L' to "555",
        'M' to "6", 'N' to "66", 'O' to "666",
        'P' to "7", 'Q' to "77", 'R' to "777", 'S' to "7777",
        'T' to "8", 'U' to "88", 'V' to "888",
        'W' to "9", 'X' to "99", 'Y' to "999", 'Z' to "9999"
    )

    private val punctuation = mapOf(
        '.' to "1", ',' to "11", '?' to "111", '!' to "1111",
        ':' to "11111", ';' to "111111", '\'' to "1111111",
        '"' to "11111111", '-' to "111111111"
    )
    private val decodeMap = encodeMap.entries.associate { it.value to it.key.toString() } +
        punctuation.entries.associate { it.value to it.key.toString() }

    fun encode(input: String): String {
        val normalized = Normalizer.normalize(input.uppercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return normalized.split(' ').joinToString(" / ") { word ->
            word.mapNotNull { ch ->
                when {
                    encodeMap.containsKey(ch) -> encodeMap[ch]
                    punctuation.containsKey(ch) -> punctuation[ch]
                    ch.isDigit() -> ch.toString()
                    ch.isWhitespace() -> null
                    else -> null
                }
            }.joinToString(" ")
        }
    }

    data class DecodeResult(val text: String, val invalidTokens: List<String>)

    fun decode(input: String): DecodeResult {
        val invalid = mutableListOf<String>()
        val words = input.trim().split(Regex("\\s*/\\s*"))
        val decoded = words.map { word ->
            if (word.isBlank()) return@map ""
            word.trim().split(Regex("\\s+")).mapNotNull { token ->
                when {
                    token == "0" -> " "
                    decodeMap.containsKey(token) -> decodeMap[token]
                    token.matches(Regex("\\d+")) -> {
                        invalid += token
                        null
                    }
                    else -> {
                        invalid += token
                        null
                    }
                }
            }.joinToString("")
        }
        return DecodeResult(decoded.joinToString(" ").replace(Regex(" +"), " ").trim(), invalid)
    }

    /** Número de pulsaciones de tecla que representa un código. */
    fun tapCount(code: String): Int = code.count { it.isDigit() }

    /** Versión corta para mostrar en pantalla; la secuencia real no se altera. */
    fun abbreviate(code: String, maxChars: Int = 24): String {
        val compact = code.replace(Regex("\\s+"), " ").trim()
        return if (compact.length > maxChars) compact.take(maxChars).trimEnd() + "…" else compact
    }
}
