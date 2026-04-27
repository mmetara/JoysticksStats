package com.joysticks.stats.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joysticks.stats.ui.theme.ChalkWhite
import com.joysticks.stats.ui.theme.FieldGreen
import com.joysticks.stats.ui.theme.HudBackground
import com.joysticks.stats.ui.theme.HudBackgroundDeep
import com.joysticks.stats.ui.theme.HudBorder
import com.joysticks.stats.ui.theme.HudMuted
import com.joysticks.stats.ui.theme.HudPanel as HudPanelColor

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
        color = HudPanelColor,
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
                    color = if (selected) accent else accent.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center
                )
                if (subLabel != null) {
                    Text(
                        text = subLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected || emphasizeSubLabel) accent.copy(alpha = 0.9f) else HudMuted,
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
fun HudScreenChrome(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackgroundDeep)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            FieldGreen.copy(alpha = 0.22f),
                            HudBackground.copy(alpha = 0.82f),
                            HudBackgroundDeep
                        ),
                        radius = 900f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.34f))
        )
        content()
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
