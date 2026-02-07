package com.samidevstudio.pocketdex.ui.theme

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized Retro Constants for "Pixel Perfection"
 */
object RetroStyles {
    val BorderWidth = 2.dp
    val BorderColor = Color.Black
    val GridSize = 20.dp
    
    val CanvasColor1 = Color.White
    val CanvasColor2 = Color(0xFFEEEEEE)
}

/**
 * A custom modifier that draws a retro checkered "canvas" background.
 * Moved here for centralized project-wide styling.
 */
fun Modifier.retroBackground(
    gridSize: Dp = RetroStyles.GridSize,
    color1: Color = RetroStyles.CanvasColor1,
    color2: Color = RetroStyles.CanvasColor2
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
