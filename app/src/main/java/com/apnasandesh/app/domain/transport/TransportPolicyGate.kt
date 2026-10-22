package com.apnasandesh.app.domain.transport

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import com.apnasandesh.app.core.model.SimSubscriptionInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransportPolicyGate @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun hasDirectSmsRuntimePermissions(): Boolean {
        val sendGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
        val phoneStateGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
        return sendGranted && phoneStateGranted
    }

    @Suppress("MissingPermission")
    fun getActiveSimSubscriptions(): List<SimSubscriptionInfo> {
        val phoneStateGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
        if (!phoneStateGranted) return emptyList()

        return runCatching {
            val subManager = context.getSystemService(SubscriptionManager::class.java)
            subManager?.activeSubscriptionInfoList.orEmpty().map { info ->
                SimSubscriptionInfo(
                    subscriptionId = info.subscriptionId,
                    simSlotIndex = info.simSlotIndex,
                    carrierName = info.carrierName?.toString().orEmpty().ifBlank { "Carrier" },
                    displayName = info.displayName?.toString().orEmpty().ifBlank { "SIM ${info.simSlotIndex + 1}" }
                )
            }
        }.getOrDefault(emptyList())
    }
}
