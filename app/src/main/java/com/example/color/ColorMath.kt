package com.example.color

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

data class RgbColor(val r: Int, val g: Int, val b: Int) {
    fun toHex(): String {
        return String.format("#%02X%02X%02X", r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
    }

    fun toComposeColor(): Color {
        return Color(r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
    }

    fun toHsl(): HslColor = HslColor.fromRgb(r, g, b)
}

data class HslColor(
    val hue: Float,        // 0.0 to 360.0
    val saturation: Float, // 0.0 to 1.0
    val lightness: Float   // 0.0 to 1.0
) {
    init {
        // Enforce valid bounds while keeping safe
        require(!hue.isNaN() && !saturation.isNaN() && !lightness.isNaN()) { "HSL values must not be NaN" }
    }

    fun normalizedHue(): Float {
        var h = hue % 360f
        if (h < 0f) h += 360f
        return h
    }

    fun normalizedSaturation(): Float = saturation.coerceIn(0f, 1f)
    fun normalizedLightness(): Float = lightness.coerceIn(0f, 1f)

    fun rotateHue(degrees: Float): HslColor {
        // Guard: if pure black/white/grayscale, hue shift should preserve valid bounds safely
        val newHue = (normalizedHue() + degrees) % 360f
        val safeHue = if (newHue < 0f) newHue + 360f else newHue
        return copy(hue = safeHue)
    }

    fun adjustLightness(delta: Float): HslColor {
        return copy(lightness = (normalizedLightness() + delta).coerceIn(0f, 1f))
    }

    fun adjustSaturation(delta: Float): HslColor {
        return copy(saturation = (normalizedSaturation() + delta).coerceIn(0f, 1f))
    }

    fun toRgb(): RgbColor {
        val h = normalizedHue()
        val s = normalizedSaturation()
        val l = normalizedLightness()

        if (s == 0f) {
            // Grayscale edge case
            val gray = (l * 255f).roundToInt().coerceIn(0, 255)
            return RgbColor(gray, gray, gray)
        }

        val c = (1f - abs(2f * l - 1f)) * s
        val x = c * (1f - abs((h / 60f) % 2f - 1f))
        val m = l - c / 2f

        val (rPrime, gPrime, bPrime) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        val r = ((rPrime + m) * 255f).roundToInt().coerceIn(0, 255)
        val g = ((gPrime + m) * 255f).roundToInt().coerceIn(0, 255)
        val b = ((bPrime + m) * 255f).roundToInt().coerceIn(0, 255)

        return RgbColor(r, g, b)
    }

    fun toHex(): String = toRgb().toHex()
    fun toComposeColor(): Color = toRgb().toComposeColor()

    companion object {
        fun fromRgb(r: Int, g: Int, b: Int): HslColor {
            val rNorm = r.coerceIn(0, 255) / 255f
            val gNorm = g.coerceIn(0, 255) / 255f
            val bNorm = b.coerceIn(0, 255) / 255f

            val maxVal = max(rNorm, max(gNorm, bNorm))
            val minVal = min(rNorm, min(gNorm, bNorm))
            val delta = maxVal - minVal

            val lightness = (maxVal + minVal) / 2f

            if (delta == 0f) {
                // Grayscale: Hue undefined, Saturation 0
                return HslColor(hue = 0f, saturation = 0f, lightness = lightness)
            }

            val saturation = if (lightness > 0.5f) {
                delta / (2f - maxVal - minVal)
            } else {
                delta / (maxVal + minVal)
            }

            val hue = when (maxVal) {
                rNorm -> ((gNorm - bNorm) / delta + (if (gNorm < bNorm) 6f else 0f)) * 60f
                gNorm -> ((bNorm - rNorm) / delta + 2f) * 60f
                else -> ((rNorm - gNorm) / delta + 4f) * 60f
            }

            return HslColor(
                hue = if (hue.isNaN()) 0f else hue % 360f,
                saturation = saturation.coerceIn(0f, 1f),
                lightness = lightness.coerceIn(0f, 1f)
            )
        }

        fun fromHex(hexString: String): HslColor {
            val cleanHex = hexString.removePrefix("#").trim()
            val hex = when (cleanHex.length) {
                3 -> cleanHex.map { "$it$it" }.joinToString("")
                6 -> cleanHex
                else -> "000000"
            }
            return try {
                val r = hex.substring(0, 2).toInt(16)
                val g = hex.substring(2, 4).toInt(16)
                val b = hex.substring(4, 6).toInt(16)
                fromRgb(r, g, b)
            } catch (e: Exception) {
                HslColor(0f, 0f, 0f)
            }
        }
    }
}

/**
 * WCAG 2.1 Contrast Calculation Utilities using exact sRGB relative luminance formula.
 */
object WcagUtils {

    private fun sRgbToLinear(channel: Int): Float {
        val c = channel.coerceIn(0, 255) / 255f
        return if (c <= 0.04045f) {
            c / 12.92f
        } else {
            ((c + 0.055f) / 1.055f).pow(2.4f)
        }
    }

    fun relativeLuminance(rgb: RgbColor): Float {
        val rLinear = sRgbToLinear(rgb.r)
        val gLinear = sRgbToLinear(rgb.g)
        val bLinear = sRgbToLinear(rgb.b)
        return 0.2126f * rLinear + 0.7152f * gLinear + 0.0722f * bLinear
    }

    fun contrastRatio(rgb1: RgbColor, rgb2: RgbColor): Float {
        val l1 = relativeLuminance(rgb1)
        val l2 = relativeLuminance(rgb2)
        val brighter = max(l1, l2)
        val darker = min(l1, l2)
        return (brighter + 0.05f) / (darker + 0.05f)
    }

    enum class ContrastLevel(val label: String, val isPassAA: Boolean, val isPassAAA: Boolean) {
        FAIL("Fail (<3.0)", false, false),
        AA_LARGE("AA Large (3.0+)", true, false),
        AA("AA Normal (4.5+)", true, false),
        AAA("AAA Premium (7.0+)", true, true)
    }

    fun getContrastLevel(ratio: Float): ContrastLevel {
        return when {
            ratio >= 7.0f -> ContrastLevel.AAA
            ratio >= 4.5f -> ContrastLevel.AA
            ratio >= 3.0f -> ContrastLevel.AA_LARGE
            else -> ContrastLevel.FAIL
        }
    }

    /**
     * Nudges lightness of foreground color to pass AA contrast (>= 4.5) against background color.
     */
    fun ensureAaContrast(fg: HslColor, bg: HslColor, minRatio: Float = 4.5f): HslColor {
        var currentFg = fg
        var ratio = contrastRatio(currentFg.toRgb(), bg.toRgb())
        if (ratio >= minRatio) return currentFg

        val bgLuminance = relativeLuminance(bg.toRgb())
        val step = if (bgLuminance > 0.5f) -0.05f else 0.05f

        var iterations = 0
        while (ratio < minRatio && iterations < 18) {
            val newL = (currentFg.lightness + step).coerceIn(0.05f, 0.95f)
            if (abs(newL - currentFg.lightness) < 0.001f) break
            currentFg = currentFg.copy(lightness = newL)
            ratio = contrastRatio(currentFg.toRgb(), bg.toRgb())
            iterations++
        }
        return currentFg
    }
}

enum class ColorBlindnessType(
    val title: String,
    val description: String,
    val shortName: String
) {
    NONE("Normal Vision", "Standard trichromatic perception", "Normal"),
    PROTANOPIA("Protanopia", "Red-blind (Long-wavelength deficiency)", "Protanopia"),
    DEUTERANOPIA("Deuteranopia", "Green-blind (Medium-wavelength deficiency)", "Deuteranopia"),
    TRITANOPIA("Tritanopia", "Blue-blind (Short-wavelength deficiency)", "Tritanopia"),
    ACHROMATOPSIA("Achromatopsia", "Total color blindness (Monochromatic grayscale)", "Achromatopsia")
}

object ColorBlindnessSimulator {
    fun simulate(rgb: RgbColor, type: ColorBlindnessType): RgbColor {
        if (type == ColorBlindnessType.NONE) return rgb
        val r = rgb.r.toFloat()
        val g = rgb.g.toFloat()
        val b = rgb.b.toFloat()

        val (simR, simG, simB) = when (type) {
            ColorBlindnessType.PROTANOPIA -> Triple(
                0.56667f * r + 0.43333f * g + 0.00000f * b,
                0.55833f * r + 0.44167f * g + 0.00000f * b,
                0.00000f * r + 0.24167f * g + 0.75833f * b
            )
            ColorBlindnessType.DEUTERANOPIA -> Triple(
                0.62500f * r + 0.37500f * g + 0.00000f * b,
                0.70000f * r + 0.30000f * g + 0.00000f * b,
                0.00000f * r + 0.30000f * g + 0.70000f * b
            )
            ColorBlindnessType.TRITANOPIA -> Triple(
                0.95000f * r + 0.05000f * g + 0.00000f * b,
                0.00000f * r + 0.43333f * g + 0.56667f * b,
                0.00000f * r + 0.47500f * g + 0.52500f * b
            )
            ColorBlindnessType.ACHROMATOPSIA -> {
                val gray = 0.299f * r + 0.587f * g + 0.114f * b
                Triple(gray, gray, gray)
            }
            ColorBlindnessType.NONE -> Triple(r, g, b)
        }

        return RgbColor(
            simR.roundToInt().coerceIn(0, 255),
            simG.roundToInt().coerceIn(0, 255),
            simB.roundToInt().coerceIn(0, 255)
        )
    }

    fun simulate(hsl: HslColor, type: ColorBlindnessType): HslColor {
        if (type == ColorBlindnessType.NONE) return hsl
        val rgb = hsl.toRgb()
        val simRgb = simulate(rgb, type)
        return HslColor.fromRgb(simRgb.r, simRgb.g, simRgb.b)
    }
}

enum class ColorHarmonyAlgorithm(
    val title: String,
    val description: String,
    val angles: List<Float>
) {
    COMPLEMENTARY("Complementary", "Exact 180° opposite hue for bold dynamic contrast.", listOf(0f, 180f)),
    ANALOGOUS("Analogous", "Adjacent hues ±30° for cohesive, serene warmth.", listOf(0f, -30f, 30f)),
    TRIADIC("Triadic", "Equilateral 120° triangle across the color wheel.", listOf(0f, 120f, 240f)),
    SPLIT_COMPLEMENTARY("Split-Complementary", "Base with 150° and 210° accents for balanced vibrancy.", listOf(0f, 150f, 210f)),
    TETRADIC("Tetradic / Square", "Four harmonious hues spaced at 90° intervals.", listOf(0f, 90f, 180f, 270f)),
    MONOCHROMATIC("Monochromatic", "Pure tonal variations across the same base hue.", listOf(0f))
}

