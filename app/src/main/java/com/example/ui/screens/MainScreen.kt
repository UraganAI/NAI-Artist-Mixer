package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import com.example.ui.components.AboutAppDialog
import com.example.ui.components.FavoritePreviewDialog
import com.example.ui.components.FavoriteMixesDialog
import com.example.ui.components.MultipleHeartsIcon
import com.example.ui.components.TagManagementDialog
import androidx.compose.material.icons.filled.Label
import com.example.util.mouseWheelScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.ui.components.ArtistTagSourceDialog
import com.example.ui.components.FolderSelector
import com.example.ui.components.FormattingPanel
import com.example.ui.components.FormattingSettingsDialog
import com.example.ui.components.MatchArtistDialog
import com.example.ui.components.NovelAiGenerationDialog
import com.example.ui.components.NovelAiSettingsDialog
import com.example.ui.components.NumbersOfArtistsCard
import com.example.ui.components.PictureGrid
import com.example.ui.components.ResultCard
import com.example.ui.components.ThemeSelectorDialog
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    val appThemePreset by viewModel.appThemePreset.collectAsStateWithLifecycle()
    val folderName by viewModel.folderName.collectAsStateWithLifecycle()
    val availablePictures by viewModel.availablePictures.collectAsStateWithLifecycle()
    val requestedCount by viewModel.requestedCount.collectAsStateWithLifecycle()
    val allowDuplicates by viewModel.allowDuplicates.collectAsStateWithLifecycle()
    val formattingOptions by viewModel.formattingOptions.collectAsStateWithLifecycle()
    val currentResult by viewModel.currentResult.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val showFirstStartPrompt by viewModel.showFirstStartPrompt.collectAsStateWithLifecycle()
    val favoriteArtistNames by viewModel.favoriteArtistNames.collectAsStateWithLifecycle()
    val favoriteMixes by viewModel.favoriteMixes.collectAsStateWithLifecycle()

    val headerColorArgb by viewModel.headerColorArgb.collectAsStateWithLifecycle()
    val paragraphColorArgb by viewModel.paragraphColorArgb.collectAsStateWithLifecycle()
    val warningColorArgb by viewModel.warningColorArgb.collectAsStateWithLifecycle()
    val foregroundColorArgb by viewModel.foregroundColorArgb.collectAsStateWithLifecycle()
    val backgroundColorArgb by viewModel.backgroundColorArgb.collectAsStateWithLifecycle()
    val darkBgColorArgb by viewModel.darkBgColorArgb.collectAsStateWithLifecycle()
    val inputBgColorArgb by viewModel.inputBgColorArgb.collectAsStateWithLifecycle()

    // NovelAI state flows
    val naiApiKey by viewModel.naiApiKey.collectAsStateWithLifecycle()
    val naiMainPrompt by viewModel.naiMainPrompt.collectAsStateWithLifecycle()
    val naiUc by viewModel.naiUc.collectAsStateWithLifecycle()
    val naiCharacter by viewModel.naiCharacter.collectAsStateWithLifecycle()
    val naiModel by viewModel.naiModel.collectAsStateWithLifecycle()
    val naiWidth by viewModel.naiWidth.collectAsStateWithLifecycle()
    val naiHeight by viewModel.naiHeight.collectAsStateWithLifecycle()
    val naiSeed by viewModel.naiSeed.collectAsStateWithLifecycle()
    val naiScale by viewModel.naiScale.collectAsStateWithLifecycle()
    val naiGenerating by viewModel.naiGenerating.collectAsStateWithLifecycle()
    val naiGeneratedBitmap by viewModel.naiGeneratedBitmap.collectAsStateWithLifecycle()
    val naiError by viewModel.naiError.collectAsStateWithLifecycle()

    var showInfoDialog by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }
    var showFavoriteMixesDialog by remember { mutableStateOf(false) }
    var showThemeSelectorDialog by remember { mutableStateOf(false) }
    var showMatchArtistDialog by remember { mutableStateOf(false) }
    var showArtistTagSourceDialog by remember { mutableStateOf(false) }
    var showFormattingSettingsDialog by remember { mutableStateOf(false) }
    var previewFavoriteIndex by remember { mutableIntStateOf(-1) }

    var showNovelAiGenDialog by remember { mutableStateOf(false) }
    var showNovelAiSettingsDialog by remember { mutableStateOf(false) }
    var novelAiTargetArtistTags by remember { mutableStateOf("") }
    var overlayMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(overlayMessage) {
        if (overlayMessage != null) {
            kotlinx.coroutines.delay(2200)
            overlayMessage = null
        }
    }

    val lockedCount = currentResult?.selectedPictures?.count { it.isLocked } ?: 0
    val canDecrement = requestedCount > 1 && requestedCount > lockedCount

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            viewModel.loadFromTreeUri(context, uri)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.loadFromMultipleUris(context, uris)
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                val jsonString = viewModel.exportDataJson()
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(jsonString.toByteArray(Charsets.UTF_8))
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { isStream ->
                    val jsonString = isStream.bufferedReader().readText()
                    viewModel.importDataFromJson(jsonString)
                }
            }
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()

    CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                    ModalDrawerSheet(
                        modifier = Modifier.width(280.dp),
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                        drawerShape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                    ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = com.example.R.drawable.ic_app_logo),
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color.Unspecified,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Options Menu",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                // 1. Match your artist
                NavigationDrawerItem(
                    label = { Text("Match your artist", fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            painter = painterResource(id = com.example.R.drawable.ic_match_artist),
                            contentDescription = "Match your artist",
                            tint = androidx.compose.ui.graphics.Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    selected = false,
                    onClick = {
                        viewModel.dismissFirstStartPrompt()
                        showMatchArtistDialog = true
                        drawerScope.launch { drawerState.close() }
                    },
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .testTag("drawer_match_artist")
                )

                // 2. Try mix (NovelAI Generator)
                NavigationDrawerItem(
                    label = { Text("Try mix", fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Try mix",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    selected = false,
                    onClick = {
                        viewModel.dismissFirstStartPrompt()
                        val activeTags = currentResult?.formattedString ?: ""
                        novelAiTargetArtistTags = activeTags
                        showNovelAiGenDialog = true
                        drawerScope.launch { drawerState.close() }
                    },
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .testTag("drawer_try_mix")
                )

                // 3. Favorite artists
                NavigationDrawerItem(
                    label = { Text("Favorite artists (${favoriteArtistNames.size})", fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            imageVector = if (favoriteArtistNames.isNotEmpty()) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite artists",
                            tint = if (favoriteArtistNames.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    selected = false,
                    onClick = {
                        viewModel.dismissFirstStartPrompt()
                        showFavoritesDialog = true
                        drawerScope.launch { drawerState.close() }
                    },
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .testTag("drawer_favorites")
                )

                // 4. Favorite mixes
                NavigationDrawerItem(
                    label = { Text("Favorite mixes (${favoriteMixes.size})", fontWeight = FontWeight.SemiBold) },
                    icon = {
                        MultipleHeartsIcon(
                            tint = if (favoriteMixes.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    selected = false,
                    onClick = {
                        viewModel.dismissFirstStartPrompt()
                        showFavoriteMixesDialog = true
                        drawerScope.launch { drawerState.close() }
                    },
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .testTag("drawer_favorite_mixes")
                )

                // 5. Artist tag source
                NavigationDrawerItem(
                    label = { Text("Artist tag source", fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Artist tag source",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    selected = false,
                    onClick = {
                        viewModel.dismissFirstStartPrompt()
                        showArtistTagSourceDialog = true
                        drawerScope.launch { drawerState.close() }
                    },
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .testTag("drawer_artist_tag_source")
                )

                // 6. Formatting settings
                NavigationDrawerItem(
                    label = { Text("Formatting settings", fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Formatting settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    selected = false,
                    onClick = {
                        viewModel.dismissFirstStartPrompt()
                        showFormattingSettingsDialog = true
                        drawerScope.launch { drawerState.close() }
                    },
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .testTag("drawer_formatting_settings")
                )

                // 7. Theme selector
                NavigationDrawerItem(
                    label = { Text("Theme", fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Theme selector",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    selected = false,
                    onClick = {
                        viewModel.dismissFirstStartPrompt()
                        showThemeSelectorDialog = true
                        drawerScope.launch { drawerState.close() }
                    },
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .testTag("drawer_theme_selector")
                )

                // 8. About app
                NavigationDrawerItem(
                    label = { Text("About app", fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About app",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    selected = false,
                    onClick = {
                        viewModel.dismissFirstStartPrompt()
                        showInfoDialog = true
                        drawerScope.launch { drawerState.close() }
                    },
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .testTag("drawer_about_app")
                )
                    }
                }
            }
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Scaffold(
            modifier = modifier
                .fillMaxSize()
                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            topBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            painter = painterResource(id = com.example.R.drawable.ic_app_logo),
                                            contentDescription = null,
                                            tint = androidx.compose.ui.graphics.Color.Unspecified,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(28.dp)
                                        .background(MaterialTheme.colorScheme.outline)
                                )
                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "NAI Artist Mixer",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Artist Mixing tool for NovelAI",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(28.dp)
                                        .background(MaterialTheme.colorScheme.outline)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { drawerScope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("hamburger_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Open Options Panel"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                }
            },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                .verticalScroll(scrollState)
                .mouseWheelScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // 1. FORMATTED RESULT CARD
            ResultCard(
                result = currentResult,
                onPickAgain = { viewModel.pickRandomPictures() },
                onCopy = { viewModel.copyResultToClipboard(context, it) },
                onTryNovelAi = { artistTags ->
                    novelAiTargetArtistTags = artistTags
                    showNovelAiGenDialog = true
                },
                onEditGenSettings = {
                    showNovelAiSettingsDialog = true
                }
            )

            // 2. STANDALONE NUMBERS OF ARTISTS SETTING
            NumbersOfArtistsCard(
                requestedCount = requestedCount,
                totalAvailable = availablePictures.size,
                canDecrement = canDecrement,
                onCountChange = { viewModel.setRequestedCount(it) }
            )

            // 3. SELECTED ARTISTS VISUAL GRID
            PictureGrid(
                pictures = currentResult?.selectedPictures ?: emptyList(),
                availablePictures = availablePictures,
                favoriteArtistNames = favoriteArtistNames,
                viewModel = viewModel,
                onRerollItem = { viewModel.rerollSinglePicture(it) },
                onToggleLock = { viewModel.toggleLockPicture(it) },
                onToggleFavorite = { viewModel.toggleFavoriteArtist(it) },
                onRemoveItem = { index ->
                    val newAmount = viewModel.removePictureAt(index)
                    overlayMessage = "Number of artists decreased to $newAmount"
                },
                onAddManualPicture = { viewModel.addManualPicture(it) },
                onMoveItem = { from, to -> viewModel.movePicture(from, to) }
            )

            Spacer(modifier = Modifier.height(24.dp))


    // Top-Level Overlay Message Badge floating over all content and dialogs
    if (overlayMessage != null) {
        androidx.compose.ui.window.Popup(
            alignment = Alignment.TopCenter,
            properties = androidx.compose.ui.window.PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 12.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    text = overlayMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }

    // About App Dialog
    if (showInfoDialog) {
        AboutAppDialog(
            viewModel = viewModel,
            onExportData = {
                val jsonStr = viewModel.exportDataJson()
                viewModel.copyResultToClipboard(context, jsonStr)
                exportLauncher.launch("nai_artist_mixer_backup.json")
            },
            onImportData = {
                importLauncher.launch("application/json")
            },
            onDismiss = { showInfoDialog = false }
        )
    }

    // Artist Tag Source Submenu Dialog
    if (showArtistTagSourceDialog) {
        ArtistTagSourceDialog(
            currentFolderName = folderName,
            totalCount = availablePictures.size,
            onSelectTreeUri = { viewModel.loadFromTreeUri(context, it) },
            onSelectMultipleUris = { viewModel.loadFromMultipleUris(context, it) },
            onSelectNaxMoeLooseGallery = { viewModel.loadNaxMoeLooseGallery() },
            onSelectNaxMoeConstrainedGallery = { viewModel.loadNaxMoeConstrainedGallery() },
            onDismiss = { showArtistTagSourceDialog = false }
        )
    }

    // Formatting Settings Submenu Dialog
    if (showFormattingSettingsDialog) {
        val isNaxMoe = folderName.contains("nax.moe", ignoreCase = true) || 
                       folderName.contains("danbooru", ignoreCase = true) || 
                       folderName.contains("prompt", ignoreCase = true)
        FormattingSettingsDialog(
            requestedCount = requestedCount,
            totalAvailable = availablePictures.size,
            allowDuplicates = allowDuplicates,
            formattingOptions = formattingOptions,
            isNaxMoe = isNaxMoe,
            canDecrement = canDecrement,
            onCountChange = { viewModel.setRequestedCount(it) },
            onAllowDuplicatesChange = { viewModel.setAllowDuplicates(it) },
            onFormattingOptionsChange = { viewModel.updateFormattingOptions(it) },
            onDismiss = { showFormattingSettingsDialog = false }
        )
    }

    // Theme Selector Submenu Dialog
    if (showThemeSelectorDialog) {
        val headingsFont by viewModel.headingsFont.collectAsState()
        val defaultFont by viewModel.defaultFont.collectAsState()
        ThemeSelectorDialog(
            currentTheme = appThemePreset,
            headerColorArgb = headerColorArgb,
            paragraphColorArgb = paragraphColorArgb,
            foregroundColorArgb = foregroundColorArgb,
            backgroundColorArgb = backgroundColorArgb,
            darkBgColorArgb = darkBgColorArgb,
            inputBgColorArgb = inputBgColorArgb,
            headingsFont = headingsFont,
            defaultFont = defaultFont,
            onSelectPreset = { viewModel.setAppThemePreset(it) },
            onUpdateThemeColor = { h, p, fg, bg, dbg, ibg ->
                viewModel.updateThemeColor(h, p, 0xFFFF7878L, fg, bg, dbg, ibg)
            },
            onUpdateThemeFonts = { hFont, dFont ->
                viewModel.updateThemeFonts(hFont, dFont)
            },
            onDismiss = { showThemeSelectorDialog = false }
        )
    }

    // Match your Artist Tinder-style Submenu Dialog
    if (showMatchArtistDialog) {
        MatchArtistDialog(
            viewModel = viewModel,
            onDismiss = { showMatchArtistDialog = false }
        )
    }

    // NovelAI Image Generation Dialog ("Try Mix")
    if (showNovelAiGenDialog) {
        val activeTags = currentResult?.formattedString ?: novelAiTargetArtistTags
        NovelAiGenerationDialog(
            artistTags = activeTags,
            apiKey = naiApiKey,
            width = naiWidth,
            height = naiHeight,
            initialSeed = naiSeed,
            isGenerating = naiGenerating,
            generatedBitmap = naiGeneratedBitmap,
            errorMessage = naiError,
            viewModel = viewModel,
            onShowOverlayMessage = { msg -> overlayMessage = msg },
            onDismiss = {
                showNovelAiGenDialog = false
            },
            onOpenSettings = {
                showNovelAiSettingsDialog = true
            },
            onGenerate = { tags, overrideSeed ->
                viewModel.generateNovelAiImage(artistTags = tags, overrideSeed = overrideSeed)
            },
            onSaveMix = { _, _, _ ->
                // NovelAiGenerationDialog directly calls viewModel.saveFavoriteMix to prevent double saving
            },
            onRerollMix = {
                viewModel.pickRandomPictures()
                val newTags = viewModel.currentResult.value?.formattedString ?: activeTags
                viewModel.generateNovelAiImage(artistTags = newTags)
            },
            onClearState = {
                viewModel.clearNaiGenerationState()
            }
        )
    }

    // NovelAI Settings Dialog ("Edit Gen Settings")
    if (showNovelAiSettingsDialog) {
        NovelAiSettingsDialog(
            apiKey = naiApiKey,
            mainPrompt = naiMainPrompt,
            ucPrompt = naiUc,
            characterPrompt = naiCharacter,
            model = naiModel,
            width = naiWidth,
            height = naiHeight,
            seed = naiSeed,
            scale = naiScale,
            onDismiss = { showNovelAiSettingsDialog = false },
            onUpdateApiKey = { viewModel.updateNaiApiKey(it) },
            onUpdateMainPrompt = { viewModel.updateNaiMainPrompt(it) },
            onUpdateUcPrompt = { viewModel.updateNaiUc(it) },
            onUpdateCharacterPrompt = { viewModel.updateNaiCharacter(it) },
            onUpdateModel = { viewModel.updateNaiModel(it) },
            onUpdateResolution = { w, h -> viewModel.updateNaiResolution(w, h) },
            onUpdateSeed = { viewModel.updateNaiSeed(it) },
            onUpdateScale = { viewModel.updateNaiScale(it) },
            onResetDefaults = { viewModel.resetNaiSettingsToDefault() },
            onTryMix = {
                showNovelAiSettingsDialog = false
                val activeTags = currentResult?.formattedString ?: ""
                novelAiTargetArtistTags = activeTags
                showNovelAiGenDialog = true
            }
        )
    }

    // Favorite Mixes Submenu Dialog
    if (showFavoriteMixesDialog) {
        FavoriteMixesDialog(
            favoriteMixes = favoriteMixes,
            formattingOptions = formattingOptions,
            viewModel = viewModel,
            onSelectMix = { mix ->
                viewModel.restoreFavoriteMix(mix)
            },
            onDeleteMix = { mixId ->
                viewModel.removeFavoriteMix(mixId)
            },
            onRenameMix = { mixId, newTitle ->
                viewModel.updateFavoriteMixTitle(mixId, newTitle)
            },
            onDismiss = { showFavoriteMixesDialog = false }
        )
    }

    // Favorite Artists Submenu Dialog
    val sortedFavorites = remember(favoriteArtistNames) { favoriteArtistNames.toList().sorted() }
    val artistUserTags by viewModel.artistUserTags.collectAsStateWithLifecycle()
    val artistToTags by viewModel.artistToTags.collectAsStateWithLifecycle()
    var selectedArtistFilterTag by remember { mutableStateOf<String?>(null) }
    var showArtistFilterDialog by remember { mutableStateOf(false) }
    var tagEditArtistName by remember { mutableStateOf<String?>(null) }

    val filteredFavorites = remember(sortedFavorites, selectedArtistFilterTag, artistToTags) {
        if (selectedArtistFilterTag == null) {
            sortedFavorites
        } else {
            sortedFavorites.filter { artistName ->
                artistToTags[artistName]?.contains(selectedArtistFilterTag) == true
            }
        }
    }

    if (previewFavoriteIndex in sortedFavorites.indices) {
        FavoritePreviewDialog(
            favoritesList = sortedFavorites,
            initialIndex = previewFavoriteIndex,
            availablePictures = availablePictures,
            viewModel = viewModel,
            onDismiss = { previewFavoriteIndex = -1 }
        )
    }

    if (showFavoritesDialog) {
        val closeFavoritesDialog = {
            selectedArtistFilterTag = null
            showFavoritesDialog = false
        }

        Dialog(onDismissRequest = closeFavoritesDialog) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "FAVORITE ARTISTS (${favoriteArtistNames.size})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Auto-Tag Button
                                IconButton(onClick = { viewModel.autoTagFavoriteArtists() }) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Auto-Tag Artists",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Filter by Tag Button
                                IconButton(onClick = {
                                    if (selectedArtistFilterTag != null) {
                                        selectedArtistFilterTag = null
                                    } else {
                                        showArtistFilterDialog = true
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Label,
                                        contentDescription = "Filter by Tag",
                                        tint = if (selectedArtistFilterTag != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                IconButton(onClick = closeFavoritesDialog) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Close")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedArtistFilterTag != null) "Filtered by tag: $selectedArtistFilterTag" else "Tap a picture to preview or swipe. Tap name to add to selection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedArtistFilterTag != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        if (filteredFavorites.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (selectedArtistFilterTag != null) "No artists found with tag '$selectedArtistFilterTag'." else "No favorite artists added yet.\nTap the heart icon on any picture or use Match your Artist to add!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(filteredFavorites) { index, artistName ->
                                    val matchingPic = availablePictures.firstOrNull {
                                        it.fileName.equals(artistName, ignoreCase = true)
                                    }
                                    val isPresent = matchingPic != null
                                    val artistTags = artistToTags[artistName] ?: emptySet()

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isPresent)
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            // Left Thumbnail / Icon
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                    .then(
                                                        if (isPresent) Modifier.clickable {
                                                            previewFavoriteIndex = sortedFavorites.indexOf(artistName)
                                                        } else Modifier
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isPresent && matchingPic != null) {
                                                    if (matchingPic.drawableResId != null) {
                                                        Image(
                                                            painter = painterResource(id = matchingPic.drawableResId),
                                                            contentDescription = artistName,
                                                            contentScale = ContentScale.Crop,
                                                            alignment = Alignment.TopCenter,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    } else if (matchingPic.uriString.isNotBlank()) {
                                                        SubcomposeAsyncImage(
                                                            model = ImageRequest.Builder(context)
                                                                .data(matchingPic.uriString)
                                                                .crossfade(true)
                                                                .build(),
                                                            contentDescription = artistName,
                                                            contentScale = ContentScale.Crop,
                                                            alignment = Alignment.TopCenter,
                                                            loading = {
                                                                CircularProgressIndicator(
                                                                    modifier = Modifier.size(16.dp),
                                                                    strokeWidth = 2.dp
                                                                )
                                                            },
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.BrokenImage,
                                                        contentDescription = "Absent",
                                                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            // Artist Name & Status
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable(enabled = isPresent) {
                                                        viewModel.addFavoriteToSelection(artistName)
                                                    }
                                            ) {
                                                Text(
                                                    text = artistName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    color = if (isPresent)
                                                        MaterialTheme.colorScheme.onSurface
                                                    else
                                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                if (artistTags.isNotEmpty()) {
                                                    Text(
                                                        text = "Tags: ${artistTags.joinToString(", ")}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                        color = MaterialTheme.colorScheme.primary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                if (!isPresent) {
                                                    Text(
                                                        text = "artist absent from database",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            // Action buttons: Tag & Delete
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                IconButton(
                                                    onClick = { tagEditArtistName = artistName },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Label,
                                                        contentDescription = "Edit Tags",
                                                        tint = if (artistTags.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { viewModel.toggleFavoriteArtist(artistName) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Remove favorite",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showArtistFilterDialog) {
        val artistItemCounts = remember(artistUserTags, artistToTags, favoriteArtistNames) {
            artistUserTags.associateWith { tag ->
                favoriteArtistNames.count { name ->
                    artistToTags[name]?.contains(tag) == true
                }
            }
        }

        TagManagementDialog(
            title = "Filter Favorite Artists by Tag",
            allTags = artistUserTags,
            initialSelectedTags = if (selectedArtistFilterTag != null) setOf(selectedArtistFilterTag!!) else emptySet(),
            itemCounts = artistItemCounts,
            confirmButtonText = "Apply Tag Filter",
            itemTypeName = "favorite artists",
            onCreateTag = { viewModel.createArtistTag(it) },
            onDeleteTag = { viewModel.deleteArtistTag(it) },
            onRenameTag = { oldTag, newTag -> viewModel.renameArtistTag(oldTag, newTag) },
            onSelectFilterTag = { selectedTag ->
                selectedArtistFilterTag = selectedTag
            },
            onConfirm = { selectedTags ->
                showArtistFilterDialog = false
                selectedArtistFilterTag = selectedTags.firstOrNull()
            },
            onDismiss = { showArtistFilterDialog = false }
        )
    }

    tagEditArtistName?.let { artistName ->
        val artistItemCounts = remember(artistUserTags, artistToTags, favoriteArtistNames) {
            artistUserTags.associateWith { tag ->
                favoriteArtistNames.count { name ->
                    artistToTags[name]?.contains(tag) == true
                }
            }
        }

        TagManagementDialog(
            title = "Tag Artist: $artistName",
            allTags = artistUserTags,
            initialSelectedTags = artistToTags[artistName] ?: emptySet(),
            itemCounts = artistItemCounts,
            confirmButtonText = "Save Tags",
            itemTypeName = "favorite artists",
            onCreateTag = { viewModel.createArtistTag(it) },
            onDeleteTag = { viewModel.deleteArtistTag(it) },
            onRenameTag = { oldTag, newTag -> viewModel.renameArtistTag(oldTag, newTag) },
            onConfirm = { selectedTags ->
                viewModel.setArtistTags(artistName, selectedTags)
                tagEditArtistName = null
            },
            onDismiss = { tagEditArtistName = null }
        )
    }

    // Prompt for Image Folder on First Startup
    if (showFirstStartPrompt && availablePictures.isEmpty()) {
        Dialog(onDismissRequest = { viewModel.dismissFirstStartPrompt() }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Select artist tag source",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "To start picking random artists, select a danbooru database (nax.moe), or locale data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            viewModel.dismissFirstStartPrompt()
                            viewModel.loadNaxMoeLooseGallery()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Loose Prompt (nax.moe)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.dismissFirstStartPrompt()
                            viewModel.loadNaxMoeConstrainedGallery()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Constrained Prompt (nax.moe)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.dismissFirstStartPrompt()
                            folderPickerLauncher.launch(null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Folder", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.dismissFirstStartPrompt()
                            filePickerLauncher.launch(arrayOf("image/*"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Specific Photos", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
}
}
}
}
}
