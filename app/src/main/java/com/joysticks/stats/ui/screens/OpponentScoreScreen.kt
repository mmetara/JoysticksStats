package com.joysticks.stats.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joysticks.stats.engine.GameState
import com.joysticks.stats.ui.components.HudActionButton
import com.joysticks.stats.ui.components.HudPanel
import com.joysticks.stats.ui.theme.ChalkWhite
import com.joysticks.stats.ui.theme.FieldGreen
import com.joysticks.stats.ui.theme.HudBlue
import com.joysticks.stats.ui.theme.HudMuted
import com.joysticks.stats.ui.theme.HudRed

    @Composable
    fun OpponentScorePanel(gameState: GameState,
                           onScoreEntered: (Int) -> Unit) {

        var runs by remember { mutableStateOf(0) }

        val isOpenInning = gameState.inning >= 9
        val maxRuns = if (isOpenInning) Int.MAX_VALUE else 3
        val opponentHomeRunLimitReached = (gameState.roster?.gameInfo?.isHomeTeam == true && gameState.awayTeamHomeRuns >= 5) ||
                                         (gameState.roster?.gameInfo?.isHomeTeam == false && gameState.homeTeamHomeRuns >= 5)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            HudPanel(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .widthIn(max = 520.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isOpenInning)
                            "POINTAGE ADVERSE - MANCHE OUVERTE"
                        else
                            "POINTAGE ADVERSE",
                        style = MaterialTheme.typography.titleLarge,
                        color = ChalkWhite,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = if (isOpenInning) "AUCUNE LIMITE DE POINTS" else "MAXIMUM 3 POINTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = HudMuted,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    if (opponentHomeRunLimitReached) {
                        Text(
                            text = "Limite de circuits adverse atteinte !",
                            style = MaterialTheme.typography.bodyLarge,
                            color = HudRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        HudActionButton(
                            label = "-",
                            subLabel = "MOINS",
                            accent = HudRed,
                            filledStyle = true,
                            filledGradient = listOf(
                                Color(0xFF39141A),
                                Color(0xFFB93445),
                                Color(0xFF5D202B)
                            ),
                            enabled = runs > 0,
                            height = 72.dp,
                            modifier = Modifier.weight(1f),
                            onClick = { if (runs > 0) runs-- }
                        )

                        Text(
                            runs.toString(),
                            fontSize = 44.sp,
                            color = FieldGreen,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        HudActionButton(
                            label = "+",
                            subLabel = "PLUS",
                            accent = HudBlue,
                            filledStyle = true,
                            filledGradient = listOf(
                                Color(0xFF152846),
                                Color(0xFF3377D2),
                                Color(0xFF1C355A)
                            ),
                            enabled = runs < maxRuns,
                            height = 72.dp,
                            modifier = Modifier.weight(1f),
                            onClick = { if (runs < maxRuns) runs++ }
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    HudActionButton(
                        label = "OK",
                        subLabel = "FIN DE LA DEMI-MANCHE",
                        accent = FieldGreen,
                        filledStyle = true,
                        filledGradient = listOf(
                            Color(0xFF17331E),
                            Color(0xFF2F8A44),
                            Color(0xFF1A4727)
                        ),
                        emphasizeSubLabel = true,
                        enabled = !opponentHomeRunLimitReached,
                        height = 68.dp,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onScoreEntered(runs) }
                    )
                }
            }
        }
    }
