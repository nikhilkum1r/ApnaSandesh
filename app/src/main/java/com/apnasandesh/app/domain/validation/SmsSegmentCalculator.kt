package com.apnasandesh.app.domain.validation

import com.apnasandesh.app.core.model.SmsEncoding
import com.apnasandesh.app.core.model.SmsEncodingResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil

@Singleton
class SmsSegmentCalculator @Inject constructor() {

    private val gsm7BasicChars = setOf(
        '@', '£', '$', '¥', 'è', 'é', 'ù', 'ì', 'ò', 'Ç', '\n', 'Ø', 'ø', '\r', 'Å', 'å',
        'Δ', '_', 'Φ', 'Γ', 'Λ', 'Ω', 'Π', 'Ψ', 'Σ', 'Θ', 'Ξ', 'Æ', 'æ', 'ß', 'É', ' ',
        '!', '"', '#', '¤', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
        '¡', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Ä', 'Ö', 'Ñ', 'Ü', '§',
        '¿', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
        'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'ä', 'ö', 'ñ', 'ü', 'à'
    )

    private val gsm7ExtendedChars = setOf('^', '{', '}', '\\', '[', '~', ']', '|', '€')

    fun calculate(message: String): SmsEncodingResult {
        if (message.isEmpty()) {
            return SmsEncodingResult(
                encoding = SmsEncoding.GSM_7BIT,
                isUcs2Unicode = false,
                charCount = 0,
                segmentCount = 0,
                remainingInCurrentSegment = 160,
                maxPerSingleSegment = 160,
                maxPerMultipartSegment = 153
            )
        }

        var isUcs2 = false
        var gsmSeptetLength = 0

        for (ch in message) {
            when (ch) {
                in gsm7BasicChars -> gsmSeptetLength += 1
                in gsm7ExtendedChars -> gsmSeptetLength += 2
                else -> {
                    isUcs2 = true
                    break
                }
            }
        }

        return if (!isUcs2) {
            val segments = if (gsmSeptetLength <= 160) {
                1
            } else {
                ceil(gsmSeptetLength / 153.0).toInt()
            }
            val capacity = if (segments == 1) 160 else segments * 153
            SmsEncodingResult(
                encoding = SmsEncoding.GSM_7BIT,
                isUcs2Unicode = false,
                charCount = gsmSeptetLength,
                segmentCount = segments,
                remainingInCurrentSegment = capacity - gsmSeptetLength,
                maxPerSingleSegment = 160,
                maxPerMultipartSegment = 153
            )
        } else {
            val codeUnits = message.length
            val segments = if (codeUnits <= 70) {
                1
            } else {
                ceil(codeUnits / 67.0).toInt()
            }
            val capacity = if (segments == 1) 70 else segments * 67
            SmsEncodingResult(
                encoding = SmsEncoding.UCS_2,
                isUcs2Unicode = true,
                charCount = codeUnits,
                segmentCount = segments,
                remainingInCurrentSegment = capacity - codeUnits,
                maxPerSingleSegment = 70,
                maxPerMultipartSegment = 67
            )
        }
    }

    /**
     * Returns true if the message can be encoded strictly using GSM-7 standard characters.
     */
    fun isGsm7Compliant(message: String): Boolean {
        for (ch in message) {
            if (ch !in gsm7BasicChars && ch !in gsm7ExtendedChars) {
                return false
            }
        }
        return true
    }

    /**
     * Identifies non-GSM characters in the message (e.g. Hindi, emojis, curly quotes)
     * that trigger UCS-2 encoding.
     */
    fun findNonGsmCharacters(message: String): List<Char> {
        val nonGsm = mutableListOf<Char>()
        for (ch in message) {
            if (ch !in gsm7BasicChars && ch !in gsm7ExtendedChars && ch !in nonGsm) {
                nonGsm.add(ch)
            }
        }
        return nonGsm
    }

    /**
     * User-friendly diagnostic summary explaining encoding and segment allocation.
     */
    fun getEncodingExplanation(result: SmsEncodingResult): String {
        return if (!result.isUcs2Unicode) {
            if (result.segmentCount <= 1) {
                "GSM-7 standard • ${result.charCount}/160 chars • 1 SMS segment"
            } else {
                "GSM-7 multi-part • ${result.charCount} chars (${result.segmentCount} segments @ 153 chars/segment)"
            }
        } else {
            if (result.segmentCount <= 1) {
                "Unicode (UCS-2) • ${result.charCount}/70 chars • 1 SMS segment"
            } else {
                "Unicode multi-part • ${result.charCount} chars (${result.segmentCount} segments @ 67 chars/segment)"
            }
        }
    }
}

