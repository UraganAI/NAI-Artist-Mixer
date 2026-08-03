package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.PictureItem
import com.example.viewmodel.DbVersionCheckResult
import com.example.viewmodel.AlternateDbOption
import com.example.ui.theme.NovelAiOnPrimary
import com.example.ui.theme.NovelAiPrimary
import com.example.viewmodel.MainViewModel

@Composable
fun FavoritePreviewDialog(
    favoritesList: List<String>,
    initialIndex: Int,
    availablePictures: List<PictureItem>,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    if (favoritesList.isEmpty() || initialIndex !in favoritesList.indices) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(favoritesList.indices),
        pageCount = { favoritesList.size }
    )

    val favoriteSet by viewModel.favoriteArtistNames.collectAsState()
    var randomArtArtistName by remember { mutableStateOf<String?>(null) }
    var showArtistTagDialog by remember { mutableStateOf(false) }

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
            val currentArtistName = favoritesList[pagerState.currentPage.coerceIn(favoritesList.indices)]
            val isFavorite = favoriteSet.contains(currentArtistName)

            val matchingPic = remember(currentArtistName, availablePictures) {
                availablePictures.firstOrNull { it.fileName.equals(currentArtistName, ignoreCase = true) }
            }

            var overridePicture by remember(currentArtistName) { mutableStateOf<PictureItem?>(null) }
            var overrideLabel by remember(currentArtistName) { mutableStateOf<String?>(null) }
            var checkResult by remember(currentArtistName) { mutableStateOf<com.example.viewmodel.DbVersionCheckResult?>(null) }
            var isCheckingDb by remember(currentArtistName) { mutableStateOf(true) }
            var currentOptionIndex by remember(currentArtistName) { mutableIntStateOf(-1) }

            LaunchedEffect(currentArtistName) {
                isCheckingDb = true
                currentOptionIndex = -1
                overridePicture = null
                overrideLabel = null
                checkResult = viewModel.checkDatabaseVersions(currentArtistName)
                isCheckingDb = false
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // HorizontalPager for images only
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val artistName = favoritesList[page]
                    val pic = availablePictures.firstOrNull { it.fileName.equals(artistName, ignoreCase = true) }
                    val displayPic = if (page == pagerState.currentPage) (overridePicture ?: pic) else pic

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 70.dp, bottom = 140.dp, start = 16.dp, end = 16.dp)
                    ) {
                        ZoomableBox(modifier = Modifier.fillMaxSize()) {
                            if (displayPic != null) {
                                if (displayPic.drawableResId != null) {
                                    Image(
                                        painter = painterResource(id = displayPic.drawableResId),
                                        contentDescription = artistName,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else if (displayPic.uriString.isNotBlank()) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(displayPic.uriString)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = artistName,
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
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.BrokenImage,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Image absent from current database",
                                            color = Color.White.copy(alpha = 0.7f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
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
                            text = "#${pagerState.currentPage + 1}/${favoritesList.size}: $currentArtistName",
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

                // Bottom Action Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 12.dp, end = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Upper Row: Favorite and Add (colored NovelAI yellow)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Split Favorite / Tag Double Button
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = NovelAiPrimary,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left half: Favorite
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable {
                                            viewModel.toggleFavoriteArtist(currentArtistName)
                                        },
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = NovelAiOnPrimary
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Favorite", fontSize = 11.sp, color = NovelAiOnPrimary, fontWeight = FontWeight.Bold)
                                }

                                // Divider
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .fillMaxHeight(0.6f)
                                        .background(NovelAiOnPrimary.copy(alpha = 0.4f))
                                )

                                // Right half: Tag
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable {
                                            showArtistTagDialog = true
                                        },
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Label,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = NovelAiOnPrimary
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Tag", fontSize = 11.sp, color = NovelAiOnPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.addFavoriteToSelection(currentArtistName)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NovelAiPrimary,
                                contentColor = NovelAiOnPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = NovelAiOnPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", fontSize = 12.sp, color = NovelAiOnPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Middle Row: Switch database (full width row of its own)
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
                            .height(40.dp)
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lower Row: Danbooru Page (left) and Random Art (right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Danbooru Page Button
                        OutlinedButton(
                            onClick = {
                                val tag = Uri.encode(currentArtistName.trim().lowercase().replace(" ", "_"))
                                val url = "https://danbooru.donmai.us/posts?tags=$tag"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_danbooru),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Danbooru page",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Random Art Button
                        OutlinedButton(
                            onClick = { randomArtArtistName = currentArtistName },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
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
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
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

            if (showArtistTagDialog) {
                val currentArtistName = favoritesList[pagerState.currentPage.coerceIn(favoritesList.indices)]
                val artistUserTags by viewModel.artistUserTags.collectAsStateWithLifecycle()
                val artistToTags by viewModel.artistToTags.collectAsStateWithLifecycle()
                val favoriteArtistNames by viewModel.favoriteArtistNames.collectAsStateWithLifecycle()

                val artistItemCounts = remember(artistUserTags, artistToTags, favoriteArtistNames) {
                    artistUserTags.associateWith { tag ->
                        favoriteArtistNames.count { name ->
                            artistToTags[name]?.contains(tag) == true
                        }
                    }
                }

                TagManagementDialog(
                    title = "Tag Artist",
                    allTags = artistUserTags,
                    initialSelectedTags = artistToTags[currentArtistName] ?: emptySet(),
                    itemCounts = artistItemCounts,
                    confirmButtonText = "Apply tags & Favorite",
                    itemTypeName = "favorite artists",
                    onCreateTag = { viewModel.createArtistTag(it) },
                    onDeleteTag = { viewModel.deleteArtistTag(it) },
                    onRenameTag = { oldTag, newTag -> viewModel.renameArtistTag(oldTag, newTag) },
                    onConfirm = { selectedTags ->
                        showArtistTagDialog = false
                        viewModel.setArtistTags(currentArtistName, selectedTags)
                        viewModel.addFavoriteArtist(currentArtistName)
                    },
                    onDismiss = { showArtistTagDialog = false }
                )
            }
        }
    }
}

private sealed class OppositeStatus {
    object Checking : OppositeStatus()
    data class Found(val matchPic: PictureItem, val dbName: String) : OppositeStatus()
    data class NotFound(val dbName: String) : OppositeStatus()
}
