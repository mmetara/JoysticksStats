package com.joysticks.stats.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.navigation.NavController
import com.joysticks.stats.engine.GameScreenMode
import com.joysticks.stats.ui.navigation.Screen
import com.joysticks.stats.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatsSheetScreen(navController: NavController, gameState: GameState, roster: Roster?) {
    roster ?: return

    var isEditingMode by remember { mutableStateOf(false) }

    val maxInnings = 9
    val playerNamesWidth = 140.dp
    val posColumnWidth = 50.dp
    val inningCellWidth = 85.dp
    val headerHeight = 48.dp

    val horizontalScrollState = rememberScrollState()

    BaseballScreenTemplate {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "FEUILLE DE MATCH",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ChalkWhite,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Box(Modifier.width(80.dp).height(4.dp).background(FieldGreen))
                    
                    Text(
                        text = if (isEditingMode) "MODE ÉDITION ACTIF" else "MODE CONSULTATION",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isEditingMode) HudRed else HudMuted,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Edit Toggle Button (HUD Style)
                    Surface(
                        modifier = Modifier.clickable { isEditingMode = !isEditingMode },
                        color = if (isEditingMode) HudRed else HudPanel,
                        shape = RoundedCornerShape(8.dp),
                        border = borderStroke(1.dp, if (isEditingMode) HudRed else HudBorder)
                    ) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, null, tint = ChalkWhite, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isEditingMode) "QUITTER ÉDITION" else "MODIFIER", color = ChalkWhite, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }

                    if (gameState.screenMode != GameScreenMode.PRE_GAME) {
                        Surface(
                            modifier = Modifier.clickable { navController.navigate(Screen.Game.route) },
                            color = HudBlue,
                            shape = RoundedCornerShape(8.dp),
                            border = borderStroke(1.dp, HudBorder)
                        ) {
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ChalkWhite, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("RETOUR PARTIE", color = ChalkWhite, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats Table
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = HudPanel.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp),
                border = borderStroke(1.dp, HudBorder)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        stickyHeader {
                            Row(modifier = Modifier.background(HudPanelSoft).horizontalScroll(horizontalScrollState)) {
                                TableHeaderCell("JOUEUR", playerNamesWidth, headerHeight, Alignment.CenterStart)
                                TableHeaderCell("POS", posColumnWidth, headerHeight)
                                repeat(maxInnings) { inningNum ->
                                    TableHeaderCell("${inningNum + 1}", inningCellWidth, headerHeight)
                                }
                            }
                            HorizontalDivider(thickness = 1.dp, color = HudBorder)
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
                                isEditingMode = isEditingMode,
                                horizontalScrollState = horizontalScrollState,
                                onEditEventClick = { event ->
                                    navController.navigate(Screen.Game.route + "?editEventPlayerIndex=${event.playerIndex}&editEventInning=${event.inning}&editEventIsHomeTeam=${event.isHomeTeam}")
                                }
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = HudBorder.copy(alpha = 0.3f))
                        }

                        item {
                            val isUserHome = roster.gameInfo.isHomeTeam
                            InningScoreSummaryRow(
                                label = roster.gameInfo.awayTeamName.uppercase(),
                                runsPerInning = if (isUserHome) gameState.opponentRunsPerInning else (1..maxInnings).associateWith { inv ->
                                    gameState.gameHistory.count { it.inning == inv && it.finalBase == 4 && it.isHomeTeam == false }
                                },
                                maxInnings = maxInnings,
                                inningCellWidth = inningCellWidth,
                                playerNamesWidth = playerNamesWidth,
                                posColumnWidth = posColumnWidth,
                                horizontalScrollState = horizontalScrollState,
                                accentColor = if (isUserHome) HudOrange else FieldGreen
                            )
                        }

                        item {
                            val isUserHome = roster.gameInfo.isHomeTeam
                            val userRuns = (1..maxInnings).associateWith { inv ->
                                gameState.gameHistory.count { it.inning == inv && it.finalBase == 4 && it.isHomeTeam == isUserHome }
                            }
                            InningScoreSummaryRow(
                                label = roster.gameInfo.homeTeamName.uppercase(),
                                runsPerInning = if (isUserHome) userRuns else gameState.opponentRunsPerInning,
                                maxInnings = maxInnings,
                                inningCellWidth = inningCellWidth,
                                playerNamesWidth = playerNamesWidth,
                                posColumnWidth = posColumnWidth,
                                horizontalScrollState = horizontalScrollState,
                                accentColor = if (isUserHome) FieldGreen else HudOrange
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, width: Dp, height: Dp, alignment: Alignment = Alignment.Center) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .border(0.5.dp, HudBorder),
        contentAlignment = alignment
    ) {
        Text(
            text = text,
            color = HudMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            modifier = if (alignment == Alignment.CenterStart) Modifier.padding(start = 12.dp) else Modifier
        )
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
    isEditingMode: Boolean,
    horizontalScrollState: androidx.compose.foundation.ScrollState,
    onEditEventClick: (AtBatEvent) -> Unit
) {
    Row(modifier = Modifier.height(54.dp).horizontalScroll(horizontalScrollState)) {
        // Player Name
        Box(
            modifier = Modifier
                .width(playerNamesWidth)
                .fillMaxHeight()
                .background(HudPanel.copy(alpha = 0.3f))
                .border(0.5.dp, HudBorder)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = player.playerName.uppercase(),
                fontSize = 12.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Black,
                color = ChalkWhite,
                maxLines = 2,
                softWrap = true
            )
        }

        // Position
        Box(
            modifier = Modifier
                .width(posColumnWidth)
                .fillMaxHeight()
                .border(0.5.dp, HudBorder),
            contentAlignment = Alignment.Center
        ) {
            Text(player.posDef, fontSize = 12.sp, fontWeight = FontWeight.Black, color = FieldGreen)
        }

        // Innings
        repeat(maxInnings) { inningNum ->
            val currentInning = inningNum + 1
            val playerEventsInInning = gameHistory.filter {
                it.playerIndex == player.index && it.inning == currentInning && it.isHomeTeam == isUserTeamHomeTeam
            }
            Box(
                modifier = Modifier
                    .width(inningCellWidth)
                    .fillMaxHeight()
                    .border(0.5.dp, HudBorder)
                    .then(if (isEditingMode && playerEventsInInning.isNotEmpty()) {
                        Modifier.clickable { onEditEventClick(playerEventsInInning.last()) }.background(HudRed.copy(alpha = 0.1f))
                    } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                if (playerEventsInInning.isNotEmpty()) {
                    InningResultDiamond(
                        eventsInInning = playerEventsInInning,
                        modifier = Modifier.fillMaxSize(),
                        cellSize = inningCellWidth
                    )
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
    posColumnWidth: Dp,
    horizontalScrollState: androidx.compose.foundation.ScrollState,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .height(48.dp)
            .background(HudPanelSoft.copy(alpha = 0.5f))
            .horizontalScroll(horizontalScrollState)
    ) {
        Box(
            modifier = Modifier
                .width(playerNamesWidth + posColumnWidth)
                .fillMaxHeight()
                .border(0.5.dp, HudBorder),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(label, fontWeight = FontWeight.Black, fontSize = 12.sp, color = accentColor, modifier = Modifier.padding(start = 12.dp))
        }

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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$runs / $runningTotal", fontWeight = FontWeight.Black, fontSize = 12.sp, color = ChalkWhite)
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
        event.result in listOf(BattingResult.Single, BattingResult.Double, BattingResult.Triple, BattingResult.HomeRun, BattingResult.Optionel, BattingResult.Strikeout, BattingResult.Out)
    }

    val resultText = primaryBatterEvent?.let { getShortResult(it.result) } ?: ""
    val finalBaseForDiamond = eventsInInning.maxOf { it.finalBase }
    val isScored = eventsInInning.any { it.finalBase == 4 }
    val retiredOnOptionelCount = eventsInInning.count { it.retiredOnOptionel }
    val textMeasurer = rememberTextMeasurer()
    val activeColor = FieldGreen

    Box(modifier = modifier.padding(4.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width * 0.35f
            val centerY = size.height / 2
            val radius = size.height * 0.32f

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
            drawPath(pathBase, HudBorder.copy(alpha = 0.5f), style = Stroke(width = 1.5f))

            // Dessin des lignes de course
            if (finalBaseForDiamond >= 1) drawLine(activeColor, home, first, strokeWidth = 4f)
            if (finalBaseForDiamond >= 2) drawLine(activeColor, first, second, strokeWidth = 4f)
            if (finalBaseForDiamond >= 3) drawLine(activeColor, second, third, strokeWidth = 4f)
            if (finalBaseForDiamond >= 4) drawLine(activeColor, third, home, strokeWidth = 4f)

            if (isScored) {
                drawPath(pathBase, activeColor.copy(alpha = 0.8f))
            }

            // 1. R si OUT (BattingResult.Out) - En ROUGE et moins gras
            val isOutResult = primaryBatterEvent?.result == BattingResult.Out
            if (isOutResult) {
                val outRText = "R"
                val outRLayout = textMeasurer.measure(
                    AnnotatedString(outRText),
                    style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = HudRed.copy(alpha = 0.7f))
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = outRText,
                    topLeft = Offset(centerX - outRLayout.size.width / 2, centerY - outRLayout.size.height / 2),
                    style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = HudRed.copy(alpha = 0.7f))
                )
            }

            fun DrawScope.drawSmallBasePoint(pos: Offset, isActive: Boolean) {
                val bSize = 6f
                rotate(45f, pivot = pos) {
                    drawRect(
                        color = if (isActive) activeColor else Color.Transparent,
                        topLeft = Offset(pos.x - bSize / 2, pos.y - bSize / 2),
                        size = Size(bSize, bSize)
                    )
                }
            }
            if (finalBaseForDiamond in 1..3) {
                drawSmallBasePoint(if(finalBaseForDiamond==1) first else if(finalBaseForDiamond==2) second else third, true)
            }

            if (!isOutResult && resultText.isNotBlank()) {
                val textLayoutResult = textMeasurer.measure(AnnotatedString(resultText), style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Black, color = ChalkWhite))
                drawText(
                    textMeasurer = textMeasurer,
                    text = resultText,
                    topLeft = Offset(size.width - textLayoutResult.size.width, size.height - textLayoutResult.size.height),
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Black, color = ChalkWhite)
                )
            }

            val totalRbis = eventsInInning.sumOf { it.rbi }
            if (totalRbis > 0) {
                val rbiText = totalRbis.toString()
                val rbiLayout = textMeasurer.measure(AnnotatedString(rbiText), style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black))
                val rbiCircleCenter = Offset(size.width * 0.85f, size.height * 0.15f)
                
                // Cercle blanc avec bordure noire pour les RBI
                drawCircle(Color.White, radius = 8.dp.toPx(), center = rbiCircleCenter)
                drawCircle(Color.Black, radius = 8.dp.toPx(), center = rbiCircleCenter, style = Stroke(width = 1f))

                drawText(textMeasurer, rbiText, Offset(rbiCircleCenter.x - rbiLayout.size.width/2, rbiCircleCenter.y - rbiLayout.size.height/2), style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black))
            }

            if (retiredOnOptionelCount > 0) {
                val rText = "R"
                drawText(
                    textMeasurer = textMeasurer,
                    text = rText,
                    topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Black, color = HudRed)
                )
            }
        }
    }
}

private fun borderStroke(width: Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
