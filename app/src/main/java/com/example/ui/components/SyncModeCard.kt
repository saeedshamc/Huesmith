package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.color.HslColor
import java.util.Locale

@Composable
fun SyncModeCard(
    isSyncActive: Boolean,
    syncedIndices: Set<Int>,
    currentColors: List<HslColor>,
    onToggleSyncMode: () -> Unit,
    onToggleSyncIndex: (Int) -> Unit,
    onSyncAll: () -> Unit,
    onApplySyncAdjustments: (deltaHue: Float, deltaSat: Float, deltaLight: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var deltaHue by remember { mutableFloatStateOf(0f) }
    var deltaSat by remember { mutableFloatStateOf(0f) }
    var deltaLight by remember { mutableFloatStateOf(0f) }

    val colorLabels = listOf("Base", "Comp", "Accent", "Neutral", "Tonal")

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSyncActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSyncActive) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("sync_mode_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Master Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSyncActive) Icons.Default.Link else Icons.Default.LinkOff,
                        contentDescription = null,
                        tint = if (isSyncActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Harmonic Sync Mode",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isSyncActive) "Linked adjustments maintaining color harmony" else "Disabled — Tap switch to link palette colors",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSyncActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isSyncActive,
                    onCheckedChange = { onToggleSyncMode() },
                    modifier = Modifier.testTag("sync_mode_switch")
                )
            }

            AnimatedVisibility(visible = isSyncActive) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // Sync Selection Bar (which colors are linked)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Linked Colors (${syncedIndices.size}/${currentColors.size}):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedButton(
                            onClick = onSyncAll,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Link All", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Color Link Swatches Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentColors.take(5).forEachIndexed { idx, col ->
                            val isLinked = syncedIndices.contains(idx)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isLinked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(
                                        width = if (isLinked) 2.dp else 1.dp,
                                        color = if (isLinked) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onToggleSyncIndex(idx) }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(col.toComposeColor())
                                        .border(1.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLinked) {
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = "Linked",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = colorLabels.getOrElse(idx) { "C${idx + 1}" },
                                    fontSize = 10.sp,
                                    fontWeight = if (isLinked) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isLinked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isLinked) "SYNC" else "OFF",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLinked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Linked Harmonic Hue Rotation Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Harmonic Hue Rotation (ΔH)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${if (deltaHue >= 0) "+" else ""}${deltaHue.toInt()}°",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = deltaHue,
                            onValueChange = { newVal ->
                                val diff = newVal - deltaHue
                                deltaHue = newVal
                                onApplySyncAdjustments(diff, 0f, 0f)
                            },
                            valueRange = -180f..180f,
                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("sync_hue_slider")
                        )
                    }

                    // 2. Linked Saturation Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Relative Saturation (ΔS)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${if (deltaSat >= 0) "+" else ""}${(deltaSat * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = deltaSat,
                            onValueChange = { newVal ->
                                val diff = newVal - deltaSat
                                deltaSat = newVal
                                onApplySyncAdjustments(0f, diff, 0f)
                            },
                            valueRange = -0.5f..0.5f,
                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("sync_saturation_slider")
                        )
                    }

                    // 3. Linked Lightness Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Relative Lightness (ΔL)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${if (deltaLight >= 0) "+" else ""}${(deltaLight * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = deltaLight,
                            onValueChange = { newVal ->
                                val diff = newVal - deltaLight
                                deltaLight = newVal
                                onApplySyncAdjustments(0f, 0f, diff)
                            },
                            valueRange = -0.4f..0.4f,
                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("sync_lightness_slider")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Reset Sliders Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                deltaHue = 0f
                                deltaSat = 0f
                                deltaLight = 0f
                            }
                        ) {
                            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Sliders", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
