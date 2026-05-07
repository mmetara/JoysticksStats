package com.joysticks.stats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.joysticks.stats.ui.components.BaseballScreenTemplate
import com.joysticks.stats.ui.components.HudButton
import com.joysticks.stats.ui.components.HudPanel
import com.joysticks.stats.ui.components.HudScreenHeader
import com.joysticks.stats.ui.components.HudTitleBlock
import com.joysticks.stats.ui.theme.*

@Composable
fun HelpScreen(navController: NavController) {
    BaseballScreenTemplate {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            HudScreenHeader(
                title = "AIDE",
                onBack = { navController.popBackStack() },
                accent = HudBlue
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HelpSection(
                    title = "PHOTOS DES JOUEURS",
                    icon = Icons.Default.Info,
                    accent = FieldGreen,
                    content = "Les photos sont chargées depuis le serveur central. " +
                            "Format requis : .webp. Si une photo manque, une silhouette par défaut est affichée."
                )

                HelpSection(
                    title = "SAUVEGARDE AUTO",
                    icon = Icons.Default.CheckCircle,
                    accent = HudBlue,
                    content = "Chaque action (point, retrait) est sauvegardée instantanément. " +
                            "Si l'application se ferme, vous reprendrez exactement où vous en étiez."
                )

                HelpSection(
                    title = "MODE HORS-LIGNE",
                    icon = Icons.Default.Refresh,
                    accent = Color.Yellow,
                    content = "Une fois chargées avec internet, les photos sont stockées dans le cache. " +
                            "L'application est 100% fonctionnelle sur le terrain sans réseau."
                )

                HelpSection(
                    title = "IMPORT CSV",
                    icon = Icons.Default.Warning,
                    accent = Color(0xFFFF9800),
                    content = "Assurez-vous que votre CSV contient au moins 7 colonnes. " +
                            "Les espaces inutiles sont automatiquement nettoyés lors de l'import."
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HelpSection(
    title: String,
    icon: ImageVector,
    accent: Color,
    content: String
) {
    HudPanel(
        modifier = Modifier.fillMaxWidth(),
        borderColor = accent.copy(alpha = 0.3f),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = ChalkWhite,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = HudMuted,
            lineHeight = 20.sp
        )
    }
}
