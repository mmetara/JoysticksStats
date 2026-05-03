package com.joysticks.stats.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.joysticks.stats.data.Team
import com.joysticks.stats.data.TeamDataStore
import com.joysticks.stats.ui.components.HudButton
import com.joysticks.stats.ui.components.HudPanel
import com.joysticks.stats.ui.theme.*
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
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().widthIn(max = 800.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(44.dp).background(HudPanel, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ChalkWhite)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "GESTION ÉQUIPES",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ChalkWhite,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Box(Modifier.width(60.dp).height(4.dp).background(FieldGreen))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth().widthIn(max = 900.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Form Section
                HudPanel(
                    modifier = Modifier.weight(0.45f),
                    borderColor = if (teamToEdit != null) HudBlue else HudBorder
                ) {
                    Text(
                        text = if (teamToEdit != null) "ÉDITION" else "NOUVELLE ÉQUIPE",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (teamToEdit != null) HudBlue else FieldGreen,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = newTeamName,
                        onValueChange = { newTeamName = it },
                        placeholder = { Text("Nom de l'équipe...", color = HudMuted) },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = HudPanelSoft,
                            unfocusedContainerColor = HudPanelSoft,
                            focusedTextColor = ChalkWhite,
                            unfocusedTextColor = ChalkWhite,
                            focusedIndicatorColor = if (teamToEdit != null) HudBlue else FieldGreen,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    HudButton(
                        text = if (teamToEdit != null) "Mettre à jour" else "Ajouter l'équipe",
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
                        accent = if (teamToEdit != null) HudBlue else FieldGreen,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (teamToEdit != null) {
                        TextButton(
                            onClick = { teamToEdit = null; newTeamName = "" },
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                        ) {
                            Text("ANNULER", color = HudRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // List Section
                HudPanel(
                    modifier = Modifier.weight(0.55f),
                    borderColor = HudBorder
                ) {
                    Text(
                        text = "RÉPERTOIRE (${teams.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = HudMuted,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(teams) { team ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp),
                                color = HudPanelSoft.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, HudBorder.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = team.name.uppercase(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = ChalkWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "ID: #${team.id}",
                                            fontSize = 10.sp,
                                            color = HudMuted
                                        )
                                    }

                                    Row {
                                        IconButton(onClick = { teamToEdit = team; newTeamName = team.name }) {
                                            Icon(Icons.Default.Edit, "Modifier", tint = HudBlue, modifier = Modifier.size(20.dp))
                                        }
                                        IconButton(onClick = {
                                            val updated = teams.filter { it.id != team.id }
                                            scope.launch { teamStore.saveTeams(updated) ; teams = updated }
                                        }) {
                                            Icon(Icons.Default.Delete, "Supprimer", tint = HudRed.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
