package com.joysticks.stats.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.joysticks.stats.data.TeamDataStore
import com.joysticks.stats.engine.AlignementRepository
import com.joysticks.stats.engine.GameViewModel
import com.joysticks.stats.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(gameViewModel: GameViewModel, createPdfDocumentLauncher: ActivityResultLauncher<String>) {

    val navController = rememberNavController() // Réintroduction de navController

    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreen.route
    ) {
        composable(Screen.SplashScreen.route) {
            SplashScreen(navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController, gameViewModel) // Repasse gameViewModel à HomeScreen
        }

        composable(Screen.TeamManager.route) {

            val context = LocalContext.current
            val teamStore = remember { TeamDataStore(context) }

            TeamManagementScreen(
                navController = navController,
                teamStore = teamStore
            )
        }

        composable(
            route = Screen.Game.route + "?editEventPlayerIndex={editEventPlayerIndex}&editEventInning={editEventInning}&editEventIsHomeTeam={editEventIsHomeTeam}",
            arguments = listOf(
                navArgument("editEventPlayerIndex") { type = NavType.IntType; defaultValue = -1 },
                navArgument("editEventInning") { type = NavType.IntType; defaultValue = -1 },
                navArgument("editEventIsHomeTeam") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val repository = remember { AlignementRepository() }
            val editEventPlayerIndex = backStackEntry.arguments?.getInt("editEventPlayerIndex") ?: -1
            val editEventInning = backStackEntry.arguments?.getInt("editEventInning") ?: -1
            val editEventIsHomeTeam = backStackEntry.arguments?.getBoolean("editEventIsHomeTeam") ?: false

            GameScreen(
                navController = navController,
                gameViewModel = gameViewModel,
                onNavigateToHome = { navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }},
                onNavigateToStatsSheet = { navController.navigate(Screen.StatsSheet.route) },
                onExportPdf = { createPdfDocumentLauncher.launch("game_stats.pdf") },
                onUploadCsv = {
                    if (!com.joysticks.stats.utils.NetworkUtils.isTargetWifiConnected(context)) {
                        android.widget.Toast.makeText(context, "Échec : Connectez-vous au WiFi 'mmetara' pour uploader", android.widget.Toast.LENGTH_LONG).show()
                    } else {
                        scope.launch {
                            val csvData = gameViewModel.generateCsv()
                            val success = repository.uploadCSV(csvData)
                            if (success) {
                                android.widget.Toast.makeText(context, "Statistiques envoyées avec succès !", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(context, "Erreur lors de l'envoi au serveur", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                editEventPlayerIndex = editEventPlayerIndex, // Pass to GameScreen
                editEventInning = editEventInning,           // Pass to GameScreen
                editEventIsHomeTeam = editEventIsHomeTeam    // Pass to GameScreen
            )
        }

        composable(Screen.StatsSheet.route) {
            StatsSheetScreen(navController = navController, gameState = gameViewModel.gameState, roster = gameViewModel.gameState.roster)
        }
    }
}