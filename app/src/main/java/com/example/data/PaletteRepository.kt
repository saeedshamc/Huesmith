package com.example.data

import kotlinx.coroutines.flow.Flow

class PaletteRepository(private val paletteDao: PaletteDao) {

    val allPalettes: Flow<List<SavedPalette>> = paletteDao.getAllPalettes()
    val favoritePalettes: Flow<List<SavedPalette>> = paletteDao.getFavoritePalettes()

    fun getPalettesByDomain(domain: String): Flow<List<SavedPalette>> {
        return paletteDao.getPalettesByDomain(domain)
    }

    suspend fun savePalette(palette: SavedPalette): Long {
        return paletteDao.insertPalette(palette)
    }

    suspend fun updatePalette(palette: SavedPalette) {
        paletteDao.updatePalette(palette)
    }

    suspend fun deletePalette(palette: SavedPalette) {
        paletteDao.deletePalette(palette)
    }

    suspend fun deleteById(id: Long) {
        paletteDao.deleteById(id)
    }
}
