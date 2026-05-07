package com.joysticks.stats.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.joysticks.stats.data.TeamDataStore
import com.joysticks.stats.engine.AlignementRepository
import com.joysticks.stats.engine.GameViewModel
import com.joysticks.stats.ui.components.BaseballScreenTemplate
import com.joysticks.stats.ui.components.HudButton
import com.joysticks.stats.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(gameViewModel: GameViewModel, createPdfDocumentLauncher: ActivityResultLauncher<String>) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreen.route
    ) {
        composable(Screen.SplashScreen.route) {
            SplashScreen(navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController, gameViewModel)
        }

        composable(Screen.TeamManager.route) {
            val context = LocalContext.current
            val teamStore = remember { TeamDataStore(context) }

            TeamManagementScreen(
                navController = navController,
                teamStore = teamStore
            )
        }

        composable(Screen.LineUp.route) {
            val roster = gameViewModel.gameState.roster
            if (roster != null) {
                val isRosterComplete = roster.players.size >= 9
                
                BaseballScreenTemplate {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        LineupScreen(roster = roster)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        if (!isRosterComplete) {
                            Text(
                                text = "L'alignement doit comporter au moins 9 joueurs.",
                                color = androidx.compose.ui.graphics.Color.Red,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        HudButton(
                            text = "COMMENCER LE MATCH",
                            enabled = isRosterComplete,
                            onClick = {
                                gameViewModel.startGame()
                                navController.navigate(Screen.Game.route) {
                                    popUpTo(Screen.Home.route)
                                }
                            }
                        )
                    }
                }
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
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
                    scope.launch {
                        val csvData = gameViewModel.generateCsv()
                        AlignementRepository.uploadCSV(context, csvData)
                            .onSuccess { success ->
                                if (success) {
                                    android.widget.Toast.makeText(context, "Statistiques envoyées avec succès !", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Erreur serveur lors de l'envoi", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                            .onFailure { error ->
                                android.widget.Toast.makeText(context, "Échec : ${error.message}", android.widget.Toast.LENGTH_LONG).show()
                            }
                    }
                },
                editEventPlayerIndex = editEventPlayerIndex,
                editEventInning = editEventInning,
                editEventIsHomeTeam = editEventIsHomeTeam
            )
        }

        composable(Screen.StatsSheet.route) {
            StatsSheetScreen(navController = navController, gameState = gameViewModel.gameState, roster = gameViewModel.gameState.roster)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }

        composable(Screen.Help.route) {
            HelpScreen(navController)
        }
    }
}
