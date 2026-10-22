package com.apnasandesh.app.domain.validation

import com.apnasandesh.app.core.database.CustomerEntity
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

        var headerMap: Map<String, Int>? = null
        var lineIndex = 0

        reader.forEachLine { rawLine ->
            lineIndex++
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEachLine

            val columns = splitCsvLine(line)

            // Detect header row
            if (lineIndex == 1 && columns.any { it.lowercase().contains("phone") || it.lowercase().contains("name") }) {
                val map = mutableMapOf<String, Int>()
                columns.forEachIndexed { idx, colHeader ->
                    val cleanHeader = colHeader.trim().lowercase().replace(" ", "_").replace("-", "_")
                    when {
                        cleanHeader.contains("phone") || cleanHeader.contains("mobile") -> map["phone"] = idx
                        cleanHeader.contains("name") -> map["name"] = idx
                        cleanHeader.contains("tag") -> map["tags"] = idx
                        cleanHeader.contains("consent") -> map["consent"] = idx
                        cleanHeader.contains("note") -> map["notes"] = idx
                        cleanHeader.contains("email") -> map["email"] = idx
                        cleanHeader.contains("company") || cleanHeader.contains("org") -> map["company"] = idx
                        cleanHeader.contains("custom_field_1") || cleanHeader == "custom1" || cleanHeader == "field1" -> map["custom1"] = idx
                        cleanHeader.contains("custom_field_2") || cleanHeader == "custom2" || cleanHeader == "field2" -> map["custom2"] = idx
                        cleanHeader.contains("order_id") || cleanHeader == "order" -> map["order_id"] = idx
                        cleanHeader.contains("order_amount") || cleanHeader == "amount" -> map["order_amount"] = idx
                        cleanHeader.contains("order_date") -> map["order_date"] = idx
                    }
                }
                headerMap = map
                return@forEachLine // Skip header row
            }

            fun getCol(key: String, fallbackIndex: Int): String {
                val idx = headerMap?.get(key) ?: fallbackIndex
                return columns.getOrNull(idx)?.trim().orEmpty()
            }

            val name = getCol("name", 0)
            val rawPhone = getCol("phone", 1)
            val tags = getCol("tags", 2)
            val consentStr = getCol("consent", 3).ifEmpty { "GRANTED" }.uppercase()
            val notes = getCol("notes", 4)
            val email = getCol("email", 5)
            val company = getCol("company", 6)
            val custom1 = getCol("custom1", 7)
            val custom2 = getCol("custom2", 8)
            val lastOrderId = getCol("order_id", 9)
            val lastOrderAmount = getCol("order_amount", 10)
            val lastOrderDate = getCol("order_date", 11)

            if (columns.size < 2 && (name.isEmpty() || rawPhone.isEmpty())) {
                invalidRows.add(lineIndex to "Row requires at least Name and Phone columns")
                return@forEachLine
            }

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
                            email = email,
                            company = company,
                            customField1 = custom1,
                            customField2 = custom2,
                            lastOrderId = lastOrderId,
                            lastOrderAmount = lastOrderAmount,
                            lastOrderDate = lastOrderDate,
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
