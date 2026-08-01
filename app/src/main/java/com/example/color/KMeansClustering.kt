package com.example.color

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object KMeansClustering {

    private data class ClusterCentroid(
        var r: Float,
        var g: Float,
        var b: Float,
        var pixelCount: Int = 0
    )

    /**
     * Extracts [k] dominant colors from [sourceBitmap] using custom pure Kotlin K-Means clustering.
     * Runs safely on [Dispatchers.Default] to preserve UI thread responsiveness.
     */
    suspend fun extractDominantColors(
        sourceBitmap: Bitmap,
        k: Int = 5,
        maxIterations: Int = 12
    ): List<HslColor> = withContext(Dispatchers.Default) {
        if (sourceBitmap.width <= 0 || sourceBitmap.height <= 0) return@withContext emptyList()

        // Downsample bitmap to max 100x100 to keep clustering ultra fast & lightweight
        val maxDim = 100
        val scale = min(maxDim.toFloat() / sourceBitmap.width, maxDim.toFloat() / sourceBitmap.height)
            .coerceAtMost(1f)

        val targetWidth = max(1, (sourceBitmap.width * scale).toInt())
        val targetHeight = max(1, (sourceBitmap.height * scale).toInt())

        val scaledBitmap = Bitmap.createScaledBitmap(sourceBitmap, targetWidth, targetHeight, false)
        val pixelCount = targetWidth * targetHeight
        val pixels = IntArray(pixelCount)
        scaledBitmap.getPixels(pixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)

        if (pixels.isEmpty()) return@withContext emptyList()

        // Extract RGB arrays
        val rPixels = FloatArray(pixelCount)
        val gPixels = FloatArray(pixelCount)
        val bPixels = FloatArray(pixelCount)

        for (i in pixels.indices) {
            val c = pixels[i]
            rPixels[i] = ((c shr 16) and 0xFF).toFloat()
            gPixels[i] = ((c shr 8) and 0xFF).toFloat()
            bPixels[i] = (c and 0xFF).toFloat()
        }

        val actualK = min(k, pixelCount)
        val centroids = ArrayList<ClusterCentroid>(actualK)

        // Initialize centroids with distributed sample pixels
        val step = pixelCount / actualK
        for (i in 0 until actualK) {
            val idx = (i * step + Random.nextInt(0, max(1, step))).coerceIn(0, pixelCount - 1)
            centroids.add(ClusterCentroid(rPixels[idx], gPixels[idx], bPixels[idx]))
        }

        val assignments = IntArray(pixelCount)

        // K-Means loop
        for (iter in 0 until maxIterations) {
            // Step 1: Assign each pixel to nearest centroid
            for (p in 0 until pixelCount) {
                val pr = rPixels[p]
                val pg = gPixels[p]
                val pb = bPixels[p]

                var minDist = Float.MAX_VALUE
                var closestCentroid = 0

                for (c in 0 until actualK) {
                    val cent = centroids[c]
                    val dr = pr - cent.r
                    val dg = pg - cent.g
                    val db = pb - cent.b
                    val dist = dr * dr + dg * dg + db * db

                    if (dist < minDist) {
                        minDist = dist
                        closestCentroid = c
                    }
                }
                assignments[p] = closestCentroid
            }

            // Step 2: Recalculate centroids
            val sumR = FloatArray(actualK)
            val sumG = FloatArray(actualK)
            val sumB = FloatArray(actualK)
            val counts = IntArray(actualK)

            for (p in 0 until pixelCount) {
                val cluster = assignments[p]
                sumR[cluster] += rPixels[p]
                sumG[cluster] += gPixels[p]
                sumB[cluster] += bPixels[p]
                counts[cluster]++
            }

            var maxShift = 0f
            for (c in 0 until actualK) {
                if (counts[c] > 0) {
                    val newR = sumR[c] / counts[c]
                    val newG = sumG[c] / counts[c]
                    val newB = sumB[c] / counts[c]

                    val shift = (newR - centroids[c].r) * (newR - centroids[c].r) +
                            (newG - centroids[c].g) * (newG - centroids[c].g) +
                            (newB - centroids[c].b) * (newB - centroids[c].b)

                    if (shift > maxShift) maxShift = shift

                    centroids[c].r = newR
                    centroids[c].g = newG
                    centroids[c].b = newB
                    centroids[c].pixelCount = counts[c]
                }
            }

            // Early exit if centroids converged
            if (maxShift < 1.0f) break
        }

        // Sort clusters by popularity (pixel count)
        centroids.sortByDescending { it.pixelCount }

        // Convert centroids to HslColors
        return@withContext centroids.map { cent ->
            HslColor.fromRgb(
                cent.r.toInt().coerceIn(0, 255),
                cent.g.toInt().coerceIn(0, 255),
                cent.b.toInt().coerceIn(0, 255)
            )
        }
    }
}
