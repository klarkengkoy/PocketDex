package com.samidevstudio.pocketdex.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samidevstudio.pocketdex.ui.theme.PokemonTypeColors

@Composable
fun DetailTypeBadge(
    type: String,
    label: String = type,
    onClick: (() -> Unit)? = null
) {
    val colors = PokemonTypeColors.map[type.lowercase()] ?: (Color.Gray to Color.Gray)
    val whiteContrast = minOf(
        contrastRatio(colors.first, Color.White),
        contrastRatio(colors.second, Color.White)
    )
    val blackContrast = minOf(
        contrastRatio(colors.first, Color.Black),
        contrastRatio(colors.second, Color.Black)
    )
    val textColor = if (whiteContrast >= blackContrast) Color.White else Color.Black

    val brush = Brush.verticalGradient(
        0.5f to colors.first,
        0.5f to colors.second
    )
    val clickModifier = onClick?.let { click ->
        Modifier
            .heightIn(min = 48.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = "Open ${type.lowercase()} type details",
                onClick = click
            )
    } ?: Modifier

    Box(
        modifier = clickModifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color.Transparent,
            shape = RectangleShape,
            modifier = Modifier
                .widthIn(min = 90.dp)
                .background(brush)
                .border(1.dp, MaterialTheme.colorScheme.onSurface, RectangleShape)
        ) {
            Text(
                text = label.uppercase(),
                color = textColor,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

private fun contrastRatio(background: Color, foreground: Color): Float {
    val backgroundLuminance = background.luminance()
    val foregroundLuminance = foreground.luminance()
    val lighter = maxOf(backgroundLuminance, foregroundLuminance)
    val darker = minOf(backgroundLuminance, foregroundLuminance)
    return (lighter + 0.05f) / (darker + 0.05f)
}

@Composable
fun RetroStatBar(name: String, value: Int) {
    val animatedValue by animateIntAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 500),
        label = "statValue"
    )
    
    val animatedProgress by animateFloatAsState(
        targetValue = value / 255f,
        animationSpec = tween(durationMillis = 500),
        label = "statProgress"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name.uppercase(),
            modifier = Modifier.width(90.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp, // Slightly smaller to ensure fit
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Text(
            text = animatedValue.toString().padStart(3),
            modifier = Modifier.padding(start = 8.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
