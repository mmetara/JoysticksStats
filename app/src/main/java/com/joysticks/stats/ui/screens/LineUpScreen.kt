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

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun LineupScreen(roster: Roster) {

    HudPanel(modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(0.82f)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "ALIGNEMENT PARTANT",
                style = MaterialTheme.typography.titleLarge,
                color = FieldGreen,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            roster.players.forEachIndexed { index, player ->
                Text(
                    text = "${index + 1}. ${player.playerName.uppercase()} - ${player.posDef}",
                    fontSize = 16.sp,
                    color = ChalkWhite,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
