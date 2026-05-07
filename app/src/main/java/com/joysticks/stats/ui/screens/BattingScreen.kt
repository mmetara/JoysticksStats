package com.joysticks.stats.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joysticks.stats.engine.BattingResult
import com.joysticks.stats.engine.GameState
import com.joysticks.stats.ui.components.HudActionButton
import com.joysticks.stats.ui.components.PlayerAvatar
import com.joysticks.stats.ui.components.Scoreboard
import com.joysticks.stats.ui.theme.ChalkWhite
import com.joysticks.stats.ui.theme.FieldGreen
import com.joysticks.stats.ui.theme.FieldGreenLight
import com.joysticks.stats.ui.theme.HudBlue
import com.joysticks.stats.ui.theme.HudBorder
import com.joysticks.stats.ui.theme.HudMuted
import com.joysticks.stats.ui.theme.HudOrange
import com.joysticks.stats.ui.theme.HudPanel
import com.joysticks.stats.ui.theme.HudRed
import com.joysticks.stats.utils.getShortResult
import com.joysticks.stats.engine.AtBatEvent

@Composable
fun DiamondView(state: GameState, rbiCount: Int, isRetiredOnOptionel: Boolean, modifier: Modifier = Modifier) {
    val playerIdx = state.currentBatterIndex
    val hasScored = state.playersWhoScored.contains(playerIdx)
    val isOnThird = state.runnerOnThird == playerIdx
    val isOnSecond = state.runnerOnSecond == playerIdx
    val isOnFirst = state.runnerOnFirst == playerIdx

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    Box(
        modifier = modifier.padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.width * 0.42f

            val home = Offset(centerX, centerY + radius * 0.98f)
            val first = Offset(centerX + radius * 1.06f, centerY)
            val second = Offset(centerX, centerY - radius * 0.92f)
            val third = Offset(centerX - radius * 1.06f, centerY)

            val pathBase = Path().apply {
                moveTo(home.x, home.y)
                lineTo(first.x, first.y)
                lineTo(second.x, second.y)
                lineTo(third.x, third.y)
                close()
            }
            val shadowPath = Path().apply {
                moveTo(home.x, home.y + 12f)
                lineTo(first.x, first.y + 12f)
                lineTo(second.x, second.y + 12f)
                lineTo(third.x, third.y + 12f)
                close()
            }
            drawPath(shadowPath, Color.Black.copy(alpha = 0.35f))
            drawPath(
                pathBase,
                Brush.verticalGradient(
                    listOf(Color(0xD01A2029), Color(0xB7111720), Color(0xD00A0F16))
                )
            )
            drawPath(pathBase, Color.White.copy(alpha = 0.35f), style = Stroke(width = 2.5f))
            drawLine(Color.White.copy(alpha = 0.15f), third, first, strokeWidth = 1.5f)
            drawLine(Color.White.copy(alpha = 0.15f), second, home, strokeWidth = 1.5f)
            
            val activeColor = FieldGreen

            if (hasScored) {
                drawPath(pathBase, activeColor.copy(alpha = 0.3f))
                drawPath(pathBase, activeColor, style = Stroke(width = 5f))
            } else {
                if (isOnFirst || isOnSecond || isOnThird) drawLine(activeColor, home, first, strokeWidth = 10f)
                if (isOnSecond || isOnThird) drawLine(activeColor, first, second, strokeWidth = 10f)
                if (isOnThird) drawLine(activeColor, second, third, strokeWidth = 10f)
            }

            fun drawBasePoint(pos: Offset, isActive: Boolean) {
                val bSize = 22f
                rotate(45f, pivot = pos) {
                    drawRect(
                        color = if (isActive) activeColor else Color(0xFFE4E0D7).copy(alpha = 0.85f),
                        topLeft = Offset(pos.x - bSize / 2, pos.y - bSize / 2),
                        size = Size(bSize, bSize)
                    )
                }
            }

            drawBasePoint(first, isOnFirst)
            drawBasePoint(second, isOnSecond)
            drawBasePoint(third, isOnThird)
            
            val plateWidth = 32f
            val plateHeight = 22f
            val plate = Path().apply {
                moveTo(home.x - plateWidth / 2f, home.y - plateHeight / 2f)
                lineTo(home.x + plateWidth / 2f, home.y - plateHeight / 2f)
                lineTo(home.x + plateWidth * 0.45f, home.y + plateHeight * 0.20f)
                lineTo(home.x, home.y + plateHeight / 2f)
                lineTo(home.x - plateWidth * 0.45f, home.y + plateHeight * 0.20f)
                close()
            }
            drawPath(plate, Color(0xFFE4E0D7).copy(alpha = 0.95f))

            if (rbiCount > 0) {
                val rbiText = rbiCount.toString()
                val textLayoutResult = textMeasurer.measure(AnnotatedString(rbiText), style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
                val circleRadius = (textLayoutResult.size.width / 2f).coerceAtLeast(textLayoutResult.size.height / 2f) + (10 * density.density)
                drawCircle(FieldGreen.copy(alpha = 0.85f), radius = circleRadius, center = Offset(centerX, centerY))
                drawText(textMeasurer, rbiText, Offset(centerX - textLayoutResult.size.width / 2f, centerY - textLayoutResult.size.height / 2f), style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
            }

            if (isRetiredOnOptionel) {
                val rText = "R"
                drawText(textMeasurer, rText, Offset(12 * density.density, 12 * density.density), style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = HudRed))
            }
        }
    }
}

