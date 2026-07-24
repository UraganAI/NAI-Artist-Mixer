package com.example.model

enum class TextCasing(val label: String) {
    AS_IS("As is"),
    LOWERCASE("lowercase"),
    UPPERCASE("UPPERCASE"),
    TITLE_CASE("Title Case")
}

enum class SeparatorType(val label: String, val value: String) {
    COMMA_SPACE("Comma + Space", ", "),
    COMMA("Comma", ","),
    CUSTOM("Custom...", "")
}

enum class TagPrefix(val label: String, val value: String) {
    ARTIST("artist:", "artist:"),
    NONE("None", "")
}

data class FormattingOptions(
    val removeExtensions: Boolean = true,
    val replaceSeparatorsWithSpaces: Boolean = false,
    val separatorType: SeparatorType = SeparatorType.COMMA_SPACE,
    val customSeparator: String = ", ",
    val casing: TextCasing = TextCasing.AS_IS,
    val wrapQuotes: Boolean = false,
    val tagPrefix: TagPrefix = TagPrefix.ARTIST,
    val prefix: String = "",
    val suffix: String = ""
) {
    val actualSeparator: String
        get() = if (separatorType == SeparatorType.CUSTOM) customSeparator else separatorType.value
}

