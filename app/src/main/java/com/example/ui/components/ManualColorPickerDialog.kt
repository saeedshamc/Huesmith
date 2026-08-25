package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.color.HslColor
import com.example.color.RgbColor

@Composable
fun ManualColorPickerDialog(
    initialColor: HslColor,
    onColorSelected: (HslColor) -> Unit,
    onDismiss: () -> Unit
) {
    var hue by remember { mutableFloatStateOf(initialColor.hue) }
    var saturation by remember { mutableFloatStateOf(initialColor.saturation) }
    var lightness by remember { mutableFloatStateOf(initialColor.lightness) }
    var hexInput by remember { mutableStateOf(initialColor.toHex()) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: HSL, 1: RGB, 2: Presets

    val currentColor = remember(hue, saturation, lightness) {
        HslColor(hue, saturation, lightness)
    }

    val currentRgb = remember(currentColor) {
        currentColor.toRgb()
    }

    val systemPresets = listOf(
        "#E65100" to "Craftsman Amber",
        "#D32F2F" to "Crimson Red",
        "#C2185B" to "Vibrant Rose",
        "#7B1FA2" to "Royal Purple",
        "#512DA8" to "Deep Indigo",
        "#1976D2" to "Electric Cobalt",
        "#0288D1" to "Ocean Cerulean",
        "#0097A7" to "Cyan Teal",
        "#00796B" to "Emerald Green",
        "#388E3C" to "Forest Mint",
        "#689F38" to "Lime Green",
        "#FBC02D" to "Sunburst Gold",
        "#FFA000" to "Warm Marigold",
        "#F57C00" to "Tangerine",
        "#5D4037" to "Anvil Bronze",
        "#455A64" to "Slate Steel"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Set Manual Base Color",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Fine-tune HSL, RGB, Hex, or system swatches",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Live Comparison Swatch (Initial vs Picked)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Original
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Current", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(initialColor.toComposeColor())
                                    .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(initialColor.toHex(), fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )

                        // New Picked Color
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("New Base", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(currentColor.toComposeColor())
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(currentColor.toHex(), fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Hex Code Quick Input Field
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        hexInput = input
                        val clean = input.trim()
                        if (clean.startsWith("#") && (clean.length == 7 || clean.length == 4)) {
                            try {
                                val hsl = HslColor.fromHex(clean)
                                hue = hsl.hue
                                saturation = hsl.saturation
                                lightness = hsl.lightness
                            } catch (_: Exception) {}
                        } else if (!clean.startsWith("#") && (clean.length == 6 || clean.length == 3)) {
                            try {
                                val hsl = HslColor.fromHex("#$clean")
                                hue = hsl.hue
                                saturation = hsl.saturation
                                lightness = hsl.lightness
                            } catch (_: Exception) {}
                        }
                    },
                    label = { Text("Hex Color Code") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_picker_hex_input")
                )

                // Tabs: HSL / RGB / Presets
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("HSL", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("RGB", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Presets", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                when (selectedTab) {
                    0 -> {
                        // HSL Sliders
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Hue
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Hue", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text("${hue.toInt()}°", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = hue,
                                    onValueChange = {
                                        hue = it
                                        hexInput = HslColor(it, saturation, lightness).toHex()
                                    },
                                    valueRange = 0f..360f,
                                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                                )
                            }

                            // Saturation
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Saturation", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text("${(saturation * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = saturation,
                                    onValueChange = {
                                        saturation = it
                                        hexInput = HslColor(hue, it, lightness).toHex()
                                    },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                                )
                            }

                            // Lightness
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Lightness", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text("${(lightness * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = lightness,
                                    onValueChange = {
                                        lightness = it
                                        hexInput = HslColor(hue, saturation, it).toHex()
                                    },
                                    valueRange = 0.05f..0.95f,
                                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }

                    1 -> {
                        // RGB Sliders
                        var r by remember(currentRgb) { mutableFloatStateOf(currentRgb.r.toFloat()) }
                        var g by remember(currentRgb) { mutableFloatStateOf(currentRgb.g.toFloat()) }
                        var b by remember(currentRgb) { mutableFloatStateOf(currentRgb.b.toFloat()) }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Red
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Red (R)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                                    Text("${r.toInt()}", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = r,
                                    onValueChange = {
                                        r = it
                                        val hsl = RgbColor(it.toInt(), g.toInt(), b.toInt()).toHsl()
                                        hue = hsl.hue
                                        saturation = hsl.saturation
                                        lightness = hsl.lightness
                                        hexInput = hsl.toHex()
                                    },
                                    valueRange = 0f..255f
                                )
                            }

                            // Green
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Green (G)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                                    Text("${g.toInt()}", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = g,
                                    onValueChange = {
                                        g = it
                                        val hsl = RgbColor(r.toInt(), it.toInt(), b.toInt()).toHsl()
                                        hue = hsl.hue
                                        saturation = hsl.saturation
                                        lightness = hsl.lightness
                                        hexInput = hsl.toHex()
                                    },
                                    valueRange = 0f..255f
                                )
                            }

                            // Blue
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Blue (B)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                                    Text("${b.toInt()}", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = b,
                                    onValueChange = {
                                        b = it
                                        val hsl = RgbColor(r.toInt(), g.toInt(), it.toInt()).toHsl()
                                        hue = hsl.hue
                                        saturation = hsl.saturation
                                        lightness = hsl.lightness
                                        hexInput = hsl.toHex()
                                    },
                                    valueRange = 0f..255f
                                )
                            }
                        }
                    }

                    2 -> {
                        // Presets Grid
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("System & Domain Master Presets", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            val chunked = systemPresets.chunked(4)
                            chunked.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { (hex, name) ->
                                        val presetHsl = HslColor.fromHex(hex)
                                        val isSelected = currentColor.toHex().equals(hex, ignoreCase = true)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(38.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(presetHsl.toComposeColor())
                                                .border(
                                                    width = if (isSelected) 2.5.dp else 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    hue = presetHsl.hue
                                                    saturation = presetHsl.saturation
                                                    lightness = presetHsl.lightness
                                                    hexInput = hex
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected(currentColor)
                    onDismiss()
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("apply_manual_color_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Set Base Color", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
