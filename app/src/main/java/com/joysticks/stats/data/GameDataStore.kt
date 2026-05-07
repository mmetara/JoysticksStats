package com.joysticks.stats.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.joysticks.stats.engine.GameState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.gameDataStore by preferencesDataStore(name = "current_game")

class GameDataStore(private val context: Context) {
    private val gson = Gson()

    companion object {
        private val GAME_STATE_KEY = stringPreferencesKey("game_state")
    }

    val gameStateFlow: Flow<GameState?> = context.gameDataStore.data.map { prefs ->
        val json = prefs[GAME_STATE_KEY]
        if (json.isNullOrBlank()) null
        else {
            try {
                gson.fromJson(json, GameState::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun saveGameState(state: GameState) {
        val json = gson.toJson(state)
        context.gameDataStore.edit { prefs ->
            prefs[GAME_STATE_KEY] = json
        }
    }

    suspend fun clearGame() {
        context.gameDataStore.edit { prefs ->
            prefs.remove(GAME_STATE_KEY)
        }
    }
}
