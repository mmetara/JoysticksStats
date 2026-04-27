package com.joysticks.stats.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable // Added import for clickable modifier
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.joysticks.stats.engine.GameState
import com.joysticks.stats.engine.PlayerStats
import com.joysticks.stats.engine.Roster
import com.joysticks.stats.engine.AtBatEvent
import com.joysticks.stats.engine.BattingResult
import com.joysticks.stats.utils.getShortResult
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Added import
import androidx.compose.material3.Icon
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.joysticks.stats.engine.GameScreenMode // Added import
import com.joysticks.stats.ui.theme.ChalkWhite
import com.joysticks.stats.ui.theme.FieldGreen
import com.joysticks.stats.ui.theme.HudBlue
import com.joysticks.stats.ui.theme.HudBorder
import com.joysticks.stats.ui.theme.HudMuted
import com.joysticks.stats.ui.theme.HudPanel
import com.joysticks.stats.ui.theme.HudPanelSoft

@OptIn(ExperimentalFoundationApi::class)


@Composable
fun StatsSheetScreen(navController: NavController, gameState: GameState, roster: Roster?) {
    roster ?: return

    var isEditingMode by remember { mutableStateOf(false) }

    val maxInnings = 9
    val playerNamesWidth = 120.dp
    val posColumnWidth = 50.dp
    val inningCellWidth = 80.dp
    val headerHeight = 40.dp

    val horizontalScrollState = rememberScrollState()

    BaseballScreenTemplate {
    Box(modifier = Modifier.fillMaxSize().padding(10.dp).background(HudPanel.copy(alpha = 0.42f))) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "FEUILLE DE MATCH",
                style = MaterialTheme.typography.headlineMedium,
                color = ChalkWhite,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 14.dp, top = 12.dp)
            )

            Text(
                text = if (isEditingMode) "MODE EDITION ACTIF" else "MODE LECTURE",
                style = MaterialTheme.typography.labelMedium,
                color = if (isEditingMode) FieldGreen else HudMuted,
                modifier = Modifier.padding(start = 14.dp, top = 2.dp, bottom = 10.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(modifier = Modifier.fillMaxSize()) {
        val columnTitles = @Composable {
            Row(modifier = Modifier.fillMaxWidth().height(headerHeight).horizontalScroll(horizontalScrollState)) {
                Box(
                    modifier = Modifier
                        .width(playerNamesWidth)
                        .height(headerHeight)
                        .background(HudPanelSoft)
                        .border(0.5.dp, HudBorder),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("Joueur", fontWeight = FontWeight.Bold, color = ChalkWhite, modifier = Modifier.padding(start = 4.dp))
                }

                Box(
                    modifier = Modifier
                        .width(posColumnWidth)
                        .height(headerHeight)
                        .background(HudPanelSoft)
                        .border(0.5.dp, HudBorder),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Pos.", fontWeight = FontWeight.Bold, color = ChalkWhite)
                }

                Row {
                    repeat(maxInnings) { inningNum ->
                        Box(
                            modifier = Modifier
                                .width(inningCellWidth)
                                .height(headerHeight)
                                .background(HudPanelSoft)
                                .border(0.5.dp, HudBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${inningNum + 1}", fontWeight = FontWeight.Bold, color = ChalkWhite)
                        }
                    }
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
            stickyHeader {
                columnTitles()
                HorizontalDivider(thickness = 2.dp, color = HudBorder)
            }

            items(roster.players) { player ->
                PlayerStatsRow(
                    player = player,
                    maxInnings = maxInnings,
                    inningCellWidth = inningCellWidth,
                    playerNamesWidth = playerNamesWidth,
                    posColumnWidth = posColumnWidth,
                    gameHistory = gameState.gameHistory,
                    isUserTeamHomeTeam = roster.gameInfo.isHomeTeam,
                    isEditingMode = isEditingMode, // Pass the editing mode state
                    onEditEventClick = { event ->
                        // TODO: Implement navigation to BattingScreen with event details
                        navController.navigate("game?editEventPlayerIndex=${event.playerIndex}&editEventInning=${event.inning}&editEventIsHomeTeam=${event.isHomeTeam}")
                    }
                )
            }

            item {
                HorizontalDivider(thickness = 2.dp, color = HudBorder)
            }

            item {
                val userRunsPerInning = (1..maxInnings).associateWith { inning ->
                    gameState.gameHistory.count { event ->
                        event.inning == inning && event.finalBase == 4 && event.isHomeTeam == !gameState.isTop
                    }
                }

                InningScoreSummaryRow(
                    label = "Total :",
                    runsPerInning = userRunsPerInning,
                    maxInnings = maxInnings,
                    inningCellWidth = inningCellWidth,
                    playerNamesWidth = playerNamesWidth,
                    posColumnWidth = posColumnWidth
                )
            }

            item {
                InningScoreSummaryRow(
                    label = "Adv. :",
                    runsPerInning = gameState.opponentRunsPerInning,
                    maxInnings = maxInnings,
                    inningCellWidth = inningCellWidth,
                    playerNamesWidth = playerNamesWidth,
                    posColumnWidth = posColumnWidth
                )
            }
        }

        // Floating Action Button for Edit Mode
        FloatingActionButton(
            onClick = { isEditingMode = !isEditingMode },
            modifier = Modifier
                .align(Alignment.TopEnd) // Changed to TopEnd
                .padding(16.dp),
            containerColor = if (isEditingMode) Color(0xFFE91E63) else FieldGreen,
            contentColor = ChalkWhite
        ) {
            Icon(Icons.Filled.Edit, "Mode Édition")
        }

        val isGameInProgress = gameState.roster != null &&
                gameState.screenMode != GameScreenMode.PRE_GAME &&
                gameState.screenMode != GameScreenMode.GAME_OVER // Check if game is not in pre-game or game-over modes

        if (isGameInProgress) {
            FloatingActionButton(
                onClick = { navController.navigate("game") }, // Navigate directly to game screen
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Changed to BottomEnd
                    .padding(16.dp),
                containerColor = HudBlue,
                contentColor = ChalkWhite
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour à la partie")
            }
        }
            }
        }
    }
    }
}

@Composable
fun PlayerStatsRow(
    player: PlayerStats,
    maxInnings: Int,
    inningCellWidth: Dp,
    playerNamesWidth: Dp,
    posColumnWidth: Dp,
    gameHistory: List<AtBatEvent>,
    isUserTeamHomeTeam: Boolean,
    isEditingMode: Boolean, // Added parameter
    onEditEventClick: (AtBatEvent) -> Unit // Added parameter
) {
    Row(modifier = Modifier.fillMaxWidth().height(40.dp)) {
        Box(
            modifier = Modifier
                .width(playerNamesWidth)
                .height(40.dp)
                .background(HudPanelSoft)
                .border(0.5.dp, HudBorder)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(player.playerName, fontSize = 12.sp, lineHeight = 14.sp, color = ChalkWhite)
        }

        Box(
            modifier = Modifier
                .width(posColumnWidth)
                .height(40.dp)
                .background(HudPanelSoft)
                .border(0.5.dp, HudBorder),
            contentAlignment = Alignment.Center
        ) {
            Text(player.posDef, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FieldGreen)
        }

        Row {
            repeat(maxInnings) { inningNum ->
                val currentInning = inningNum + 1
                val playerEventsInInning = gameHistory.filter {
                    it.playerIndex == player.index && it.inning == currentInning && it.isHomeTeam == isUserTeamHomeTeam
                }
                Box(
                    modifier = Modifier
                        .width(inningCellWidth)
                        .height(40.dp)
                        .background(HudPanel.copy(alpha = 0.55f))
                        .border(0.5.dp, HudBorder)
                        .then(if (isEditingMode && playerEventsInInning.isNotEmpty()) { // Only clickable if in edit mode and there are events
                            Modifier.clickable {
                                // Get the last event for this player in this inning to send for editing
                                val eventToEdit = playerEventsInInning.last()
                                onEditEventClick(eventToEdit)
                            }
                        } else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    if (playerEventsInInning.isNotEmpty()) {
                        InningResultDiamond(
                            eventsInInning = playerEventsInInning,
                            cellSize = inningCellWidth,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Optionally display a placeholder or empty text if no event
                        // Text("", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
@Composable
fun InningScoreSummaryRow(
    label: String,
    runsPerInning: Map<Int, Int>,
    maxInnings: Int,
    inningCellWidth: Dp,
    playerNamesWidth: Dp,
    posColumnWidth: Dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(HudPanelSoft)
    ) {
        Box(
            modifier = Modifier
                .width(playerNamesWidth + posColumnWidth)
                .fillMaxHeight()
                .border(0.5.dp, HudBorder),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(label, fontWeight = FontWeight.Bold, color = ChalkWhite, modifier = Modifier.padding(start = 4.dp))
        }

        Row {
            var runningTotal = 0
            repeat(maxInnings) { inningNum ->
                val currentInning = inningNum + 1
                val runs = runsPerInning[currentInning] ?: 0
                runningTotal += runs
                Box(
                    modifier = Modifier
                        .width(inningCellWidth)
                        .fillMaxHeight()
                        .border(0.5.dp, HudBorder),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${runs} / ${runningTotal}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ChalkWhite)
                }
            }
        }
    }
}

@Composable
fun InningResultDiamond(
    eventsInInning: List<AtBatEvent>,
    modifier: Modifier = Modifier,
    cellSize: Dp
) {
    if (eventsInInning.isEmpty()) return

    val primaryBatterEvent = eventsInInning.lastOrNull { event ->
        event.result == BattingResult.Single ||
        event.result == BattingResult.Double ||
        event.result == BattingResult.Triple ||
        event.result == BattingResult.HomeRun ||
        event.result == BattingResult.Optionel ||
        event.result == BattingResult.Strikeout ||
        event.result == BattingResult.Out
    }

    val resultText = primaryBatterEvent?.let { getShortResult(it.result) } ?: ""

    val finalBaseForDiamond = primaryBatterEvent?.finalBase ?: 0

    val retiredOnOptionelCount = eventsInInning.count { it.retiredOnOptionel }

    val textMeasurer = rememberTextMeasurer()
    val activeColor = FieldGreen

    Box(modifier = modifier.size(cellSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width * 0.3f
            val centerY = size.height / 2
            val radius = size.width * 0.15f

            val home = Offset(centerX, centerY + radius)
            val first = Offset(centerX + radius, centerY)
            val second = Offset(centerX, centerY - radius)
            val third = Offset(centerX - radius, centerY)

            val pathBase = Path().apply {
                moveTo(home.x, home.y)
                lineTo(first.x, first.y)
                lineTo(second.x, second.y)
                lineTo(third.x, third.y)
                close()
            }
            drawPath(pathBase, HudMuted.copy(alpha = 0.45f), style = Stroke(width = 2f))

            when (finalBaseForDiamond) {
                1 -> drawLine(activeColor, home, first, strokeWidth = 5f)
                2 -> {
                    drawLine(activeColor, home, first, strokeWidth = 5f)
                    drawLine(activeColor, first, second, strokeWidth = 5f)
                }
                3 -> {
                    drawLine(activeColor, home, first, strokeWidth = 5f)
                    drawLine(activeColor, first, second, strokeWidth = 5f)
                    drawLine(activeColor, second, third, strokeWidth = 5f)
                }
                4 -> drawPath(pathBase, activeColor)
            }

            fun DrawScope.drawSmallBasePoint(pos: Offset, isActive: Boolean) {
                val bSize = 8f
                rotate(45f, pivot = pos) {
                    drawRect(
                        color = if (isActive) activeColor else Color.Black.copy(alpha = 0.5f),
                        topLeft = Offset(pos.x - bSize / 2, pos.y - bSize / 2),
                        size = Size(bSize, bSize)
                    )
                }
            }
            when (finalBaseForDiamond) {
                1 -> drawSmallBasePoint(first, true)
                2 -> drawSmallBasePoint(second, true)
                3 -> drawSmallBasePoint(third, true)
            }

            if (resultText.isNotBlank()) {
                val textLayoutResult = textMeasurer.measure(AnnotatedString(resultText), style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ChalkWhite))
                drawText(
                    textMeasurer = textMeasurer,
                    text = resultText,
                    topLeft = Offset(
                        x = size.width - textLayoutResult.size.width - 4.dp.toPx(),
                        y = size.height - textLayoutResult.size.height - 4.dp.toPx()
                    ),
                    style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ChalkWhite)
                )
            }

            val totalRbis = eventsInInning.sumOf { it.rbi }
            if (totalRbis > 0) {
                val rbiCountText = totalRbis.toString()
                val rbiTextLayoutResult = textMeasurer.measure(AnnotatedString(rbiCountText), style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Black))
                val rbiCircleRadius = (rbiTextLayoutResult.size.width / 2f).coerceAtLeast(rbiTextLayoutResult.size.height / 2f) + 4.dp.toPx()

                val rbiCircleCenter = Offset(size.width * 0.75f, size.height * 0.25f)
                
                drawCircle(FieldGreen.copy(alpha = 0.4f), radius = rbiCircleRadius + 1.dp.toPx(), center = rbiCircleCenter, style = Stroke(width = 1.dp.toPx()))
                drawCircle(Color(0xFF17202C), radius = rbiCircleRadius, center = rbiCircleCenter)
                drawText(
                    textMeasurer = textMeasurer,
                    text = rbiCountText,
                    topLeft = Offset(
                        x = rbiCircleCenter.x - rbiTextLayoutResult.size.width / 2f,
                        y = rbiCircleCenter.y - rbiTextLayoutResult.size.height / 2f
                    ),
                    style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Black, color = ChalkWhite)
                )
            }

            if (retiredOnOptionelCount > 0) {
                val rText = if (retiredOnOptionelCount > 1) "R x$retiredOnOptionelCount" else "R"
                val rTextLayoutResult = textMeasurer.measure(
                    AnnotatedString(rText),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Red)
                )

                val rX = 4.dp.toPx()
                val rY = 4.dp.toPx()

                drawText(
                    textMeasurer = textMeasurer,
                    text = rText,
                    topLeft = Offset(rX, rY),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Red)
                )
            }
        }
    }
}