package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
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
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MatchArtistDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var cardDeck by remember { mutableStateOf<List<PictureItem>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    // Alternate picture preview override if user checked other prompt version
    var overridePicture by remember { mutableStateOf<PictureItem?>(null) }
    var overrideLabel by remember { mutableStateOf<String?>(null) }

    // Random Danbooru picture override
    var randomArtArtistName by remember { mutableStateOf<String?>(null) }

    // Card drag & swipe animation state
    val animOffsetX = remember { Animatable(0f) }
    val animOffsetY = remember { Animatable(0f) }

    // Load unseen cards on launch
    LaunchedEffect(Unit) {
        isLoading = true
        cardDeck = viewModel.getMatchDeckArtists()
        currentIndex = 0
        isLoading = false
    }

    val currentCard = cardDeck.getOrNull(currentIndex)
    val nextCard = cardDeck.getOrNull(currentIndex + 1)

    // Check database versions for the current card
    var checkResult by remember { mutableStateOf<DbVersionCheckResult?>(null) }
    var isCheckingDb by remember { mutableStateOf(true) }
    var currentOptionIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(currentCard?.fileName) {
        val card = currentCard
        currentOptionIndex = -1
        overridePicture = null
        overrideLabel = null
        if (card != null) {
            isCheckingDb = true
            checkResult = viewModel.checkDatabaseVersions(card.fileName)
            isCheckingDb = false
        } else {
            checkResult = null
            isCheckingDb = false
        }
    }

    fun advanceToNextCard(isMatch: Boolean) {
        val artist = currentCard ?: return
        viewModel.swipeArtist(artist.fileName, isMatch)

        // Reset card state for next card
        overridePicture = null
        overrideLabel = null
        currentIndex++
    }

    fun triggerSwipe(isMatch: Boolean) {
        if (currentCard == null) return
        coroutineScope.launch {
            val targetX = if (isMatch) 800f else -800f
            animOffsetX.animateTo(targetX, animationSpec = tween(220))
            advanceToNextCard(isMatch)
            animOffsetX.snapTo(0f)
            animOffsetY.snapTo(0f)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_match_artist),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Match your Artist",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Swipe right to favorite • Swipe left to pass",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Main Stack Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    } else if (currentCard == null) {
                        // Empty deck state
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_match_artist),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "All Caught Up!",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "You have reviewed all available artists in this database. New artists will appear here automatically when loaded.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Next Card (behind active card for smooth stack effect)
                        if (nextCard != null) {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    val displayPic = nextCard
                                    if (displayPic.drawableResId != null) {
                                        Image(
                                            painter = painterResource(id = displayPic.drawableResId),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            alignment = Alignment.TopCenter,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else if (displayPic.uriString.isNotBlank()) {
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(displayPic.uriString)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            alignment = Alignment.TopCenter,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }

                        // Active Card
                        val displayPic = overridePicture ?: currentCard
                        val rotation = (animOffsetX.value / 18f).coerceIn(-25f, 25f)

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .offset { IntOffset(animOffsetX.value.roundToInt(), animOffsetY.value.roundToInt()) }
                                .rotate(rotation)
                                .pointerInput(currentIndex) {
                                    detectDragGestures(
                                        onDragEnd = {
                                            if (animOffsetX.value > 250f) {
                                                triggerSwipe(isMatch = true)
                                            } else if (animOffsetX.value < -250f) {
                                                triggerSwipe(isMatch = false)
                                            } else {
                                                coroutineScope.launch {
                                                    animOffsetX.animateTo(0f, animationSpec = tween(150))
                                                    animOffsetY.animateTo(0f, animationSpec = tween(150))
                                                }
                                            }
                                        },
                                        onDragCancel = {
                                            coroutineScope.launch {
                                                animOffsetX.snapTo(0f)
                                                animOffsetY.snapTo(0f)
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            coroutineScope.launch {
                                                animOffsetX.snapTo(animOffsetX.value + dragAmount.x)
                                                animOffsetY.snapTo(animOffsetY.value + dragAmount.y)
                                            }
                                        }
                                    )
                                }
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Image
                                ZoomableBox(modifier = Modifier.fillMaxSize()) {
                                    if (displayPic.drawableResId != null) {
                                        Image(
                                            painter = painterResource(id = displayPic.drawableResId),
                                            contentDescription = displayPic.fileName,
                                            contentScale = ContentScale.Crop,
                                            alignment = Alignment.TopCenter,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else if (displayPic.uriString.isNotBlank()) {
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(displayPic.uriString)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = displayPic.fileName,
                                            contentScale = ContentScale.Crop,
                                            alignment = Alignment.TopCenter,
                                            loading = {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator()
                                                }
                                            },
                                            error = {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.BrokenImage,
                                                        contentDescription = "Image Error",
                                                        tint = MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                // Overlay Swipe Feedback Badges (MATCH / PASS)
                                if (animOffsetX.value > 80f) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF4CAF50).copy(alpha = 0.9f),
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(16.dp)
                                            .rotate(-12f)
                                    ) {
                                        Text(
                                            text = "MATCH ❤️",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                        )
                                    }
                                } else if (animOffsetX.value < -80f) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF44336).copy(alpha = 0.9f),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(16.dp)
                                            .rotate(12f)
                                    ) {
                                        Text(
                                            text = "PASS ✖",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                // Previewing badge if active
                                if (overrideLabel != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(top = 12.dp)
                                    ) {
                                        Text(
                                            text = "Previewing: $overrideLabel",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Bottom Overlay Banner inside card (Artist Name)
                                Surface(
                                    color = Color.Black.copy(alpha = 0.85f),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        text = currentCard.fileName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Controls
                if (currentCard != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val options = checkResult?.options ?: emptyList()
                        val (buttonEnabled, buttonText) = when {
                            isCheckingDb -> Pair(false, "Checking DB...")
                            options.isEmpty() -> Pair(false, "artist absent from ${checkResult?.defaultOppositeName ?: "opposite"} database")
                            currentOptionIndex == -1 -> Pair(true, "Check ${options[0].dbName}")
                            currentOptionIndex + 1 < options.size -> Pair(true, "Check ${options[currentOptionIndex + 1].dbName}")
                            else -> Pair(true, "Return to selected database")
                        }

                        // Row 1: Switch database Button
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
                            enabled = buttonEnabled,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            if (isCheckingDb) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = buttonText,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        // Row 2: Danbooru Page (left) & Random Art (NSFW?) (right)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val tag = Uri.encode(currentCard.fileName.trim().replace(" ", "_"))
                                    val url = "https://danbooru.donmai.us/posts?tags=$tag"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_danbooru),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Danbooru page",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = { randomArtArtistName = currentCard.fileName },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Random Art (NSFW?)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Row 3: Pass (X) / Match (Heart) Buttons - Bigger size and icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Pass Button (Red X)
                            Button(
                                onClick = { triggerSwipe(isMatch = false) },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(68.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Pass",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            // Match Button (Green Heart)
                            Button(
                                onClick = { triggerSwipe(isMatch = true) },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(68.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Favorite Match",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
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
