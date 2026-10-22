package com.localreach.bizsms.domain.validation

import com.localreach.bizsms.core.database.CustomerEntity
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

data class CsvImportSummary(
    val validCustomers: List<CustomerEntity>,
    val duplicatePhonesInFile: List<String>,
    val invalidRows: List<Pair<Int, String>>,
    val suppressedSkippedCount: Int
)

@Singleton
class CsvCustomerParser @Inject constructor(
    private val phoneNormalizer: PhoneNormalizer
) {
    /**
     * Parses CSV input stream with header support:
     * Expected columns: name, phone, tags (optional), consent_status (optional), notes (optional)
     */
    fun parseCsvStream(
        inputStream: InputStream,
        defaultCountryCode: String,
        existingSuppressedPhones: Set<String>
    ): CsvImportSummary {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val validList = mutableListOf<CustomerEntity>()
        val seenPhones = mutableSetOf<String>()
        val duplicatePhones = mutableListOf<String>()
        val invalidRows = mutableListOf<Pair<Int, String>>()
        var suppressedSkipped = 0

        var lineIndex = 0
        reader.forEachLine { rawLine ->
            lineIndex++
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEachLine

            val columns = splitCsvLine(line)
            if (lineIndex == 1 && columns.any { it.lowercase().contains("phone") || it.lowercase().contains("name") }) {
                return@forEachLine // Skip header row
            }

            if (columns.size < 2) {
                invalidRows.add(lineIndex to "Row requires at least Name and Phone columns")
                return@forEachLine
            }

            val name = columns[0].trim()
            val rawPhone = columns[1].trim()
            val tags = columns.getOrNull(2)?.trim() ?: ""
            val consentStr = columns.getOrNull(3)?.trim()?.uppercase() ?: "GRANTED"
            val notes = columns.getOrNull(4)?.trim() ?: ""

            if (name.isEmpty()) {
                invalidRows.add(lineIndex to "Customer name is blank")
                return@forEachLine
            }

            when (val norm = phoneNormalizer.normalize(rawPhone, defaultCountryCode)) {
                is PhoneValidationResult.Invalid -> {
                    invalidRows.add(lineIndex to norm.reason)
                }
                is PhoneValidationResult.Valid -> {
                    if (norm.e164Phone in existingSuppressedPhones) {
                        suppressedSkipped++
                        return@forEachLine
                    }
                    if (!seenPhones.add(norm.e164Phone)) {
                        duplicatePhones.add(norm.e164Phone)
                        return@forEachLine
                    }

                    val sanitizedConsent = when (consentStr) {
                        "GRANTED", "OPTED_IN", "YES", "TRUE" -> "GRANTED"
                        "OPTED_OUT", "NO", "FALSE" -> "OPTED_OUT"
                        else -> "UNKNOWN"
                    }

                    validList.add(
                        CustomerEntity(
                            fullName = name,
                            rawPhoneNumber = rawPhone,
                            normalizedPhoneE164 = norm.e164Phone,
                            isPhoneValid = true,
                            tagsCsv = tags,
                            notes = notes,
                            consentStatus = sanitizedConsent,
                            consentSource = "CSV_IMPORT",
                            consentTimestampMillis = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        return CsvImportSummary(
            validCustomers = validList,
            duplicatePhonesInFile = duplicatePhones,
            invalidRows = invalidRows,
            suppressedSkippedCount = suppressedSkipped
        )
    }

    internal fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ch == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(ch)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}
