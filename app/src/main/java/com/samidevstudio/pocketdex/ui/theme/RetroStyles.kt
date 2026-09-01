package com.samidevstudio.pocketdex.ui.theme

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
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
    fun cradleShape(cutoutRadius: Dp = 64.dp): Shape = CradleShapeImpl(
        cutoutRadius = cutoutRadius,
        cornerRadius = 15.dp  
    )
    
    val CradleShape = cradleShape()
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
 * A hardware-accelerated modifier that draws a retro checkered background.
 * Uses drawWithCache to reduce draw calls from O(N) to O(1) and cache the shader.
 */
fun Modifier.retroBackground(
    gridSize: Dp = RetroStyles.GridSize,
    color1: Color,
    color2: Color
): Modifier = this.drawWithCache {
    val sizePx = gridSize.toPx().toInt()
    if (sizePx <= 0) {
        onDrawBehind { }
    } else {
        // Create a tiny 2x2 pattern bitmap
        val patternSize = sizePx * 2
        val bitmap = ImageBitmap(patternSize, patternSize)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        // Square 1 (Top-Left)
        paint.color = color1
        canvas.drawRect(Rect(0f, 0f, sizePx.toFloat(), sizePx.toFloat()), paint)
        // Square 2 (Top-Right)
        paint.color = color2
        canvas.drawRect(Rect(sizePx.toFloat(), 0f, patternSize.toFloat(), sizePx.toFloat()), paint)
        // Square 3 (Bottom-Left)
        paint.color = color2
        canvas.drawRect(Rect(0f, sizePx.toFloat(), sizePx.toFloat(), patternSize.toFloat()), paint)
        // Square 4 (Bottom-Right)
        paint.color = color1
        canvas.drawRect(Rect(sizePx.toFloat(), sizePx.toFloat(), patternSize.toFloat(), patternSize.toFloat()), paint)

        val shader = ImageShader(bitmap, TileMode.Repeated, TileMode.Repeated)
        val brush = ShaderBrush(shader)

        onDrawBehind {
            drawRect(brush = brush)
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
