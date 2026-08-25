package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.color.GeneratedPaletteSet
import com.example.color.HslColor
import com.example.color.PaletteGenerator
import com.example.color.RgbColor
import com.example.color.WcagUtils

import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share

@Composable
fun CompanionPalettesSection(
    paletteSet: GeneratedPaletteSet,
    isDarkModeTransformActive: Boolean,
    lockedIndices: Set<Int>,
    onToggleLock: (Int) -> Unit,
    onShuffle: () -> Unit,
    onInspectColor: (HslColor) -> Unit,
    onSharePng: (title: String, colors: List<HslColor>) -> Unit,
    onSavePalette: (title: String, paletteType: String, colorsHex: List<String>, notes: String) -> Unit
) {
    val context = LocalContext.current
    var saveDialogPalette by remember { mutableStateOf<Pair<String, List<HslColor>>?>(null) }

    val activeExact = if (isDarkModeTransformActive) PaletteGenerator.transformToDarkMode(paletteSet.exactComplementary) else paletteSet.exactComplementary
    val activeDomain = if (isDarkModeTransformActive) PaletteGenerator.transformToDarkMode(paletteSet.domainAdjustedComplementary) else paletteSet.domainAdjustedComplementary
    val activeHarmony = if (isDarkModeTransformActive) PaletteGenerator.transformToDarkMode(paletteSet.harmonyPalette) else paletteSet.harmonyPalette
    val activeAnalogous = if (isDarkModeTransformActive) PaletteGenerator.transformToDarkMode(paletteSet.analogousTriad) else paletteSet.analogousTriad
    val activeNeutral = if (isDarkModeTransformActive) PaletteGenerator.transformToDarkMode(paletteSet.neutralCompanion) else paletteSet.neutralCompanion
    val activeTonal = if (isDarkModeTransformActive) PaletteGenerator.transformToDarkMode(paletteSet.tonalVariants) else paletteSet.tonalVariants

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("companion_palettes_section")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Derived Companion Palettes",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap lock icon on swatches to keep favorite colors fixed during shuffle.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onShuffle,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("shuffle_colors_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Shuffle Unlocked Colors", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Shuffle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Palette 0: Active Harmony Algorithm Preset
        PaletteCard(
            title = "${paletteSet.selectedHarmony.title} Preset",
            badgeText = "Harmony Preset",
            noteText = paletteSet.selectedHarmony.description,
            colors = activeHarmony,
            lockedIndices = lockedIndices,
            onToggleLock = onToggleLock,
            onInspectColor = onInspectColor,
            onSaveClick = { saveDialogPalette = "${paletteSet.selectedHarmony.title} Preset" to activeHarmony },
            onCopyClick = {
                val code = PaletteGenerator.exportCssVariables(activeHarmony, paletteSet.selectedHarmony.name.lowercase())
                copyToClipboard(context, code)
            },
            onSharePngClick = { onSharePng("${paletteSet.selectedHarmony.title} Preset", activeHarmony) },
            isFeatured = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Palette 1: Domain-Adjusted Complementary (Featured First)
        PaletteCard(
            title = "Domain-Adjusted Complementary",
            badgeText = "${paletteSet.domain.title} Rule",
            noteText = paletteSet.domainTransformationNote,
            colors = activeDomain,
            lockedIndices = lockedIndices,
            onToggleLock = onToggleLock,
            onInspectColor = onInspectColor,
            onSaveClick = { saveDialogPalette = "Domain-Adjusted ${paletteSet.domain.title}" to activeDomain },
            onCopyClick = {
                val code = PaletteGenerator.exportCssVariables(activeDomain, "domain")
                copyToClipboard(context, code)
            },
            onSharePngClick = { onSharePng("Domain-Adjusted ${paletteSet.domain.title}", activeDomain) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Palette 2: Exact Complementary
        PaletteCard(
            title = "Exact Complementary (180°)",
            badgeText = "Pure Hue Math",
            noteText = "Exact opposite hue on the HSL color wheel.",
            colors = activeExact,
            lockedIndices = lockedIndices,
            onToggleLock = onToggleLock,
            onInspectColor = onInspectColor,
            onSaveClick = { saveDialogPalette = "Exact Complementary" to activeExact },
            onCopyClick = {
                val code = PaletteGenerator.exportCssVariables(activeExact, "exact")
                copyToClipboard(context, code)
            },
            onSharePngClick = { onSharePng("Exact Complementary", activeExact) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Palette 3: Analogous Triad
        PaletteCard(
            title = "Analogous Triad (±30°)",
            badgeText = "Harmonic Spectrum",
            noteText = "Adjacent hues creating naturally cohesive visual warmth.",
            colors = activeAnalogous,
            lockedIndices = lockedIndices,
            onToggleLock = onToggleLock,
            onInspectColor = onInspectColor,
            onSaveClick = { saveDialogPalette = "Analogous Triad" to activeAnalogous },
            onCopyClick = {
                val code = PaletteGenerator.exportCssVariables(activeAnalogous, "analogous")
                copyToClipboard(context, code)
            },
            onSharePngClick = { onSharePng("Analogous Triad", activeAnalogous) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Palette 4: Neutral Companion
        PaletteCard(
            title = "Neutral Companion",
            badgeText = "Desaturated Balance",
            noteText = "Achromatic gray/beige anchors tuned to base color lightness.",
            colors = activeNeutral,
            lockedIndices = lockedIndices,
            onToggleLock = onToggleLock,
            onInspectColor = onInspectColor,
            onSaveClick = { saveDialogPalette = "Neutral Companion" to activeNeutral },
            onCopyClick = {
                val code = PaletteGenerator.exportCssVariables(activeNeutral, "neutral")
                copyToClipboard(context, code)
            },
            onSharePngClick = { onSharePng("Neutral Companion", activeNeutral) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Palette 5: Tonal Lightness Variants
        PaletteCard(
            title = "Tonal Lightness Steps",
            badgeText = "Monochromatic Depth",
            noteText = "+12% lighter, base anchor, -15% & -30% darker lightness steps.",
            colors = activeTonal,
            lockedIndices = lockedIndices,
            onToggleLock = onToggleLock,
            onInspectColor = onInspectColor,
            onSaveClick = { saveDialogPalette = "Tonal Steps" to activeTonal },
            onCopyClick = {
                val code = PaletteGenerator.exportCssVariables(activeTonal, "tonal")
                copyToClipboard(context, code)
            },
            onSharePngClick = { onSharePng("Tonal Steps", activeTonal) }
        )
    }

    // Save Palette Dialog
    saveDialogPalette?.let { (type, colors) ->
        var titleInput by remember { mutableStateOf(type) }
        var notesInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { saveDialogPalette = null },
            title = { Text("Save Palette to Collection") },
            text = {
                Column {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Palette Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Notes / Usage (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        colors.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(c.toComposeColor())
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSavePalette(
                            titleInput,
                            type,
                            colors.map { it.toHex() },
                            notesInput
                        )
                        saveDialogPalette = null
                    },
                    modifier = Modifier.testTag("confirm_save_palette_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { saveDialogPalette = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PaletteCard(
    title: String,
    badgeText: String,
    noteText: String,
    colors: List<HslColor>,
    lockedIndices: Set<Int>,
    onToggleLock: (Int) -> Unit,
    onInspectColor: (HslColor) -> Unit,
    onSaveClick: () -> Unit,
    onCopyClick: () -> Unit,
    onSharePngClick: () -> Unit,
    isFeatured: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFeatured) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isFeatured) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = noteText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = if (isFeatured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFeatured) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Swatches Bar with Lock Toggle Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colors.forEachIndexed { idx, hsl ->
                    val rgb = hsl.toRgb()
                    val contrastWhite = WcagUtils.contrastRatio(rgb, RgbColor(255, 255, 255))
                    val isLight = contrastWhite < 4.5f
                    val isLocked = lockedIndices.contains(idx)
                    val contentColor = if (isLight) Color.Black else Color.White

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(hsl.toComposeColor())
                            .border(
                                width = if (isLocked) 2.dp else 1.dp,
                                color = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onInspectColor(hsl) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = { onToggleLock(idx) },
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("lock_color_button_$idx")
                        ) {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = if (isLocked) "Unlock Color" else "Lock Color",
                                tint = contentColor.copy(alpha = if (isLocked) 1.0f else 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = hsl.toHex(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (contrastWhite >= 4.5f) "AA ✓" else "AA ⚡",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onSharePngClick,
                    modifier = Modifier.testTag("share_png_card_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share PNG Image Card",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedButton(
                    onClick = onCopyClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Code", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSaveClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Palette", fontSize = 11.sp)
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Huesmith CSS Code", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied palette code to clipboard!", Toast.LENGTH_SHORT).show()
}
