package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.color.DomainProfile
import com.example.color.GeneratedPaletteSet
import com.example.color.HslColor
import com.example.color.KMeansClustering
import com.example.color.PaletteGenerator
import com.example.data.AppDatabase
import com.example.data.PaletteRepository
import com.example.data.SavedPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MockupType(val label: String, val iconName: String) {
    MOBILE_APP("Mobile App Screen", "Phone"),
    BRAND_CARD("Brand Identity Card", "Badge"),
    INTERIOR_ROOM("Interior Spatial Palette", "Home"),
    POSTER_BANNER("Poster & Banner Layout", "Dashboard")
}

data class HuesmithUiState(
    val baseColor: HslColor = HslColor(14f, 1.0f, 0.57f), // Warm Terracotta default
    val selectedDomain: DomainProfile = DomainProfile.BRANDING,
    val isDarkModeTransformActive: Boolean = false,
    val mockupType: MockupType = MockupType.MOBILE_APP,
    val isAccessibilityInspectorActive: Boolean = false,
    val lockedColorIndices: Set<Int> = emptySet(),
    val extractedImageColors: List<HslColor> = emptyList(),
    val isExtractingImage: Boolean = false,
    val searchQuery: String = "",
    val domainFilter: String? = null,
    val userNotification: String? = null
)

class HuesmithViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PaletteRepository

    init {
        val database = AppDatabase.getInstance(application)
        repository = PaletteRepository(database.paletteDao())
    }

    private val _uiState = MutableStateFlow(HuesmithUiState())
    val uiState: StateFlow<HuesmithUiState> = _uiState.asStateFlow()

    // Reactive list of saved palettes from database
    val savedPalettes: StateFlow<List<SavedPalette>> = combine(
        repository.allPalettes,
        _uiState
    ) { palettes, state ->
        palettes.filter { palette ->
            val matchesSearch = state.searchQuery.isEmpty() ||
                    palette.title.contains(state.searchQuery, ignoreCase = true) ||
                    palette.colorsHex.contains(state.searchQuery, ignoreCase = true)
            val matchesDomain = state.domainFilter.isNullOrEmpty() ||
                    palette.domain.equals(state.domainFilter, ignoreCase = true)
            matchesSearch && matchesDomain
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Derived reactive current generated palette set
    val generatedPaletteSet: StateFlow<GeneratedPaletteSet> = combine(
        _uiState,
        _uiState
    ) { state, _ ->
        PaletteGenerator.generatePalettes(state.baseColor, state.selectedDomain)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PaletteGenerator.generatePalettes(
            HslColor(14f, 1.0f, 0.57f),
            DomainProfile.BRANDING
        )
    )

    fun updateBaseColor(color: HslColor) {
        _uiState.value = _uiState.value.copy(baseColor = color)
    }

    fun updateBaseHex(hex: String) {
        val color = HslColor.fromHex(hex)
        _uiState.value = _uiState.value.copy(baseColor = color)
    }

    fun selectDomain(domain: DomainProfile) {
        _uiState.value = _uiState.value.copy(selectedDomain = domain)
    }

    fun toggleDarkModeTransform() {
        val current = _uiState.value.isDarkModeTransformActive
        _uiState.value = _uiState.value.copy(isDarkModeTransformActive = !current)
    }

    fun toggleColorLock(index: Int) {
        val currentLocks = _uiState.value.lockedColorIndices.toMutableSet()
        if (currentLocks.contains(index)) {
            currentLocks.remove(index)
        } else {
            currentLocks.add(index)
        }
        _uiState.value = _uiState.value.copy(lockedColorIndices = currentLocks)
    }

    fun toggleAccessibilityInspector() {
        val current = _uiState.value.isAccessibilityInspectorActive
        _uiState.value = _uiState.value.copy(isAccessibilityInspectorActive = !current)
    }

    fun shuffleUnlockedColors() {
        val state = _uiState.value
        val isBaseLocked = state.lockedColorIndices.contains(0)

        // If base color is not locked, randomize base hue & saturation slightly
        val newBase = if (!isBaseLocked) {
            val randomHue = (0..360).random().toFloat()
            val randomSat = (60..100).random() / 100f
            val randomLight = (35..65).random() / 100f
            HslColor(randomHue, randomSat, randomLight)
        } else {
            state.baseColor
        }

        _uiState.value = state.copy(
            baseColor = newBase,
            userNotification = if (state.lockedColorIndices.isNotEmpty()) "Shuffled unlocked colors while keeping ${state.lockedColorIndices.size} color(s) locked!" else "Shuffled palette!"
        )
    }

    fun setMockupType(type: MockupType) {
        _uiState.value = _uiState.value.copy(mockupType = type)
    }

    fun extractColorsFromBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExtractingImage = true)
            try {
                val extracted = KMeansClustering.extractDominantColors(bitmap, k = 5)
                if (extracted.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        extractedImageColors = extracted,
                        baseColor = extracted.first(), // Auto-set dominant color as base
                        isExtractingImage = false,
                        userNotification = "Extracted ${extracted.size} dominant colors from image!"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isExtractingImage = false,
                        userNotification = "Could not extract colors from image."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExtractingImage = false,
                    userNotification = "Image processing error: ${e.message}"
                )
            }
        }
    }

    fun savePalette(title: String, paletteType: String, colorsHex: List<String>, notes: String) {
        viewModelScope.launch {
            val palette = SavedPalette(
                title = title.ifBlank { "${_uiState.value.selectedDomain.title} Palette" },
                domain = _uiState.value.selectedDomain.title,
                baseHex = _uiState.value.baseColor.toHex(),
                paletteType = paletteType,
                colorsHex = colorsHex.joinToString(","),
                notes = notes,
                isFavorite = false
            )
            repository.savePalette(palette)
            _uiState.value = _uiState.value.copy(userNotification = "Palette saved to local collection!")
        }
    }

    fun toggleFavorite(palette: SavedPalette) {
        viewModelScope.launch {
            repository.updatePalette(palette.copy(isFavorite = !palette.isFavorite))
        }
    }

    fun deletePalette(palette: SavedPalette) {
        viewModelScope.launch {
            repository.deletePalette(palette)
            _uiState.value = _uiState.value.copy(userNotification = "Palette deleted")
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setDomainFilter(domain: String?) {
        _uiState.value = _uiState.value.copy(domainFilter = domain)
    }

    fun clearNotification() {
        _uiState.value = _uiState.value.copy(userNotification = null)
    }
}
