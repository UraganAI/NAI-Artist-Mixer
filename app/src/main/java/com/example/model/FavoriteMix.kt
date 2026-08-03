package com.example.model

import com.example.model.PictureItem

data class FavoriteMix(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val pictures: List<PictureItem> = emptyList(),
    val imagePath: String? = null,
    val seed: Long? = null,
    val tags: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
