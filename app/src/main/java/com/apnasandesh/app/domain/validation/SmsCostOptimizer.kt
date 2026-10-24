package com.apnasandesh.app.domain.validation

import javax.inject.Inject
import javax.inject.Singleton

data class OptimizationResult(
    val originalText: String,
    val optimizedText: String,
    val originalCharCount: Int,
    val optimizedCharCount: Int,
    val originalSegments: Int,
    val optimizedSegments: Int,
    val savedCharacters: Int,
    val savedSegments: Int,
    val changesApplied: List<String>
)

@Singleton
class SmsCostOptimizer @Inject constructor(
    private val segmentCalculator: SmsSegmentCalculator
) {
    /**
     * Determines whether the message can be meaningfully optimized to save SMS segments.
     */
    fun canOptimize(text: String): Boolean {
        if (text.isBlank()) return false
        val currentMetrics = segmentCalculator.calculate(text)
        if (currentMetrics.segmentCount <= 1 && currentMetrics.charCount <= 140) return false
        val optimized = optimize(text)
        return optimized.savedSegments > 0 || optimized.savedCharacters >= 3
    }

    /**
     * Optimizes message text by replacing verbose phrases with standard professional abbreviations
     * and trimming unnecessary whitespace, targeting 1 SMS segment whenever possible.
     */
    fun optimize(text: String): OptimizationResult {
        val originalMetrics = segmentCalculator.calculate(text)
        var result = text
        val applied = mutableListOf<String>()

        // 1. Collapse multiple whitespaces and trim trailing line spaces
        val collapsedSpaces = result.lines().joinToString("\n") { line ->
            line.replace("\\s+".toRegex(), " ").trimEnd()
        }.trim()
        if (collapsedSpaces.length < result.length) {
            applied.add("Trimmed redundant spacing")
            result = collapsedSpaces
        }

        // 2. Intelligent phrase condensation dictionary (case-insensitive regex replacements)
        val replacements = listOf(
            Pair("(?i)\\bthank you very much\\b", "Thanks a lot"),
            Pair("(?i)\\bthank you so much\\b", "Thanks a lot"),
            Pair("(?i)\\bthank you\\b", "Thanks"),
            Pair("(?i)\\bplease visit our store\\b", "Visit us"),
            Pair("(?i)\\bplease visit\\b", "Visit"),
            Pair("(?i)\\bplease\\b", "Pls"),
            Pair("(?i)\\bdo not hesitate to\\b", "Feel free to"),
            Pair("(?i)\\bfeel free to contact\\b", "Call"),
            Pair("(?i)\\bcontact us at\\b", "Call"),
            Pair("(?i)\\bcontact us\\b", "Call us"),
            Pair("(?i)\\bappointment\\b", "Appt"),
            Pair("(?i)\\bdiscount\\b", "Disc"),
            Pair("(?i)\\binformation\\b", "Info"),
            Pair("(?i)\\bregarding\\b", "re:"),
            Pair("(?i)\\bahead of time\\b", "early"),
            Pair("(?i)\\bas soon as possible\\b", "ASAP"),
            Pair("(?i)\\blooking forward to\\b", "awaiting"),
            Pair("(?i)\\breply stop to opt out\\b", "Reply STOP to stop")
        )

        for ((pattern, replacement) in replacements) {
            val regex = pattern.toRegex()
            if (regex.containsMatchIn(result)) {
                val beforeLen = result.length
                result = regex.replace(result, replacement)
                if (result.length < beforeLen) {
                    applied.add("Condensed wording for SMS brevity")
                }
            }
        }

        // 3. Clean up any accidental double spaces resulting from substitutions
        result = result.replace("  ", " ").trim()

        val newMetrics = segmentCalculator.calculate(result)

        return OptimizationResult(
            originalText = text,
            optimizedText = result,
            originalCharCount = originalMetrics.charCount,
            optimizedCharCount = newMetrics.charCount,
            originalSegments = originalMetrics.segmentCount,
            optimizedSegments = newMetrics.segmentCount,
            savedCharacters = (originalMetrics.charCount - newMetrics.charCount).coerceAtLeast(0),
            savedSegments = (originalMetrics.segmentCount - newMetrics.segmentCount).coerceAtLeast(0),
            changesApplied = applied.distinct()
        )
    }
}
