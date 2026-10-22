package com.apnasandesh.app.core.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "biz_sms_preferences")

@Singleton
class AppPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DIRECT_SIM_POLICY_ACKNOWLEDGED = booleanPreferencesKey("direct_sim_policy_ack")
        val KEY_DIRECT_SIM_ENABLED = booleanPreferencesKey("direct_sim_enabled")
        val KEY_INDIA_TRAI_MODE = booleanPreferencesKey("india_trai_mode")
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: "LIGHT"
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    val directSimPolicyAcknowledged: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DIRECT_SIM_POLICY_ACKNOWLEDGED] ?: false
    }

    suspend fun setDirectSimPolicyAcknowledged(acknowledged: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DIRECT_SIM_POLICY_ACKNOWLEDGED] = acknowledged
        }
    }

    val directSimEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DIRECT_SIM_ENABLED] ?: false
    }

    suspend fun setDirectSimEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DIRECT_SIM_ENABLED] = enabled
        }
    }

    suspend fun setDirectSimFeatureEnabled(enabled: Boolean) = setDirectSimEnabled(enabled)

    val indiaTraiMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_INDIA_TRAI_MODE] ?: true
    }

    suspend fun setIndiaTraiMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_INDIA_TRAI_MODE] = enabled
        }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
