package com.example.color

import kotlin.math.abs

enum class DomainProfile(
    val title: String,
    val description: String,
    val iconName: String
) {
    BRANDING("Branding", "WCAG AA contrast verified for high-impact logos and identity systems.", "Star"),
    FASHION("Fashion", "Softened complementary tints for elegant textile & apparel coordination.", "Palette"),
    DECORATION("Interior / Decor", "Tonal lightness steps (+10% lighter, base, -30% darker) for spatial harmony.", "Home"),
    NATURE("Nature", "Earthy desaturated hues with organic green-brown shifts.", "Eco"),
    INDUSTRIAL("Industrial / Freestyle", "Pure unadjusted mathematical color complements and triads.", "Build")
}

data class GeneratedPaletteSet(
    val baseColor: HslColor,
    val domain: DomainProfile,
    val selectedHarmony: ColorHarmonyAlgorithm = ColorHarmonyAlgorithm.COMPLEMENTARY,
    val harmonyPalette: List<HslColor>,
    val exactComplementary: List<HslColor>,
    val domainAdjustedComplementary: List<HslColor>,
    val analogousTriad: List<HslColor>,
    val neutralCompanion: List<HslColor>,
    val tonalVariants: List<HslColor>,
    val fiveStepGradient: List<HslColor>,
    val domainTransformationNote: String
)

object PaletteGenerator {

    fun generateHarmonyPalette(
        baseColor: HslColor,
        harmony: ColorHarmonyAlgorithm,
        domain: DomainProfile
    ): List<HslColor> {
        val rawColors = when (harmony) {
            ColorHarmonyAlgorithm.COMPLEMENTARY -> {
                val comp = baseColor.rotateHue(180f)
                val (adjustedComp, _) = applyDomainRuleToComplement(baseColor, comp, domain)
                listOf(
                    baseColor,
                    adjustedComp,
                    baseColor.rotateHue(30f).adjustLightness(0.15f),
                    adjustedComp.adjustLightness(-0.20f)
                )
            }
            ColorHarmonyAlgorithm.ANALOGOUS -> {
                listOf(
                    baseColor,
                    baseColor.rotateHue(-30f),
                    baseColor.rotateHue(30f),
                    calculateNeutralCompanion(baseColor)
                )
            }
            ColorHarmonyAlgorithm.TRIADIC -> {
                listOf(
                    baseColor,
                    baseColor.rotateHue(120f),
                    baseColor.rotateHue(240f),
                    baseColor.rotateHue(120f).adjustLightness(-0.25f)
                )
            }
            ColorHarmonyAlgorithm.SPLIT_COMPLEMENTARY -> {
                listOf(
                    baseColor,
                    baseColor.rotateHue(150f),
                    baseColor.rotateHue(210f),
                    calculateNeutralCompanion(baseColor)
                )
            }
            ColorHarmonyAlgorithm.TETRADIC -> {
                listOf(
                    baseColor,
                    baseColor.rotateHue(90f),
                    baseColor.rotateHue(180f),
                    baseColor.rotateHue(270f)
                )
            }
            ColorHarmonyAlgorithm.MONOCHROMATIC -> {
                listOf(
                    baseColor.adjustLightness(0.18f).adjustSaturation(-0.1f),
                    baseColor,
                    baseColor.adjustLightness(-0.15f).adjustSaturation(0.08f),
                    baseColor.adjustLightness(-0.32f)
                )
            }
        }

        if (domain == DomainProfile.BRANDING) {
            return rawColors.mapIndexed { idx, col ->
                if (idx == 0) col
                else WcagUtils.ensureAaContrast(fg = col, bg = baseColor, minRatio = 4.5f)
            }
        }
        return rawColors
    }

