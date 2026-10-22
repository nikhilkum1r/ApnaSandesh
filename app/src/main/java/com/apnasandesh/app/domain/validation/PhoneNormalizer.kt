package com.apnasandesh.app.domain.validation

import javax.inject.Inject
import javax.inject.Singleton

sealed class PhoneValidationResult {
    data class Valid(val e164Phone: String) : PhoneValidationResult()
    data class Invalid(val rawInput: String, val reason: String) : PhoneValidationResult()
}

@Singleton
class PhoneNormalizer @Inject constructor() {

    fun normalize(rawPhone: String, defaultCountryCode: String = "+91"): PhoneValidationResult {
        val trimmed = rawPhone.trim()
        if (trimmed.isEmpty()) {
            return PhoneValidationResult.Invalid(rawPhone, "Phone number cannot be blank")
        }

        val sanitizedCallingCode = if (defaultCountryCode.startsWith("+")) {
            defaultCountryCode
        } else {
            "+${defaultCountryCode.filter { it.isDigit() }}"
        }

        // Strip formatting characters (spaces, hyphens, parentheses, dots)
        val cleaned = buildString {
            trimmed.forEachIndexed { index, ch ->
                if (ch.isDigit() || (ch == '+' && index == 0)) {
                    append(ch)
                } else if (!ch.isWhitespace() && ch != '-' && ch != '(' && ch != ')' && ch != '.') {
                    return PhoneValidationResult.Invalid(rawPhone, "Contains unsupported character '$ch'")
                }
            }
        }

        val e164Candidate = when {
            cleaned.startsWith("+") -> cleaned
            cleaned.startsWith("00") -> "+" + cleaned.drop(2)
            sanitizedCallingCode == "+91" && cleaned.length == 11 && cleaned.startsWith("0") -> {
                "+91" + cleaned.drop(1)
            }
            sanitizedCallingCode == "+91" && cleaned.length == 10 -> {
                "+91$cleaned"
            }
            cleaned.length in 7..12 -> {
                "$sanitizedCallingCode${cleaned.trimStart('0')}"
            }
            else -> "+$cleaned"
        }

        val digitsOnly = e164Candidate.drop(1)
        if (digitsOnly.length < 8 || digitsOnly.length > 15 || !digitsOnly.all { it.isDigit() }) {
            return PhoneValidationResult.Invalid(
                rawPhone,
                "E.164 phone number must contain 8 to 15 digits (found ${digitsOnly.length})"
            )
        }

        // Specific validation for Indian mobile numbers (+91)
        if (e164Candidate.startsWith("+91")) {
            val national = e164Candidate.removePrefix("+91")
            if (national.length != 10 || national.first() !in charArrayOf('6', '7', '8', '9')) {
                return PhoneValidationResult.Invalid(
                    rawPhone,
                    "Indian mobile numbers (+91) must have 10 digits starting with 6, 7, 8, or 9"
                )
            }
        }

        return PhoneValidationResult.Valid(e164Candidate)
    }

    fun isValidE164(phone: String, defaultCountryCode: String = "+91"): Boolean {
        return normalize(phone, defaultCountryCode) is PhoneValidationResult.Valid
    }
}
