package com.example.util

import com.example.model.FormattingOptions
import com.example.model.PictureItem
import com.example.model.TextCasing
import java.util.Locale

object NameFormatter {

    fun formatPictureNames(
        pictures: List<PictureItem>,
        options: FormattingOptions
    ): String {
        if (pictures.isEmpty()) return ""

        val formattedNames = pictures.map { picture ->
            formatSingleName(picture.fileName, options)
        }

        val joined = formattedNames.joinToString(separator = options.actualSeparator)
        return "${options.prefix}$joined${options.suffix}"
    }

    fun formatSingleName(fileName: String, options: FormattingOptions): String {
        var name = fileName.trim()

        if (options.removeExtensions) {
            val lastDotIndex = name.lastIndexOf('.')
            if (lastDotIndex > 0) {
                name = name.substring(0, lastDotIndex)
            }
        }

        if (options.replaceSeparatorsWithSpaces) {
            name = name.replace('_', ' ')
            // clean up double spaces
            name = name.replace(Regex("\\s+"), " ")
        }

        name = when (options.casing) {
            TextCasing.AS_IS -> name
            TextCasing.LOWERCASE -> name.lowercase(Locale.getDefault())
            TextCasing.UPPERCASE -> name.uppercase(Locale.getDefault())
            TextCasing.TITLE_CASE -> name.split(" ")
                .joinToString(" ") { word ->
                    word.lowercase(Locale.getDefault())
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }
        }

        if (options.tagPrefix.value.isNotEmpty()) {
            name = "${options.tagPrefix.value}$name"
        }

        if (options.wrapQuotes) {
            name = "\"$name\""
        }


        return name
    }
}
