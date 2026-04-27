package com.joysticks.stats.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.joysticks.stats.engine.GameScreenMode
import com.joysticks.stats.engine.GameViewModel
import com.joysticks.stats.engine.parseRoster
import com.joysticks.stats.ui.components.HudButton
import com.joysticks.stats.ui.components.HudPanel
import com.joysticks.stats.ui.components.Scoreboard
import com.joysticks.stats.ui.theme.ChalkWhite
import com.joysticks.stats.ui.theme.FieldGreen
import com.joysticks.stats.ui.theme.HudBlue
import com.joysticks.stats.ui.theme.HudOrange
import com.joysticks.stats.utils.getCountdownText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameScreen(
    navController: NavController,
    gameViewModel: GameViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToStatsSheet: () -> Unit,
    onExportPdf: () -> Unit,
    editEventPlayerIndex: Int = -1,
    editEventInning: Int = -1,
    editEventIsHomeTeam: Boolean = false
) {

    val context = LocalContext.current
    val state = gameViewModel.gameState
    val roster = state.roster
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->

        uri?.let {

            scope.launch {
                val parsedRoster = parseRoster(context, uri)
                gameViewModel.loadRoster(parsedRoster)
            }
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        uri?.let {
            scope.launch {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val csvContent = gameViewModel.generateCsv()
                    outputStream.write(csvContent.toByteArray())
                }
            }
        }
    }

    if (roster == null) {
        BaseballScreenTemplate {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                HudPanel(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "AUCUN ALIGNEMENT CHARGE",
                        style = MaterialTheme.typography.titleLarge,
                        color = ChalkWhite,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(12.dp))
                    HudButton(
                        text = "Importer l'alignement",
                        onClick = { launcher.launch(arrayOf("text/*")) }
                    )
                }
            }
        }

    } else {
        var now by remember { mutableStateOf(System.currentTimeMillis()) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                now = System.currentTimeMillis()
            }
        }

        val gameStart = roster.gameInfo.gameTimeMillis
        val canStartGame = now >= gameStart

        Box(modifier = Modifier.fillMaxSize())
        {

            BaseballScreenTemplate {

                Box(modifier = Modifier.fillMaxSize()) {

                    if (state.maxRunsReached) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Limite de 3 points atteinte.",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                HudButton(
                                    text = "Ok",
                                    onClick = { gameViewModel.forceEndHalfInningFromUI() }
                                )
                            }
                        }
                    }

                    when (state.screenMode) {

                        GameScreenMode.PRE_GAME -> {

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp)
                            ) {

                                LineupScreen(roster)

                                Spacer(Modifier.height(20.dp))

                                HudPanel {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Début du match dans : ", color = ChalkWhite)

                                        Text(
                                            text = getCountdownText(gameStart, now),
                                            fontWeight = FontWeight.Bold,
                                            color = FieldGreen
                                        )
                                    }
                                }

                                Spacer(Modifier.height(20.dp))

                                HudButton(
                                    text = "Play Ball",
                                    onClick = { gameViewModel.startGame() },
                                    enabled = canStartGame || gameViewModel.devMode
                                )
                            }
                        }

                        GameScreenMode.OPPONENT_SCORING -> {
                            OpponentScorePanel( gameState = state,
                                onScoreEntered = {  runs ->
                                    gameViewModel.endOpponentHalfInning(runs)
                                }
                            )
                        }

                        GameScreenMode.BATTING -> {

                            BattingPanel(
                                state = state,
                                onAction = { result ->
                                    gameViewModel.handleBattingResult(result)
                                },
                                onNextBatter = { gameViewModel.nextBatter() },
                                onPreviousBatter = { gameViewModel.previousBatter() },
                                onManualAdvance = { idx -> gameViewModel.manualAdvancePlayer(idx) },
                                onManualRetreat = { idx -> gameViewModel.manualRetreatPlayer(idx) },
                                onManualRemove = { idx -> gameViewModel.manualRemovePlayer(idx) },
                                onManualAddRBI = { idx -> gameViewModel.manualAddRBI(idx) },
                                onManualRemoveRBI = { idx -> gameViewModel.manualRemoveRBI(idx) },
                                onStatsClick = onNavigateToStatsSheet,
                                editEventPlayerIndex = editEventPlayerIndex,
                                editEventInning = editEventInning,
                                editEventIsHomeTeam = editEventIsHomeTeam,
                                onSaveEdit = { editedEvent ->
                                    gameViewModel.editAtBatEvent(editedEvent)
                                    navController.popBackStack() // Go back to StatsSheetScreen
                                    navController.navigate("statsSheet") // Re-navigate to refresh stats
                                },
                                onCancelEdit = {
                                    navController.popBackStack() // Go back to StatsSheetScreen
                                    navController.navigate("statsSheet") // Re-navigate to refresh stats
                                }
                            )
                        }
                        GameScreenMode.GAME_OVER -> {
                            GameOverPanel(
                                state = state,
                                onNewGame = {
                                    gameViewModel.resetGame()
                                },
                                onExportCsv = { createDocumentLauncher.launch("game_stats.txt") },
                                onExportPdf = onExportPdf
                            )
                        }
                    }

                    if (state.screenMode != GameScreenMode.PRE_GAME && state.screenMode != GameScreenMode.BATTING) {
                        Scoreboard(
                            state = state,
                            roster = roster,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                            onStatsClick = onNavigateToStatsSheet
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameOverPanel(state: com.joysticks.stats.engine.GameState, onNewGame: () -> Unit, onExportCsv: () -> Unit, onExportPdf: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HudPanel(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "PARTIE TERMINÉE",
                style = MaterialTheme.typography.headlineLarge,
                color = ChalkWhite,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Score final: ${state.homeScore} - ${state.awayScore}",
                style = MaterialTheme.typography.headlineMedium,
                color = FieldGreen,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            HudButton(
                text = "Exporter les statistiques CSV",
                onClick = onExportCsv,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            HudButton(
                text = "Exporter en PDF",
                onClick = onExportPdf,
                accent = HudBlue,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            HudButton(
                text = "Nouvelle partie",
                onClick = onNewGame,
                accent = HudOrange
            )
        }
    }
}
