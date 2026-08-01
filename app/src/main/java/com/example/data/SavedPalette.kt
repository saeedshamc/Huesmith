package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_palettes")
data class SavedPalette(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val domain: String,
    val baseHex: String,
    val paletteType: String,
    val colorsHex: String, // Comma-separated hex values e.g. "#FF5722,#2196F3,#4CAF50,#FFC107"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
) {
    fun getColorList(): List<String> {
        return colorsHex.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
