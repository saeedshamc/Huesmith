package com.example.color

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.SavedPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipExporter {

    /**
     * Packages selected saved palettes into a single structured ZIP archive
     * containing individual JSON and CSS files for each palette, plus a master index.
     */
    suspend fun createPalettesZip(
        context: Context,
        palettes: List<SavedPalette>
    ): Uri? = withContext(Dispatchers.IO) {
        if (palettes.isEmpty()) return@withContext null

        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val zipFile = File(exportsDir, "huesmith_palettes_$timestamp.zip")

        try {
            FileOutputStream(zipFile).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    val masterIndexJson = JSONObject()
                    val palettesArray = JSONArray()
                    val masterCssBuilder = StringBuilder()

                    masterCssBuilder.append("/* Huesmith Batch Palettes Export */\n")
                    masterCssBuilder.append("/* Export Date: ${Date()} */\n")
                    masterCssBuilder.append("/* Total Palettes: ${palettes.size} */\n\n")

                    palettes.forEachIndexed { index, palette ->
                        val safeSlug = palette.title
                            .lowercase(Locale.US)
                            .replace(Regex("[^a-z0-9_]"), "_")
                            .take(24)
                            .ifBlank { "palette" }
                        val filePrefix = "palette_${index + 1}_$safeSlug"

                        val colorHexList = palette.getColorList()
                        val hslColors = colorHexList.map { HslColor.fromHex(it) }

                        // 1. Generate Individual JSON file
                        val paletteJson = JSONObject().apply {
                            put("id", palette.id)
                            put("title", palette.title)
                            put("domain", palette.domain)
                            put("paletteType", palette.paletteType)
                            put("baseHex", palette.baseHex)
                            put("isFavorite", palette.isFavorite)
                            put("notes", palette.notes)
                            put("timestamp", palette.timestamp)

                            val colorsArray = JSONArray()
                            hslColors.forEachIndexed { colIdx, hsl ->
                                val rgb = hsl.toRgb()
                                val colObj = JSONObject().apply {
                                    put("index", colIdx)
                                    put("hex", hsl.toHex())
                                    put("rgb", JSONObject().apply {
                                        put("r", rgb.r)
                                        put("g", rgb.g)
                                        put("b", rgb.b)
                                    })
                                    put("hsl", JSONObject().apply {
                                        put("hue", hsl.hue.toInt())
                                        put("saturation", String.format(Locale.US, "%.1f%%", hsl.saturation * 100))
                                        put("lightness", String.format(Locale.US, "%.1f%%", hsl.lightness * 100))
                                    })
                                }
                                colorsArray.put(colObj)
                            }
                            put("colors", colorsArray)
                        }

                        addStringToZip(
                            zos = zos,
                            fileName = "json/$filePrefix.json",
                            content = paletteJson.toString(2)
                        )

                        // 2. Generate Individual CSS Variable file
                        val cssContent = PaletteGenerator.exportCssVariables(hslColors, safeSlug)
                        addStringToZip(
                            zos = zos,
                            fileName = "css/$filePrefix.css",
                            content = cssContent
                        )

                        // Append to Master Collections
                        palettesArray.put(paletteJson)
                        masterCssBuilder.append("/* --- ${palette.title} (${palette.domain}) --- */\n")
                        masterCssBuilder.append(cssContent).append("\n\n")
                    }

                    // 3. Write Master Index JSON
                    masterIndexJson.apply {
                        put("generator", "Huesmith Palette Craftsman")
                        put("exportDate", Date().toString())
                        put("count", palettes.size)
                        put("palettes", palettesArray)
                    }
                    addStringToZip(zos, "index.json", masterIndexJson.toString(2))

                    // 4. Write Master Bundled CSS
                    addStringToZip(zos, "all_palettes_bundle.css", masterCssBuilder.toString())

                    // 5. Write README.txt
                    val readmeContent = """
                        ====================================================
                        Huesmith Palettes Export Archive
                        ====================================================
                        Total Palettes: ${palettes.size}
                        Export Timestamp: ${Date()}
                        
                        Directory Structure:
                        - /json/ : Individual structured JSON metadata for each palette
                        - /css/  : Standard CSS custom property declarations
                        - index.json : Combined registry manifest
                        - all_palettes_bundle.css : Unified CSS variable stylesheet
                        
                        Crafted offline with Huesmith Color System.
                    """.trimIndent()
                    addStringToZip(zos, "README.txt", readmeContent)
                }
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun addStringToZip(zos: ZipOutputStream, fileName: String, content: String) {
        val entry = ZipEntry(fileName)
        zos.putNextEntry(entry)
        val bytes = content.toByteArray(StandardCharsets.UTF_8)
        zos.write(bytes, 0, bytes.size)
        zos.closeEntry()
    }

    fun shareZipFile(context: Context, uri: Uri, count: Int) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Huesmith Batch Palettes Export ($count palettes)")
            putExtra(Intent.EXTRA_TEXT, "Exported $count color palettes from Huesmith.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Palettes ZIP Archive"))
    }
}
