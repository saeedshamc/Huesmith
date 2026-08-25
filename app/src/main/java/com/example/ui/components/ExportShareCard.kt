package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.color.AseExporter
import com.example.color.GeneratedPaletteSet
import com.example.color.HslColor
import com.example.color.PaletteGenerator
import com.example.color.WcagUtils
import java.util.Locale

enum class ExportFormat(val title: String) {
    ASE("Adobe ASE"),
    TAILWIND("Tailwind CSS"),
    CSS("CSS Variables"),
    JSON("JSON"),
    COMPOSE("Jetpack Compose"),
    FLUTTER("Flutter Dart")
}

data class PairwiseContrastResult(
    val color1: HslColor,
    val color2: HslColor,
    val name1: String,
    val name2: String,
    val ratio: Float,
    val passesAa: Boolean,
    val passesAaLarge: Boolean
)

@Composable
fun ExportShareCard(paletteSet: GeneratedPaletteSet) {
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val activePalette = paletteSet.domainAdjustedComplementary

    var previewFormat by remember { mutableStateOf<ExportFormat?>(null) }
    var showStatsDetails by remember { mutableStateOf(false) }

    // Calculate accessibility statistics
    val stats = remember(activePalette) {
        val pairs = mutableListOf<PairwiseContrastResult>()
        val colorNames = listOf("Base", "Domain Comp", "Accent", "Surface/Neutral", "Tonal High")
        var sumRatio = 0f
        var totalPairs = 0

        for (i in activePalette.indices) {
            for (j in (i + 1) until activePalette.size) {
                val c1 = activePalette[i]
                val c2 = activePalette[j]
                val ratio = WcagUtils.contrastRatio(c1.toRgb(), c2.toRgb())
                sumRatio += ratio
                totalPairs++
                val passesAa = ratio >= 4.5f
                val passesAaLarge = ratio >= 3.0f
                pairs.add(
                    PairwiseContrastResult(
                        color1 = c1,
                        color2 = c2,
                        name1 = colorNames.getOrElse(i) { "Color #${i + 1}" },
                        name2 = colorNames.getOrElse(j) { "Color #${j + 1}" },
                        ratio = ratio,
                        passesAa = passesAa,
                        passesAaLarge = passesAaLarge
                    )
                )
            }
        }

        val avgRatio = if (totalPairs > 0) sumRatio / totalPairs else 0f
        val failingPairs = pairs.filter { !it.passesAa }
        val passingPairsCount = pairs.count { it.passesAa }

        object {
            val averageRatio = avgRatio
            val totalPairsCount = totalPairs
            val passingCount = passingPairsCount
            val failingList = failingPairs
            val allPairs = pairs
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("export_share_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Offline Export & Code Generation",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Export palette formatted for Adobe Swatch Exchange (.ase), Tailwind CSS, CSS variables, JSON, Compose, or Flutter with zero network overhead.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Summary Stats Panel (Average Contrast Ratio & WCAG AA Evaluation)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (stats.failingList.isEmpty()) Color(0xFF2E7D32).copy(alpha = 0.5f) else Color(0xFFD32F2F).copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_summary_stats_panel")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Palette Accessibility Summary",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        TextButton(
                            onClick = { showStatsDetails = !showStatsDetails },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = if (showStatsDetails) "Hide Breakdown" else "View Breakdown",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Metric 1: Average Contrast Ratio
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Avg Contrast", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", stats.averageRatio)}:1",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (stats.averageRatio >= 4.5f) Color(0xFF2E7D32) else Color(0xFFE65100)
                                )
                            }
                        }

                        // Metric 2: WCAG AA Status
                        Surface(
                            color = if (stats.failingList.isEmpty()) Color(0xFF2E7D32).copy(alpha = 0.12f)
                            else Color(0xFFD32F2F).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("WCAG AA Normal", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (stats.failingList.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (stats.failingList.isEmpty()) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (stats.failingList.isEmpty()) "100% Pass" else "${stats.failingList.size} Pairs Fail",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (stats.failingList.isEmpty()) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                    )
                                }
                            }
                        }
                    }

                    // Failing Colors Highlight Warning
                    if (stats.failingList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFD32F2F).copy(alpha = 0.08f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${stats.failingList.size} color pair(s) fail WCAG AA (ratio < 4.5:1). Avoid using them directly as text foreground/background.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB71C1C),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    // Detailed Failing / Passing Breakdown List
                    AnimatedVisibility(visible = showStatsDetails) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Pairwise Contrast Breakdown:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            stats.allPairs.forEach { pair ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(pair.color1.toComposeColor())
                                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(pair.color1.toHex(), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                        Text(" / ", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(pair.color2.toComposeColor())
                                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(pair.color2.toHex(), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", pair.ratio)}:1",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (pair.passesAa) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = if (pair.passesAa) Color(0xFF2E7D32).copy(alpha = 0.15f)
                                            else if (pair.passesAaLarge) Color(0xFFF57C00).copy(alpha = 0.15f)
                                            else Color(0xFFD32F2F).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (pair.passesAa) "AA PASS" else if (pair.passesAaLarge) "AA LARGE" else "FAIL",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (pair.passesAa) Color(0xFF2E7D32) else if (pair.passesAaLarge) Color(0xFFE65100) else Color(0xFFD32F2F),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Adobe ASE Export Button
            Button(
                onClick = {
                    val paletteTitle = "Huesmith ${paletteSet.domain.title} ${paletteSet.selectedHarmony.title}"
                    AseExporter.exportAndShareAse(context, coroutineScope, activePalette, paletteTitle)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_ase_button")
            ) {
                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Adobe Swatch Exchange (.ASE)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Code Export Formats Row 1: Tailwind CSS & CSS Variables
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { previewFormat = ExportFormat.TAILWIND },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_tailwind_button")
                ) {
                    Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tailwind CSS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { previewFormat = ExportFormat.CSS },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_css_button")
                ) {
                    Text("CSS Variables", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Code Export Formats Row 2: JSON, Compose, Flutter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { previewFormat = ExportFormat.JSON },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { previewFormat = ExportFormat.COMPOSE },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Compose", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { previewFormat = ExportFormat.FLUTTER },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Flutter", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Code Preview, Visual Preview & Share Dialog
    previewFormat?.let { format ->
        val codeContent = when (format) {
            ExportFormat.ASE -> ""
            ExportFormat.TAILWIND -> PaletteGenerator.exportTailwindConfig(activePalette, paletteSet.domain.title.lowercase().replace("[^a-z]".toRegex(), ""))
            ExportFormat.CSS -> PaletteGenerator.exportCssVariables(activePalette)
            ExportFormat.JSON -> PaletteGenerator.exportJsonCode(activePalette, "${paletteSet.domain.title} Palette")
            ExportFormat.COMPOSE -> PaletteGenerator.exportComposeCode(activePalette)
            ExportFormat.FLUTTER -> PaletteGenerator.exportFlutterCode(activePalette)
        }

        AlertDialog(
            onDismissRequest = { previewFormat = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Export ${format.title}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { previewFormat = null }, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Visual Preview Component: Common UI patterns in application context
                    Text(
                        text = "Live UI Pattern Context Preview:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    ExportUiVisualPreview(colors = activePalette)

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Formatted Code (${format.title}):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 220.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp)
                        ) {
                            Text(
                                text = codeContent,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            shareText(context, codeContent, "Huesmith ${format.title}")
                            previewFormat = null
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Huesmith Export", codeContent)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied ${format.title} code to clipboard!", Toast.LENGTH_SHORT).show()
                            previewFormat = null
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Code")
                    }
                }
            },
            dismissButton = null
        )
    }
}

/**
 * Visual Preview Component for Export Dialog:
 * Renders the selected palette using common UI patterns (buttons, surface cards, badges, text)
 * to demonstrate real-world application context.
 */
@Composable
fun ExportUiVisualPreview(colors: List<HslColor>) {
    val primaryColor = colors.getOrNull(0)?.toComposeColor() ?: MaterialTheme.colorScheme.primary
    val secondaryColor = colors.getOrNull(1)?.toComposeColor() ?: MaterialTheme.colorScheme.secondary
    val accentColor = colors.getOrNull(2)?.toComposeColor() ?: MaterialTheme.colorScheme.tertiary
    val surfaceColor = colors.getOrNull(3)?.toComposeColor() ?: MaterialTheme.colorScheme.surfaceVariant
    val textColor = colors.getOrNull(4)?.toComposeColor() ?: MaterialTheme.colorScheme.onSurface

    // Determine high-contrast text color on primary
    val primaryHsl = colors.getOrNull(0) ?: HslColor(0f, 0f, 0.5f)
    val onPrimaryColor = if (primaryHsl.lightness > 0.55f) Color(0xFF1E1E1E) else Color.White

    Surface(
        color = surfaceColor.copy(alpha = 0.35f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("export_visual_ui_preview")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Pattern
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Application Pattern Preview",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Rendering palette tokens in UI context",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge Tag
                Surface(
                    color = accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor)
                ) {
                    Text(
                        text = "Active Palette",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Card Container Pattern
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Crafted Domain Experience",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Theme tokens mapped across surface, primary actions, and tonal accents.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Buttons Pattern Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Primary Filled Button
                        Surface(
                            color = primaryColor,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Primary CTA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onPrimaryColor
                                )
                            }
                        }

                        // Secondary Outlined Button
                        Surface(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, secondaryColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Secondary Action",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = secondaryColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Palette Swatch Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                colors.forEachIndexed { idx, col ->
                    val roleLabel = when (idx) {
                        0 -> "Base"
                        1 -> "Comp"
                        2 -> "Accent"
                        3 -> "Surface"
                        else -> "Tonal"
                    }
                    Surface(
                        color = col.toComposeColor(),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(18.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = roleLabel,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (col.lightness > 0.5f) Color.Black else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun shareText(context: Context, text: String, title: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "$title\n\n$text\n\nForged with Huesmith — 100% Offline Domain Color Craftsman")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, title)
    context.startActivity(shareIntent)
}
