package com.example.model

enum class PictureSourceType {
    SYSTEM_TREE,
    SYSTEM_PICKER,
    SAMPLE_PRESET,
    WEB_GALLERY
}

data class PictureItem(
    val id: String,
    val fileName: String,
    val uriString: String,
    val sizeBytes: Long = 0L,
    val mimeType: String = "image/*",
    val sourceType: PictureSourceType = PictureSourceType.SAMPLE_PRESET,
    val drawableResId: Int? = null,
    val isLocked: Boolean = false
)
