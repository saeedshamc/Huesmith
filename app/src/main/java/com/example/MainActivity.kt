package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.color.HslColor
import com.example.ui.components.BaseColorPickerCard
import com.example.ui.components.ColorDetailSheet
import com.example.ui.components.CompanionPalettesSection
import com.example.ui.components.DomainSelectorBar
import com.example.ui.components.ExportShareCard
import com.example.ui.components.GradientAndToolsTab
import com.example.ui.components.LiveMockupStudio
import com.example.ui.components.SavedPalettesTab
import com.example.ui.theme.HuesmithTheme
import com.example.viewmodel.HuesmithViewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.color.PngExporter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HuesmithTheme {
                HuesmithMainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HuesmithMainApp(viewModel: HuesmithViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val paletteSet by viewModel.generatedPaletteSet.collectAsStateWithLifecycle()
    val savedPalettes by viewModel.savedPalettes.collectAsStateWithLifecycle()

    var currentTab by remember { mutableIntStateOf(0) }
    var inspectedColor by remember { mutableStateOf<HslColor?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.userNotification) {
        uiState.userNotification?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Huesmith",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "100% Offline Domain Color Craftsman",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleDarkModeTransform() },
                        modifier = Modifier.testTag("top_bar_dark_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (uiState.isDarkModeTransformActive) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Toggle Dark Mode Transform",
                            tint = if (uiState.isDarkModeTransformActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(imageVector = Icons.Default.Build, contentDescription = "Forge") },
                    label = { Text("Forge Studio", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_tab_studio")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Mockups") },
                    label = { Text("Live Mockup", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_tab_mockups")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(imageVector = Icons.Default.Gradient, contentDescription = "Gradients") },
                    label = { Text("Gradients", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_tab_gradients")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Saved") },
                    label = { Text("Saved (${savedPalettes.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_tab_saved")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> {
                    // Studio Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        BaseColorPickerCard(
                            baseColor = uiState.baseColor,
                            extractedColors = uiState.extractedImageColors,
                            isExtractingImage = uiState.isExtractingImage,
                            onColorChanged = { viewModel.updateBaseColor(it) },
                            onHexChanged = { viewModel.updateBaseHex(it) },
                            onImageSelected = { bitmap -> viewModel.extractColorsFromBitmap(bitmap) }
                        )

                        DomainSelectorBar(
                            selectedDomain = uiState.selectedDomain,
                            onDomainSelected = { viewModel.selectDomain(it) }
                        )

                        CompanionPalettesSection(
                            paletteSet = paletteSet,
                            isDarkModeTransformActive = uiState.isDarkModeTransformActive,
                            lockedIndices = uiState.lockedColorIndices,
                            onToggleLock = { idx -> viewModel.toggleColorLock(idx) },
                            onShuffle = { viewModel.shuffleUnlockedColors() },
                            onInspectColor = { inspectedColor = it },
                            onSharePng = { title, colors ->
                                coroutineScope.launch {
                                    val uri = PngExporter.generatePaletteCardPng(context, title, uiState.selectedDomain.title, colors)
                                    uri?.let { PngExporter.sharePngImage(context, it, title) }
                                }
                            },
                            onSavePalette = { title, type, colors, notes ->
                                viewModel.savePalette(title, type, colors, notes)
                            }
                        )

                        ExportShareCard(paletteSet = paletteSet)

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                1 -> {
                    // Live Mockup Studio Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        LiveMockupStudio(
                            paletteSet = paletteSet,
                            mockupType = uiState.mockupType,
                            isDarkModeTransformActive = uiState.isDarkModeTransformActive,
                            isAccessibilityInspectorActive = uiState.isAccessibilityInspectorActive,
                            onMockupTypeSelected = { viewModel.setMockupType(it) },
                            onToggleAccessibilityInspector = { viewModel.toggleAccessibilityInspector() }
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                2 -> {
                    // Gradients & Tools Tab
                    GradientAndToolsTab(
                        paletteSet = paletteSet,
                        isDarkModeActive = uiState.isDarkModeTransformActive,
                        onToggleDarkMode = { viewModel.toggleDarkModeTransform() },
                        onInspectColor = { inspectedColor = it }
                    )
                }

                3 -> {
                    // Saved Palettes Collection Tab
                    SavedPalettesTab(
                        savedPalettes = savedPalettes,
                        searchQuery = uiState.searchQuery,
                        domainFilter = uiState.domainFilter,
                        onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                        onDomainFilterChanged = { viewModel.setDomainFilter(it) },
                        onFavoriteToggled = { viewModel.toggleFavorite(it) },
                        onDeletePalette = { viewModel.deletePalette(it) },
                        onSelectPalette = {
                            viewModel.updateBaseColor(it)
                            currentTab = 0 // Switch back to Studio tab
                        }
                    )
                }
            }
        }

        // Modal Color Detail Sheet
        inspectedColor?.let { hsl ->
            ColorDetailSheet(
                color = hsl,
                onDismiss = { inspectedColor = null }
            )
        }
    }
}
