package com.joysticks.stats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.joysticks.stats.data.SettingsDataStore
import com.joysticks.stats.ui.components.BaseballScreenTemplate
import com.joysticks.stats.ui.components.HudButton
import com.joysticks.stats.ui.components.HudScreenHeader
import com.joysticks.stats.ui.theme.ChalkWhite
import com.joysticks.stats.ui.theme.FieldGreen
import com.joysticks.stats.ui.theme.HudMuted
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsDataStore = remember { SettingsDataStore(context) }
    val savedSsid by settingsDataStore.targetSsidFlow.collectAsState(initial = "mmetara")
    val savedUrl by settingsDataStore.playerImagesUrlFlow.collectAsState(initial = SettingsDataStore.DEFAULT_IMAGES_URL)
    
    var ssidInput by remember(savedSsid) { mutableStateOf(savedSsid) }
    var urlInput by remember(savedUrl) { mutableStateOf(savedUrl) }

    BaseballScreenTemplate {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HudScreenHeader(
                title = "PARAMÈTRES",
                onBack = { navController.popBackStack() },
                accent = FieldGreen
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(modifier = Modifier.fillMaxWidth(0.7f)) {
                    Text("SSID WIFI REQUIS", color = HudMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ssidInput,
                        onValueChange = { ssidInput = it },
                        placeholder = { Text("Ex: mmetara") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = ChalkWhite,
                            focusedTextColor = ChalkWhite,
                            focusedBorderColor = FieldGreen,
                            unfocusedBorderColor = HudMuted,
                            focusedLabelColor = FieldGreen,
                            unfocusedLabelColor = HudMuted,
                            focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("URL IMAGES JOUEURS", color = HudMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = { Text("http://...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = ChalkWhite,
                            focusedTextColor = ChalkWhite,
                            focusedBorderColor = FieldGreen,
                            unfocusedBorderColor = HudMuted,
                            focusedLabelColor = FieldGreen,
                            unfocusedLabelColor = HudMuted,
                            focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                HudButton(
                    text = "ENREGISTRER LES MODIFICATIONS",
                    onClick = {
                        scope.launch {
                            settingsDataStore.saveTargetSsid(ssidInput)
                            settingsDataStore.savePlayerImagesUrl(urlInput)
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.width(320.dp),
                    accent = FieldGreen
                )
            }
        }
    }
}
