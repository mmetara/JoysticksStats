package com.joysticks.stats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.joysticks.stats.engine.Roster
import com.joysticks.stats.ui.components.HudPanel
import com.joysticks.stats.ui.theme.ChalkWhite
import com.joysticks.stats.ui.theme.FieldGreen
import com.joysticks.stats.ui.theme.HudMuted

@Composable
fun LineupScreen(roster: Roster) {

    HudPanel(modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(0.82f)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
        Text(
            "Alignement partant (${roster.gameInfo.gameDate}  •  ${roster.gameInfo.gameTime})".uppercase(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = FieldGreen,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()

        )

        Spacer(modifier = Modifier.height(20.dp))

        roster.players.forEachIndexed { index, player ->

            Text(
                text = "${index + 1}. ${player.playerName.uppercase()} - ${player.posDef}",
                fontSize = 18.sp,
                color = if (index == 0) ChalkWhite else HudMuted,
                fontWeight = if (index == 0) FontWeight.ExtraBold else FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))
        }
        }
    }
}
