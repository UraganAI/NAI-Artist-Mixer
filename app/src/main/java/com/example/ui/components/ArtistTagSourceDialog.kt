package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.mouseWheelScroll

@Composable
fun ArtistTagSourceDialog(
    currentFolderName: String,
    totalCount: Int,
    onSelectTreeUri: (Uri) -> Unit,
    onSelectMultipleUris: (List<Uri>) -> Unit,
    onSelectNaxMoeLooseGallery: () -> Unit,
    onSelectNaxMoeConstrainedGallery: () -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(vertical = 16.dp)
                    .testTag("artist_tag_source_dialog"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.background,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                        .mouseWheelScroll(scrollState)
                ) {
                    FolderSelector(
                        currentFolderName = currentFolderName,
                        totalCount = totalCount,
                        onSelectTreeUri = {
                            onSelectTreeUri(it)
                            onDismiss()
                        },
                        onSelectMultipleUris = {
                            onSelectMultipleUris(it)
                            onDismiss()
                        },
                        onSelectNaxMoeLooseGallery = {
                            onSelectNaxMoeLooseGallery()
                            onDismiss()
                        },
                        onSelectNaxMoeConstrainedGallery = {
                            onSelectNaxMoeConstrainedGallery()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}
