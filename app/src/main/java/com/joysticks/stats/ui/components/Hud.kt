package com.joysticks.stats.ui.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.joysticks.stats.data.SettingsDataStore
import com.joysticks.stats.ui.theme.*

@Composable
fun PlayerPlaceholder(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.46f
        
        // Tête
        drawCircle(
            color = Color(0xFF3B414A),
            radius = radius * 0.26f,
            center = Offset(center.x, center.y - radius * 0.22f)
        )
        
        // Épaules
        drawOval(
            color = Color(0xFF3B414A),
            topLeft = Offset(center.x - radius * 0.52f, center.y + radius * 0.12f),
            size = Size(radius * 1.04f, radius * 0.52f)
        )
    }
}

@Composable
fun PlayerAvatar(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    borderColor: Color = FieldGreen,
    showPlaceholder: Boolean = true,
    playerName: String? = null
) {
    val context = LocalContext.current
    val settingsDataStore = remember { SettingsDataStore(context) }
    val baseUrl by settingsDataStore.playerImagesUrlFlow.collectAsState(initial = SettingsDataStore.DEFAULT_IMAGES_URL)
    
    val trimmedUrl = photoUrl?.trim()
    val fullUrl = if (!trimmedUrl.isNullOrBlank()) {
        if (trimmedUrl.startsWith("http")) trimmedUrl else "$baseUrl$trimmedUrl"
    } else null

    LaunchedEffect(fullUrl) {
        if (fullUrl != null) {
            Log.d("PlayerAvatar", "Tentative de chargement pour ${playerName ?: "Inconnu"} : $fullUrl")
        }
    }
    
    val hasPhoto = !fullUrl.isNullOrBlank()

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .then(
                if (hasPhoto || showPlaceholder) 
                    Modifier.border(2.dp, borderColor.copy(alpha = 0.5f), CircleShape)
                            .background(Color.Black.copy(alpha = 0.3f))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (hasPhoto) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(fullUrl)
                    .crossfade(true)
                    .build(),
                loading = { if (showPlaceholder) PlayerPlaceholder() },
                error = { 
                    Log.e("PlayerAvatar", "ERREUR 404 ou réseau pour ${playerName ?: "Inconnu"} : $fullUrl")
                    if (showPlaceholder) PlayerPlaceholder()
                },
                contentDescription = "Photo de ${playerName ?: "joueur"}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (showPlaceholder) {
            PlayerPlaceholder()
        }
    }
}

@Composable
fun HudPanel(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(10.dp),
    borderColor: Color = HudBorder,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = HudPanel,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.04f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.18f)
                        )
                    )
                )
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun HudActionButton(
    label: String,
    subLabel: String? = null,
    modifier: Modifier = Modifier,
    accent: Color = FieldGreen,
    selected: Boolean = false,
    filledStyle: Boolean = false,
    filledGradient: List<Color>? = null,
    emphasizeSubLabel: Boolean = false,
    enabled: Boolean = true,
    height: Dp = 64.dp,
    onClick: () -> Unit
) {
    val baseColor = if (filledStyle) {
        Color(
            red = (accent.red * 0.16f).coerceIn(0f, 1f),
            green = (accent.green * 0.12f).coerceIn(0f, 1f),
            blue = (accent.blue * 0.12f).coerceIn(0f, 1f),
            alpha = 0.96f
        )
    } else if (selected) {
        accent.copy(alpha = 0.22f)
    } else {
        Color(0xF00D131C)
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height),
        shape = RoundedCornerShape(8.dp),
        color = baseColor,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) accent else accent.copy(alpha = 0.52f)),
        shadowElevation = if (selected) 12.dp else 7.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (filledStyle) {
                        Brush.horizontalGradient(
                            colors = filledGradient ?: listOf(
                                accent.copy(alpha = 0.42f),
                                accent.copy(alpha = 0.62f),
                                accent.copy(alpha = 0.46f)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            listOf(
                                accent.copy(alpha = if (selected) 0.34f else 0.24f),
                                Color.Transparent
                            )
                        )
                    }
                )
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (selected) 0.16f else 0.10f),
                            Color.Transparent,
                            Color.Black.copy(alpha = if (filledStyle) 0.28f else 0.20f)
                        )
                    )
                )
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    fontSize = 24.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = if (filledStyle) Color.White else (if (selected) accent else accent.copy(alpha = 0.95f)),
                    textAlign = TextAlign.Center
                )
                if (subLabel != null) {
                    Text(
                        text = subLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (filledStyle) Color.White.copy(alpha = 0.85f) else (if (selected || emphasizeSubLabel) accent.copy(alpha = 0.9f) else HudMuted),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun HudButton(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = FieldGreen,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(shape)
            .background(if (enabled) accent.copy(alpha = 0.9f) else HudMuted.copy(alpha = 0.25f))
            .border(width = 1.dp, color = if (enabled) Color.White.copy(alpha = 0.18f) else Color.Transparent, shape = shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text.uppercase(),
            fontWeight = FontWeight.ExtraBold,
            color = if (enabled) ChalkWhite else HudMuted
        )
    }
}

@Composable
fun HudTitleBlock(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    accent: Color = FieldGreen
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = eyebrow.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.displaySmall,
            color = ChalkWhite,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth(0.12f)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent)
        )
    }
}

@Composable
fun HudScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = FieldGreen,
    actionContent: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .background(HudPanel, RoundedCornerShape(8.dp))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour",
                tint = ChalkWhite
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = ChalkWhite,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Box(
                Modifier
                    .width(60.dp)
                    .height(4.dp)
                    .background(accent)
            )
        }
        if (actionContent != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                content = actionContent
            )
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
            quadraticTo(w * 0.5f, h * 0.05f, rightFoul.x, rightFoul.y)
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
