package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.color.HslColor
import com.example.color.PngExporter
import com.example.ui.components.BaseColorPickerCard
import com.example.ui.components.ColorDetailSheet
import com.example.ui.components.CompanionPalettesSection
import com.example.ui.components.ContrastMatrixCard
import com.example.ui.components.CvdQuickAccessCard
import com.example.ui.components.DomainSelectorBar
import com.example.ui.components.ExportShareCard
import com.example.ui.components.GradientAndToolsTab
import com.example.ui.components.LiveMockupStudio
import com.example.ui.components.ManualColorPickerDialog
import com.example.ui.components.SavedPalettesTab
import com.example.ui.components.SyncModeCard
import com.example.ui.theme.HuesmithTheme
import com.example.viewmodel.HuesmithViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: HuesmithViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            HuesmithTheme(darkTheme = uiState.isDarkModeTransformActive) {
                HuesmithMainApp(viewModel = viewModel)
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
    var showManualColorPicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Central Color Selection / Click Handler respecting Auto-Copy Hex setting
    val handleColorSelect: (HslColor) -> Unit = { color ->
        if (uiState.autoCopyHexOnSelect) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Huesmith Hex Code", color.toHex())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied ${color.toHex()} to clipboard!", Toast.LENGTH_SHORT).show()
        }
        inspectedColor = color
    }

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
                    // Auto-Copy Hex Toggle in Top App Bar
                    IconButton(
                        onClick = { viewModel.toggleAutoCopyHex() },
                        modifier = Modifier.testTag("top_bar_auto_copy_toggle")
                    ) {
                        Icon(
                            imageVector = if (uiState.autoCopyHexOnSelect) Icons.Default.AssignmentTurnedIn else Icons.Default.ContentCopy,
                            contentDescription = "Toggle Auto-Copy Hex",
                            tint = if (uiState.autoCopyHexOnSelect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Dark Mode Transform Toggle
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
        floatingActionButton = {
            // Floating Action Button to open system/manual color picker
            FloatingActionButton(
                onClick = { showManualColorPicker = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                modifier = Modifier.testTag("main_screen_color_picker_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Colorize, contentDescription = "Manual Color Picker", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pick Color", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
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
                            selectedHarmony = uiState.selectedHarmony,
                            isColorWheelMode = uiState.isColorWheelActive,
                            extractedColors = uiState.extractedImageColors,
                            isExtractingImage = uiState.isExtractingImage,
                            onColorChanged = { viewModel.updateBaseColor(it) },
                            onHexChanged = { viewModel.updateBaseHex(it) },
                            onHarmonySelected = { viewModel.selectHarmony(it) },
                            onToggleColorWheelMode = { viewModel.toggleColorWheelMode() },
                            onImageSelected = { bitmap -> viewModel.extractColorsFromBitmap(bitmap) }
                        )

                        // Harmonic Sync Mode Controller Card
                        SyncModeCard(
                            isSyncActive = uiState.isSyncModeActive,
                            syncedIndices = uiState.syncedColorIndices,
                            currentColors = paletteSet.domainAdjustedComplementary,
                            onToggleSyncMode = { viewModel.toggleSyncMode() },
                            onToggleSyncIndex = { idx -> viewModel.toggleSyncIndex(idx) },
                            onSyncAll = { viewModel.syncAllIndices() },
                            onApplySyncAdjustments = { dH, dS, dL ->
                                viewModel.applySyncAdjustments(dH, dS, dL)
                            }
                        )

                        DomainSelectorBar(
                            selectedDomain = uiState.selectedDomain,
                            onDomainSelected = { viewModel.selectDomain(it) }
                        )

                        // Quick-Access CVD Simulation Toggle Bar for on-the-fly contrast verification
                        CvdQuickAccessCard(
                            selectedCvdType = uiState.selectedCvdType,
                            previewColors = paletteSet.domainAdjustedComplementary,
                            onCvdSelected = { viewModel.selectCvdType(it) }
                        )

                        CompanionPalettesSection(
                            paletteSet = paletteSet,
                            isDarkModeTransformActive = uiState.isDarkModeTransformActive,
                            lockedIndices = uiState.lockedColorIndices,
                            onToggleLock = { idx -> viewModel.toggleColorLock(idx) },
                            onShuffle = { viewModel.shuffleUnlockedColors() },
                            onInspectColor = handleColorSelect,
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

                        // Pairwise WCAG Contrast Matrix Component
                        ContrastMatrixCard(
                            colors = paletteSet.domainAdjustedComplementary,
                            paletteTitle = "${paletteSet.domain.title} Palette",
                            onColorClick = handleColorSelect
                        )

                        ExportShareCard(paletteSet = paletteSet)

                        Spacer(modifier = Modifier.height(72.dp))
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
                            selectedCvdType = uiState.selectedCvdType,
                            isDarkModeTransformActive = uiState.isDarkModeTransformActive,
                            isAccessibilityInspectorActive = uiState.isAccessibilityInspectorActive,
                            onMockupTypeSelected = { viewModel.setMockupType(it) },
                            onCvdTypeSelected = { viewModel.selectCvdType(it) },
                            onToggleAccessibilityInspector = { viewModel.toggleAccessibilityInspector() }
                        )

                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }

                2 -> {
                    // Gradients & Tools Tab
                    GradientAndToolsTab(
                        paletteSet = paletteSet,
                        isDarkModeActive = uiState.isDarkModeTransformActive,
                        onToggleDarkMode = { viewModel.toggleDarkModeTransform() },
                        onInspectColor = handleColorSelect
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

        // Manual / System Color Picker Dialog
        if (showManualColorPicker) {
            ManualColorPickerDialog(
                initialColor = uiState.baseColor,
                onColorSelected = { newCol ->
                    viewModel.updateBaseColor(newCol)
                },
                onDismiss = { showManualColorPicker = false }
            )
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
