package com.example.model

data class SelectionResult(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val folderName: String,
    val countRequested: Int,
    val selectedPictures: List<PictureItem>,
    val formattedString: String
)