    /**
     * Core function to generate all companion palettes + gradient + tonal variants
     * using domain-aware deterministic color math and active harmony algorithm.
     */
    fun generatePalettes(
        baseColor: HslColor,
        domain: DomainProfile,
        harmony: ColorHarmonyAlgorithm = ColorHarmonyAlgorithm.COMPLEMENTARY
    ): GeneratedPaletteSet {
        val harmonyColors = generateHarmonyPalette(baseColor, harmony, domain)

        // 1. Exact Complementary
        val exactComplement = baseColor.rotateHue(180f)
        val exactPalette = listOf(
            baseColor,
            exactComplement,
            baseColor.rotateHue(30f).adjustLightness(0.15f),
            exactComplement.adjustLightness(-0.25f)
        )

        // 2. Domain-Adjusted Complementary
        val (domainComplement, note) = applyDomainRuleToComplement(baseColor, exactComplement, domain)
        var domainPalette = listOf(
            baseColor,
            domainComplement,
            baseColor.adjustSaturation(-0.1f).adjustLightness(0.2f),
            domainComplement.adjustLightness(-0.25f)
        )

        // If Branding domain, enforce WCAG contrast check across pairs
        if (domain == DomainProfile.BRANDING) {
            domainPalette = domainPalette.mapIndexed { idx, col ->
                if (idx == 0) col
                else WcagUtils.ensureAaContrast(fg = col, bg = baseColor, minRatio = 4.5f)
            }
        }

        // 3. Analogous Triad (±30° from base hue)
        val analogousMinus = baseColor.rotateHue(-30f)
        val analogousPlus = baseColor.rotateHue(30f)
        val analogousNeutral = calculateNeutralCompanion(baseColor)
        val analogousPalette = listOf(
            baseColor,
            analogousMinus,
            analogousPlus,
            analogousNeutral
        )

        // 4. Neutral Companion
        val neutralBase = calculateNeutralCompanion(baseColor)
        val lightNeutral = neutralBase.adjustLightness(0.2f)
        val darkNeutral = neutralBase.adjustLightness(-0.3f)
        val neutralPalette = listOf(
            baseColor,
            neutralBase,
            lightNeutral,
            darkNeutral
        )

        // 5. Tonal Variants (approx. +10% lighter, base, -30% darker in lightness)
        val tonalPalette = listOf(
            baseColor.adjustLightness(0.12f),
            baseColor,
            baseColor.adjustLightness(-0.15f),
            baseColor.adjustLightness(-0.30f)
        )

        // 6. 5-step Gradient between base and domain complement
        val gradientSteps = generate5StepGradient(baseColor, domainComplement)

        return GeneratedPaletteSet(
            baseColor = baseColor,
            domain = domain,
            selectedHarmony = harmony,
            harmonyPalette = harmonyColors,
            exactComplementary = exactPalette,
            domainAdjustedComplementary = domainPalette,
            analogousTriad = analogousPalette,
            neutralCompanion = neutralPalette,
            tonalVariants = tonalPalette,
            fiveStepGradient = gradientSteps,
            domainTransformationNote = note
        )
    }

    private fun applyDomainRuleToComplement(
        base: HslColor,
        exactComp: HslColor,
        domain: DomainProfile
    ): Pair<HslColor, String> {
        return when (domain) {
            DomainProfile.FASHION -> {
                // Soften harshness: Increase lightness slightly (+10%), reduce saturation (-15%)
                val softened = exactComp
                    .adjustLightness(0.10f)
                    .adjustSaturation(-0.15f)
                Pair(softened, "Fashion rule applied: Lightened lightness +10% & softened saturation -15% to eliminate visual glare.")
            }
            DomainProfile.BRANDING -> {
                // Run WCAG contrast check against base color background
                val verified = WcagUtils.ensureAaContrast(exactComp, base, minRatio = 4.5f)
                val ratio = WcagUtils.contrastRatio(verified.toRgb(), base.toRgb())
                Pair(verified, String.format("Branding rule applied: WCAG AA verified (Ratio %.2f:1 against base).", ratio))
            }
            DomainProfile.DECORATION -> {
                // Tonal variant emphasis: Shift hue slightly closer (+15°) and desaturate (-20%) for architectural walls
                val interiorAccent = base.adjustLightness(-0.25f).adjustSaturation(-0.15f)
                Pair(interiorAccent, "Interior Decor rule applied: Generated 3 tonal lightness anchors (+12%, base, -30%) for wall/furniture pairings.")
            }
            DomainProfile.NATURE -> {
                // Earthy tone shift: Reduce saturation (-25%), shift hue towards green/brown range (80°)
                val currentHue = exactComp.normalizedHue()
                // Nudge hue towards 75° (earthy olive/ochre/brown range)
                val targetHue = 75f
                val shiftedHue = (currentHue * 0.6f + targetHue * 0.4f) % 360f
                val earthy = exactComp
                    .copy(hue = shiftedHue)
                    .adjustSaturation(-0.25f)
                    .adjustLightness(-0.05f)
                Pair(earthy, "Nature rule applied: Biased toward earthy tones (-25% saturation, hue shifted toward organic green/ochre).")
            }
            DomainProfile.INDUSTRIAL -> {
                Pair(exactComp, "Industrial rule applied: Pure mathematical 180° hue rotation.")
            }
        }
    }

