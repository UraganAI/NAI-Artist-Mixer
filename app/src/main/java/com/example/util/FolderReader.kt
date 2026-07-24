package com.example.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import com.example.model.PictureItem
import com.example.model.PictureSourceType

object FolderReader {

    private val IMAGE_EXTENSIONS = setOf(
        "png", "jpg", "jpeg", "webp", "gif", "bmp", "heic", "svg", "raw", "tiff"
    )

    fun readPicturesFromTreeUri(context: Context, treeUri: Uri): Pair<String, List<PictureItem>> {
        val rootDocument = DocumentFile.fromTreeUri(context, treeUri) ?: return Pair("Unknown Folder", emptyList())
        val folderName = rootDocument.name ?: "Designated Folder"

        val pictureList = mutableListOf<PictureItem>()
        val files = rootDocument.listFiles()

        for (file in files) {
            if (file.isFile && isImageFile(file)) {
                val rawName = file.name ?: "unnamed"
                val name = if (rawName.contains('.')) rawName.substringBeforeLast('.') else rawName
                val uriStr = file.uri.toString()
                val size = file.length()
                val mime = file.type ?: "image/*"

                pictureList.add(
                    PictureItem(
                        id = uriStr,
                        fileName = name,
                        uriString = uriStr,
                        sizeBytes = size,
                        mimeType = mime,
                        sourceType = PictureSourceType.SYSTEM_TREE
                    )
                )
            }
        }

        // Sort by filename naturally
        return Pair(folderName, pictureList.sortedBy { it.fileName.lowercase() })
    }

    fun readPicturesFromUris(context: Context, uris: List<Uri>): List<PictureItem> {
        val pictureList = mutableListOf<PictureItem>()

        for (uri in uris) {
            var fileName = "image_${System.currentTimeMillis()}.png"
            var sizeBytes = 0L
            var mimeType = "image/*"

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex) ?: fileName
                    }
                    if (sizeIndex != -1) {
                        sizeBytes = cursor.getLong(sizeIndex)
                    }
                }
            }

            mimeType = context.contentResolver.getType(uri) ?: "image/*"
            val cleanName = if (fileName.contains('.')) fileName.substringBeforeLast('.') else fileName

            pictureList.add(
                PictureItem(
                    id = uri.toString(),
                    fileName = cleanName,
                    uriString = uri.toString(),
                    sizeBytes = sizeBytes,
                    mimeType = mimeType,
                    sourceType = PictureSourceType.SYSTEM_PICKER
                )
            )
        }

        return pictureList.sortedBy { it.fileName.lowercase() }
    }

    private fun isImageFile(file: DocumentFile): Boolean {
        val mime = file.type
        if (mime != null && mime.startsWith("image/")) return true

        val name = file.name ?: return false
        val ext = name.substringAfterLast('.', "").lowercase()
        return IMAGE_EXTENSIONS.contains(ext)
    }
}
