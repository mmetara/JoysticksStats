package com.joysticks.stats.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.joysticks.stats.engine.AlignementRepository
import com.joysticks.stats.engine.GameViewModel
import com.joysticks.stats.engine.parseRoster
import com.joysticks.stats.engine.GameScreenMode
import com.joysticks.stats.ui.components.HudButton
import com.joysticks.stats.ui.components.HudPanel
import com.joysticks.stats.ui.theme.ChalkWhite
import com.joysticks.stats.ui.theme.FieldGreen
import com.joysticks.stats.ui.theme.HudBlue
import com.joysticks.stats.ui.theme.HudBorder
import com.joysticks.stats.ui.theme.HudMuted
import com.joysticks.stats.ui.theme.HudPanelSoft
import com.joysticks.stats.ui.navigation.Screen
import com.joysticks.stats.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast

@Composable
fun HomeScreen(navController: NavController, viewModel: GameViewModel) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isBusy by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val repository = remember { AlignementRepository() }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            statusMessage = "Import annulé"
            isBusy = false
            return@rememberLauncherForActivityResult
        }
        if (isBusy) return@rememberLauncherForActivityResult
        scope.launch {
            isBusy = true
            statusMessage = "Import en cours..."
            try {
                val parsedRoster = withContext(Dispatchers.IO) {
                    parseRoster(context, uri)
                }
                viewModel.loadRoster(parsedRoster)
                statusMessage = "Import réussi"
                navController.navigate(Screen.Game.route)
            } catch (e: Exception) {
                statusMessage = "Import impossible: fichier invalide"
                Toast.makeText(context, "Import impossible: fichier invalide", Toast.LENGTH_LONG).show()
            } finally {
                isBusy = false
            }
        }
    }

    val onDownloadClick: () -> Unit = {
        if (!isBusy) {
            if (!NetworkUtils.isTargetWifiConnected(context)) {
                statusMessage = "Erreur: Connectez-vous au WiFi 'mmetara'"
                Toast.makeText(context, "Veuillez vous connecter au WiFi 'mmetara'", Toast.LENGTH_LONG).show()
            } else {
                scope.launch {
                    isBusy = true
                    statusMessage = "Téléchargement en cours..."
                    Toast.makeText(context, "Téléchargement en cours...", Toast.LENGTH_SHORT).show()
                    try {
                        val roster = withContext(Dispatchers.IO) {
                            val file = repository.downloadRoster(context) ?: return@withContext null
                            val uri = Uri.fromFile(file)
                            parseRoster(context, uri)
                        }

                        if (roster == null) {
                            statusMessage = "Erreur de téléchargement"
                            Toast.makeText(context, "Erreur de téléchargement", Toast.LENGTH_LONG).show()
                        } else {
                            statusMessage = "Téléchargement réussi"
                            viewModel.loadRoster(roster)
                            navController.navigate(Screen.Game.route)
                        }
                    } catch (e: Exception) {
                        statusMessage = "Téléchargement impossible"
                        Toast.makeText(context, "Téléchargement impossible", Toast.LENGTH_LONG).show()
                    } finally {
                        isBusy = false
                    }
                }
            }
        }
    }

    val onImportClick: () -> Unit = {
        if (!isBusy) {
            statusMessage = "Ouverture du sélecteur de fichier..."
            filePicker.launch(arrayOf("text/csv", "text/*", "*/*"))
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BaseballScreenTemplate {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                HudPanel(
                    modifier = Modifier
                        .padding(12.dp)
                        .widthIn(max = 460.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    borderColor = HudBorder,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "JOYSTICKS STATS",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = ChalkWhite
                        )

                        Text(
                            text = "GESTION DE MATCH",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = FieldGreen
                        )

                        Spacer(Modifier.height(16.dp))
                        HudButton(
                            text = "Télécharger l'alignement",
                            onClick = onDownloadClick,
                            modifier = Modifier.fillMaxWidth(0.82f),
                            enabled = !isBusy
                        )

                        Spacer(Modifier.height(16.dp))

                        HudButton(
                            text = "Importer l'alignement",
                            onClick = onImportClick,
                            modifier = Modifier.fillMaxWidth(0.82f),
                            enabled = !isBusy
                        )

                        Spacer(Modifier.height(8.dp))

                        statusMessage?.let {
                            Text(
                                text = it,
                                color = if (it.contains("impossible") || it.contains("Erreur")) MaterialTheme.colorScheme.error else HudMuted,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        HudButton(
                            text = "Gérer les équipes",
                            onClick = { navController.navigate(Screen.TeamManager.route) },
                            modifier = Modifier.fillMaxWidth(0.82f),
                            accent = HudPanelSoft
                        )

                        Spacer(Modifier.height(16.dp)) // Added spacer for the new button

                        val isGameInProgress = viewModel.gameState.roster != null &&
                                viewModel.gameState.screenMode != GameScreenMode.PRE_GAME &&
                                viewModel.gameState.screenMode != GameScreenMode.GAME_OVER

                        if (isGameInProgress) {
                            HudButton(
                                text = "Retour à la partie",
                                onClick = { navController.navigate(Screen.Game.route) },
                                modifier = Modifier.fillMaxWidth(0.82f),
                                accent = HudBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BaseballScreenTemplate(content: @Composable BoxScope.() -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        StadiumBackdrop(modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.14f))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(
                            FieldGreen.copy(alpha = 0.10f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.88f)
                        ),
                        radius = 1350f
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            content = content
        )
    }
}

@Composable
private fun StadiumBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRect(
            Brush.verticalGradient(
                listOf(
                    Color(0xFF02050A),
                    Color(0xFF08110F),
                    Color(0xFF06110B),
                    Color(0xFF020403)
                )
            )
        )

        drawOval(
            brush = Brush.radialGradient(
                listOf(
                    Color(0x88376C38),
                    Color(0x331B2C1F),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.54f),
                radius = w * 0.56f
            ),
            topLeft = Offset(w * 0.06f, h * 0.10f),
            size = Size(w * 0.88f, h * 0.72f)
        )

        val home = Offset(w * 0.5f, h * 0.72f)
        val leftFoul = Offset(w * 0.18f, h * 0.20f)
        val rightFoul = Offset(w * 0.82f, h * 0.20f)

        val field = Path().apply {
            moveTo(home.x, home.y)
            lineTo(leftFoul.x, leftFoul.y)
            quadraticBezierTo(w * 0.5f, h * 0.05f, rightFoul.x, rightFoul.y)
            close()
        }
        drawPath(
            path = field,
            brush = Brush.verticalGradient(
                listOf(
                    Color(0x33255B33),
                    Color(0x66274A28),
                    Color(0x77172117)
                )
            )
        )

        repeat(8) { index ->
            val startX = w * (0.18f + index * 0.085f)
            val stripe = Path().apply {
                moveTo(startX, h * 0.18f)
                lineTo(startX + w * 0.16f, h * 0.18f)
                lineTo(startX - w * 0.08f, h * 0.77f)
                lineTo(startX - w * 0.24f, h * 0.77f)
                close()
            }
            drawPath(stripe, Color.White.copy(alpha = if (index % 2 == 0) 0.025f else 0.012f))
        }

        drawLine(Color.White.copy(alpha = 0.13f), home, leftFoul, strokeWidth = 2.5f)
        drawLine(Color.White.copy(alpha = 0.13f), home, rightFoul, strokeWidth = 2.5f)
        drawArc(
            color = Color.White.copy(alpha = 0.08f),
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(w * 0.22f, h * 0.08f),
            size = Size(w * 0.56f, h * 0.56f),
            style = Stroke(width = 2f)
        )

        drawRect(
            Brush.verticalGradient(
                listOf(
                    Color.Black.copy(alpha = 0.88f),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.84f)
                )
            )
        )
        drawRect(
            Brush.horizontalGradient(
                listOf(
                    Color.Black.copy(alpha = 0.82f),
                    Color.Transparent,
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.82f)
                )
            )
        )
    }
}
