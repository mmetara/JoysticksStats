package com.joysticks.stats.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.joysticks.stats.data.TeamDataStore
import com.joysticks.stats.engine.GameViewModel
import com.joysticks.stats.ui.screens.*

@Composable
fun AppNavigation(gameViewModel: GameViewModel, createPdfDocumentLauncher: ActivityResultLauncher<String>) {

    val navController = rememberNavController() // Réintroduction de navController

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController)
        }

        composable("home") {
            HomeScreen(navController, gameViewModel) // Repasse gameViewModel à HomeScreen
        }

        composable("Teams") {

            val context = LocalContext.current
            val teamStore = remember { TeamDataStore(context) }

            TeamManagementScreen(
                navController = navController,
                teamStore = teamStore
            )
        }

        composable(
            route = "game?editEventPlayerIndex={editEventPlayerIndex}&editEventInning={editEventInning}&editEventIsHomeTeam={editEventIsHomeTeam}",
            arguments = listOf(
                navArgument("editEventPlayerIndex") { type = NavType.IntType; defaultValue = -1 },
                navArgument("editEventInning") { type = NavType.IntType; defaultValue = -1 },
                navArgument("editEventIsHomeTeam") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val editEventPlayerIndex = backStackEntry.arguments?.getInt("editEventPlayerIndex") ?: -1
            val editEventInning = backStackEntry.arguments?.getInt("editEventInning") ?: -1
            val editEventIsHomeTeam = backStackEntry.arguments?.getBoolean("editEventIsHomeTeam") ?: false

            GameScreen(
                navController = navController,
                gameViewModel = gameViewModel,
                onNavigateToHome = { navController.navigate("home") {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }},
                onNavigateToStatsSheet = { navController.navigate("statsSheet") },
                onExportPdf = { createPdfDocumentLauncher.launch("game_stats.pdf") },
                editEventPlayerIndex = editEventPlayerIndex, // Pass to GameScreen
                editEventInning = editEventInning,           // Pass to GameScreen
                editEventIsHomeTeam = editEventIsHomeTeam    // Pass to GameScreen
            )
        }

        composable("statsSheet") {
            StatsSheetScreen(navController = navController, gameState = gameViewModel.gameState, roster = gameViewModel.gameState.roster)
        }
    }
}