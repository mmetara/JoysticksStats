package com.joysticks.stats.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joysticks.stats.engine.GameState
import com.joysticks.stats.ui.components.HudActionButton
import com.joysticks.stats.ui.components.HudPanel
import com.joysticks.stats.ui.theme.*

@Composable
fun OpponentScorePanel(
    gameState: GameState,
    onScoreEntered: (Int) -> Unit
) {
    var runs by remember { mutableIntStateOf(0) }

    val isOpenInning = gameState.inning >= 9
    val maxRuns = if (isOpenInning) 99 else 3
    val opponentHomeRunLimitReached = (gameState.roster?.gameInfo?.isHomeTeam == true && gameState.awayTeamHomeRuns >= 5) ||
                                     (gameState.roster?.gameInfo?.isHomeTeam == false && gameState.homeTeamHomeRuns >= 5)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        HudPanel(
            modifier = Modifier
                .padding(top = 60.dp, start = 32.dp, end = 32.dp, bottom = 24.dp)
                .widthIn(max = 440.dp)
                .wrapContentHeight()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(14.dp)
            ) {
                Text(
                    text = if (isOpenInning) "MANCHE OUVERTE" else "POINTAGE ADVERSAIRE",
                    style = MaterialTheme.typography.labelSmall,
                    color = HudOrange,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "ENTREZ LES POINTS MARQUÉS",
                    color = ChalkWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )

                if (!isOpenInning) {
                    Text(
                        text = "(MAXIMUM 3 POINTS)",
                        style = MaterialTheme.typography.labelSmall,
                        color = HudMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Score Display & Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HudActionButton(
                        label = "-",
                        subLabel = "MOINS",
                        accent = HudRed,
                        filledStyle = true,
                        filledGradient = listOf(Color(0xFF39141A), Color(0xFFB93445), Color(0xFF5D202B)),
                        enabled = runs > 0,
                        height = 72.dp,
                        modifier = Modifier.weight(1f),
                        onClick = { if (runs > 0) runs-- }
                    )

                    Text(
                        text = runs.toString(),
                        fontSize = 64.sp,
                        color = ChalkWhite,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 20.dp),
                        textAlign = TextAlign.Center
                    )

                    HudActionButton(
                        label = "+",
                        subLabel = "PLUS",
                        accent = HudBlue,
                        filledStyle = true,
                        filledGradient = listOf(Color(0xFF152846), Color(0xFF3377D2), Color(0xFF1C355A)),
                        enabled = runs < maxRuns,
                        height = 72.dp,
                        modifier = Modifier.weight(1f),
                        onClick = { if (runs < maxRuns) runs++ }
                    )
                }

                Spacer(Modifier.height(20.dp))

                if (opponentHomeRunLimitReached) {
                    Text(
                        text = "LIMITE DE CIRCUITS ADVERSE ATTEINTE",
                        color = HudRed,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                HudActionButton(
                    label = "VALIDER",
                    subLabel = "FIN DE LA DEMI-MANCHE",
                    accent = FieldGreen,
                    filledStyle = true,
                    filledGradient = listOf(Color(0xFF17331E), Color(0xFF2F8A44), Color(0xFF1A4727)),
                    emphasizeSubLabel = true,
                    enabled = !opponentHomeRunLimitReached || runs == 0,
                    height = 60.dp,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onScoreEntered(runs) }
                )
            }
        }
    }
}
