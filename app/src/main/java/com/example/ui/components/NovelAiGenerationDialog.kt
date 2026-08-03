package com.example.ui.components

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.GenerationHistoryItem
import com.example.model.PictureItem
import com.example.util.NovelAiGenerator
import com.example.viewmodel.MainViewModel
import kotlin.random.Random

@Composable
fun NovelAiGenerationDialog(
    artistTags: String,
    apiKey: String,
    width: Int = 832,
    height: Int = 1216,
    initialSeed: Long = -1L,
    isGenerating: Boolean,
    generatedBitmap: Bitmap?,
    errorMessage: String?,
    viewModel: MainViewModel? = null,
    onShowOverlayMessage: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onGenerate: (artistTags: String, overrideSeed: Long?) -> Unit,
    onSaveMix: (pictures: List<PictureItem>, bitmap: Bitmap?, seed: Long?) -> Unit = { _, _, _ -> },
    onRerollMix: () -> Unit = {},
    onClearState: () -> Unit
) {
    val context = LocalContext.current

    val targetAspectRatio = remember(width, height) {
        if (height > 0) width.toFloat() / height.toFloat() else 832f / 1216f
    }

    // Instance-specific seed locking, synced with initialSeed from settings
    var isSeedLocked by remember(initialSeed) { mutableStateOf(initialSeed >= 0L) }
    var lockedSeedValue by remember(initialSeed) { mutableStateOf<Long?>(if (initialSeed >= 0L) initialSeed else null) }

    val currentSeedToUse = if (isSeedLocked) lockedSeedValue else -1L

    // API key prompt state
    var showApiKeyPromptDialog by remember(apiKey) { mutableStateOf(apiKey.isBlank()) }
    var inputApiKey by remember { mutableStateOf("") }

    // Session history tracking (persists across dialog opens)
    val historyList = viewModel?.generationHistory ?: remember { mutableStateListOf<GenerationHistoryItem>() }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showMixTagDialog by remember { mutableStateOf(false) }
    var showFullImagePreview by remember { mutableStateOf(false) }
    var lastRecordedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var overlayMessage by remember { mutableStateOf<String?>(null) }

    val lastTags by viewModel?.lastGeneratedArtistTags?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }
    val lastSeed by viewModel?.lastGeneratedSeed?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }

    var hasHandledInitialLaunch by remember { mutableStateOf(false) }

    LaunchedEffect(overlayMessage) {
        if (overlayMessage != null) {
            kotlinx.coroutines.delay(2200)
            overlayMessage = null
        }
    }

    // Record generated bitmap into history
    LaunchedEffect(generatedBitmap) {
        if (generatedBitmap != null && generatedBitmap != lastRecordedBitmap) {
            lastRecordedBitmap = generatedBitmap
            val currentPics = viewModel?.currentResult?.value?.selectedPictures ?: emptyList()
            val seedUsed = lastSeed ?: currentSeedToUse ?: (lockedSeedValue ?: initialSeed)
            historyList.add(
                0,
                GenerationHistoryItem(
                    pictures = currentPics,
                    artistTags = artistTags,
                    bitmap = generatedBitmap,
                    seed = seedUsed
                )
            )
        }
    }

    // Trigger generation automatically on dialog open if selection/seed modified or not generated yet, but NOT on artist add/remove inside dialog
    LaunchedEffect(Unit) {
        if (!hasHandledInitialLaunch) {
            hasHandledInitialLaunch = true
            val alreadyHasGeneratedBitmap = generatedBitmap != null
            val matchesLastGeneration = alreadyHasGeneratedBitmap && (lastTags == artistTags) && (lastSeed == currentSeedToUse)

            if (apiKey.isNotBlank() && !isGenerating && !matchesLastGeneration && errorMessage == null) {
                onGenerate(artistTags, currentSeedToUse)
            }
        }
    }

    val isRegenDisabled = generatedBitmap != null && (artistTags == lastTags) && isSeedLocked && (currentSeedToUse == lastSeed)
    val hasPicture = generatedBitmap != null
    val matchesPicture = hasPicture && (artistTags == lastTags) && (!isSeedLocked || currentSeedToUse == lastSeed)
    val isSaveMixEnabled = hasPicture && matchesPicture

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f)
                .padding(vertical = 4.dp)
                .testTag("novelai_generation_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isGenerating) {
                    TopMovingLineIndicator(
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                // FIXED TOP SECTION (Generated Picture + Control Buttons)
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TRY MIX",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_gen_dialog_button")
                    ) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                // PICTURE DISPLAY AREA - Aspect ratio frame set strictly according to settings dimensions
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .aspectRatio(targetAspectRatio)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (generatedBitmap != null) {
                            Image(
                                bitmap = generatedBitmap.asImageBitmap(),
                                contentDescription = "Generated Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { showFullImagePreview = true },
                                contentScale = ContentScale.Fit
                            )
                        } else if (isGenerating) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                            }
                        } else if (errorMessage != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Generation Error",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onGenerate(artistTags, currentSeedToUse) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Retry", fontSize = 12.sp)
                                }
                            }
                        } else if (apiKey.isBlank()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "API Key Required",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Set persistent API key in Gen Settings.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { showApiKeyPromptDialog = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Enter API Key", fontSize = 12.sp)
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(30.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Ready to generate mix",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        // Re-try Mix button OVER the picture (bottom right, icon only)
                        if (apiKey.isNotBlank()) {
                            FilledIconButton(
                                onClick = {
                                    if (isRegenDisabled) {
                                        Toast.makeText(context, "Cannot regenerate: artist selection and seed haven't changed.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onGenerate(artistTags, currentSeedToUse)
                                    }
                                },
                                enabled = !isGenerating,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .size(36.dp)
                                    .testTag("retry_mix_button"),
                                colors = if (isRegenDisabled) {
                                    IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                } else {
                                    IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.90f),
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Re-try Mix",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Temporary overlay text badge (e.g., "Number of artists increased to X")
                        androidx.compose.animation.AnimatedVisibility(
                            visible = overlayMessage != null,
                            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { -it / 2 },
                            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically { -it / 2 },
                            modifier = Modifier.align(Alignment.TopCenter)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                tonalElevation = 8.dp,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = overlayMessage ?: "",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ROW 1 CONTROLS: Edit Gen Settings, Lock/Unlock Seed, History
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gen_dialog_edit_settings_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GEN SETTINGS", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = {
                            if (isSeedLocked) {
                                isSeedLocked = false
                                lockedSeedValue = null
                                Toast.makeText(context, "Seed unlocked (Random)", Toast.LENGTH_SHORT).show()
                            } else {
                                isSeedLocked = true
                                lockedSeedValue = if (initialSeed >= 0L) initialSeed else Random.nextLong(1000000000L, 9999999999L)
                                Toast.makeText(context, "Seed locked: $lockedSeedValue", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lock_seed_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isSeedLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSeedLocked) "UNLOCK SEED" else "LOCK SEED",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = { showHistoryDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_history_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("HISTORY (${historyList.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // ROW 2 CONTROLS: Save Mix, Add Artist, Re-roll
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Split Save / Tag Double Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_save_mix_button"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSaveMixEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Half: Save
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(enabled = isSaveMixEnabled) {
                                        if (!hasPicture) {
                                            Toast.makeText(context, "Please generate a picture first before saving mix.", Toast.LENGTH_SHORT).show()
                                        } else if (!matchesPicture) {
                                            Toast.makeText(context, "Selected artists and seed do not match the generated picture.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            val currentPics = viewModel?.currentResult?.value?.selectedPictures ?: emptyList()
                                            if (currentPics.isNotEmpty()) {
                                                val savedTitle = viewModel?.saveFavoriteMix(currentPics, generatedBitmap, currentSeedToUse)
                                                    ?: "Mix#${(viewModel?.favoriteMixes?.value?.size ?: 0) + 1}"
                                                val msg = "$savedTitle saved in Favorite Mixes"
                                                overlayMessage = msg
                                                onShowOverlayMessage?.invoke(msg)
                                            } else {
                                                Toast.makeText(context, "No artists in current mix to save", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = if (isSaveMixEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "SAVE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSaveMixEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }

                            // Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight(0.6f)
                                    .background(if (isSaveMixEnabled) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                            )

                            // Right Half: Tag
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(enabled = isSaveMixEnabled) {
                                        showMixTagDialog = true
                                    },
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Label,
                                    contentDescription = null,
                                    tint = if (isSaveMixEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "TAG",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSaveMixEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val newAmount = viewModel?.addRandomArtistToSelection() ?: 0
                            if (newAmount > 0) {
                                val msg = "Number of artists increased to $newAmount"
                                overlayMessage = msg
                                onShowOverlayMessage?.invoke(msg)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_add_artist_button"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ADD ARTIST", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }

                    Button(
                        onClick = { onRerollMix() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_reroll_button"),
                        enabled = !isGenerating,
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RE-ROLL", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Seed Status & Save/Copy Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSeedLocked && lockedSeedValue != null) "Seed: ${lockedSeedValue} (Locked)" else "Seed: Random",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isSeedLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (generatedBitmap != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Download Image
                            OutlinedButton(
                                onClick = {
                                    val uri = NovelAiGenerator.saveBitmapToGallery(context, generatedBitmap)
                                    if (uri != null) {
                                        Toast.makeText(context, "Saved image to Pictures/NAIArtistMixer!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Failed to save image.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = "Save Image", modifier = Modifier.size(14.dp))
                            }

                            // Copy Artist Tags
                            OutlinedButton(
                                onClick = {
                                    if (viewModel != null) {
                                        viewModel.copyResultToClipboard(context)
                                    } else {
                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Selected Picture Names", artistTags)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied artist tags to clipboard! 📋", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Artist Tags", modifier = Modifier.size(14.dp))
                            }

                            // Copy Picture
                            OutlinedButton(
                                onClick = {
                                    NovelAiGenerator.copyBitmapToClipboard(context, generatedBitmap)
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Image, contentDescription = "Copy Image", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(4.dp))

                // ARTIST SECTION (First row visible, scroll down for rest)
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (viewModel != null) {
                        val currentResult by viewModel.currentResult.collectAsStateWithLifecycle()
                        val availablePictures by viewModel.availablePictures.collectAsStateWithLifecycle()
                        val favoriteArtistNames by viewModel.favoriteArtistNames.collectAsStateWithLifecycle()

                        PictureGrid(
                            pictures = currentResult?.selectedPictures ?: emptyList(),
                            availablePictures = availablePictures,
                            favoriteArtistNames = favoriteArtistNames,
                            viewModel = viewModel,
                            hideSearch = true,
                            containerHeight = 185.dp,
                            useScrollableContainer = true,
                            onRerollItem = { viewModel.rerollSinglePicture(it) },
                            onRemoveItem = { index ->
                                val newAmount = viewModel.removePictureAt(index)
                                val msg = "Number of artists decreased to $newAmount"
                                overlayMessage = msg
                                onShowOverlayMessage?.invoke(msg)
                            },
                            onToggleLock = { viewModel.toggleLockPicture(it) },
                            onToggleFavorite = { viewModel.toggleFavoriteArtist(it) },
                            onAddManualPicture = { viewModel.addManualPicture(it) },
                            onMoveItem = { from, to -> viewModel.movePicture(from, to) }
                        )
                    }
                }
            }
        }
    }
}

    if (showHistoryDialog) {
        GenerationHistoryDialog(
            historyList = historyList,
            onSelectHistoryItem = { item ->
                viewModel?.restorePicturesList(item.pictures)
                viewModel?.restoreGeneratedImage(item.bitmap, item.artistTags, item.seed)
                lastRecordedBitmap = item.bitmap
                isSeedLocked = true
                lockedSeedValue = item.seed
                Toast.makeText(context, "Restored artists, image & seed (${item.seed})", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showHistoryDialog = false }
        )
    }

    if (showApiKeyPromptDialog && apiKey.isBlank()) {
        AlertDialog(
            onDismissRequest = { showApiKeyPromptDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NovelAI API Key Required",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Please enter your NovelAI API key to start generating images:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputApiKey,
                        onValueChange = { inputApiKey = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("novelai_api_key_prompt_input"),
                        label = { Text("NovelAI API Key") },
                        placeholder = { Text("pst-...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputApiKey.isNotBlank()) {
                            viewModel?.updateNaiApiKey(inputApiKey.trim())
                            showApiKeyPromptDialog = false
                        }
                    },
                    enabled = inputApiKey.isNotBlank(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Key")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showApiKeyPromptDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFullImagePreview && generatedBitmap != null) {
        TryMixImagePreviewDialog(
            generatedBitmap = generatedBitmap,
            historySize = historyList.size,
            isSaveMixEnabled = isSaveMixEnabled,
            isRegenDisabled = isRegenDisabled,
            isGenerating = isGenerating,
            onDismiss = { showFullImagePreview = false },
            onOpenHistory = { showHistoryDialog = true },
            onCopyImage = { NovelAiGenerator.copyBitmapToClipboard(context, generatedBitmap) },
            onDownloadImage = {
                val uri = NovelAiGenerator.saveBitmapToGallery(context, generatedBitmap)
                if (uri != null) {
                    Toast.makeText(context, "Saved image to Pictures/NAIArtistMixer!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to save image.", Toast.LENGTH_SHORT).show()
                }
            },
            onSaveMixClicked = {
                if (!hasPicture) {
                    Toast.makeText(context, "Please generate a picture first before saving mix.", Toast.LENGTH_SHORT).show()
                } else if (!matchesPicture) {
                    Toast.makeText(context, "Selected artists and seed do not match the generated picture.", Toast.LENGTH_SHORT).show()
                } else {
                    val currentPics = viewModel?.currentResult?.value?.selectedPictures ?: emptyList()
                    if (currentPics.isNotEmpty()) {
                        val savedTitle = viewModel?.saveFavoriteMix(currentPics, generatedBitmap, currentSeedToUse)
                            ?: "Mix#${(viewModel?.favoriteMixes?.value?.size ?: 0) + 1}"
                        val msg = "$savedTitle saved in Favorite Mixes"
                        overlayMessage = msg
                        onShowOverlayMessage?.invoke(msg)
                    } else {
                        Toast.makeText(context, "No artists in current mix to save", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onTagMixClicked = { showMixTagDialog = true },
            onRedoClicked = {
                if (isRegenDisabled) {
                    Toast.makeText(context, "Cannot regenerate: artist selection and seed haven't changed.", Toast.LENGTH_SHORT).show()
                } else {
                    onGenerate(artistTags, currentSeedToUse)
                }
            },
            onRerollMixClicked = { onRerollMix() }
        )
    }

    if (showMixTagDialog) {
        val mixTags by viewModel?.mixTags?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptySet()) }
        val favoriteMixes by viewModel?.favoriteMixes?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
        val mixItemCounts = remember(mixTags, favoriteMixes) {
            mixTags.associateWith { tag ->
                favoriteMixes.count { it.tags.contains(tag) }
            }
        }

        TagManagementDialog(
            title = "Tag Artist Mix",
            allTags = mixTags,
            initialSelectedTags = emptySet(),
            itemCounts = mixItemCounts,
            confirmButtonText = "Apply tags & Save",
            itemTypeName = "artist mixes",
            onCreateTag = { tag -> viewModel?.createMixTag(tag) },
            onDeleteTag = { tag -> viewModel?.deleteMixTag(tag) },
            onRenameTag = { oldTag, newTag -> viewModel?.renameMixTag(oldTag, newTag) },
            onConfirm = { selectedTags ->
                showMixTagDialog = false
                if (!hasPicture) {
                    Toast.makeText(context, "Please generate a picture first before saving mix.", Toast.LENGTH_SHORT).show()
                } else if (!matchesPicture) {
                    Toast.makeText(context, "Selected artists and seed do not match the generated picture.", Toast.LENGTH_SHORT).show()
                } else {
                    val currentPics = viewModel?.currentResult?.value?.selectedPictures ?: emptyList()
                    if (currentPics.isNotEmpty()) {
                        val savedTitle = viewModel?.saveFavoriteMix(currentPics, generatedBitmap, currentSeedToUse, selectedTags.toList())
                            ?: "Mix#${(viewModel?.favoriteMixes?.value?.size ?: 0) + 1}"
                        val msg = "$savedTitle saved in Favorite Mixes"
                        overlayMessage = msg
                        onShowOverlayMessage?.invoke(msg)
                    } else {
                        Toast.makeText(context, "No artists in current mix to save", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { showMixTagDialog = false }
        )
    }
}

@Composable
private fun TryMixImagePreviewDialog(
    generatedBitmap: Bitmap,
    historySize: Int,
    isSaveMixEnabled: Boolean,
    isRegenDisabled: Boolean,
    isGenerating: Boolean,
    onDismiss: () -> Unit,
    onOpenHistory: () -> Unit,
    onCopyImage: () -> Unit,
    onDownloadImage: () -> Unit,
    onSaveMixClicked: () -> Unit,
    onTagMixClicked: () -> Unit,
    onRedoClicked: () -> Unit,
    onRerollMixClicked: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isGenerating) {
                    TopMovingLineIndicator(
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Top Header with Close icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Generated Mix Preview",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Centered generated image
                Image(
                    bitmap = generatedBitmap.asImageBitmap(),
                    contentDescription = "Full Generated Mix",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp, bottom = 110.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )

                // Bottom Buttons (2 Rows)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ROW 1: Copy, Download
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onCopyImage,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("preview_dialog_copy_button"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("COPY", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = onDownloadImage,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("preview_dialog_download_button"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DOWNLOAD", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                        }
                    }

                    // ROW 2: Save Mix, Re-do, Re-roll
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Split Save / Tag Double Button for Preview
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("preview_dialog_save_mix_button"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSaveMixEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Half: Save
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable(enabled = isSaveMixEnabled) {
                                            onSaveMixClicked()
                                        },
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = if (isSaveMixEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "SAVE",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSaveMixEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }

                                // Divider
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .fillMaxHeight(0.6f)
                                        .background(if (isSaveMixEnabled) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                                )

                                // Right Half: Tag
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable(enabled = isSaveMixEnabled) {
                                            onTagMixClicked()
                                        },
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Label,
                                        contentDescription = null,
                                        tint = if (isSaveMixEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "TAG",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSaveMixEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                onRedoClicked()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("preview_dialog_redo_button"),
                            enabled = !isGenerating,
                            colors = if (isRegenDisabled) {
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            } else {
                                ButtonDefaults.buttonColors()
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RE-DO", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                        }

                        Button(
                            onClick = {
                                onRerollMixClicked()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("preview_dialog_reroll_button"),
                            enabled = !isGenerating,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RE-ROLL", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun TopMovingLineIndicator(
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TopLineAnim")
    val progress by infiniteTransition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lineProgress"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
    ) {
        val w = size.width
        val lineWidth = w * 0.6f
        val startX = (w + lineWidth) * progress - lineWidth
        val endX = startX + lineWidth

        val brush = Brush.horizontalGradient(
            colors = listOf(
                accentColor.copy(alpha = 0f),
                accentColor,
                accentColor,
                accentColor.copy(alpha = 0f)
            ),
            startX = startX,
            endX = endX
        )

        drawRect(
            brush = brush,
            topLeft = Offset(startX.coerceAtLeast(0f), 0f),
            size = Size((endX - startX).coerceAtLeast(0f), size.height)
        )
    }
}
