package com.example.electricitybillmonitoring.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

@Composable
fun WaveHeader(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF1E88E5),
    secondaryColor: Color = Color(0xFF00ACC1)
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val width = size.width
        val height = size.height

        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(width, 0f)
            lineTo(width, height * 0.7f)
            cubicTo(
                width * 0.75f, height * 0.95f,
                width * 0.35f, height * 0.5f,
                0f, height * 0.85f
            )
            close()
        }

        val secondPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(width, 0f)
            lineTo(width, height * 0.5f)
            cubicTo(
                width * 0.7f, height * 0.8f,
                width * 0.3f, height * 0.4f,
                0f, height * 0.75f
            )
            close()
        }

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor, secondaryColor)
            )
        )

        drawPath(
            path = secondPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.5f), secondaryColor.copy(alpha = 0.5f))
            )
        )
    }
}
