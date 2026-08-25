package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.color.ColorHarmonyAlgorithm
import com.example.color.HslColor
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun InteractiveColorWheel(
    baseColor: HslColor,
    harmony: ColorHarmonyAlgorithm,
    onColorChanged: (HslColor) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_color_wheel"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Live Harmony Feedback Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(baseColor.toComposeColor())
                        .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Wheel Harmonic Map",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${harmony.title} Harmony • ${baseColor.hue.toInt()}°",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = baseColor.toHex(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Color Wheel Canvas with Drag/Tap Gesture Detection
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(1f)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = min(size.width, size.height) / 2f - 16f
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val dist = sqrt(dx * dx + dy * dy)

                            var angleDeg = (atan2(dy.toDouble(), dx.toDouble()) * 180.0 / PI).toFloat()
                            if (angleDeg < 0f) angleDeg += 360f

                            val newSat = (dist / radius).coerceIn(0.1f, 1.0f)
                            onColorChanged(baseColor.copy(hue = angleDeg, saturation = newSat))
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                            onDrag = { change, _ ->
                                change.consume()
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val radius = min(size.width, size.height) / 2f - 16f
                                val dx = change.position.x - center.x
                                val dy = change.position.y - center.y
                                val dist = sqrt(dx * dx + dy * dy)

                                var angleDeg = (atan2(dy.toDouble(), dx.toDouble()) * 180.0 / PI).toFloat()
                                if (angleDeg < 0f) angleDeg += 360f

                                val newSat = (dist / radius).coerceIn(0.1f, 1.0f)
                                onColorChanged(baseColor.copy(hue = angleDeg, saturation = newSat))
                            }
                        )
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = min(size.width, size.height) / 2f - 16f

                // 1. Draw Chromatic Hue-Saturation Wheel Disk
                drawColorWheelDisk(center, radius)

                // 2. Draw Wheel Guide Rings
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = radius * 0.5f,
                    center = center,
                    style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
                )

                // 3. Compute Base Pin Position
                val baseRad = baseColor.hue * PI / 180.0
                val baseDist = radius * baseColor.saturation.coerceIn(0.1f, 1f)
                val basePinPos = Offset(
                    x = (center.x + baseDist * cos(baseRad)).toFloat(),
                    y = (center.y + baseDist * sin(baseRad)).toFloat()
                )

                // 4. Draw Harmony Connection Lines & Companion Pins
                val harmonyOffsets = harmony.angles.map { angleOffset ->
                    val totalHue = (baseColor.hue + angleOffset + 360f) % 360f
                    val rad = totalHue * PI / 180.0
                    val dist = if (harmony == ColorHarmonyAlgorithm.MONOCHROMATIC && angleOffset != 0f) {
                        radius * (baseColor.saturation * (1f - angleOffset / 100f)).coerceIn(0.2f, 0.9f)
                    } else {
                        baseDist
                    }
                    Offset(
                        x = (center.x + dist * cos(rad)).toFloat(),
                        y = (center.y + dist * sin(rad)).toFloat()
                    )
                }

                // Draw connecting geometric chords
                if (harmonyOffsets.size > 1) {
                    for (i in harmonyOffsets.indices) {
                        val p1 = harmonyOffsets[i]
                        val p2 = harmonyOffsets[(i + 1) % harmonyOffsets.size]
                        drawLine(
                            color = Color.White.copy(alpha = 0.6f),
                            start = p1,
                            end = p2,
                            strokeWidth = 2.5f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    }
                }

                // Draw center spoke lines to pins
                harmonyOffsets.forEach { pin ->
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f),
                        start = center,
                        end = pin,
                        strokeWidth = 1.5f
                    )
                }

                // Draw Companion Harmony Markers
                harmony.angles.forEachIndexed { index, angleOffset ->
                    if (angleOffset != 0f) {
                        val companionHue = (baseColor.hue + angleOffset + 360f) % 360f
                        val pinPos = harmonyOffsets.getOrElse(index) { basePinPos }
                        val companionColor = HslColor(companionHue, baseColor.saturation, baseColor.lightness).toComposeColor()

                        // Outer ring
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.5f),
                            radius = 11f,
                            center = pinPos
                        )
                        // Swatch fill
                        drawCircle(
                            color = companionColor,
                            radius = 9f,
                            center = pinPos
                        )
                        // White border
                        drawCircle(
                            color = Color.White,
                            radius = 9f,
                            center = pinPos,
                            style = Stroke(width = 2.5f)
                        )
                    }
                }

                // 5. Draw Primary Base Anchor Pin (Highlighted with halo ring)
                drawCircle(
                    color = Color.Black.copy(alpha = 0.4f),
                    radius = 20f,
                    center = basePinPos
                )
                drawCircle(
                    color = baseColor.toComposeColor(),
                    radius = 15f,
                    center = basePinPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 15f,
                    center = basePinPos,
                    style = Stroke(width = 3.5f)
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.6f),
                    radius = 4f,
                    center = basePinPos
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Harmony Description Note
        Text(
            text = harmony.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

/**
 * Draws a smooth 360° sweep hue ring with radial white desaturation towards center.
 */
private fun DrawScope.drawColorWheelDisk(center: Offset, radius: Float) {
    val sweepColors = listOf(
        Color(0xFFFF0000), // 0° Red
        Color(0xFFFFFF00), // 60° Yellow
        Color(0xFF00FF00), // 120° Green
        Color(0xFF00FFFF), // 180° Cyan
        Color(0xFF0000FF), // 240° Blue
        Color(0xFFFF00FF), // 300° Magenta
        Color(0xFFFF0000)  // 360° Red
    )

    val sweepBrush = Brush.sweepGradient(
        colors = sweepColors,
        center = center
    )

    // Base Hue Disk
    drawCircle(
        brush = sweepBrush,
        radius = radius,
        center = center
    )

    // Center Radial Desaturation (White Center -> Transparent Edge)
    val radialDesat = Brush.radialGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.95f),
            Color.White.copy(alpha = 0.4f),
            Color.Transparent
        ),
        center = center,
        radius = radius
    )
    drawCircle(
        brush = radialDesat,
        radius = radius,
        center = center
    )

    // Outer Dark Vignette for depth
    val radialDark = Brush.radialGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = 0.15f)
        ),
        center = center,
        radius = radius
    )
    drawCircle(
        brush = radialDark,
        radius = radius,
        center = center
    )
}
