package com.samidevstudio.pocketdex.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * A custom-drawn Pokeball using the Compose Canvas API.
 * Uses relative coordinates to ensure "Pixel Perfection" at any size.
 */
@Composable
fun PokeballCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)
        val strokeWidth = 4.dp.toPx()

        // 1. Draw the Bottom White Half
        drawArc(
            color = Color.White,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
            size = Size(size.width, size.height)
        )

        // 2. Draw the Top Red Half
        drawArc(
            color = Color(0xFFE3350D), // Classic Pokedex Red
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            size = Size(size.width, size.height)
        )

        // 3. Draw the Horizontal Black "Belt"
        // We draw this slightly thick to give it that retro heavy-line feel
        drawLine(
            color = Color.Black,
            start = Offset(0f, center.y),
            end = Offset(size.width, center.y),
            strokeWidth = strokeWidth * 1.5f
        )

        // 4. Draw the Outer Black Circle (The shell border)
        drawCircle(
            color = Color.Black,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // 5. Draw the Center Button (Outer Black Circle)
        drawCircle(
            color = Color.Black,
            radius = radius * 0.25f,
            center = center
        )

        // 6. Draw the Center Button (Inner White Circle)
        drawCircle(
            color = Color.White,
            radius = radius * 0.15f,
            center = center
        )
    }
}
