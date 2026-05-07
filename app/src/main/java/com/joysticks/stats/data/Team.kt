package com.joysticks.stats.data

data class Team(
    val id: Int,
    val name: String,
    val playerNames: List<String> = emptyList()
)