    private fun calculateNeutralCompanion(base: HslColor): HslColor {
        // Desaturate to 6-12% and invert lightness smoothly
        val lowSat = (base.saturation * 0.12f).coerceIn(0.04f, 0.12f)
        val targetL = if (base.lightness > 0.5f) {
            (1f - base.lightness + 0.1f).coerceIn(0.12f, 0.28f)
        } else {
            (1f - base.lightness - 0.1f).coerceIn(0.75f, 0.92f)
        }
        return HslColor(hue = base.hue, saturation = lowSat, lightness = targetL)
    }

    /**
     * 5-Step gradient generator interpolating between HSL color 1 and color 2.
     */
    fun generate5StepGradient(c1: HslColor, c2: HslColor): List<HslColor> {
        val steps = ArrayList<HslColor>(5)
        val h1 = c1.normalizedHue()
        val h2 = c2.normalizedHue()

        // Shortest hue arc interpolation
        var dh = h2 - h1
        if (dh > 180f) dh -= 360f
        if (dh < -180f) dh += 360f

        for (i in 0..4) {
            val t = i / 4f
            val h = (h1 + dh * t) % 360f
            val safeH = if (h < 0f) h + 360f else h
            val s = c1.saturation + (c2.saturation - c1.saturation) * t
            val l = c1.lightness + (c2.lightness - c1.lightness) * t
            steps.add(HslColor(safeH, s.coerceIn(0f, 1f), l.coerceIn(0f, 1f)))
        }
        return steps
    }

    /**
     * Dark Mode Palette Transform:
     * Inverts lightness sensibly while preserving hue relationships and contrast ratios.
     */
    fun transformToDarkMode(colors: List<HslColor>): List<HslColor> {
        return colors.map { col ->
            val l = col.normalizedLightness()
            // Map lightness into dark theme scale:
            // Very bright (0.9) becomes dark background (0.12)
            // Very dark (0.1) becomes high contrast text/accent (0.88)
            val darkL = (0.94f - l * 0.76f).coerceIn(0.08f, 0.94f)
            col.copy(lightness = darkL)
        }
    }

    fun exportCssGradient(gradientColors: List<HslColor>): String {
        val hexes = gradientColors.joinToString(", ") { it.toHex() }
        return "background: linear-gradient(90deg, $hexes);"
    }

    fun exportComposeGradientCode(gradientColors: List<HslColor>): String {
        val colorsList = gradientColors.joinToString(",\n    ") { col ->
            "Color(0xFF${col.toHex().removePrefix("#")})"
        }
        return """
            val gradientBrush = Brush.linearGradient(
                colors = listOf(
                    $colorsList
                )
            )
        """.trimIndent()
    }

    fun exportCssVariables(colors: List<HslColor>, prefix: String = "huesmith"): String {
        return colors.mapIndexed { idx, col ->
            "--$prefix-color-${idx + 1}: ${col.toHex()}; /* HSL(${col.hue.toInt()}°, ${(col.saturation * 100).toInt()}%, ${(col.lightness * 100).toInt()}%) */"
        }.joinToString("\n")
    }

    fun exportJsonCode(colors: List<HslColor>, paletteName: String = "Huesmith Palette"): String {
        val jsonArray = colors.joinToString(",\n    ") { col ->
            """{"hex": "${col.toHex()}", "hue": ${col.hue.toInt()}, "saturation": ${(col.saturation * 100).toInt()}, "lightness": ${(col.lightness * 100).toInt()}}"""
        }
        return """
            {
              "name": "$paletteName",
              "colors": [
                $jsonArray
              ]
            }
        """.trimIndent()
    }

    fun exportFlutterCode(colors: List<HslColor>): String {
        val colorItems = colors.mapIndexed { idx, col ->
            "static const color${idx + 1} = Color(0xFF${col.toHex().removePrefix("#")});"
        }.joinToString("\n  ")
        return """
            import 'package:flutter/material.dart';

            class AppPalette {
              $colorItems

              static ThemeData get themeData => ThemeData(
                    colorScheme: ColorScheme.light(
                      primary: color1,
                      secondary: color2,
                      tertiary: ${if (colors.size > 2) "color3" else "color1"},
                      surface: ${if (colors.size > 3) "color4" else "Colors.white"},
                    ),
                  );
            }
        """.trimIndent()
    }

    fun exportComposeCode(colors: List<HslColor>): String {
        val colorItems = colors.mapIndexed { idx, col ->
            "val Color${idx + 1} = Color(0xFF${col.toHex().removePrefix("#")})"
        }.joinToString("\n")
        return colorItems
    }
}
