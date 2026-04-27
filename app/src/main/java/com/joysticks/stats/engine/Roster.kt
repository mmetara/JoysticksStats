package com.joysticks.stats.engine

data class Roster(
    val players: MutableList<PlayerStats>,
    val gameInfo: GameInfo
)

data class GameInfo(
    val homeTeamName: String,
    val awayTeamName: String,
    val gameDate: String,
    val gameTime: String,
    val gameTimeMillis: Long,
    val isHomeTeam: Boolean,
    val maxInnings: Int = 9 // Ajout de la propriété maxInnings avec une valeur par défaut
)