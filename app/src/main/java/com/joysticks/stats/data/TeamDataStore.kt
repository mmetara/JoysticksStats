package com.joysticks.stats.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*

val Context.teamDataStore by preferencesDataStore(name = "teams")

class TeamDataStore(private val context: Context) {

    companion object {
        val TEAMS = stringPreferencesKey("teams")
    }

    suspend fun saveTeams(teams: List<Team>) {

        val serialized = teams.joinToString(";") {
            val players = it.playerNames.joinToString("|")
            "${it.id},${it.name},$players"
        }

        context.teamDataStore.edit { prefs ->
            prefs[TEAMS] = serialized
        }
    }

    suspend fun loadTeams(): List<Team> {

        val prefs = context.teamDataStore.data.first()

        val raw = prefs[TEAMS] ?: return emptyList()

        return raw.split(";").mapNotNull {

            val parts = it.split(",")

            if (parts.size >= 2) {
                val players = if (parts.size > 2 && parts[2].isNotBlank()) {
                    parts[2].split("|")
                } else emptyList()
                
                Team(
                    id = parts[0].toInt(),
                    name = parts[1],
                    playerNames = players
                )
            } else null
        }
    }
}