@Composable
fun BattingPanel(
    state: GameState,
    onAction: (BattingResult) -> Unit,
    onNextBatter: () -> Unit = {},
    onPreviousBatter: () -> Unit = {},
    onManualAdvance: (Int) -> Unit = {},
    onManualRetreat: (Int) -> Unit = {},
    onManualRemove: (Int) -> Unit = {},
    onManualAddRBI: (Int) -> Unit = {},
    onManualRemoveRBI: (Int) -> Unit = {},
    onStatsClick: () -> Unit = {},
    editEventPlayerIndex: Int = -1,
    editEventInning: Int = -1,
    editEventIsHomeTeam: Boolean = false,
    onSaveEdit: (AtBatEvent) -> Unit = {},
    onCancelEdit: () -> Unit = {}
) {
    val isEditingEvent = editEventPlayerIndex != -1 && editEventInning != -1
    var playerIdx by remember { mutableIntStateOf(state.currentBatterIndex) }
    var currentResult by remember { mutableStateOf<BattingResult?>(null) }
    var currentRBIState by remember { mutableIntStateOf(0) }
    var retiredOnOptionelState by remember { mutableStateOf(false) }

    LaunchedEffect(isEditingEvent, editEventPlayerIndex, editEventInning, editEventIsHomeTeam, state.currentBatterIndex, state.inning, state.gameHistory) {
        if (isEditingEvent) {
            val eventToEdit = state.gameHistory.find { it.playerIndex == editEventPlayerIndex && it.inning == editEventInning && it.isHomeTeam == editEventIsHomeTeam }
            eventToEdit?.let {
                playerIdx = it.playerIndex
                currentResult = it.result
                currentRBIState = it.rbi
                retiredOnOptionelState = it.retiredOnOptionel
            }
        } else {
            playerIdx = state.currentBatterIndex
            val lastEvent = state.gameHistory.findLast { it.playerIndex == playerIdx && it.inning == state.inning && it.isHomeTeam == state.isHomeTeamBatting() }
            currentResult = lastEvent?.result ?: state.atBatResults[playerIdx]
            currentRBIState = lastEvent?.rbi ?: 0
            retiredOnOptionelState = lastEvent?.retiredOnOptionel ?: false
        }
    }

    val isOnBase = state.runnerOnFirst == playerIdx || state.runnerOnSecond == playerIdx || state.runnerOnThird == playerIdx
    val hasScored = state.playersWhoScored.contains(playerIdx)
    var showEditMenu by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.05f))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        val scoreboardWidth = maxWidth * 0.28f
        val sidePanelWidth = maxWidth * 0.20f
        val centerWidth = maxWidth * 0.48f
        val topOffset = 16.dp
        val actionHeight = maxHeight * 0.14f
        val actionTallHeight = maxHeight * 0.16f
        val diamondSize = maxHeight * 0.28f
        val centerGap = maxHeight * 0.04f
        val nextPanelHeight = maxHeight * 0.50f
        val nextAvatarSize = maxHeight * 0.18f
        val actionBottomGap = 16.dp

        if (!isEditingEvent) {
            Scoreboard(
                state = state,
                roster = state.roster,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = topOffset)
                    .width(scoreboardWidth),
                onStatsClick = onStatsClick
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = topOffset)
                .width(centerWidth),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isEditingEvent) "CORRECTION" else "AU BÂTON",
                style = MaterialTheme.typography.labelLarge,
                color = FieldGreen,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val currentPlayer = if (isEditingEvent) state.roster?.players?.find { it.index == playerIdx } else state.currentBatter
                PlayerAvatar(
                    photoUrl = currentPlayer?.photoUrl,
                    modifier = Modifier.size(64.dp),
                    borderColor = FieldGreen,
                    playerName = currentPlayer?.playerName
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    var batterFontSize by remember { mutableStateOf(32.sp) }
                    Text(
                        text = (currentPlayer?.playerName ?: "").uppercase(),
                        color = ChalkWhite,
                        fontSize = batterFontSize,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        onTextLayout = { textLayoutResult ->
                            if (textLayoutResult.hasVisualOverflow && batterFontSize > 12.sp) {
                                batterFontSize *= 0.9f
                            }
                        }
                    )
                    Box(Modifier.width(60.dp).height(4.dp).background(FieldGreen))
                }
            }

            Spacer(Modifier.height(centerGap))

            Box(contentAlignment = Alignment.Center) {
                DiamondView(
                    state = state,
                    rbiCount = currentRBIState,
                    isRetiredOnOptionel = retiredOnOptionelState,
                    modifier = Modifier.size(diamondSize)
                )
                IconButton(
                    onClick = { showEditMenu = true },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(64.dp)
                        .offset(x = 44.dp, y = 0.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        null,
                        modifier = Modifier.size(48.dp),
                        tint = ChalkWhite.copy(alpha = 0.20f)
                    )
                }
                DropdownMenu(expanded = showEditMenu, onDismissRequest = { showEditMenu = false }) {
                    if (isOnBase || hasScored) {
                        if (!hasScored) DropdownMenuItem(text = { Text("+1 But") }, onClick = { onManualAdvance(playerIdx); showEditMenu = false }, enabled = !isEditingEvent)
                        if (hasScored || state.runnerOnFirst != playerIdx) DropdownMenuItem(text = { Text("-1 But") }, onClick = { onManualRetreat(playerIdx); showEditMenu = false }, enabled = !isEditingEvent)
                        DropdownMenuItem(text = { Text("Retrait") }, onClick = {
                            if (isEditingEvent && currentResult == BattingResult.Optionel) retiredOnOptionelState = !retiredOnOptionelState
                            else if (!isEditingEvent) onManualRemove(playerIdx)
                            showEditMenu = false
                        })
                    }
                    if (currentResult != null) {
                        HorizontalDivider()
                        DropdownMenuItem(text = { Text("Ajouter un PP (+1)") }, onClick = { if (isEditingEvent) currentRBIState++ else onManualAddRBI(playerIdx); showEditMenu = false })
                        if (currentRBIState > 0) DropdownMenuItem(text = { Text("Enlever un PP (-1)") }, onClick = { if (isEditingEvent) currentRBIState-- else onManualRemoveRBI(playerIdx); showEditMenu = false })
                    }
                }
            }

            if (!isEditingEvent) {
                Surface(
                    modifier = Modifier
                        .width(centerWidth * 0.92f)
                        .height(44.dp)
                        .offset(y = (-10).dp),
                    shape = RoundedCornerShape(24.dp),
                    color = HudPanel,
                    border = BorderStroke(1.dp, HudBorder)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                        IconButton(
                            onClick = onPreviousBatter,
                            enabled = state.halfInningBattersBatted > 0,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(24.dp), tint = if (state.halfInningBattersBatted > 0) FieldGreen else HudMuted)
                        }
                        Text(
                            text = (if (currentResult != null) getShortResult(currentResult!!) else "EN COURS").uppercase(),
                            color = ChalkWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                            letterSpacing = 1.sp
                        )
                        IconButton(
                            onClick = onNextBatter,
                            enabled = currentResult != null,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(24.dp), tint = if (currentResult != null) FieldGreen else HudMuted)
                        }
                    }
                }
            }
        }

        if (!isEditingEvent) {
            val nextPlayer = state.nextBatter
            val totalPlayers = state.roster?.players?.size ?: 1
            val progress = if (nextPlayer != null) (nextPlayer.index.toFloat() / totalPlayers.toFloat()) else 0f

            // Sa Soirée (Calculs)
            val playerIdxStats = nextPlayer?.index ?: -1
            val history = state.gameHistory.filter { it.playerIndex == playerIdxStats }
            val hits = history.count { it.result in listOf(BattingResult.Single, BattingResult.Double, BattingResult.Triple, BattingResult.HomeRun) }
            val ab = history.count { it.result != BattingResult.Optionel }
            val rbi = history.sumOf { it.rbi }
            val runs = state.gameHistory.count { it.playerIndex == playerIdxStats && it.finalBase == 4 }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = topOffset)
                    .width(sidePanelWidth)
                    .height(nextPanelHeight),
                color = HudPanel,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, HudBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EN ATTENTE", color = HudMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    Box(contentAlignment = Alignment.Center) {
                        NextPlayerAvatar(size = nextAvatarSize * 0.82f, progress = progress)
                        PlayerAvatar(
                            photoUrl = nextPlayer?.photoUrl,
                            modifier = Modifier.size(nextAvatarSize * 0.65f),
                            borderColor = Color.Transparent,
                            showPlaceholder = false,
                            playerName = nextPlayer?.playerName
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        (nextPlayer?.playerName ?: "---").uppercase(),
                        color = ChalkWhite,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(nextPlayer?.posDef ?: "", color = FieldGreen, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = HudBorder, thickness = 1.dp)
                    Spacer(Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NextPlayerStat("H-AB", "$hits-$ab")
                        NextPlayerStat("PP", "${rbi}PP")
                        NextPlayerStat("PC", "${runs}PC")
                    }
                }
            }
        }

        if (isEditingEvent) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = actionBottomGap)
                    .fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = { currentResult?.let { result -> onSaveEdit(AtBatEvent(playerIdx, editEventInning, result, when(result){BattingResult.Double->2;BattingResult.Triple->3;BattingResult.HomeRun->4;else->1}, editEventIsHomeTeam, currentRBIState, retiredOnOptionelState)) } },
                    colors = ButtonDefaults.buttonColors(containerColor = HudBlue), shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).height(64.dp)) { Text("SAUVEGARDER", fontWeight = FontWeight.Black) }
                Button(onClick = onCancelEdit, colors = ButtonDefaults.buttonColors(containerColor = HudRed), shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).height(64.dp)) { Text("ANNULER", fontWeight = FontWeight.Black) }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = actionBottomGap)
                    .fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionButton("1B", "SIMPLE", FieldGreenLight, Modifier.weight(1f), currentResult == BattingResult.Single, height = actionHeight) { onAction(BattingResult.Single) }
                    ActionButton("2B", "DOUBLE", FieldGreenLight, Modifier.weight(1f), currentResult == BattingResult.Double, height = actionHeight) { onAction(BattingResult.Double) }
                    ActionButton("3B", "TRIPLE", FieldGreenLight, Modifier.weight(1f), currentResult == BattingResult.Triple, height = actionHeight) { onAction(BattingResult.Triple) }
                    ActionButton("CC", "CIRCUIT", HudBlue, Modifier.weight(1f), currentResult == BattingResult.HomeRun, height = actionHeight) { onAction(BattingResult.HomeRun) }
                    ActionButton("OPT", "OPTION", HudBlue, Modifier.weight(1f), currentResult == BattingResult.Optionel, height = actionHeight) { onAction(BattingResult.Optionel) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionButton(
                        label = "RAB",
                        subLabel = "RETRAIT AU BÂTON",
                        color = HudRed,
                        modifier = Modifier.weight(1f),
                        isSelected = currentResult == BattingResult.Strikeout,
                        filledStyle = true,
                        filledGradient = listOf(Color(0xFF3A151C), Color(0xFFB73345), Color(0xFF62212C)),
                        emphasizeSubLabel = true,
                        height = actionTallHeight
                    ) { onAction(BattingResult.Strikeout) }
                    ActionButton(
                        label = "OUT",
                        subLabel = "RETRAIT",
                        color = HudOrange,
                        modifier = Modifier.weight(1f),
                        isSelected = currentResult == BattingResult.Out,
                        filledStyle = true,
                        filledGradient = listOf(Color(0xFF442B14), Color(0xFFD88224), Color(0xFF734317)),
                        emphasizeSubLabel = true,
                        height = actionTallHeight
                    ) { onAction(BattingResult.Out) }
                }
            }
        }
    }
}

