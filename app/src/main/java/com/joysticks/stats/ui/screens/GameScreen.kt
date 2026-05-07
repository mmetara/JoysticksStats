package com.joysticks.stats.ui.screens

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextAlign
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.joysticks.stats.engine.GameScreenMode
import com.joysticks.stats.ui.navigation.Screen
import com.joysticks.stats.engine.GameViewModel
import com.joysticks.stats.engine.parseRoster
import com.joysticks.stats.ui.components.BaseballScreenTemplate
import com.joysticks.stats.ui.components.HudButton
import com.joysticks.stats.ui.components.HudPanel
import com.joysticks.stats.ui.components.Scoreboard
import com.joysticks.stats.ui.theme.ChalkWhite
import com.joysticks.stats.ui.theme.FieldGreen
import com.joysticks.stats.ui.theme.HudBlue
import com.joysticks.stats.ui.theme.HudOrange
import com.joysticks.stats.ui.theme.HudRed
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
    onUploadCsv: () -> Unit,
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
                                Spacer(Modifier.height(8.dp))
                                HudPanel(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        val annotatedGameInfo = buildAnnotatedString {
                                            append("Partie du : ")
                                            withStyle(style = SpanStyle(color = FieldGreen, fontWeight = FontWeight.Bold)) {
                                                append(roster.gameInfo.gameDate)
                                                append(" à ")
                                                append(roster.gameInfo.gameTime)
                                            }
                                            append(". Début du match dans : ")
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = annotatedGameInfo, color = ChalkWhite, fontSize = 14.sp)
                                            Text(
                                                text = getCountdownText(gameStart, now),
                                                fontWeight = FontWeight.Bold,
                                                color = FieldGreen,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
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
                                    navController.popBackStack()
                                    navController.navigate(Screen.StatsSheet.route)
                                },
                                onCancelEdit = {
                                    navController.popBackStack()
                                    navController.navigate(Screen.StatsSheet.route)
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
                                onExportPdf = onExportPdf,
                                onUploadCsv = onUploadCsv,
                                onBackToHome = onNavigateToHome
                            )
                        }
                    }

                    if (state.maxRunsReached || state.threeOutsReached) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(24.dp)
                        ) {
                            HudPanel(
                                modifier = Modifier.widthIn(max = 300.dp),
                                borderColor = if (state.threeOutsReached) HudRed else HudOrange
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = if (state.threeOutsReached) "3 RETRAITS" else "LIMITE 3 PTS",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (state.threeOutsReached) HudRed else HudOrange,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    HudButton(
                                        text = "FINIR MANCHE",
                                        onClick = { gameViewModel.forceEndHalfInningFromUI() },
                                        accent = if (state.threeOutsReached) HudRed else HudOrange,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    if (state.screenMode != GameScreenMode.PRE_GAME && 
                        state.screenMode != GameScreenMode.BATTING && 
                        state.screenMode != GameScreenMode.GAME_OVER) {
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
fun GameOverPanel(
    state: com.joysticks.stats.engine.GameState,
    onNewGame: () -> Unit,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit,
    onUploadCsv: () -> Unit,
    onBackToHome: () -> Unit
) {
    var showConfirmReset by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HudPanel(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(min = 320.dp, max = 440.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "PARTIE TERMINÉE",
                    style = MaterialTheme.typography.headlineLarge,
                    color = ChalkWhite,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Score final: ${state.homeScore} - ${state.awayScore}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = FieldGreen,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                HudButton(
                    text = "Exporter les statistiques CSV",
                    onClick = onExportCsv,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
                HudButton(
                    text = "Envoyer vers le site Web",
                    onClick = onUploadCsv,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
                HudButton(
                    text = "Exporter en PDF",
                    onClick = onExportPdf,
                    accent = HudBlue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                HudButton(
                    text = "RETOUR À L'ACCUEIL",
                    onClick = onBackToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
                
                Spacer(Modifier.height(16.dp))
                
                if (!showConfirmReset) {
                    HudButton(
                        text = "EFFACER ET NOUVEAU MATCH",
                        onClick = { showConfirmReset = true },
                        accent = HudOrange,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Êtes-vous sûr ? Cela supprimera la sauvegarde.",
                            color = HudOrange,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HudButton(
                                text = "OUI, EFFACER",
                                onClick = onNewGame,
                                accent = HudRed,
                                modifier = Modifier.weight(1f)
                            )
                            HudButton(
                                text = "NON",
                                onClick = { showConfirmReset = false },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
