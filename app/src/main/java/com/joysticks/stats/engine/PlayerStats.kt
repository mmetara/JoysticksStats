package com.joysticks.stats.engine

data class PlayerStats(
    val index: Int,
    val playerName: String,
    val posOff: Int,
    val posDef: String,
    val photoUrl: String? = null
)
