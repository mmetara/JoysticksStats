package com.joysticks.stats.ui.navigation

sealed class Screen(val route: String) {

    object Home : Screen("home")

    object TeamManager : Screen("teams")

    object Game : Screen("game")

    object StatsSheet : Screen("statsSheet")

    object SplashScreen : Screen("splash")
}