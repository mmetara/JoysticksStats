package com.joysticks.stats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.joysticks.stats.data.Team
import com.joysticks.stats.data.TeamDataStore
import com.joysticks.stats.ui.components.HudButton
import com.joysticks.stats.ui.components.HudPanel
import com.joysticks.stats.ui.theme.ChalkWhite
import com.joysticks.stats.ui.theme.FieldGreen
import com.joysticks.stats.ui.theme.HudBlue
import com.joysticks.stats.ui.theme.HudBorder
import com.joysticks.stats.ui.theme.HudMuted
import com.joysticks.stats.ui.theme.HudPanelSoft
import com.joysticks.stats.ui.theme.HudRed
import kotlinx.coroutines.launch

@Composable
fun TeamManagementScreen(
    navController: NavController,
    teamStore: TeamDataStore
) {

    val scope = rememberCoroutineScope()

    var teams by remember { mutableStateOf(listOf<Team>()) }
    var newTeamName by remember { mutableStateOf("") }
    var teamToEdit by remember { mutableStateOf<Team?>(null) }

    LaunchedEffect(Unit) {
        teams = teamStore.loadTeams()
    }

    BaseballScreenTemplate {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().widthIn(max = 760.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GESTION DES EQUIPES",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ChalkWhite,
                    fontWeight = FontWeight.Black
                )
                HudButton(
                    text = "Retour",
                    onClick = { navController.popBackStack() },
                    accent = HudBlue,
                    modifier = Modifier.width(140.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HudPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp),
                borderColor = HudBorder
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (teamToEdit != null) "MODIFIER UNE EQUIPE" else "AJOUTER UNE EQUIPE",
                        style = MaterialTheme.typography.labelMedium,
                        color = FieldGreen,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TextField(
                        value = newTeamName,
                        onValueChange = { newTeamName = it },
                        label = { Text("Nom de l'equipe") },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = HudPanelSoft,
                            unfocusedContainerColor = HudPanelSoft,
                            focusedTextColor = ChalkWhite,
                            unfocusedTextColor = ChalkWhite,
                            focusedLabelColor = FieldGreen,
                            unfocusedLabelColor = HudMuted,
                            focusedIndicatorColor = FieldGreen,
                            unfocusedIndicatorColor = HudBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    HudButton(
                        text = if (teamToEdit != null) "Mettre a jour" else "Ajouter",
                        onClick = {
                            if (newTeamName.isNotBlank()) {
                                val updated = if (teamToEdit != null) {
                                    teams.map {
                                        if (it.id == teamToEdit!!.id) it.copy(name = newTeamName) else it
                                    }
                                } else {
                                    val newId = (teams.maxOfOrNull { it.id } ?: 0) + 1
                                    teams + Team(newId, newTeamName)
                                }

                                scope.launch {
                                    teamStore.saveTeams(updated)
                                    teams = updated
                                }

                                newTeamName = ""
                                teamToEdit = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        accent = FieldGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
            ) {

                items(teams) { team ->

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = HudPanelSoft,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HudBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${team.id} - ${team.name}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = ChalkWhite
                            )

                            Row {
                                TextButton(
                                    onClick = {
                                        teamToEdit = team
                                        newTeamName = team.name
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = HudBlue)
                                }

                                TextButton(
                                    onClick = {
                                        val updated = teams.filter { it.id != team.id }

                                        scope.launch {
                                            teamStore.saveTeams(updated)
                                            teams = updated
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = HudRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}