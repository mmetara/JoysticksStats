package com.joysticks.stats.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.joysticks.stats.engine.GameState
import com.joysticks.stats.engine.Roster
import com.joysticks.stats.ui.theme.ChalkWhite
import com.joysticks.stats.ui.theme.FieldGreen
import com.joysticks.stats.ui.theme.FieldGreenLight
import com.joysticks.stats.ui.theme.HudBorder
import com.joysticks.stats.ui.theme.HudMuted
import com.joysticks.stats.ui.theme.HudPanel

@Composable
fun Scoreboard(
    state: GameState,
    roster: Roster?,
    modifier: Modifier = Modifier,
    onStatsClick: () -> Unit = {}
) {

    val away = roster?.gameInfo?.awayTeamName ?: "Visiteur"
    val home = roster?.gameInfo?.homeTeamName ?: "Local"

    Surface(
        modifier = modifier.widthIn(min = 210.dp, max = 260.dp),
        color = HudPanel,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, HudBorder),
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // équipes
            TeamRow(away, state.awayScore, isPrimary = !state.isHomeTeamBatting())
            HorizontalDivider(color = HudBorder)
            TeamRow(home, state.homeScore, isPrimary = state.isHomeTeamBatting())

            HorizontalDivider(color = HudBorder)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // manche
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        text = state.inning.toString(),
                        color = ChalkWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )

                    Spacer(Modifier.width(4.dp))

                    Text(
                        text = if (state.isTop) "▲" else "▼",
                        color = FieldGreenLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // outs
                OutsIndicator(state.outs)

                // bases
                BasesDiamond(state)
                
                // Bouton Stats
                IconButton(
                    onClick = onStatsClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.List,
                        contentDescription = "Feuille de match",
                        modifier = Modifier.size(18.dp),
                        tint = ChalkWhite.copy(alpha = 0.72f)
                    )
                }
            }
        }
    }
}

@Composable
fun TeamRow(name: String, score: Int, isPrimary: Boolean = false) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(4.dp))

            Text(
                name.uppercase(),
                color = ChalkWhite,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .width(42.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                score.toString(),
                color = if (isPrimary) FieldGreenLight else ChalkWhite,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun OutsIndicator(outs: Int) {

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {

        repeat(3) { i ->

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        if (i < outs) FieldGreenLight else Color.Transparent,
                        CircleShape
                    )
                    .border(1.dp, if (i < outs) FieldGreenLight else HudMuted, CircleShape)
            )
        }
    }
}

@Composable
fun BasesDiamond(state: GameState) {

    Box(
        modifier = Modifier.size(58.dp),
        contentAlignment = Alignment.Center
    ) {

        // 2e base (haut)
        Box(
            modifier = Modifier
                .size(12.dp)
                .align(Alignment.TopCenter)
                .rotate(45f)
                .background(if (state.runnerOnSecond != -1) FieldGreenLight else Color.Transparent)
                .border(1.dp, if (state.runnerOnSecond != -1) FieldGreenLight else HudMuted)
        )

        // 1re base (droite)
        Box(
            modifier = Modifier
                .size(12.dp)
                .align(Alignment.CenterEnd)
                .rotate(45f)
                .background(if (state.runnerOnFirst != -1) FieldGreenLight else Color.Transparent)
                .border(1.dp, if (state.runnerOnFirst != -1) FieldGreenLight else HudMuted)
        )

        // 3e base (gauche)
        Box(
            modifier = Modifier
                .size(12.dp)
                .align(Alignment.CenterStart)
                .rotate(45f)
                .background(if (state.runnerOnThird != -1) FieldGreenLight else Color.Transparent)
                .border(1.dp, if (state.runnerOnThird != -1) FieldGreenLight else HudMuted)
        )
    }
}
