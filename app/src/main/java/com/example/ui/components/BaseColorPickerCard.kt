package com.example.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.color.ColorHarmonyAlgorithm
import com.example.color.HslColor

val PresetSwatches = listOf(
    "Ember" to HslColor(14f, 1.0f, 0.55f),       // #FF4500
    "Indigo" to HslColor(230f, 0.85f, 0.58f),    // #4169E1
    "Forest" to HslColor(145f, 0.70f, 0.38f),    // #1E8449
    "Teal" to HslColor(180f, 0.90f, 0.38f),      // #008080
    "Sand" to HslColor(38f, 0.65f, 0.70f),       // #E0C097
    "Plum" to HslColor(285f, 0.60f, 0.45f),      // #800080
    "Sunset" to HslColor(32f, 1.0f, 0.50f),      // #FF8C00
    "Slate" to HslColor(210f, 0.25f, 0.45f)      // #546E7A
)

@Composable
fun BaseColorPickerCard(
    baseColor: HslColor,
    selectedHarmony: ColorHarmonyAlgorithm,
    isColorWheelMode: Boolean,
    extractedColors: List<HslColor>,
    isExtractingImage: Boolean,
    onColorChanged: (HslColor) -> Unit,
    onHexChanged: (String) -> Unit,
    onHarmonySelected: (ColorHarmonyAlgorithm) -> Unit,
    onToggleColorWheelMode: () -> Unit,
    onImageSelected: (android.graphics.Bitmap) -> Unit
) {
    val context = LocalContext.current
    var hexInput by remember(baseColor) { mutableStateOf(baseColor.toHex()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    onImageSelected(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("base_color_picker_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Base Anchor & Harmony",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onToggleColorWheelMode,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("toggle_wheel_mode_button")
                    ) {
                        Icon(
                            imageVector = if (isColorWheelMode) Icons.Default.Tune else Icons.Default.ColorLens,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isColorWheelMode) "Sliders" else "Wheel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("image_picker_button")
                    ) {
                        if (isExtractingImage) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Image", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Harmony Algorithm Presets Selector Chips
            Text(
                text = "Harmony Algorithm Preset",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorHarmonyAlgorithm.values().forEach { harmony ->
                    val isSelected = harmony == selectedHarmony
                    FilterChip(
                        selected = isSelected,
                        onClick = { onHarmonySelected(harmony) },
                        label = {
                            Text(
                                text = harmony.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("harmony_chip_${harmony.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isColorWheelMode) {
                // Interactive Visual Color Wheel
                InteractiveColorWheel(
                    baseColor = baseColor,
                    harmony = selectedHarmony,
                    onColorChanged = onColorChanged
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Lightness Precision Slider under Color Wheel
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Lightness (L)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${(baseColor.lightness * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = baseColor.lightness,
                        onValueChange = { l -> onColorChanged(baseColor.copy(lightness = l)) },
                        valueRange = 0.05f..0.95f,
                        colors = SliderDefaults.colors(thumbColor = baseColor.toComposeColor()),
                        modifier = Modifier.testTag("wheel_lightness_slider")
                    )
                }
            } else {
                // Precision Sliders & Hex Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(baseColor.toComposeColor())
                            .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { input ->
                            hexInput = input
                            if (input.length == 7 || input.length == 6) {
                                onHexChanged(input)
                            }
                        },
                        label = { Text("HEX Code") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hex_input_field")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Hue Slider (0 to 360)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Hue (H)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${baseColor.hue.toInt()}°", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    val rainbowBrush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                        )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(rainbowBrush)
                    )
                    Slider(
                        value = baseColor.hue,
                        onValueChange = { h -> onColorChanged(baseColor.copy(hue = h)) },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(thumbColor = baseColor.toComposeColor()),
                        modifier = Modifier.testTag("hue_slider")
                    )
                }

                // 2. Saturation Slider (0 to 100%)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Saturation (S)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${(baseColor.saturation * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = baseColor.saturation,
                        onValueChange = { s -> onColorChanged(baseColor.copy(saturation = s)) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = baseColor.toComposeColor()),
                        modifier = Modifier.testTag("saturation_slider")
                    )
                }

                // 3. Lightness Slider (0 to 100%)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Lightness (L)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${(baseColor.lightness * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = baseColor.lightness,
                        onValueChange = { l -> onColorChanged(baseColor.copy(lightness = l)) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = baseColor.toComposeColor()),
                        modifier = Modifier.testTag("lightness_slider")
                    )
                }
            }

            // Extracted Image Colors Strip (if any)
            if (extractedColors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Extracted Image Dominant Colors (K-Means k=5):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    extractedColors.forEachIndexed { idx, col ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(col.toComposeColor())
                                .border(
                                    if (col == baseColor) 3.dp else 1.dp,
                                    if (col == baseColor) MaterialTheme.colorScheme.primary else Color.White,
                                    CircleShape
                                )
                                .clickable { onColorChanged(col) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Preset Swatches Row
            Text(
                text = "Preset Craftsman Swatches",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PresetSwatches.forEach { (name, hsl) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onColorChanged(hsl) }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(hsl.toComposeColor())
                                .border(
                                    if (baseColor.toHex() == hsl.toHex()) 2.dp else 1.dp,
                                    if (baseColor.toHex() == hsl.toHex()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(name, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

