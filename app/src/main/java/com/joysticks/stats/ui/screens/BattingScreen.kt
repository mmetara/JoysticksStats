package com.joysticks.stats.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
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
        modifier = modifier
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.width * 0.39f

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
                moveTo(home.x, home.y + 10f)
                lineTo(first.x, first.y + 10f)
                lineTo(second.x, second.y + 10f)
                lineTo(third.x, third.y + 10f)
                close()
            }
            drawPath(shadowPath, Color.Black.copy(alpha = 0.30f))
            drawPath(
                pathBase,
                Brush.verticalGradient(
                    listOf(
                        Color(0xD01A2029),
                        Color(0xB7111720),
                        Color(0xD00A0F16)
                    )
                )
            )
            drawPath(pathBase, Color.White.copy(alpha = 0.28f), style = Stroke(width = 2.3f))
            drawLine(Color.White.copy(alpha = 0.10f), third, first, strokeWidth = 1.4f)
            drawLine(Color.White.copy(alpha = 0.10f), second, home, strokeWidth = 1.4f)
            drawCircle(Color.White.copy(alpha = 0.10f), radius = radius * 0.18f, center = Offset(centerX, centerY + radius * 0.08f))
            drawCircle(Color.White.copy(alpha = 0.62f), radius = 4.2f, center = Offset(centerX, centerY + radius * 0.08f))

            val activeColor = FieldGreen

            if (hasScored) {
                drawPath(pathBase, activeColor.copy(alpha = 0.3f))
                drawPath(pathBase, activeColor, style = Stroke(width = 4f))
            } else {
                if (isOnFirst || isOnSecond || isOnThird) drawLine(activeColor, home, first, strokeWidth = 8f)
                if (isOnSecond || isOnThird) drawLine(activeColor, first, second, strokeWidth = 8f)
                if (isOnThird) drawLine(activeColor, second, third, strokeWidth = 8f)
            }

            fun drawBasePoint(pos: Offset, isActive: Boolean) {
                val bSize = 18f
                rotate(45f, pivot = pos) {
                    drawRect(
                        color = if (isActive) activeColor else Color(0xFFE4E0D7).copy(alpha = 0.82f),
                        topLeft = Offset(pos.x - bSize / 2, pos.y - bSize / 2),
                        size = Size(bSize, bSize)
                    )
                }
            }

            drawBasePoint(first, isOnFirst)
            drawBasePoint(second, isOnSecond)
            drawBasePoint(third, isOnThird)
            val plateWidth = 26f
            val plateHeight = 18f
            val plate = Path().apply {
                moveTo(home.x - plateWidth / 2f, home.y - plateHeight / 2f)
                lineTo(home.x + plateWidth / 2f, home.y - plateHeight / 2f)
                lineTo(home.x + plateWidth * 0.42f, home.y + plateHeight * 0.15f)
                lineTo(home.x, home.y + plateHeight / 2f)
                lineTo(home.x - plateWidth * 0.42f, home.y + plateHeight * 0.15f)
                close()
            }
            drawPath(plate, Color(0xFFE4E0D7).copy(alpha = 0.90f))
            drawPath(plate, Color.Black.copy(alpha = 0.22f), style = Stroke(width = 1.2f))

            if (rbiCount > 0) {
                val rbiText = rbiCount.toString()
                val textLayoutResult = textMeasurer.measure(AnnotatedString(rbiText), style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
                val circleRadius = (textLayoutResult.size.width / 2f).coerceAtLeast(textLayoutResult.size.height / 2f) + (8 * density.density)
                drawCircle(FieldGreen.copy(alpha = 0.75f), radius = circleRadius, center = Offset(centerX, centerY))
                drawText(textMeasurer, rbiText, Offset(centerX - textLayoutResult.size.width / 2f, centerY - textLayoutResult.size.height / 2f), style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
            }

            if (isRetiredOnOptionel) {
                val rText = "R"
                val textLayoutResult = textMeasurer.measure(AnnotatedString(rText), style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935)))
                drawText(textMeasurer, rText, Offset(10 * density.density, 10 * density.density), style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935)))
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
    var playerIdx by remember { mutableStateOf(state.currentBatterIndex) }
    var currentResult by remember { mutableStateOf<BattingResult?>(null) }
    var currentRBIState by remember { mutableStateOf(0) }
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
            currentResult = state.atBatResults[playerIdx]
            val lastEvent = state.gameHistory.findLast { it.playerIndex == playerIdx && it.inning == state.inning && it.isHomeTeam == state.isHomeTeamBatting() }
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
            .background(Color.Black.copy(alpha = 0.10f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        val scoreboardWidth = maxWidth * 0.29f
        val sidePanelWidth = maxWidth * 0.18f
        val centerWidth = maxWidth * 0.46f
        val topOffset = 82.dp
        val actionHeight = maxHeight * 0.12f
        val actionTallHeight = maxHeight * 0.13f
        val diamondSize = maxHeight * 0.20f
        val centerGap = maxHeight * 0.03f
        val nextPanelHeight = maxHeight * 0.46f
        val nextAvatarSize = maxHeight * 0.14f
        val bottomNavHeight = 72.dp
        val actionBottomGap = bottomNavHeight + 36.dp

        HudTopBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(54.dp)
        )

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
                style = MaterialTheme.typography.labelMedium,
                color = FieldGreen,
                fontWeight = FontWeight.Black
            )
            Text(
                text = ((if (isEditingEvent) state.roster?.players?.find { it.index == playerIdx }?.playerName else state.currentBatter?.playerName ?: "---") ?: "").uppercase(),
                color = ChalkWhite,
                fontSize = 25.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Box(Modifier.width(42.dp).height(3.dp).background(FieldGreen))

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
                        .align(Alignment.TopEnd)
                        .size(30.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = ChalkWhite.copy(alpha = 0.18f)
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
                        .width(centerWidth * 0.88f)
                        .height(38.dp)
                        .offset(y = (-22).dp),
                    shape = RoundedCornerShape(24.dp),
                    color = HudPanel,
                    border = BorderStroke(1.dp, HudBorder)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
                        IconButton(
                            onClick = onPreviousBatter,
                            enabled = state.currentBatterIndex != state.firstBatterIndexOfHalfInning,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(20.dp), tint = if (state.currentBatterIndex != state.firstBatterIndexOfHalfInning) FieldGreen else HudMuted)
                        }
                        Text(
                            text = (if (currentResult != null) getShortResult(currentResult!!) else "EN ATTENTE...").uppercase(),
                            color = ChalkWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(
                            onClick = onNextBatter,
                            enabled = currentResult != null,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp), tint = if (currentResult != null) FieldGreen else HudMuted)
                        }
                    }
                }
            }
        }

        if (!isEditingEvent) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = topOffset)
                    .width(sidePanelWidth)
                    .height(nextPanelHeight),
                color = HudPanel,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, HudBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PROCHAIN", color = HudMuted, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(12.dp))
                    NextPlayerAvatar(size = nextAvatarSize)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        (state.nextBatter?.playerName ?: "---").uppercase(),
                        color = ChalkWhite,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                    Text(state.nextBatter?.posDef ?: "", color = FieldGreen, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = HudBorder)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        NextPlayerStat("ORD", state.nextBatter?.posOff?.toString() ?: "-")
                        NextPlayerStat("POS", state.nextBatter?.posDef ?: "-")
                        NextPlayerStat("IDX", state.nextBatter?.index?.toString() ?: "-")
                    }
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, HudBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "VOIR FICHE JOUEUR",
                                color = HudMuted,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        if (isEditingEvent) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = actionBottomGap)
                    .fillMaxWidth(0.82f),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(onClick = { currentResult?.let { result -> onSaveEdit(AtBatEvent(playerIdx, editEventInning, result, when(result){BattingResult.Double->2;BattingResult.Triple->3;BattingResult.HomeRun->4;else->1}, editEventIsHomeTeam, currentRBIState, retiredOnOptionelState)) } },
                    colors = ButtonDefaults.buttonColors(containerColor = HudBlue), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(56.dp)) { Text("SAUVEGARDER") }
                Button(onClick = onCancelEdit, colors = ButtonDefaults.buttonColors(containerColor = HudRed), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(56.dp)) { Text("ANNULER") }
            }
        } else {
            val btnEnabled = !state.maxRunsReached
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = actionBottomGap)
                    .fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionButton("1B", "SIMPLE", FieldGreenLight, Modifier.weight(1f), currentResult == BattingResult.Single, enabled = btnEnabled, height = actionHeight) { onAction(BattingResult.Single) }
                    ActionButton("2B", "DOUBLE", FieldGreenLight, Modifier.weight(1f), currentResult == BattingResult.Double, enabled = btnEnabled, height = actionHeight) { onAction(BattingResult.Double) }
                    ActionButton("3B", "TRIPLE", FieldGreenLight, Modifier.weight(1f), currentResult == BattingResult.Triple, enabled = btnEnabled, height = actionHeight) { onAction(BattingResult.Triple) }
                    ActionButton("CC", "COUP SÛR", HudBlue, Modifier.weight(1f), currentResult == BattingResult.HomeRun, enabled = btnEnabled, height = actionHeight) { onAction(BattingResult.HomeRun) }
                    ActionButton("OPT", "OPTION", HudBlue, Modifier.weight(1f), currentResult == BattingResult.Optionel, enabled = btnEnabled, height = actionHeight) { onAction(BattingResult.Optionel) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionButton(
                        label = "RAB",
                        subLabel = "RETRAIT AU BÂTON",
                        color = HudRed,
                        modifier = Modifier.weight(1f),
                        isSelected = currentResult == BattingResult.Strikeout,
                        filledStyle = true,
                        filledGradient = listOf(
                            Color(0xFF3A151C),
                            Color(0xFFB73345),
                            Color(0xFF62212C)
                        ),
                        emphasizeSubLabel = true,
                        enabled = btnEnabled,
                        height = actionTallHeight
                    ) { onAction(BattingResult.Strikeout) }
                    ActionButton(
                        label = "OUT",
                        subLabel = "RETRAIT",
                        color = HudOrange,
                        modifier = Modifier.weight(1f),
                        isSelected = currentResult == BattingResult.Out,
                        filledStyle = true,
                        filledGradient = listOf(
                            Color(0xFF442B14),
                            Color(0xFFD88224),
                            Color(0xFF734317)
                        ),
                        emphasizeSubLabel = true,
                        enabled = btnEnabled,
                        height = actionTallHeight
                    ) { onAction(BattingResult.Out) }
                }
            }
        }

        HudBottomNav(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomNavHeight)
        )
    }
}

