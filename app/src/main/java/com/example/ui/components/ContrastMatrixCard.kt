package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.color.HslColor
import com.example.color.WcagUtils
import java.util.Locale

data class SelectedContrastPair(
    val bg: HslColor,
    val bgLabel: String,
    val fg: HslColor,
    val fgLabel: String,
    val ratio: Float,
    val level: WcagUtils.ContrastLevel
)

@Composable
fun ContrastMatrixCard(
    colors: List<HslColor>,
    paletteTitle: String = "Active Palette",
    onColorClick: (HslColor) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(true) }
    var selectedPair by remember { mutableStateOf<SelectedContrastPair?>(null) }

    val matrixColors = colors.take(5)
    val colorLabels = listOf("Base", "Comp", "Accent", "Neutral", "Tonal")

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("contrast_matrix_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "WCAG 2.1 Contrast Matrix",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Pairwise contrast ratios of all colors in $paletteTitle",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.Info else Icons.Default.Info,
                        contentDescription = "Toggle Information",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rating Legends Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendChip(label = "AAA (7.0+:1)", color = Color(0xFF1B5E20), bg = Color(0xFFC8E6C9))
                LegendChip(label = "AA (4.5+:1)", color = Color(0xFF2E7D32), bg = Color(0xFFDCEDC8))
                LegendChip(label = "AA-L (3.0+:1)", color = Color(0xFFF57F17), bg = Color(0xFFFFF9C4))
                LegendChip(label = "Fail (<3.0:1)", color = Color(0xFFC62828), bg = Color(0xFFFFCDD2))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Matrix Table Grid View
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                Column {
                    // Header Row: Top swatches (Foreground labels)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Empty top-left cell
                        Box(
                            modifier = Modifier
                                .size(width = 54.dp, height = 48.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "BG\\FG",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        matrixColors.forEachIndexed { fgIdx, fgColor ->
                            Column(
                                modifier = Modifier
                                    .size(width = 62.dp, height = 48.dp)
                                    .padding(2.dp)
                                    .clickable { onColorClick(fgColor) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(fgColor.toComposeColor())
                                        .border(1.dp, Color.White, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = colorLabels.getOrElse(fgIdx) { "C${fgIdx + 1}" },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Matrix Data Rows: Each background row vs all foreground columns
                    matrixColors.forEachIndexed { bgIdx, bgColor ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Left header cell (Background label)
                            Row(
                                modifier = Modifier
                                    .size(width = 54.dp, height = 46.dp)
                                    .padding(2.dp)
                                    .clickable { onColorClick(bgColor) },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(bgColor.toComposeColor())
                                        .border(1.dp, Color.White, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = colorLabels.getOrElse(bgIdx) { "C${bgIdx + 1}" },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Columns for this row
                            matrixColors.forEachIndexed { fgIdx, fgColor ->
                                val ratio = WcagUtils.contrastRatio(fgColor.toRgb(), bgColor.toRgb())
                                val level = WcagUtils.getContrastLevel(ratio)
                                val isSelf = (bgIdx == fgIdx)

                                val cellBgColor = when {
                                    isSelf -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    level == WcagUtils.ContrastLevel.AAA -> Color(0xFFC8E6C9)
                                    level == WcagUtils.ContrastLevel.AA -> Color(0xFFDCEDC8)
                                    level == WcagUtils.ContrastLevel.AA_LARGE -> Color(0xFFFFF9C4)
                                    else -> Color(0xFFFFCDD2)
                                }

                                val textColor = when {
                                    isSelf -> MaterialTheme.colorScheme.outline
                                    level == WcagUtils.ContrastLevel.AAA -> Color(0xFF1B5E20)
                                    level == WcagUtils.ContrastLevel.AA -> Color(0xFF2E7D32)
                                    level == WcagUtils.ContrastLevel.AA_LARGE -> Color(0xFFE65100)
                                    else -> Color(0xFFB71C1C)
                                }

                                val badgeText = when {
                                    isSelf -> "—"
                                    level == WcagUtils.ContrastLevel.AAA -> "AAA"
                                    level == WcagUtils.ContrastLevel.AA -> "AA"
                                    level == WcagUtils.ContrastLevel.AA_LARGE -> "AA-L"
                                    else -> "FAIL"
                                }

                                Box(
                                    modifier = Modifier
                                        .size(width = 62.dp, height = 46.dp)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(cellBgColor)
                                        .clickable(enabled = !isSelf) {
                                            selectedPair = SelectedContrastPair(
                                                bg = bgColor,
                                                bgLabel = colorLabels.getOrElse(bgIdx) { "C${bgIdx + 1}" },
                                                fg = fgColor,
                                                fgLabel = colorLabels.getOrElse(fgIdx) { "C${fgIdx + 1}" },
                                                ratio = ratio,
                                                level = level
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelf) {
                                        Text(
                                            text = "—",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = String.format(Locale.US, "%.1f:1", ratio),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = textColor
                                            )
                                            Text(
                                                text = badgeText,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "💡 Tap any cell in the matrix to inspect pairing preview and copy CSS snippet.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Detail Dialog when a specific matrix pair is tapped
    selectedPair?.let { pair ->
        AlertDialog(
            onDismissRequest = { selectedPair = null },
            title = {
                Text(
                    text = "${pair.fgLabel} on ${pair.bgLabel}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Live UI Preview of the pair
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(pair.bg.toComposeColor())
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Sample Text Headline",
                                color = pair.fg.toComposeColor(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Body copy rendered using ${pair.fg.toHex()} on ${pair.bg.toHex()}.",
                                color = pair.fg.toComposeColor(),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pair info details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Background", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${pair.bgLabel}: ${pair.bg.toHex()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Foreground", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${pair.fgLabel}: ${pair.fg.toHex()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "WCAG 2.1 Score: ${String.format(Locale.US, "%.2f:1", pair.ratio)} • ${pair.level.label}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (pair.ratio >= 4.5f) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            Text(
                                text = when {
                                    pair.ratio >= 7.0f -> "Passes AAA for normal text, body copy, and UI components."
                                    pair.ratio >= 4.5f -> "Passes AA for standard body text. AAA for large headings (18pt+)."
                                    pair.ratio >= 3.0f -> "Passes AA for large headers only (18pt+ bold). Fails for body text."
                                    else -> "Fails WCAG accessibility guidelines for text. Use for decorative art only."
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val css = "/* Foreground on Background combo */\ncolor: ${pair.fg.toHex()};\nbackground-color: ${pair.bg.toHex()};\n/* Contrast Ratio: ${String.format(Locale.US, "%.2f:1", pair.ratio)} (${pair.level.label}) */"
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Huesmith Pair CSS", css))
                        Toast.makeText(context, "Copied Pair CSS to clipboard!", Toast.LENGTH_SHORT).show()
                        selectedPair = null
                    }
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Pair CSS")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPair = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun LegendChip(label: String, color: Color, bg: Color) {
    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
