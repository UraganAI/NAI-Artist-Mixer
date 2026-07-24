package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.util.DanbooruFetcher
import com.example.util.DanbooruPost
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.mutableFloatStateOf
import coil.compose.AsyncImagePainter
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun RandomArtDialog(
    artistName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentPost by remember { mutableStateOf<DanbooruPost?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    fun loadNextPost() {
        coroutineScope.launch {
            isLoading = true
            hasError = false
            var post: DanbooruPost? = null
            // Retry up to 3 times automatically if initial fetch returns null
            for (attempt in 1..3) {
                post = DanbooruFetcher.fetchRandomArtistPost(artistName)
                if (post != null) break
            }
            if (post != null) {
                currentPost = post
            } else {
                hasError = true
            }
            isLoading = false
        }
    }

    LaunchedEffect(artistName) {
        loadNextPost()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            color = Color.Black.copy(alpha = 0.72f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Image container
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 70.dp, bottom = 140.dp)
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { /* consume click on loading content */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color.White)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Fetching Random Danbooru Art...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }
                    } else if (hasError || currentPost == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { /* consume click on error content */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.BrokenImage,
                                    contentDescription = "Error",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No artwork found on Danbooru",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { loadNextPost() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Try Again")
                                }
                            }
                        }
                    } else {
                        var loadedAspectRatio by remember(currentPost?.imageUrl) {
                            mutableFloatStateOf(
                                if ((currentPost?.width ?: 0) > 0 && (currentPost?.height ?: 0) > 0) {
                                    currentPost!!.width.toFloat() / currentPost!!.height.toFloat()
                                } else 0f
                            )
                        }

                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val maxW = maxWidth
                            val maxH = maxHeight

                            val imgModifier = if (loadedAspectRatio > 0f) {
                                val containerRatio = if (maxH > 0.dp) maxW / maxH else 1f
                                if (loadedAspectRatio > containerRatio) {
                                    val calculatedH = maxW / loadedAspectRatio
                                    Modifier
                                        .width(maxW)
                                        .height(calculatedH)
                                } else {
                                    val calculatedW = maxH * loadedAspectRatio
                                    Modifier
                                        .width(calculatedW)
                                        .height(maxH)
                                }
                            } else {
                                Modifier.fillMaxSize()
                            }

                            ZoomableBox(
                                modifier = imgModifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { /* consume click on picture itself */ }
                            ) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(currentPost!!.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = artistName,
                                    contentScale = ContentScale.Fit,
                                    alignment = Alignment.Center,
                                    onSuccess = { successState ->
                                        val size = successState.painter.intrinsicSize
                                        if (size.width > 0 && size.height > 0) {
                                            loadedAspectRatio = size.width / size.height
                                        }
                                    },
                                    loading = {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = Color.White)
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // Top Bar
                Surface(
                    color = Color.Black.copy(alpha = 0.90f),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* consume click on top bar */ }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Random Art (NSFW?)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = artistName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Bottom Controls
                Surface(
                    color = Color.Black,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* consume click on bottom bar */ }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 48.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Roll New Button
                        Button(
                            onClick = { loadNextPost() },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1.15f)
                                .height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Roll New (NSFW?)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Picture's Actual Page Button
                        OutlinedButton(
                            onClick = {
                                val url = currentPost?.postUrl
                                    ?: "https://danbooru.donmai.us/posts?tags=${Uri.encode(artistName.trim().lowercase().replace(" ", "_"))}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Picture Page",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
