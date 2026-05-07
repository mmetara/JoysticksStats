package com.joysticks.stats.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        val TARGET_SSID = stringPreferencesKey("target_ssid")
        val PLAYER_IMAGES_URL = stringPreferencesKey("player_images_url")
        const val DEFAULT_IMAGES_URL = "http://www.lesjoysticks.info/db/statsapp/"
    }

    val targetSsidFlow: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[TARGET_SSID] ?: "mmetara"
    }

    val playerImagesUrlFlow: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[PLAYER_IMAGES_URL] ?: DEFAULT_IMAGES_URL
    }

    suspend fun saveTargetSsid(ssid: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[TARGET_SSID] = ssid
        }
    }

    suspend fun savePlayerImagesUrl(url: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[PLAYER_IMAGES_URL] = url
        }
    }
}
