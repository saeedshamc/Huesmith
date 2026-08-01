package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PaletteDao {

    @Query("SELECT * FROM saved_palettes ORDER BY timestamp DESC")
    fun getAllPalettes(): Flow<List<SavedPalette>>

    @Query("SELECT * FROM saved_palettes WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoritePalettes(): Flow<List<SavedPalette>>

    @Query("SELECT * FROM saved_palettes WHERE domain = :domain ORDER BY timestamp DESC")
    fun getPalettesByDomain(domain: String): Flow<List<SavedPalette>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPalette(palette: SavedPalette): Long

    @Update
    suspend fun updatePalette(palette: SavedPalette)

    @Delete
    suspend fun deletePalette(palette: SavedPalette)

    @Query("DELETE FROM saved_palettes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