@Composable
private fun NextPlayerStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = HudMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
        Text(value.uppercase(), color = FieldGreenLight, fontSize = 20.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun NextPlayerAvatar(size: Dp, progress: Float = 0.8f) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension * 0.46f
            drawCircle(Color.Black.copy(alpha = 0.40f), radius = radius, center = center)
            
            // Cercle de progression (dynamique)
            drawArc(
                color = FieldGreenLight.copy(alpha = 0.90f),
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0.1f, 1f),
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = 4f)
            )
        }
        
        // Silhouette HUD mutualisée
        com.joysticks.stats.ui.components.PlayerPlaceholder(
            modifier = Modifier.fillMaxSize(0.7f)
        )
    }
}

@Composable
fun ActionButton(
    label: String,
    subLabel: String,
    color: Color,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    filledStyle: Boolean = false,
    filledGradient: List<Color>? = null,
    emphasizeSubLabel: Boolean = false,
    enabled: Boolean = true,
    height: androidx.compose.ui.unit.Dp = 54.dp,
    onClick: () -> Unit
) {
    HudActionButton(
        label = label,
        subLabel = subLabel,
        accent = color,
        modifier = modifier,
        selected = isSelected,
        filledStyle = filledStyle,
        filledGradient = filledGradient,
        emphasizeSubLabel = emphasizeSubLabel,
        enabled = enabled,
        height = height,
        onClick = onClick
    )
}
