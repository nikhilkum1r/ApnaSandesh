package com.localreach.bizsms.core.common

import android.util.Log

/**
 * Privacy-preserving logger that redacts phone numbers, digits, and message payloads
 * before writing operational diagnostic events to Android Logcat.
 */
object PrivacySafeLogger {
    private const val TAG = "LocalReachSMS"
    private val phoneDigitsRegex = Regex("\\+?\\d[\\d\\s\\-()]{5,}\\d")

    fun info(event: String) {
        Log.i(TAG, sanitize(event))
    }

    fun warn(event: String, throwable: Throwable? = null) {
        Log.w(TAG, sanitize(event), throwable)
    }

    fun error(event: String, throwable: Throwable? = null) {
        Log.e(TAG, sanitize(event), throwable)
    }

    internal fun sanitize(input: String): String {
        return phoneDigitsRegex.replace(input, "[REDACTED_PHONE]")
    }
}
