package com.apnasandesh.app.core.database

import androidx.room.TypeConverter
import com.apnasandesh.app.core.model.CampaignStatus
import com.apnasandesh.app.core.model.ConsentSource
import com.apnasandesh.app.core.model.ConsentStatus
import com.apnasandesh.app.core.model.RecipientDeliveryState
import com.apnasandesh.app.core.model.TemplateCategory
import com.apnasandesh.app.core.model.TransportMode

class DatabaseConverters {
    @TypeConverter
    fun fromConsentStatus(v: ConsentStatus?): String? = v?.name

    @TypeConverter
    fun toConsentStatus(v: String?): ConsentStatus? = v?.let { runCatching { ConsentStatus.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromConsentSource(v: ConsentSource?): String? = v?.name

    @TypeConverter
    fun toConsentSource(v: String?): ConsentSource? = v?.let { runCatching { ConsentSource.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromTemplateCategory(v: TemplateCategory?): String? = v?.name

    @TypeConverter
    fun toTemplateCategory(v: String?): TemplateCategory? = v?.let { runCatching { TemplateCategory.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromCampaignStatus(v: CampaignStatus?): String? = v?.name

    @TypeConverter
    fun toCampaignStatus(v: String?): CampaignStatus? = v?.let { runCatching { CampaignStatus.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromRecipientDeliveryState(v: RecipientDeliveryState?): String? = v?.name

    @TypeConverter
    fun toRecipientDeliveryState(v: String?): RecipientDeliveryState? = v?.let { runCatching { RecipientDeliveryState.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromTransportMode(v: TransportMode?): String? = v?.name

    @TypeConverter
    fun toTransportMode(v: String?): TransportMode? = v?.let { runCatching { TransportMode.valueOf(it) }.getOrNull() }
}
