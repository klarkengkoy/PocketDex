package com.samidevstudio.pocketdex.ui.theme

import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Centralized Retro Constants for "Pixel Perfection"
 */
object RetroStyles {
    val BorderWidth = 2.dp
    val BorderColor = Color.Black
    val GridSize = 20.dp
    
    // Default colors for light mode
    val CanvasColor1 = Color.White
    val CanvasColor2 = Color(0xFFEEEEEE)

    /**
     * A custom shape for the BottomAppBar that creates a "Cradle" cutout
     * for the central Pokeball.
     */
    val CradleShape: Shape = CradleShapeImpl(
        cutoutRadius = 64.dp,
        cornerRadius = 15.dp  
    )
}

/**
 * Custom implementation of a Shape with a top-center semi-circle cutout
 * and rounded top corners.
 */
private class CradleShapeImpl(
    private val cutoutRadius: Dp,
    private val cornerRadius: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val radiusPx = with(density) { cutoutRadius.toPx() }
        val cornerPx = with(density) { cornerRadius.toPx() }
        val cutoutWidth = radiusPx * 2
        
        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(0f, cornerPx)
            arcTo(
                rect = Rect(0f, 0f, cornerPx * 2, cornerPx * 2),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo((size.width - cutoutWidth) / 2, 0f)
            arcTo(
                rect = Rect(
                    left = (size.width - cutoutWidth) / 2,
                    top = -radiusPx,
                    right = (size.width + cutoutWidth) / 2,
                    bottom = radiusPx
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            lineTo(size.width - cornerPx, 0f)
            arcTo(
                rect = Rect(size.width - cornerPx * 2, 0f, size.width, cornerPx * 2),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * A custom modifier that draws a retro checkered "canvas" background.
 * Now supports dynamic theming by accepting colors.
 */
fun Modifier.retroBackground(
    gridSize: Dp = RetroStyles.GridSize,
    color1: Color,
    color2: Color
): Modifier = this.drawBehind {
    val sizePx = gridSize.toPx()
    val columns = (size.width / sizePx).toInt() + 1
    val rows = (size.height / sizePx).toInt() + 1

    for (x in 0 until columns) {
        for (y in 0 until rows) {
            val color = if ((x + y) % 2 == 0) color1 else color2
            drawRect(
                color = color,
                topLeft = Offset(x * sizePx, y * sizePx),
                size = Size(sizePx, sizePx)
            )
        }
    }
}

/**
 * Applies the standard "GameBoy" black border.
 */
fun Modifier.retroBorder(
    width: Dp = RetroStyles.BorderWidth,
    color: Color = RetroStyles.BorderColor
): Modifier = this.border(width = width, color = color, shape = RectangleShape)
