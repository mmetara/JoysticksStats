package com.joysticks.stats.ui.navigation

sealed class Screen(val route: String) {

    object Home : Screen("home")

    object TeamManager : Screen("teams")

    object LineUp : Screen("lineup")

    object Game : Screen("game")

    object StatsSheet : Screen("statsSheet")

    object Settings : Screen("settings")

    object Help : Screen("help")

    object SplashScreen : Screen("splash")
}