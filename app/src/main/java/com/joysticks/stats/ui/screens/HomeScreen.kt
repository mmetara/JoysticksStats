package com.joysticks.stats.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.joysticks.stats.R
import com.joysticks.stats.engine.AlignementRepository
import com.joysticks.stats.engine.GameScreenMode
import com.joysticks.stats.engine.GameViewModel
import com.joysticks.stats.engine.parseRoster
import com.joysticks.stats.ui.components.*
import com.joysticks.stats.ui.navigation.Screen
import com.joysticks.stats.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    gameViewModel: GameViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val pickCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                scope.launch {
                    isLoading = true
                    try {
                        val roster = parseRoster(context, it)
                        gameViewModel.loadRoster(roster)
                        navController.navigate(Screen.LineUp.route)
                    } catch (e: Exception) {
                        // Gérer l'erreur
                    } finally {
                        isLoading = false
                    }
                }
            }
        }
    )

    BaseballScreenTemplate {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header / Logo
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    color = HudPanel,
                    border = androidx.compose.foundation.BorderStroke(2.dp, FieldGreen.copy(alpha = 0.5f))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "JOYSTICKS",
                    style = MaterialTheme.typography.displaySmall,
                    color = ChalkWhite,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "STATISTIQUES",
                    style = MaterialTheme.typography.labelMedium,
                    color = FieldGreen,
                    letterSpacing = 2.sp
                )
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .width(40.dp)
                        .height(3.dp)
                        .background(FieldGreen)
                )
            }

            // Main Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val state = gameViewModel.gameState
                
                // Détection robuste : alignement chargé + progression (manche, historique ou mode)
                // Masqué si le match est terminé (GAME_OVER)
                val isGameInProgress = state.roster != null && 
                    state.screenMode != GameScreenMode.GAME_OVER && (
                    state.gameStarted || 
                    state.inning > 1 || 
                    state.gameHistory.isNotEmpty()
                )

                if (isGameInProgress) {
                    HudActionButton(
                        label = "CONTINUER LA PARTIE",
                        subLabel = "Manche ${state.inning} - ${if (state.isTop) "Haut" else "Bas"}",
                        modifier = Modifier.fillMaxWidth(),
                        accent = Color(0xFFFF9800), // Orange pour se démarquer
                        onClick = {
                            navController.navigate(Screen.Game.route)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HudActionButton(
                        label = "NOUVEAU MATCH",
                        subLabel = "Importer un fichier CSV",
                        modifier = Modifier.weight(1f),
                        accent = FieldGreen,
                        onClick = { pickCsvLauncher.launch(arrayOf("*/*")) }
                    )

                    HudActionButton(
                        label = "RÉCUPÉRER",
                        subLabel = "Télécharger alignement",
                        modifier = Modifier.weight(1f),
                        accent = HudBlue,
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val result = AlignementRepository.downloadRoster(context)
                                result.onSuccess { file ->
                                    val roster = parseRoster(context, Uri.fromFile(file))
                                    gameViewModel.loadRoster(roster)
                                    navController.navigate(Screen.LineUp.route)
                                }.onFailure {
                                    // Erreur (ex: mauvais WiFi)
                                }
                                isLoading = false
                            }
                        }
                    )
                }
            }

            // Bottom Tools
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickToolButton(
                    icon = Icons.AutoMirrored.Filled.List,
                    label = "Équipes",
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.TeamManager.route) }
                )
                QuickToolButton(
                    icon = Icons.Default.Settings,
                    label = "Paramètres",
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Settings.route) }
                )
                QuickToolButton(
                    icon = Icons.Default.Info,
                    label = "Aide",
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Help.route) }
                )
            }

            // Version info
            Text(
                text = "VERSION 2.1.0",
                style = MaterialTheme.typography.labelSmall,
                color = HudMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FieldGreen)
            }
        }
    }
}

@Composable
fun QuickToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        color = HudPanel,
        border = androidx.compose.foundation.BorderStroke(1.dp, HudBorder)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(icon, contentDescription = null, tint = HudMuted, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = ChalkWhite,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