@Composable
private fun NextPlayerStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = HudMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
        Text(value.uppercase(), color = FieldGreenLight, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun NextPlayerAvatar(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = this.size.minDimension * 0.46f
        drawCircle(Color.Black.copy(alpha = 0.36f), radius = radius, center = center)
        drawArc(
            color = FieldGreenLight.copy(alpha = 0.85f),
            startAngle = 205f,
            sweepAngle = 290f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = 3.5f)
        )
        drawCircle(
            color = Color(0xFF3B414A),
            radius = radius * 0.24f,
            center = Offset(center.x, center.y - radius * 0.22f)
        )
        drawOval(
            color = Color(0xFF3B414A),
            topLeft = Offset(center.x - radius * 0.48f, center.y + radius * 0.12f),
            size = Size(radius * 0.96f, radius * 0.48f)
        )
        drawCircle(Color.White.copy(alpha = 0.08f), radius = radius, center = center, style = Stroke(width = 1.2f))
    }
}

@Composable
private fun HudTopBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = HudPanel,
                border = BorderStroke(1.dp, HudBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = ChalkWhite
                    )
                }
            }
            Spacer(Modifier.width(18.dp))
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(8.dp),
                color = FieldGreen.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, FieldGreen.copy(alpha = 0.45f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    JoysticksMark(modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("JOYSTICKS", color = ChalkWhite, fontWeight = FontWeight.Black, fontSize = 17.sp, lineHeight = 17.sp)
                Text("STATS", color = HudMuted, fontWeight = FontWeight.Black, fontSize = 15.sp, lineHeight = 15.sp)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TopStatusIcon(kind = 0, active = true)
            TopStatusIcon(kind = 1)
            TopStatusIcon(kind = 2)
            Text("87%", color = ChalkWhite.copy(alpha = 0.68f), fontWeight = FontWeight.Black, fontSize = 15.sp)
            TopStatusIcon(kind = 3)
        }
    }
}

