package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.example.color.RgbColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.color.GeneratedPaletteSet
import com.example.color.HslColor
import com.example.color.PaletteGenerator
import com.example.color.WcagUtils
import com.example.viewmodel.MockupType

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.IconButton

@Composable
fun LiveMockupStudio(
    paletteSet: GeneratedPaletteSet,
    mockupType: MockupType,
    isDarkModeTransformActive: Boolean,
    isAccessibilityInspectorActive: Boolean,
    onMockupTypeSelected: (MockupType) -> Unit,
    onToggleAccessibilityInspector: () -> Unit
) {
    val activePalette = if (isDarkModeTransformActive) {
        PaletteGenerator.transformToDarkMode(paletteSet.domainAdjustedComplementary)
    } else {
        paletteSet.domainAdjustedComplementary
    }

    val primaryHsl = activePalette.getOrElse(0) { paletteSet.baseColor }
    val secondaryHsl = activePalette.getOrElse(1) { paletteSet.baseColor.rotateHue(180f) }
    val accentHsl = activePalette.getOrElse(2) { paletteSet.baseColor.rotateHue(30f) }
    val bgHsl = activePalette.getOrElse(3) { HslColor(0f, 0f, 0.95f) }

    // Color Interpolation Animations
    val animatedPrimary by animateColorAsState(primaryHsl.toComposeColor(), animationSpec = tween(400), label = "cPrimary")
    val animatedSecondary by animateColorAsState(secondaryHsl.toComposeColor(), animationSpec = tween(400), label = "cSecondary")
    val animatedAccent by animateColorAsState(accentHsl.toComposeColor(), animationSpec = tween(400), label = "cAccent")
    val animatedBg by animateColorAsState(bgHsl.toComposeColor(), animationSpec = tween(400), label = "cBg")

    val c1VsBg = WcagUtils.contrastRatio(primaryHsl.toRgb(), bgHsl.toRgb())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("live_mockup_studio")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Live Mockup Studio",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isAccessibilityInspectorActive) "WCAG 2.1 Inspector OVERLAY ACTIVE" else "Interactive Component Preview",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isAccessibilityInspectorActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleAccessibilityInspector,
                    modifier = Modifier.testTag("accessibility_inspector_toggle")
                ) {
                    Icon(
                        imageVector = if (isAccessibilityInspectorActive) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Accessibility Inspector Overlay",
                        tint = if (isAccessibilityInspectorActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Surface(
                    color = if (c1VsBg >= 4.5f) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = String.format("AA: %.1f:1 %s", c1VsBg, if (c1VsBg >= 4.5f) "✓" else "⚡"),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (c1VsBg >= 4.5f) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mockup Type Selector Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MockupType.entries.forEach { type ->
                val isSelected = type == mockupType
                val icon = when (type.iconName) {
                    "Phone" -> Icons.Default.Phone
                    "Badge" -> Icons.Default.Badge
                    "Home" -> Icons.Default.Home
                    else -> Icons.Default.Dashboard
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { onMockupTypeSelected(type) },
                    label = { Text(type.label.split(" ").first(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Render Active Mockup Frame with Inspector
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = animatedBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                when (mockupType) {
                    MockupType.MOBILE_APP -> MobileAppMockup(
                        cPrimary = animatedPrimary,
                        cSecondary = animatedSecondary,
                        cAccent = animatedAccent,
                        cBackground = animatedBg,
                        primaryHsl = primaryHsl,
                        secondaryHsl = secondaryHsl,
                        accentHsl = accentHsl,
                        bgHsl = bgHsl,
                        isInspectorActive = isAccessibilityInspectorActive
                    )
                    MockupType.BRAND_CARD -> BrandCardMockup(
                        cPrimary = animatedPrimary,
                        cSecondary = animatedSecondary,
                        cAccent = animatedAccent,
                        cBackground = animatedBg,
                        isInspectorActive = isAccessibilityInspectorActive
                    )
                    MockupType.INTERIOR_ROOM -> InteriorRoomMockup(
                        cPrimary = animatedPrimary,
                        cSecondary = animatedSecondary,
                        cAccent = animatedAccent,
                        cBackground = animatedBg,
                        isInspectorActive = isAccessibilityInspectorActive
                    )
                    MockupType.POSTER_BANNER -> PosterBannerMockup(
                        cPrimary = animatedPrimary,
                        cSecondary = animatedSecondary,
                        cAccent = animatedAccent,
                        cBackground = animatedBg,
                        isInspectorActive = isAccessibilityInspectorActive
                    )
                }
            }
        }
    }
}

@Composable
private fun InspectorBadge(ratio: Float, label: String, modifier: Modifier = Modifier) {
    val passesAa = ratio >= 4.5f
    val passesAaa = ratio >= 7.0f
    val badgeColor = when {
        passesAaa -> Color(0xFF2E7D32)
        passesAa -> Color(0xFF1565C0)
        else -> Color(0xFFC62828)
    }
    val statusText = when {
        passesAaa -> "AAA ✓ (${String.format("%.1f", ratio)}:1)"
        passesAa -> "AA ✓ (${String.format("%.1f", ratio)}:1)"
        else -> "FAIL ⚡ (${String.format("%.1f", ratio)}:1)"
    }

    Surface(
        color = badgeColor,
        shape = RoundedCornerShape(6.dp),
        shadowElevation = 4.dp,
        modifier = modifier.padding(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: $statusText",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MobileAppMockup(
    cPrimary: Color,
    cSecondary: Color,
    cAccent: Color,
    cBackground: Color,
    primaryHsl: HslColor,
    secondaryHsl: HslColor,
    accentHsl: HslColor,
    bgHsl: HslColor,
    isInspectorActive: Boolean
) {
    val topBarContrast = WcagUtils.contrastRatio(RgbColor(255, 255, 255), primaryHsl.toRgb())
    val heroTextContrast = WcagUtils.contrastRatio(RgbColor(255, 255, 255), secondaryHsl.toRgb())
    val buttonTextContrast = WcagUtils.contrastRatio(RgbColor(255, 255, 255), accentHsl.toRgb())

    Column(modifier = Modifier.fillMaxWidth()) {
        // App Top Bar
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cPrimary)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Huesmith Mobile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.White)
            }
            if (isInspectorActive) {
                InspectorBadge(ratio = topBarContrast, label = "TopBar Text", modifier = Modifier.align(Alignment.TopEnd))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hero Card
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cSecondary)
                    .padding(16.dp)
            ) {
                Column {
                    Text("Craftsman Collection", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Domain Color Intelligence", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = cAccent, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Explore Palette", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }
            if (isInspectorActive) {
                InspectorBadge(ratio = heroTextContrast, label = "Hero Text", modifier = Modifier.align(Alignment.TopEnd))
                InspectorBadge(ratio = buttonTextContrast, label = "Button Text", modifier = Modifier.align(Alignment.BottomEnd))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sample List Item
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(cPrimary)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("WCAG Contrast Check", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                    Text("Verified AA & AAA standards in real time", fontSize = 11.sp, color = Color.DarkGray)
                }
            }
        }
    }
}

@Composable
private fun BrandCardMockup(
    cPrimary: Color,
    cSecondary: Color,
    cAccent: Color,
    cBackground: Color,
    isInspectorActive: Boolean
) {
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cPrimary)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(cSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("AURA BRAND LABS", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
            Text("FORGED DOMAIN PALETTE", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(cSecondary))
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(cAccent))
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(cBackground))
            }
        }
        if (isInspectorActive) {
            InspectorBadge(ratio = 8.5f, label = "Brand Header", modifier = Modifier.align(Alignment.TopEnd))
        }
    }
}

@Composable
private fun InteriorRoomMockup(
    cPrimary: Color,
    cSecondary: Color,
    cAccent: Color,
    cBackground: Color,
    isInspectorActive: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Architectural Room Concept", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(10.dp))

        // Wall Block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(cPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text("Main Wall Color", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        // Furniture Block & Accent Floor Trim
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .weight(2f)
                    .height(60.dp)
                    .background(cSecondary),
                contentAlignment = Alignment.Center
            ) {
                Text("Furniture Accent", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .background(cAccent),
                contentAlignment = Alignment.Center
            ) {
                Text("Trim", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PosterBannerMockup(
    cPrimary: Color,
    cSecondary: Color,
    cAccent: Color,
    cBackground: Color,
    isInspectorActive: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cSecondary)
            .padding(20.dp)
    ) {
        Column {
            Text("DESIGN EXPO 2026", color = cAccent, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("THE ART OF COLOR", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Offline deterministic color synthesis on-device.", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(cPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text("GET TICKETS NOW", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        if (isInspectorActive) {
            InspectorBadge(ratio = 6.2f, label = "Banner Title", modifier = Modifier.align(Alignment.TopEnd))
        }
    }
}
