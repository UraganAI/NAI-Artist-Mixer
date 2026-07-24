package com.example.ui.components

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.filled.Sync
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.DbVersionCheckResult
import com.example.viewmodel.AlternateDbOption
import com.example.util.DanbooruFetcher
import kotlinx.coroutines.launch
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.zIndex
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.model.PictureItem
import kotlin.math.roundToInt

@Composable
fun PictureGrid(
    pictures: List<PictureItem>,
    availablePictures: List<PictureItem> = emptyList(),
    favoriteArtistNames: Set<String> = emptySet(),
    viewModel: MainViewModel? = null,
    onRerollItem: (Int) -> Unit = {},
    onToggleLock: (Int) -> Unit = {},
    onToggleFavorite: (String) -> Unit = {},
    onAddManualPicture: (PictureItem) -> Unit = {},
    onMoveItem: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var previewIndex by remember { mutableIntStateOf(-1) }

    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }

    val filteredAvailable = remember(searchQuery, availablePictures) {
        if (searchQuery.trim().length >= 2) {
            availablePictures.filter {
                it.fileName.contains(searchQuery.trim(), ignoreCase = true)
            }.take(10)
        } else {
            emptyList()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SELECTED ARTISTS (${pictures.size})",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tap to view • Hold & drag to move",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Search Bar for Manually Adding Pictures / Artists
        if (availablePictures.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        showSearchResults = it.trim().length >= 2
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("picture_search_input"),
                    placeholder = {
                        Text(
                            "Search an artist...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                showSearchResults = false
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                // Non-focusable search popup overlay so typing isn't interrupted
                if (showSearchResults && filteredAvailable.isNotEmpty()) {
                    Popup(
                        alignment = Alignment.TopStart,
                        offset = IntOffset(0, 140),
                        properties = PopupProperties(focusable = false),
                        onDismissRequest = { showSearchResults = false }
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .heightIn(max = 240.dp),
                            shape = RoundedCornerShape(16.dp),
                            tonalElevation = 8.dp,
                            shadowElevation = 8.dp,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
                                items(filteredAvailable) { picture ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onAddManualPicture(picture)
                                                searchQuery = ""
                                                showSearchResults = false
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = picture.fileName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add artist",
                                            tint = MaterialTheme.colorScheme.primary,
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

        // Container with Light Outline
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(390.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            if (pictures.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No artists selected yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 96.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .testTag("selected_pictures_grid"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    itemsIndexed(pictures, key = { index, picture -> "${index}_${picture.id}" }) { index, picture ->
                        val isFav = favoriteArtistNames.contains(picture.fileName)
                        PictureGridCard(
                            picture = picture,
                            index = index + 1,
                            totalCount = pictures.size,
                            isFavorite = isFav,
                            onClick = {
                                previewIndex = index
                            },
                            onMoveLeft = {
                                if (index > 0) {
                                    onMoveItem(index, index - 1)
                                }
                            },
                            onMoveRight = {
                                if (index < pictures.size - 1) {
                                    onMoveItem(index, index + 1)
                                }
                            },
                            onReroll = { onRerollItem(index) },
                            onToggleLock = { onToggleLock(index) },
                            onToggleFavorite = { onToggleFavorite(picture.fileName) },
                            onMoveItem = onMoveItem
                        )
                    }
                }
            }
        }
    }

    // Lightbox Dialog Preview
    if (previewIndex in pictures.indices) {
        ImageLightboxDialog(
            pictures = pictures,
            initialIndex = previewIndex,
            favoriteArtistNames = favoriteArtistNames,
            viewModel = viewModel,
            onDismiss = { previewIndex = -1 },
            onRerollItem = { idx -> onRerollItem(idx) },
            onToggleLock = { idx -> onToggleLock(idx) },
            onToggleFavorite = { name -> onToggleFavorite(name) }
        )
    }
}

@Composable
fun PictureGridCard(
    picture: PictureItem,
    index: Int,
    totalCount: Int,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onReroll: () -> Unit,
    onToggleLock: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMoveItem: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var isDragging by remember { mutableStateOf(false) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val currentIndexState by rememberUpdatedState(index)
    val totalCountState by rememberUpdatedState(totalCount)
    val currentOnMoveItem by rememberUpdatedState(onMoveItem)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.88f)
            .zIndex(if (isDragging) 100f else 0f)
            .graphicsLayer {
                translationX = dragOffsetX
                translationY = dragOffsetY
                scaleX = if (isDragging) 1.06f else 1f
                scaleY = if (isDragging) 1.06f else 1f
            }
            .clip(RoundedCornerShape(14.dp))
            .pointerInput(picture.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        isDragging = true
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                    },
                    onDragEnd = {
                        isDragging = false
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                    },
                    onDragCancel = {
                        isDragging = false
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x
                        dragOffsetY += dragAmount.y

                        val currentZeroBased = currentIndexState - 1
                        val total = totalCountState
                        val threshold = 220f

                        if (dragOffsetX > threshold && currentZeroBased < total - 1) {
                            currentOnMoveItem.invoke(currentZeroBased, currentZeroBased + 1)
                            dragOffsetX = 0f
                            dragOffsetY = 0f
                        } else if (dragOffsetX < -threshold && currentZeroBased > 0) {
                            currentOnMoveItem.invoke(currentZeroBased, currentZeroBased - 1)
                            dragOffsetX = 0f
                            dragOffsetY = 0f
                        } else if (dragOffsetY > threshold + 30f && currentZeroBased + 2 < total) {
                            val target = (currentZeroBased + 3).coerceAtMost(total - 1)
                            currentOnMoveItem.invoke(currentZeroBased, target)
                            dragOffsetX = 0f
                            dragOffsetY = 0f
                        } else if (dragOffsetY < -threshold - 30f && currentZeroBased - 2 >= 0) {
                            val target = (currentZeroBased - 3).coerceAtLeast(0)
                            currentOnMoveItem.invoke(currentZeroBased, target)
                            dragOffsetX = 0f
                            dragOffsetY = 0f
                        }
                    }
                )
            }
            .pointerInput(picture.id) {
                detectTapGestures(
                    onTap = { onClick() }
                )
            }
            .testTag("picture_card_$index"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
            else if (picture.isLocked)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            if (isDragging) 2.5.dp else if (picture.isLocked) 2.dp else 1.dp,
            if (isDragging) MaterialTheme.colorScheme.primary else if (picture.isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image Content
            if (picture.drawableResId != null) {
                Image(
                    painter = painterResource(id = picture.drawableResId),
                    contentDescription = picture.fileName,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (picture.uriString.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(picture.uriString)
                        .crossfade(true)
                        .build(),
                    contentDescription = picture.fileName,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BrokenImage,
                                contentDescription = "Failed to load",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Top-Left Reroll Control Button
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(5.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.7f),
                contentColor = Color.White
            ) {
                IconButton(
                    onClick = onReroll,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = "Reroll picture",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }

            // Top-Right Controls (Lock and Favorite)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Lock / Unlock Button
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.78f),
                    contentColor = Color.White
                ) {
                    IconButton(
                        onClick = onToggleLock,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (picture.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = if (picture.isLocked) "Unlock picture" else "Lock picture",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }

                // Favorite Button (Heart)
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.78f),
                    contentColor = if (isFavorite) Color.Red else Color.White
                ) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove favorite" else "Add favorite",
                            modifier = Modifier.size(16.dp),
                            tint = if (isFavorite) Color.Red else Color.White
                        )
                    }
                }
            }

            // Bottom Filename Pill Badge - Always dark semi-transparent for maximum readability!
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.78f)
            ) {
                Text(
                    text = picture.fileName,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ImageLightboxDialog(
    pictures: List<PictureItem>,
    initialIndex: Int,
    favoriteArtistNames: Set<String>,
    viewModel: MainViewModel? = null,
    onDismiss: () -> Unit,
    onRerollItem: (Int) -> Unit,
    onToggleLock: (Int) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    val context = LocalContext.current

    if (pictures.isEmpty() || initialIndex !in pictures.indices) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(pictures.indices),
        pageCount = { pictures.size }
    )

    var randomArtArtistName by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color = Color.Black.copy(alpha = 0.92f),
            shape = RoundedCornerShape(24.dp)
        ) {
            val currentPicture = pictures[pagerState.currentPage.coerceIn(pictures.indices)]
            val isFav = favoriteArtistNames.contains(currentPicture.fileName)

            var overridePicture by remember(currentPicture.fileName) { mutableStateOf<PictureItem?>(null) }
            var overrideLabel by remember(currentPicture.fileName) { mutableStateOf<String?>(null) }
            var checkResult by remember(currentPicture.fileName) { mutableStateOf<com.example.viewmodel.DbVersionCheckResult?>(null) }
            var isCheckingDb by remember(currentPicture.fileName) { mutableStateOf(true) }
            var currentOptionIndex by remember(currentPicture.fileName) { mutableIntStateOf(-1) }

            LaunchedEffect(currentPicture.fileName) {
                if (viewModel != null) {
                    isCheckingDb = true
                    currentOptionIndex = -1
                    overridePicture = null
                    overrideLabel = null
                    checkResult = viewModel.checkDatabaseVersions(currentPicture.fileName)
                    isCheckingDb = false
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // HorizontalPager for images only
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val pic = pictures[page]
                    val displayPic = if (page == pagerState.currentPage) (overridePicture ?: pic) else pic

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 70.dp, bottom = 160.dp, start = 16.dp, end = 16.dp)
                    ) {
                        ZoomableBox(modifier = Modifier.fillMaxSize()) {
                            if (displayPic.drawableResId != null) {
                                Image(
                                    painter = painterResource(id = displayPic.drawableResId),
                                    contentDescription = displayPic.fileName,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else if (displayPic.uriString.isNotBlank()) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(displayPic.uriString)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = displayPic.fileName,
                                    contentScale = ContentScale.Fit,
                                    loading = {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "#${pagerState.currentPage + 1}/${pictures.size}: ${currentPicture.fileName}" + if (currentPicture.isLocked) " 🔒" else "",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (overrideLabel != null) {
                            Text(
                                text = "Previewing: $overrideLabel",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Bottom Action Buttons: Row 1 (Lock, Fav, Re-roll), Row 2 (Switch DB), Row 3 (Danbooru, Random Art)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 8.dp, end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Lock | Favorite | Re-roll
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onToggleLock(pagerState.currentPage) },
                            shape = RoundedCornerShape(50),
                            colors = if (currentPicture.isLocked) {
                                ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935), contentColor = Color.White)
                            } else {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(
                                imageVector = if (currentPicture.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (currentPicture.isLocked) Color.White else MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentPicture.isLocked) "Unlock" else "Lock",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentPicture.isLocked) Color.White else MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Button(
                            onClick = { onToggleFavorite(currentPicture.fileName) },
                            shape = RoundedCornerShape(50),
                            colors = if (isFav) ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White) else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isFav) Color.White else MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFav) "Fav" else "Favorite",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFav) Color.White else MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Button(
                            onClick = { onRerollItem(pagerState.currentPage) },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Re-roll",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    // Row 2: Switch database button (Middle Row)
                    val options = checkResult?.options ?: emptyList()
                    val (oppEnabled, oppText) = when {
                        isCheckingDb -> Pair(false, "Checking DB...")
                        options.isEmpty() -> Pair(false, "Absent in ${checkResult?.defaultOppositeName ?: "opposite"} DB")
                        currentOptionIndex == -1 -> Pair(true, "Check ${options[0].dbName}")
                        currentOptionIndex + 1 < options.size -> Pair(true, "Check ${options[currentOptionIndex + 1].dbName}")
                        else -> Pair(true, "Return to selected database")
                    }

                    OutlinedButton(
                        onClick = {
                            if (options.isNotEmpty()) {
                                val nextIdx = currentOptionIndex + 1
                                if (nextIdx < options.size) {
                                    currentOptionIndex = nextIdx
                                    overridePicture = options[nextIdx].matchPic
                                    overrideLabel = options[nextIdx].dbName
                                } else {
                                    currentOptionIndex = -1
                                    overridePicture = null
                                    overrideLabel = null
                                }
                            }
                        },
                        enabled = oppEnabled,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = if (oppEnabled) Color.White else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = oppText,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (oppEnabled) Color.White else Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Row 3: Danbooru page (left) | Random Art (NSFW?) (right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Danbooru Page Button (Left)
                        OutlinedButton(
                            onClick = {
                                val tag = Uri.encode(currentPicture.fileName.trim().replace(" ", "_"))
                                val url = "https://danbooru.donmai.us/posts?tags=$tag"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = com.example.R.drawable.ic_danbooru),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Danbooru page",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Random Art Button (Right)
                        OutlinedButton(
                            onClick = { randomArtArtistName = currentPicture.fileName },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Random Art (NSFW?)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Random Art Popup Dialog
            randomArtArtistName?.let { artistName ->
                RandomArtDialog(
                    artistName = artistName,
                    onDismiss = { randomArtArtistName = null }
                )
            }
        }
    }
}