@Composable
private fun JoysticksMark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val mark = Path().apply {
            moveTo(w * 0.5f, h * 0.04f)
            lineTo(w * 0.90f, h * 0.24f)
            lineTo(w * 0.78f, h * 0.72f)
            lineTo(w * 0.5f, h * 0.96f)
            lineTo(w * 0.22f, h * 0.72f)
            lineTo(w * 0.10f, h * 0.24f)
            close()
        }
        drawPath(mark, FieldGreen.copy(alpha = 0.24f))
        drawPath(mark, FieldGreenLight.copy(alpha = 0.86f), style = Stroke(width = 2.6f))
        drawLine(FieldGreenLight, Offset(w * 0.5f, h * 0.22f), Offset(w * 0.5f, h * 0.64f), strokeWidth = 3.5f)
        drawCircle(FieldGreenLight, radius = w * 0.17f, center = Offset(w * 0.5f, h * 0.22f))
        drawCircle(Color.Black.copy(alpha = 0.25f), radius = w * 0.07f, center = Offset(w * 0.5f, h * 0.22f))
    }
}

@Composable
private fun TopStatusIcon(kind: Int, active: Boolean = false) {
    Canvas(modifier = Modifier.size(width = 25.dp, height = 22.dp)) {
        val c = HudMuted.copy(alpha = 0.78f)
        val stroke = Stroke(width = 2.2f)
        when (kind) {
            0 -> {
                drawLine(c, Offset(size.width * 0.30f, size.height * 0.62f), Offset(size.width * 0.70f, size.height * 0.62f), strokeWidth = 2.2f)
                drawLine(c, Offset(size.width * 0.36f, size.height * 0.62f), Offset(size.width * 0.42f, size.height * 0.28f), strokeWidth = 2.2f)
                drawLine(c, Offset(size.width * 0.64f, size.height * 0.62f), Offset(size.width * 0.58f, size.height * 0.28f), strokeWidth = 2.2f)
                drawCircle(c, radius = 2.2f, center = Offset(size.width * 0.5f, size.height * 0.76f))
            }
            1 -> {
                drawRoundRect(c, topLeft = Offset(3f, 5f), size = Size(size.width - 6f, size.height - 10f), cornerRadius = CornerRadius(2.5f, 2.5f), style = stroke)
                drawLine(c, Offset(7f, size.height * 0.50f), Offset(size.width - 7f, size.height * 0.50f), strokeWidth = 2f)
            }
            2 -> {
                drawArc(c, startAngle = 220f, sweepAngle = 100f, useCenter = false, topLeft = Offset(2f, 3f), size = Size(size.width - 4f, size.height + 4f), style = stroke)
                drawArc(c, startAngle = 225f, sweepAngle = 90f, useCenter = false, topLeft = Offset(7f, 8f), size = Size(size.width - 14f, size.height - 2f), style = stroke)
                drawCircle(c, radius = 2.2f, center = Offset(size.width * 0.5f, size.height * 0.78f))
            }
            else -> {
                drawRoundRect(c, topLeft = Offset(2f, 5f), size = Size(size.width - 7f, size.height - 10f), cornerRadius = CornerRadius(2.2f, 2.2f), style = stroke)
                drawRoundRect(c, topLeft = Offset(size.width - 4.5f, size.height * 0.38f), size = Size(2.8f, size.height * 0.24f), cornerRadius = CornerRadius(1.3f, 1.3f))
            }
        }
        if (active) {
            drawCircle(FieldGreenLight, radius = 3.2f, center = Offset(size.width * 0.82f, size.height * 0.18f))
        }
    }
}

@Composable
private fun HudBottomNav(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = HudPanel,
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        border = BorderStroke(1.dp, HudBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem("MATCH", active = true) {
                Icon(Icons.Default.Home, null, modifier = Modifier.size(24.dp), tint = FieldGreenLight)
            }
            NavDivider()
            BottomNavItem("ALIGNEMENT") {
                Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(24.dp), tint = HudMuted)
            }
            NavDivider()
            BottomNavItem("STATS") {
                Text("|||", color = HudMuted, fontWeight = FontWeight.Black)
            }
            NavDivider()
            BottomNavItem("JOUEURS") {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(24.dp), tint = HudMuted)
            }
            NavDivider()
            BottomNavItem("PARAMÈTRES") {
                Icon(Icons.Default.Settings, null, modifier = Modifier.size(24.dp), tint = HudMuted)
            }
        }
    }
}

@Composable
private fun NavDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(34.dp)
            .background(HudBorder)
    )
}

@Composable
private fun BottomNavItem(
    label: String,
    active: Boolean = false,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.width(180.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            color = if (active) FieldGreenLight else HudMuted,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            maxLines = 1
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
