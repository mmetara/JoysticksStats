package com.joysticks.stats.engine

import android.content.Context
import android.net.Uri
import com.joysticks.stats.data.TeamDataStore
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

suspend fun parseRoster(context: Context, uri: Uri): Roster {

    val teamStore = TeamDataStore(context)
    val teams = teamStore.loadTeams()

    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalArgumentException("Impossible d'ouvrir le fichier CSV")

    val lines = inputStream.bufferedReader().readLines()
    val players = mutableListOf<PlayerStats>()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    var isHomeTeam = false
    var eqName = ""
    var advName = ""
    var gameDateTime = LocalDateTime.now()

    for (line in lines) {
        if (line.isBlank()) continue
        val columns = line.split(",").map { it.trim() }

        if (columns.size < 7) {
            android.util.Log.w("RosterParser", "Ligne ignorée (colonnes insuffisantes < 7) : $line")
            continue
        }

        val eqId = columns[0].toIntOrNull() ?: 0
        val advId = columns[1].toIntOrNull() ?: 0

        eqName = teams.find { it.id == eqId }?.name ?: "Équipe $eqId"
        advName = teams.find { it.id == advId }?.name ?: "Équipe $advId"

        try {
            gameDateTime = LocalDateTime.parse(columns[2], formatter)
        } catch (e: Exception) {
            // Fallback si la date est mal formatée
        }

        isHomeTeam = columns[3].uppercase() == "L"

        // On prend les colonnes 4, 5, 6 et optionnellement 7 (Nom, Pos Off, Pos Def, PhotoUrl)
        val rawPhotoUrl = if (columns.size > 7) columns[7] else null
        players.add(
            PlayerStats(
                index = players.size,
                playerName = columns[4],
                posOff = columns[5].toIntOrNull() ?: 0,
                posDef = columns[6],
                photoUrl = if (rawPhotoUrl.isNullOrBlank()) null else rawPhotoUrl
            )
        )
    }

    android.util.Log.d("RosterParser", "Import terminé avec succès : ${players.size} joueurs chargés pour le match ${if (isHomeTeam) eqName else advName} vs ${if (isHomeTeam) advName else eqName}")

    val gameInfo = GameInfo(
        homeTeamName = if (isHomeTeam) eqName else advName,
        awayTeamName = if (isHomeTeam) advName else eqName,
        gameDate = gameDateTime.toLocalDate().toString(),
        gameTime = gameDateTime.toLocalTime().toString(),
        gameTimeMillis = gameDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli(),
        isHomeTeam = isHomeTeam,
        maxInnings = 9 // Ajout de la propriété maxInnings avec une valeur par défaut
    )

    return Roster(
        players = players,
        gameInfo = gameInfo
    )
}
