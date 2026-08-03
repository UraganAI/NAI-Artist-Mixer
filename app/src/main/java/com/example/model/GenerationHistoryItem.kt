package com.example.model

import android.graphics.Bitmap

data class GenerationHistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val pictures: List<PictureItem>,
    val artistTags: String,
    val bitmap: Bitmap?,
    val seed: Long,
    val timestamp: Long = System.currentTimeMillis()
)
