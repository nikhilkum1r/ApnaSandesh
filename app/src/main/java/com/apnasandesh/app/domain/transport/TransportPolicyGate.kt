package com.apnasandesh.app.domain.transport

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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

    /**
     * Checks whether the current build declares the sensitive SEND_SMS permission.
     * In the Google Play release, SEND_SMS is omitted to strictly comply with Google Play's
     * SMS and Call Log policy, utilizing the zero-permission native SMS composer instead.
     */
    fun isDirectSmsPermissionDeclared(): Boolean {
        return runCatching {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            }
            packageInfo.requestedPermissions?.contains(Manifest.permission.SEND_SMS) == true
        }.getOrDefault(false)
    }

    fun hasDirectSmsRuntimePermissions(): Boolean {
        if (!isDirectSmsPermissionDeclared()) return false
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasPhoneStatePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasAllDirectSimPermissions(): Boolean {
        return hasDirectSmsRuntimePermissions() && hasPhoneStatePermission()
    }

    @Suppress("MissingPermission")
    fun getDefaultSubscriptionId(): Int? {
        return runCatching {
            val defaultSmsId = SubscriptionManager.getDefaultSmsSubscriptionId()
            if (defaultSmsId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                return@runCatching defaultSmsId
            }
            val defaultSubId = SubscriptionManager.getDefaultSubscriptionId()
            if (defaultSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                return@runCatching defaultSubId
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val defaultDataId = SubscriptionManager.getDefaultDataSubscriptionId()
                if (defaultDataId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    return@runCatching defaultDataId
                }
            }
            null
        }.getOrNull()
    }

    @Suppress("MissingPermission")
    fun getActiveSimSubscriptions(): List<SimSubscriptionInfo> {
        val phoneStateGranted = hasPhoneStatePermission()
        val detected = if (phoneStateGranted) {
            runCatching {
                val subManager = context.getSystemService(SubscriptionManager::class.java)
                val list = subManager?.activeSubscriptionInfoList.orEmpty()
                list.map { info ->
                    SimSubscriptionInfo(
                        subscriptionId = info.subscriptionId,
                        simSlotIndex = info.simSlotIndex,
                        carrierName = info.carrierName?.toString().orEmpty().ifBlank { "Carrier ${info.simSlotIndex + 1}" },
                        displayName = info.displayName?.toString().orEmpty().ifBlank { "SIM ${info.simSlotIndex + 1}" }
                    )
                }
            }.getOrDefault(emptyList())
        } else {
            emptyList()
        }

        if (detected.isNotEmpty()) {
            return detected
        }

        val fallbackId = getDefaultSubscriptionId()
        return if (fallbackId != null) {
            listOf(
                SimSubscriptionInfo(
                    subscriptionId = fallbackId,
                    simSlotIndex = 0,
                    carrierName = "Cellular Network",
                    displayName = "Default SIM"
                )
            )
        } else {
            emptyList()
        }
    }
}
