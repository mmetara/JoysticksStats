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
            "${it.id},${it.name}"
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

            if (parts.size == 2) {
                Team(
                    id = parts[0].toInt(),
                    name = parts[1]
                )
            } else null
        }
    }
}