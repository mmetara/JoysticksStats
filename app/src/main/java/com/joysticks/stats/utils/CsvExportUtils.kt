package com.joysticks.stats.utils

import com.joysticks.stats.engine.AtBatEvent
import com.joysticks.stats.engine.BattingResult
import com.joysticks.stats.engine.GameState

object CsvExportUtils {

    private data class PlayerCsvStats(
        var atBats: Int = 0,
        var singles: Int = 0,
        var doubles: Int = 0,
        var triples: Int = 0,
        var homeRuns: Int = 0,
        var optionals: Int = 0,
        var strikeouts: Int = 0,
        var outs: Int = 0,
        var runsScored: Int = 0,
        var rbi: Int = 0
    )

    fun generateCsv(gameState: GameState): String {
        val roster = gameState.roster ?: return "Error: Roster not loaded."
        val gameInfo = roster.gameInfo
        val stringBuilder = StringBuilder()
        
        val ourTeamNumber = (if (gameInfo.isHomeTeam) gameInfo.homeTeamName else gameInfo.awayTeamName).replace("Équipe ", "")
        val opponentTeamNumber = (if (gameInfo.isHomeTeam) gameInfo.awayTeamName else gameInfo.homeTeamName).replace("Équipe ", "")
        val gameDateTime = "${gameInfo.gameDate} ${gameInfo.gameTime}"
        val teamStatus = if (gameInfo.isHomeTeam) "L" else "V"

        val playerStatsMap = mutableMapOf<Int, PlayerCsvStats>()
        roster.players.forEach { player ->
            playerStatsMap[player.index] = PlayerCsvStats()
        }

        // On ne garde que le dernier passage au bâton par manche pour chaque joueur
        val finalPlateAppearances = mutableMapOf<Pair<Int, Int>, AtBatEvent>()
        gameState.gameHistory.forEach { event ->
            val key = Pair(event.playerIndex, event.inning)
            finalPlateAppearances[key] = event
        }

        finalPlateAppearances.values.forEach { event ->
            val stats = playerStatsMap[event.playerIndex] ?: PlayerCsvStats()
            stats.atBats++
            when (event.result) {
                BattingResult.Single -> stats.singles++
                BattingResult.Double -> stats.doubles++
                BattingResult.Triple -> stats.triples++
                BattingResult.HomeRun -> stats.homeRuns++
                BattingResult.Optionel -> stats.optionals++
                BattingResult.Strikeout -> stats.strikeouts++
                BattingResult.Out -> stats.outs++
            }
            stats.rbi += event.rbi
            playerStatsMap[event.playerIndex] = stats
        }

        gameState.gameHistory.forEach { event ->
            if (event.finalBase == 4) {
                val stats = playerStatsMap[event.playerIndex] ?: PlayerCsvStats()
                stats.runsScored++
                playerStatsMap[event.playerIndex] = stats
            }
        }

        roster.players.forEach { player ->
            val stats = playerStatsMap[player.index] ?: PlayerCsvStats()
            val defensivePos = player.posDef
            val offensivePos = player.index + 1
            stringBuilder.append(
                "${ourTeamNumber},${opponentTeamNumber},${gameDateTime},${teamStatus}," +
                "${player.playerName},${offensivePos},${defensivePos}," +
                "${stats.atBats},${stats.singles},${stats.doubles},${stats.triples},${stats.homeRuns}," +
                "${stats.optionals},${stats.strikeouts}," +
                "${stats.runsScored},${stats.rbi}\n"
            )
        }

        val userTeamScore = if (gameInfo.isHomeTeam) gameState.homeScore else gameState.awayScore
        val opponentTeamScore = if (gameInfo.isHomeTeam) gameState.awayScore else gameState.homeScore
        
        stringBuilder.append("TOTAL POINTS,${ourTeamNumber},${userTeamScore}\n")
        stringBuilder.append("TOTAL POINTS,${opponentTeamNumber},${opponentTeamScore}\n")
        
        return stringBuilder.toString()
    }
}
