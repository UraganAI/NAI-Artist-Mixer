package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject
import android.graphics.Bitmap
import com.example.model.FavoriteMix
import com.example.util.NovelAiGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.FormattingOptions
import com.example.model.PictureItem
import com.example.model.PictureSourceType
import com.example.model.SelectionResult
import com.example.model.SeparatorType
import com.example.model.TextCasing
import com.example.util.FolderReader
import com.example.util.NameFormatter
import com.example.util.NaxMoeFetcher
import com.example.ui.theme.AppThemePreset
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AlternateDbOption(
    val matchPic: PictureItem,
    val dbName: String
)

data class DbVersionCheckResult(
    val options: List<AlternateDbOption>,
    val defaultOppositeName: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("picpicker_prefs", Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _appThemePreset = MutableStateFlow<AppThemePreset>(
        runCatching {
            val savedName = prefs.getString("app_theme_preset", AppThemePreset.NOVEL_AI_DARK.name)
            AppThemePreset.fromId(savedName)
        }.getOrDefault(AppThemePreset.NOVEL_AI_DARK)
    )
    val appThemePreset: StateFlow<AppThemePreset> = _appThemePreset.asStateFlow()

    private val _swipedArtistNames = MutableStateFlow<Set<String>>(
        prefs.getStringSet("swiped_artists", emptySet()) ?: emptySet()
    )
    val swipedArtistNames: StateFlow<Set<String>> = _swipedArtistNames.asStateFlow()

    private val _folderName = MutableStateFlow(prefs.getString("folder_name", "") ?: "")
    val folderName: StateFlow<String> = _folderName.asStateFlow()

    private val _folderUri = MutableStateFlow<Uri?>(
        prefs.getString("folder_uri", null)?.let { Uri.parse(it) }
    )
    val folderUri: StateFlow<Uri?> = _folderUri.asStateFlow()

    private val _availablePictures = MutableStateFlow<List<PictureItem>>(emptyList())
    val availablePictures: StateFlow<List<PictureItem>> = _availablePictures.asStateFlow()

    private val _requestedCount = MutableStateFlow(prefs.getInt("requested_count", 3).coerceAtLeast(1))
    val requestedCount: StateFlow<Int> = _requestedCount.asStateFlow()

    private val _allowDuplicates = MutableStateFlow(prefs.getBoolean("allow_duplicates", false))
    val allowDuplicates: StateFlow<Boolean> = _allowDuplicates.asStateFlow()

    private val _formattingOptions = MutableStateFlow(
        FormattingOptions(
            removeExtensions = prefs.getBoolean("remove_extensions", true),
            replaceSeparatorsWithSpaces = prefs.getBoolean("replace_separators", false),
            separatorType = runCatching {
                SeparatorType.valueOf(prefs.getString("separator_type", SeparatorType.COMMA_SPACE.name)!!)
            }.getOrDefault(SeparatorType.COMMA_SPACE),
            customSeparator = prefs.getString("custom_separator", ", ") ?: ", ",
            casing = runCatching {
                TextCasing.valueOf(prefs.getString("casing", TextCasing.AS_IS.name)!!)
            }.getOrDefault(TextCasing.AS_IS)
        )
    )
    val formattingOptions: StateFlow<FormattingOptions> = _formattingOptions.asStateFlow()

    private val _currentResult = MutableStateFlow<SelectionResult?>(null)
    val currentResult: StateFlow<SelectionResult?> = _currentResult.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showFirstStartPrompt = MutableStateFlow(false)
    val showFirstStartPrompt: StateFlow<Boolean> = _showFirstStartPrompt.asStateFlow()

    // Generation History state (persists until app closes)
    val generationHistory = androidx.compose.runtime.mutableStateListOf<com.example.model.GenerationHistoryItem>()

    // Mix Tags state
    private val _mixTags = MutableStateFlow<Set<String>>(
        prefs.getStringSet("mix_tags", emptySet()) ?: emptySet()
    )
    val mixTags: StateFlow<Set<String>> = _mixTags.asStateFlow()

    // Artist Tags state
    private val _artistUserTags = MutableStateFlow<Set<String>>(
        prefs.getStringSet("artist_user_tags", emptySet()) ?: emptySet()
    )
    val artistUserTags: StateFlow<Set<String>> = _artistUserTags.asStateFlow()

    private val _artistToTags = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val artistToTags: StateFlow<Map<String, Set<String>>> = _artistToTags.asStateFlow()

    // Favorites state
    private val _favoriteArtistNames = MutableStateFlow<Set<String>>(
        prefs.getStringSet("favorite_artists", emptySet()) ?: emptySet()
    )
    val favoriteArtistNames: StateFlow<Set<String>> = _favoriteArtistNames.asStateFlow()

    private val _favoriteMixes = MutableStateFlow<List<FavoriteMix>>(emptyList())
    val favoriteMixes: StateFlow<List<FavoriteMix>> = _favoriteMixes.asStateFlow()

    private val _isNaxMoeDatabase = MutableStateFlow(false)
    val isNaxMoeDatabase: StateFlow<Boolean> = _isNaxMoeDatabase.asStateFlow()

    // Standard NovelAI Generator Settings
    private val defaultNaiMainPrompt = "neutral background, white background, transparent background, cowboy shot, -1.5::nipples::, , very aesthetic, masterpiece, no text, "
    private val defaultNaiUc = "nsfw, lowres, artistic error, film grain, scan artifacts, worst quality, bad quality, jpeg artifacts, very displeasing, chromatic aberration, dithering, halftone, screentone, multiple views, logo, too many watermarks, negative space, blank page"
    private val defaultNaiCharacter = "girl, soft smile, red lips, hoodie, jeans, orange hair, small breasts, "
    private val defaultNaiSeed = 2710136181L

    // Initial first-launch values
    private val initialNaiMainPrompt = defaultNaiMainPrompt
    private val initialNaiUc = defaultNaiUc
    private val initialNaiCharacter = defaultNaiCharacter
    private val initialNaiSeed = defaultNaiSeed

    private val isNaiInitialized = prefs.getBoolean("nai_settings_initialized", false)

    private val _naiApiKey = MutableStateFlow(prefs.getString("nai_api_key", "") ?: "")
    val naiApiKey: StateFlow<String> = _naiApiKey.asStateFlow()

    private val _naiMainPrompt = MutableStateFlow(
        if (!isNaiInitialized) initialNaiMainPrompt else (prefs.getString("nai_main_prompt", defaultNaiMainPrompt) ?: defaultNaiMainPrompt)
    )
    val naiMainPrompt: StateFlow<String> = _naiMainPrompt.asStateFlow()

    private val _naiUc = MutableStateFlow(
        if (!isNaiInitialized) initialNaiUc else (prefs.getString("nai_uc", defaultNaiUc) ?: defaultNaiUc)
    )
    val naiUc: StateFlow<String> = _naiUc.asStateFlow()

    private val _naiCharacter = MutableStateFlow(
        if (!isNaiInitialized) initialNaiCharacter else (prefs.getString("nai_character", defaultNaiCharacter) ?: defaultNaiCharacter)
    )
    val naiCharacter: StateFlow<String> = _naiCharacter.asStateFlow()

    private val _naiModel = MutableStateFlow(prefs.getString("nai_model", "nai-diffusion-4-5-full") ?: "nai-diffusion-4-5-full")
    val naiModel: StateFlow<String> = _naiModel.asStateFlow()

    private val _naiWidth = MutableStateFlow(prefs.getInt("nai_width", 832))
    val naiWidth: StateFlow<Int> = _naiWidth.asStateFlow()

    private val _naiHeight = MutableStateFlow(prefs.getInt("nai_height", 1216))
    val naiHeight: StateFlow<Int> = _naiHeight.asStateFlow()

    private val _naiSeed = MutableStateFlow(
        if (!isNaiInitialized) initialNaiSeed else prefs.getLong("nai_seed", defaultNaiSeed)
    )
    val naiSeed: StateFlow<Long> = _naiSeed.asStateFlow()

    private val _naiScale = MutableStateFlow(
        if (!isNaiInitialized) 4.0 else prefs.getFloat("nai_scale", 4.0f).toDouble()
    )
    val naiScale: StateFlow<Double> = _naiScale.asStateFlow()

    private val _naiGenerating = MutableStateFlow(false)
    val naiGenerating: StateFlow<Boolean> = _naiGenerating.asStateFlow()

    private val _naiGeneratedBitmap = MutableStateFlow<Bitmap?>(null)
    val naiGeneratedBitmap: StateFlow<Bitmap?> = _naiGeneratedBitmap.asStateFlow()

    private val _naiError = MutableStateFlow<String?>(null)
    val naiError: StateFlow<String?> = _naiError.asStateFlow()

    private val _lastGeneratedArtistTags = MutableStateFlow<String?>(null)
    val lastGeneratedArtistTags: StateFlow<String?> = _lastGeneratedArtistTags.asStateFlow()

    private val _lastGeneratedSeed = MutableStateFlow<Long?>(null)
    val lastGeneratedSeed: StateFlow<Long?> = _lastGeneratedSeed.asStateFlow()

    init {
        if (!isNaiInitialized) {
            prefs.edit()
                .putString("nai_main_prompt", _naiMainPrompt.value)
                .putString("nai_uc", _naiUc.value)
                .putString("nai_character", _naiCharacter.value)
                .putLong("nai_seed", _naiSeed.value)
                .putFloat("nai_scale", _naiScale.value.toFloat())
                .putBoolean("nai_settings_initialized", true)
                .apply()
        }

        // Attempt automatic restoration from persistent disk backup if shared preferences is empty or missing favorites
        tryAutoRestoreFromDisk()
        _favoriteMixes.value = loadFavoriteMixesFromPrefs()
        loadArtistTagsFromPrefs()

        // Restore saved folder or nax.moe on launch if present
        val savedUriString = prefs.getString("folder_uri", null)
        val naxMoeType = prefs.getString("nax_moe_type", null)
        val hasCompletedFirstStart = prefs.getBoolean("first_start_completed", false)

        if (!hasCompletedFirstStart && savedUriString.isNullOrBlank() && naxMoeType == null) {
            _showFirstStartPrompt.value = true
        } else if (!savedUriString.isNullOrBlank()) {
            val uri = Uri.parse(savedUriString)
            _isNaxMoeDatabase.value = false
            loadFromTreeUri(getApplication(), uri, isInitialLoad = true)
        } else if (naxMoeType == "constrained") {
            _isNaxMoeDatabase.value = true
            loadNaxMoeConstrainedGallery()
        } else if (naxMoeType == "loose" || (hasCompletedFirstStart && prefs.getBoolean("is_nax_moe", false))) {
            _isNaxMoeDatabase.value = true
            loadNaxMoeLooseGallery()
        } else if (!_showFirstStartPrompt.value && availablePictures.value.isEmpty()) {
            _showFirstStartPrompt.value = true
        }
    }

    private fun tryAutoRestoreFromDisk() {
        val app = getApplication<Application>()
        val localFile = File(app.filesDir, "nai_artist_mixer_backup.json")
        val extDir = app.getExternalFilesDir(null)
        val extFile = if (extDir != null) File(extDir, "nai_artist_mixer_backup.json") else null

        val targetFile = when {
            localFile.exists() -> localFile
            extFile != null && extFile.exists() -> extFile
            else -> null
        }

        if (targetFile != null) {
            runCatching {
                val jsonText = targetFile.readText()
                if (jsonText.isNotBlank()) {
                    val json = JSONObject(jsonText)
                    if (_favoriteArtistNames.value.isEmpty() && json.has("favorites")) {
                        importDataFromJson(jsonText, isAutoRestore = true)
                    }
                }
            }
        }
    }

    fun saveBackupToDisk() {
        try {
            val jsonStr = exportDataJson()
            val app = getApplication<Application>()
            val file = File(app.filesDir, "nai_artist_mixer_backup.json")
            file.writeText(jsonStr)

            val extDir = app.getExternalFilesDir(null)
            if (extDir != null) {
                val extFile = File(extDir, "nai_artist_mixer_backup.json")
                extFile.writeText(jsonStr)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportDataJson(): String {
        val json = JSONObject()
        json.put("version", 1)
        json.put("favorites", JSONArray(_favoriteArtistNames.value))
        json.put("swiped", JSONArray(_swipedArtistNames.value))
        json.put("requested_count", _requestedCount.value)
        json.put("allow_duplicates", _allowDuplicates.value)
        json.put("app_theme_preset", _appThemePreset.value.name)
        json.put("is_dark_theme", _isDarkTheme.value)

        val fmt = _formattingOptions.value
        val fmtJson = JSONObject()
        fmtJson.put("remove_extensions", fmt.removeExtensions)
        fmtJson.put("replace_separators", fmt.replaceSeparatorsWithSpaces)
        fmtJson.put("separator_type", fmt.separatorType.name)
        fmtJson.put("custom_separator", fmt.customSeparator)
        fmtJson.put("casing", fmt.casing.name)
        json.put("formatting_options", fmtJson)

        val naxType = prefs.getString("nax_moe_type", null)
        if (naxType != null) json.put("nax_moe_type", naxType)
        val fName = _folderName.value
        if (fName.isNotBlank()) json.put("folder_name", fName)

        val mixArray = JSONArray()
        for (mix in _favoriteMixes.value) {
            val mObj = JSONObject()
            mObj.put("id", mix.id)
            mObj.put("title", mix.title)
            mObj.put("imagePath", mix.imagePath ?: "")
            if (mix.seed != null) mObj.put("seed", mix.seed)
            mObj.put("timestamp", mix.timestamp)
            val pArray = JSONArray()
            for (p in mix.pictures) {
                val pObj = JSONObject()
                pObj.put("id", p.id)
                pObj.put("fileName", p.fileName)
                pObj.put("uriString", p.uriString)
                pObj.put("sizeBytes", p.sizeBytes)
                pObj.put("mimeType", p.mimeType)
                pObj.put("sourceType", p.sourceType.name)
                pObj.put("isLocked", p.isLocked)
                pArray.put(pObj)
            }
            mObj.put("pictures", pArray)
            mixArray.put(mObj)
        }
        json.put("favorite_mixes", mixArray)

        return json.toString(2)
    }

    fun importDataFromJson(jsonString: String, isAutoRestore: Boolean = false): Boolean {
        return try {
            val json = JSONObject(jsonString)

            if (json.has("favorite_mixes")) {
                val mixes = parseFavoriteMixesJson(json.getJSONArray("favorite_mixes").toString())
                _favoriteMixes.value = mixes
                saveFavoriteMixesToPrefs(mixes)
            }

            // 1. Favorites
            if (json.has("favorites")) {
                val favArray = json.getJSONArray("favorites")
                val favs = mutableSetOf<String>()
                for (i in 0 until favArray.length()) {
                    favs.add(favArray.getString(i))
                }
                _favoriteArtistNames.value = favs
                prefs.edit().putStringSet("favorite_artists", favs).apply()
            }

            // 2. Swiped
            if (json.has("swiped")) {
                val swipedArray = json.getJSONArray("swiped")
                val swiped = mutableSetOf<String>()
                for (i in 0 until swipedArray.length()) {
                    swiped.add(swipedArray.getString(i))
                }
                _swipedArtistNames.value = swiped
                prefs.edit().putStringSet("swiped_artists", swiped).apply()
            }

            // 3. Requested Count
            if (json.has("requested_count")) {
                val count = json.getInt("requested_count")
                _requestedCount.value = count
                prefs.edit().putInt("requested_count", count).apply()
            }

            // 4. Allow Duplicates
            if (json.has("allow_duplicates")) {
                val dupes = json.getBoolean("allow_duplicates")
                _allowDuplicates.value = dupes
                prefs.edit().putBoolean("allow_duplicates", dupes).apply()
            }

            // 5. Theme Preset
            if (json.has("app_theme_preset")) {
                runCatching {
                    val themeName = json.getString("app_theme_preset")
                    val preset = AppThemePreset.valueOf(themeName)
                    _appThemePreset.value = preset
                    prefs.edit().putString("app_theme_preset", themeName).apply()
                }
            }

            // 6. Dark Theme
            if (json.has("is_dark_theme")) {
                val dark = json.getBoolean("is_dark_theme")
                _isDarkTheme.value = dark
                prefs.edit().putBoolean("is_dark_theme", dark).apply()
            }

            // 7. Formatting
            if (json.has("formatting_options")) {
                val fmtJson = json.getJSONObject("formatting_options")
                val removeExt = fmtJson.optBoolean("remove_extensions", true)
                val replaceSep = fmtJson.optBoolean("replace_separators", false)
                val sepType = runCatching { SeparatorType.valueOf(fmtJson.optString("separator_type", SeparatorType.COMMA_SPACE.name)) }.getOrDefault(SeparatorType.COMMA_SPACE)
                val customSep = fmtJson.optString("custom_separator", ", ")
                val casing = runCatching { TextCasing.valueOf(fmtJson.optString("casing", TextCasing.AS_IS.name)) }.getOrDefault(TextCasing.AS_IS)

                val newFmt = FormattingOptions(removeExt, replaceSep, sepType, customSep, casing)
                _formattingOptions.value = newFmt

                prefs.edit()
                    .putBoolean("remove_extensions", removeExt)
                    .putBoolean("replace_separators", replaceSep)
                    .putString("separator_type", sepType.name)
                    .putString("custom_separator", customSep)
                    .putString("casing", casing.name)
                    .apply()
            }

            saveBackupToDisk()

            if (!isAutoRestore) {
                _toastMessage.value = "Data imported successfully! Favorites and settings restored."
            }
            true
        } catch (e: Exception) {
            if (!isAutoRestore) {
                _toastMessage.value = "Import failed: Invalid JSON file format."
            }
            false
        }
    }

    fun dismissFirstStartPrompt() {
        _showFirstStartPrompt.value = false
        prefs.edit().putBoolean("first_start_completed", true).apply()
    }

    fun toggleDarkTheme() {
        val next = !_isDarkTheme.value
        _isDarkTheme.value = next
        prefs.edit().putBoolean("is_dark_theme", next).apply()
    }

    private val _headerColorArgb = MutableStateFlow(
        prefs.getLong("theme_header_argb", AppThemePreset.NOVEL_AI_DARK.textHeadings.toArgb().toLong())
    )
    val headerColorArgb: StateFlow<Long> = _headerColorArgb.asStateFlow()

    private val _paragraphColorArgb = MutableStateFlow(
        prefs.getLong("theme_paragraph_argb", AppThemePreset.NOVEL_AI_DARK.textMain.toArgb().toLong())
    )
    val paragraphColorArgb: StateFlow<Long> = _paragraphColorArgb.asStateFlow()

    private val _warningColorArgb = MutableStateFlow(
        prefs.getLong("theme_warning_argb", 0xFFFF7878L)
    )
    val warningColorArgb: StateFlow<Long> = _warningColorArgb.asStateFlow()

    private val _foregroundColorArgb = MutableStateFlow(
        prefs.getLong("theme_foreground_argb", AppThemePreset.NOVEL_AI_DARK.bg3.toArgb().toLong())
    )
    val foregroundColorArgb: StateFlow<Long> = _foregroundColorArgb.asStateFlow()

    private val _backgroundColorArgb = MutableStateFlow(
        prefs.getLong("theme_background_argb", AppThemePreset.NOVEL_AI_DARK.bg2.toArgb().toLong())
    )
    val backgroundColorArgb: StateFlow<Long> = _backgroundColorArgb.asStateFlow()

    private val _darkBgColorArgb = MutableStateFlow(
        prefs.getLong("theme_dark_bg_argb", AppThemePreset.NOVEL_AI_DARK.bg1.toArgb().toLong())
    )
    val darkBgColorArgb: StateFlow<Long> = _darkBgColorArgb.asStateFlow()

    private val _inputBgColorArgb = MutableStateFlow(
        prefs.getLong("theme_input_bg_argb", AppThemePreset.NOVEL_AI_DARK.bg0.toArgb().toLong())
    )
    val inputBgColorArgb: StateFlow<Long> = _inputBgColorArgb.asStateFlow()

    private val _headingsFont = MutableStateFlow(
        prefs.getString("theme_headings_font", AppThemePreset.NOVEL_AI_DARK.headingsFont) ?: "Eczar"
    )
    val headingsFont: StateFlow<String> = _headingsFont.asStateFlow()

    private val _defaultFont = MutableStateFlow(
        prefs.getString("theme_default_font", AppThemePreset.NOVEL_AI_DARK.defaultFont) ?: "Source Sans Pro"
    )
    val defaultFont: StateFlow<String> = _defaultFont.asStateFlow()

    // Backwards compatibility flows
    val customPrimaryColorArgb: StateFlow<Long> = _headerColorArgb
    val customBgColorArgb: StateFlow<Long> = _backgroundColorArgb

    fun setAppThemePreset(preset: AppThemePreset) {
        _appThemePreset.value = preset
        _isDarkTheme.value = preset.isDarkTheme

        val header = preset.textHeadings.toArgb().toLong()
        val paragraph = preset.textMain.toArgb().toLong()
        val warning = 0xFFFF7878L
        val fg = preset.bg3.toArgb().toLong()
        val bg = preset.bg2.toArgb().toLong()
        val dbg = preset.bg1.toArgb().toLong()
        val ibg = preset.bg0.toArgb().toLong()

        _headerColorArgb.value = header
        _paragraphColorArgb.value = paragraph
        _warningColorArgb.value = warning
        _foregroundColorArgb.value = fg
        _backgroundColorArgb.value = bg
        _darkBgColorArgb.value = dbg
        _inputBgColorArgb.value = ibg
        _headingsFont.value = preset.headingsFont
        _defaultFont.value = preset.defaultFont

        prefs.edit()
            .putString("app_theme_preset", preset.name)
            .putBoolean("is_dark_theme", preset.isDarkTheme)
            .putLong("theme_header_argb", header)
            .putLong("theme_paragraph_argb", paragraph)
            .putLong("theme_warning_argb", warning)
            .putLong("theme_foreground_argb", fg)
            .putLong("theme_background_argb", bg)
            .putLong("theme_dark_bg_argb", dbg)
            .putLong("theme_input_bg_argb", ibg)
            .putString("theme_headings_font", preset.headingsFont)
            .putString("theme_default_font", preset.defaultFont)
            .apply()

        _toastMessage.value = "Theme updated: ${preset.displayName}"
    }

    fun updateThemeFonts(headings: String, default: String) {
        _headingsFont.value = headings
        _defaultFont.value = default
        _appThemePreset.value = AppThemePreset.CUSTOM
        prefs.edit()
            .putString("app_theme_preset", AppThemePreset.CUSTOM.name)
            .putString("theme_headings_font", headings)
            .putString("theme_default_font", default)
            .apply()
    }

    fun swipeArtist(artistName: String, isMatch: Boolean) {
        if (artistName.isBlank()) return
        val currentSwiped = _swipedArtistNames.value.toMutableSet()
        currentSwiped.add(artistName)
        _swipedArtistNames.value = currentSwiped
        prefs.edit().putStringSet("swiped_artists", currentSwiped).apply()

        if (isMatch) {
            val currentFavs = _favoriteArtistNames.value.toMutableSet()
            currentFavs.add(artistName)
            _favoriteArtistNames.value = currentFavs
            prefs.edit().putStringSet("favorite_artists", currentFavs).apply()
        }
        saveBackupToDisk()
    }

    fun updateThemeColor(
        header: Long = _headerColorArgb.value,
        paragraph: Long = _paragraphColorArgb.value,
        warning: Long = _warningColorArgb.value,
        foreground: Long = _foregroundColorArgb.value,
        background: Long = _backgroundColorArgb.value,
        darkBg: Long = _darkBgColorArgb.value,
        inputBg: Long = _inputBgColorArgb.value
    ) {
        _headerColorArgb.value = header
        _paragraphColorArgb.value = paragraph
        _warningColorArgb.value = warning
        _foregroundColorArgb.value = foreground
        _backgroundColorArgb.value = background
        _darkBgColorArgb.value = darkBg
        _inputBgColorArgb.value = inputBg

        _appThemePreset.value = AppThemePreset.CUSTOM

        prefs.edit()
            .putString("app_theme_preset", AppThemePreset.CUSTOM.name)
            .putLong("theme_header_argb", header)
            .putLong("theme_paragraph_argb", paragraph)
            .putLong("theme_warning_argb", warning)
            .putLong("theme_foreground_argb", foreground)
            .putLong("theme_background_argb", background)
            .putLong("theme_dark_bg_argb", darkBg)
            .putLong("theme_input_bg_argb", inputBg)
            .apply()
    }

    fun setCustomColors(primaryArgb: Long, bgArgb: Long) {
        updateThemeColor(header = primaryArgb, background = bgArgb)
        _toastMessage.value = "Custom theme applied!"
    }

    suspend fun checkDatabaseVersions(artistName: String): DbVersionCheckResult {
        val context = getApplication<Application>()
        val cleanName = artistName.trim().substringBeforeLast('.')
        val isNaxMoe = _isNaxMoeDatabase.value

        if (isNaxMoe) {
            val isCurrentConstrained = _folderName.value.contains("constrained", ignoreCase = true)
            val oppositeDbName = if (isCurrentConstrained) "Loose Prompt" else "Constrained Prompt"
            val oppositeList = if (isCurrentConstrained) {
                NaxMoeFetcher.fetchLooseGalleryPictures(context)
            } else {
                NaxMoeFetcher.fetchConstrainedGalleryPictures(context)
            }
            val match = oppositeList.firstOrNull {
                it.fileName.trim().substringBeforeLast('.').equals(cleanName, ignoreCase = true)
            }
            val options = if (match != null) listOf(AlternateDbOption(match, oppositeDbName)) else emptyList()
            return DbVersionCheckResult(options, oppositeDbName)
        } else {
            val looseList = NaxMoeFetcher.fetchLooseGalleryPictures(context)
            val constrainedList = NaxMoeFetcher.fetchConstrainedGalleryPictures(context)

            val looseMatch = looseList.firstOrNull {
                it.fileName.trim().substringBeforeLast('.').equals(cleanName, ignoreCase = true)
            }
            val constrainedMatch = constrainedList.firstOrNull {
                it.fileName.trim().substringBeforeLast('.').equals(cleanName, ignoreCase = true)
            }

            val options = mutableListOf<AlternateDbOption>()
            if (looseMatch != null) {
                options.add(AlternateDbOption(looseMatch, "Loose Prompt"))
            }
            if (constrainedMatch != null) {
                options.add(AlternateDbOption(constrainedMatch, "Constrained Prompt"))
            }

            return DbVersionCheckResult(options, "nax.moe")
        }
    }

    suspend fun checkOppositeDatabaseVersion(artistName: String): Pair<PictureItem?, String> {
        val result = checkDatabaseVersions(artistName)
        val firstMatch = result.options.firstOrNull()
        return Pair(firstMatch?.matchPic, result.defaultOppositeName)
    }

    suspend fun getMatchDeckArtists(): List<PictureItem> {
        val context = getApplication<Application>()
        val swiped = _swipedArtistNames.value
        val favorites = _favoriteArtistNames.value
        var sourceList = _availablePictures.value

        if (sourceList.isEmpty()) {
            sourceList = NaxMoeFetcher.fetchLooseGalleryPictures(context)
        }

        val unseen = sourceList.filter { it.fileName !in swiped && it.fileName !in favorites }
        return unseen.shuffled()
    }

    fun toggleFavoriteArtist(artistName: String) {
        if (artistName.isBlank()) return
        val currentSet = _favoriteArtistNames.value.toMutableSet()
        val isFav = currentSet.contains(artistName)
        if (isFav) {
            currentSet.remove(artistName)
            _toastMessage.value = "Removed '$artistName' from favorites"
            val currentMap = _artistToTags.value.toMutableMap()
            if (currentMap.containsKey(artistName)) {
                currentMap.remove(artistName)
                _artistToTags.value = currentMap
                saveArtistTagsToPrefs()
            }
        } else {
            currentSet.add(artistName)
            _toastMessage.value = "Added '$artistName' to favorites ❤️"
        }
        _favoriteArtistNames.value = currentSet
        prefs.edit().putStringSet("favorite_artists", currentSet).apply()
        saveBackupToDisk()
    }

    fun addFavoriteArtist(artistName: String) {
        if (artistName.isBlank()) return
        val currentSet = _favoriteArtistNames.value.toMutableSet()
        if (!currentSet.contains(artistName)) {
            currentSet.add(artistName)
            _favoriteArtistNames.value = currentSet
            prefs.edit().putStringSet("favorite_artists", currentSet).apply()
            saveBackupToDisk()
        }
    }

    // --- TAG MANAGEMENT METHODS ---
    fun createMixTag(tag: String) {
        val clean = tag.trim()
        if (clean.isBlank()) return
        val current = _mixTags.value.toMutableSet()
        current.add(clean)
        _mixTags.value = current
        saveMixTagsToPrefs()
        saveBackupToDisk()
    }

    fun deleteMixTag(tag: String) {
        val current = _mixTags.value.toMutableSet()
        current.remove(tag)
        _mixTags.value = current
        val updatedMixes = _favoriteMixes.value.map { mix ->
            if (mix.tags.contains(tag)) mix.copy(tags = mix.tags - tag) else mix
        }
        _favoriteMixes.value = updatedMixes
        saveFavoriteMixesToPrefs(updatedMixes)
        saveMixTagsToPrefs()
        saveBackupToDisk()
    }

    fun renameMixTag(oldTag: String, newTag: String) {
        val clean = newTag.trim()
        if (clean.isBlank() || oldTag == clean) return
        val current = _mixTags.value.toMutableSet()
        if (current.remove(oldTag)) {
            current.add(clean)
            _mixTags.value = current
            saveMixTagsToPrefs()
        }
        val updatedMixes = _favoriteMixes.value.map { mix ->
            if (mix.tags.contains(oldTag)) {
                val newTags = mix.tags.map { if (it == oldTag) clean else it }.distinct()
                mix.copy(tags = newTags)
            } else mix
        }
        _favoriteMixes.value = updatedMixes
        saveFavoriteMixesToPrefs(updatedMixes)
        saveBackupToDisk()
    }

    fun updateFavoriteMixTags(mixId: String, newTags: List<String>) {
        val updatedList = _favoriteMixes.value.map { mix ->
            if (mix.id == mixId) mix.copy(tags = newTags.distinct()) else mix
        }
        _favoriteMixes.value = updatedList
        saveFavoriteMixesToPrefs(updatedList)
        saveBackupToDisk()
    }

    private fun saveMixTagsToPrefs() {
        prefs.edit().putStringSet("mix_tags", _mixTags.value).apply()
    }

    fun createArtistTag(tag: String) {
        val clean = tag.trim()
        if (clean.isBlank()) return
        val current = _artistUserTags.value.toMutableSet()
        current.add(clean)
        _artistUserTags.value = current
        saveArtistTagsToPrefs()
        saveBackupToDisk()
    }

    fun deleteArtistTag(tag: String) {
        val current = _artistUserTags.value.toMutableSet()
        current.remove(tag)
        _artistUserTags.value = current
        val currentMap = _artistToTags.value.toMutableMap()
        for ((artist, tags) in currentMap.entries.toList()) {
            if (tags.contains(tag)) {
                currentMap[artist] = tags - tag
            }
        }
        _artistToTags.value = currentMap
        saveArtistTagsToPrefs()
        saveBackupToDisk()
    }

    fun renameArtistTag(oldTag: String, newTag: String) {
        val clean = newTag.trim()
        if (clean.isBlank() || oldTag == clean) return
        val current = _artistUserTags.value.toMutableSet()
        if (current.remove(oldTag)) {
            current.add(clean)
            _artistUserTags.value = current
        }
        val currentMap = _artistToTags.value.toMutableMap()
        for ((artist, tags) in currentMap.entries.toList()) {
            if (tags.contains(oldTag)) {
                currentMap[artist] = (tags - oldTag + clean)
            }
        }
        _artistToTags.value = currentMap
        saveArtistTagsToPrefs()
        saveBackupToDisk()
    }

    fun setArtistTags(artistName: String, tags: Collection<String>) {
        val cleanName = artistName.trim()
        if (cleanName.isBlank()) return
        val currentMap = _artistToTags.value.toMutableMap()
        val distinctTags = tags.map { it.trim() }.filter { it.isNotBlank() }.toSet()
        if (distinctTags.isEmpty()) {
            currentMap.remove(cleanName)
        } else {
            currentMap[cleanName] = distinctTags
        }
        _artistToTags.value = currentMap
        saveArtistTagsToPrefs()
        saveBackupToDisk()
    }

    fun autoTagFavoriteArtists(): Int {
        val rules = mapOf(
            "cutesexyrobutts" to "western",
            "fugtrup" to "3D",
            "sade abyss" to "realistic",
            "wlop" to "painterly",
            "guweiz" to "cinematic",
            "sakimichan" to "digital",
            "artgerm" to "semi-realistic",
            "ilya kuvshinov" to "anime",
            "alphonse mucha" to "classic",
            "disney" to "western",
            "pixar" to "3D",
            "studio ghibli" to "anime",
            "kantoku" to "anime",
            "samdoesarts" to "western",
            "mika pikazo" to "vibrant",
            "redjuice" to "sci-fi",
            "fuzichoco" to "detailed",
            "dishwasher1910" to "sci-fi",
            "rurudo" to "cute"
        )

        var count = 0
        val currentToTags = _artistToTags.value.toMutableMap()
        val currentTags = _artistUserTags.value.toMutableSet()

        for (artistName in _favoriteArtistNames.value) {
            val lower = artistName.lowercase()
            val tagsToApply = mutableSetOf<String>()

            for ((key, tag) in rules) {
                if (lower.contains(key)) {
                    tagsToApply.add(tag)
                }
            }

            if (lower.contains("3d") || lower.contains("blender") || lower.contains("cgi")) {
                tagsToApply.add("3D")
            }
            if (lower.contains("west") || lower.contains("comic")) {
                tagsToApply.add("western")
            }
            if (lower.contains("real")) {
                tagsToApply.add("realistic")
            }

            if (tagsToApply.isNotEmpty()) {
                val existing = currentToTags[artistName] ?: emptySet()
                val merged = existing + tagsToApply
                if (merged != existing) {
                    currentToTags[artistName] = merged
                    currentTags.addAll(tagsToApply)
                    count++
                }
            }
        }

        if (count > 0) {
            _artistUserTags.value = currentTags
            _artistToTags.value = currentToTags
            saveArtistTagsToPrefs()
            saveBackupToDisk()
            _toastMessage.value = "Auto-tagged $count favorite artist(s)!"
        } else {
            _toastMessage.value = "No new auto-tags matched."
        }
        return count
    }

    private fun saveArtistTagsToPrefs() {
        prefs.edit().putStringSet("artist_user_tags", _artistUserTags.value).apply()
        val jsonMap = JSONObject()
        for ((artist, tags) in _artistToTags.value) {
            jsonMap.put(artist, JSONArray(tags.toList()))
        }
        prefs.edit().putString("artist_to_tags", jsonMap.toString()).apply()
    }

    private fun loadArtistTagsFromPrefs() {
        val jsonStr = prefs.getString("artist_to_tags", null) ?: return
        runCatching {
            val jsonMap = JSONObject(jsonStr)
            val map = mutableMapOf<String, Set<String>>()
            val keys = jsonMap.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val arr = jsonMap.getJSONArray(key)
                val tagSet = mutableSetOf<String>()
                for (i in 0 until arr.length()) {
                    tagSet.add(arr.getString(i))
                }
                map[key] = tagSet
            }
            _artistToTags.value = map
        }
    }

    fun addFavoriteToSelection(artistName: String) {
        val cleanName = artistName.trim().substringBeforeLast('.')
        val current = _currentResult.value
        val currentList = current?.selectedPictures ?: emptyList()
        val exists = currentList.any {
            it.fileName.trim().substringBeforeLast('.').equals(cleanName, ignoreCase = true)
        }
        if (exists) {
            _toastMessage.value = "$cleanName already present in selection"
            return
        }

        val allPics = _availablePictures.value
        val match = allPics.firstOrNull {
            it.fileName.trim().substringBeforeLast('.').equals(cleanName, ignoreCase = true)
        }
        if (match != null) {
            addManualPicture(match)
        } else {
            val customItem = PictureItem(
                id = "fav_${System.currentTimeMillis()}",
                fileName = cleanName,
                uriString = "https://cdn.zele.st/data/NAX/Images/danbooru-artist-tags-2-v4.5/${cleanName.replace("%", "%25")}.jpg",
                sourceType = PictureSourceType.WEB_GALLERY,
                isLocked = true
            )
            addManualPicture(customItem)
        }
    }

    fun loadFromTreeUri(context: Context, treeUri: Uri, isInitialLoad: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {}

                val (name, pics) = FolderReader.readPicturesFromTreeUri(context, treeUri)
                if (pics.isEmpty()) {
                    _toastMessage.value = "No image files found in selected folder."
                    if (isInitialLoad) {
                        _showFirstStartPrompt.value = true
                    }
                } else {
                    _folderName.value = name
                    _folderUri.value = treeUri
                    _availablePictures.value = pics
                    _showFirstStartPrompt.value = false
                    _isNaxMoeDatabase.value = false

                    prefs.edit()
                        .putString("folder_uri", treeUri.toString())
                        .putString("folder_name", name)
                        .remove("nax_moe_type")
                        .putBoolean("is_nax_moe", false)
                        .putBoolean("first_start_completed", true)
                        .apply()

                    _toastMessage.value = "Loaded '$name' with ${pics.size} pictures!"
                    pickRandomPictures()
                }
            } catch (e: Exception) {
                _toastMessage.value = "Error reading folder: ${e.localizedMessage}"
                if (isInitialLoad) {
                    _showFirstStartPrompt.value = true
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFromMultipleUris(context: Context, uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val pics = FolderReader.readPicturesFromUris(context, uris)
                val name = "Selected (${pics.size} Photos)"
                _folderName.value = name
                _folderUri.value = null
                _availablePictures.value = pics
                _showFirstStartPrompt.value = false
                _isNaxMoeDatabase.value = false

                prefs.edit()
                    .remove("folder_uri")
                    .putString("folder_name", name)
                    .remove("nax_moe_type")
                    .putBoolean("is_nax_moe", false)
                    .putBoolean("first_start_completed", true)
                    .apply()

                _toastMessage.value = "Loaded ${pics.size} custom photos!"
                pickRandomPictures()
            } catch (e: Exception) {
                _toastMessage.value = "Error loading images: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNaxMoeLooseGallery() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val pics = NaxMoeFetcher.fetchLooseGalleryPictures(getApplication())
                val name = "Danbooru Artists (loose prompt)"
                _folderName.value = name
                _folderUri.value = null
                _availablePictures.value = pics
                _showFirstStartPrompt.value = false
                _isNaxMoeDatabase.value = true

                prefs.edit()
                    .remove("folder_uri")
                    .putString("folder_name", name)
                    .putString("nax_moe_type", "loose")
                    .putBoolean("is_nax_moe", true)
                    .putBoolean("first_start_completed", true)
                    .apply()

                _toastMessage.value = "Loaded ${pics.size} Loose Danbooru artists!"
                // Clear current result and pick new selection
                _currentResult.value = null
                pickRandomPictures()
            } catch (e: Exception) {
                _toastMessage.value = "Error loading loose gallery: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNaxMoeConstrainedGallery() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val pics = NaxMoeFetcher.fetchConstrainedGalleryPictures(getApplication())
                val name = "Danbooru Artists (constrained prompt)"
                _folderName.value = name
                _folderUri.value = null
                _availablePictures.value = pics
                _showFirstStartPrompt.value = false
                _isNaxMoeDatabase.value = true

                prefs.edit()
                    .remove("folder_uri")
                    .putString("folder_name", name)
                    .putString("nax_moe_type", "constrained")
                    .putBoolean("is_nax_moe", true)
                    .putBoolean("first_start_completed", true)
                    .apply()

                _toastMessage.value = "Loaded ${pics.size} Constrained Danbooru artists!"
                // Clear current result and pick new selection
                _currentResult.value = null
                pickRandomPictures()
            } catch (e: Exception) {
                _toastMessage.value = "Error loading constrained gallery: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun movePicture(fromIndex: Int, toIndex: Int) {
        val current = _currentResult.value ?: return
        val currentList = current.selectedPictures.toMutableList()
        if (fromIndex !in currentList.indices || toIndex !in currentList.indices || fromIndex == toIndex) return

        val item = currentList.removeAt(fromIndex)
        currentList.add(toIndex, item)

        val newFormatted = NameFormatter.formatPictureNames(currentList, _formattingOptions.value)
        _currentResult.value = current.copy(
            selectedPictures = currentList,
            formattedString = newFormatted
        )
        _toastMessage.value = "Moved '${item.fileName}' to position #${toIndex + 1}"
    }

    fun setRequestedCount(count: Int) {
        val current = _currentResult.value
        val lockedCount = current?.selectedPictures?.count { it.isLocked } ?: 0
        val minAllowed = maxOf(0, lockedCount)

        if (count < minAllowed) {
            _toastMessage.value = "Cannot decrease count below $lockedCount locked picture(s)"
            return
        }

        val clamped = count.coerceAtLeast(minAllowed)
        _requestedCount.value = clamped
        prefs.edit().putInt("requested_count", clamped).apply()

        val allPics = _availablePictures.value
        if (allPics.isEmpty()) return

        val currentList = current?.selectedPictures ?: emptyList()

        val newList = if (clamped == 0) {
            emptyList()
        } else if (clamped < currentList.size) {
            val mutableList = currentList.toMutableList()
            var itemsToRemove = currentList.size - clamped
            while (itemsToRemove > 0 && mutableList.isNotEmpty()) {
                val lastUnlockedIndex = mutableList.indexOfLast { !it.isLocked }
                if (lastUnlockedIndex != -1) {
                    mutableList.removeAt(lastUnlockedIndex)
                } else {
                    mutableList.removeAt(mutableList.lastIndex)
                }
                itemsToRemove--
            }
            mutableList
        } else if (clamped > currentList.size) {
            val needed = clamped - currentList.size
            val allowDupes = _allowDuplicates.value

            val existingIds = currentList.map { it.id }.toSet()
            val unusedPics = allPics.filter { it.id !in existingIds }

            val additions = if (unusedPics.isNotEmpty()) {
                if (allowDupes) {
                    List(needed) { unusedPics.random() }
                } else {
                    unusedPics.shuffled().take(needed)
                }
            } else {
                if (allowDupes) {
                    List(needed) { allPics.random() }
                } else {
                    allPics.shuffled().take(needed)
                }
            }
            currentList + additions
        } else {
            currentList
        }

        val newFormatted = NameFormatter.formatPictureNames(newList, _formattingOptions.value)
        val updated = SelectionResult(
            folderName = current?.folderName ?: _folderName.value,
            countRequested = clamped,
            selectedPictures = newList,
            formattedString = newFormatted
        )
        _currentResult.value = updated
    }

    fun setAllowDuplicates(allow: Boolean) {
        _allowDuplicates.value = allow
        prefs.edit().putBoolean("allow_duplicates", allow).apply()
    }

    fun updateFormattingOptions(options: FormattingOptions) {
        _formattingOptions.value = options

        prefs.edit()
            .putBoolean("remove_extensions", options.removeExtensions)
            .putBoolean("replace_separators", options.replaceSeparatorsWithSpaces)
            .putString("separator_type", options.separatorType.name)
            .putString("custom_separator", options.customSeparator)
            .putString("casing", options.casing.name)
            .apply()

        val current = _currentResult.value
        if (current != null) {
            val newFormatted = NameFormatter.formatPictureNames(current.selectedPictures, options)
            _currentResult.value = current.copy(formattedString = newFormatted)
        }
    }

    fun toggleLockPicture(index: Int) {
        val current = _currentResult.value ?: return
        val currentList = current.selectedPictures.toMutableList()
        if (index !in currentList.indices) return

        val item = currentList[index]
        val updatedItem = item.copy(isLocked = !item.isLocked)
        currentList[index] = updatedItem

        val newFormatted = NameFormatter.formatPictureNames(currentList, _formattingOptions.value)
        _currentResult.value = current.copy(
            selectedPictures = currentList,
            formattedString = newFormatted
        )
        saveLockedPicturesToPrefs(currentList)

        val statusStr = if (updatedItem.isLocked) "Locked 🔒" else "Unlocked 🔓"
        _toastMessage.value = "${item.fileName} is now $statusStr"
    }

    fun addManualPicture(picture: PictureItem) {
        val cleanName = picture.fileName.trim().substringBeforeLast('.')
        val current = _currentResult.value
        val currentList = current?.selectedPictures ?: emptyList()

        val exists = currentList.any {
            it.fileName.trim().substringBeforeLast('.').equals(cleanName, ignoreCase = true)
        }
        if (exists) {
            _toastMessage.value = "$cleanName already present in selection"
            return
        }

        val newItem = picture.copy(fileName = cleanName, isLocked = true)

        val allAreLocked = currentList.isNotEmpty() && currentList.all { it.isLocked }

        val newList = if (currentList.isEmpty() || allAreLocked) {
            val newCount = currentList.size + 1
            _requestedCount.value = newCount
            prefs.edit().putInt("requested_count", newCount).apply()
            listOf(newItem) + currentList
        } else {
            val lastUnlockedIndex = currentList.indexOfLast { !it.isLocked }
            val mutable = currentList.toMutableList()
            if (lastUnlockedIndex != -1) {
                mutable.removeAt(lastUnlockedIndex)
            } else if (mutable.isNotEmpty()) {
                mutable.removeAt(mutable.lastIndex)
            }
            listOf(newItem) + mutable
        }

        val formattedText = NameFormatter.formatPictureNames(newList, _formattingOptions.value)
        val updatedResult = SelectionResult(
            folderName = _folderName.value,
            countRequested = newList.size,
            selectedPictures = newList,
            formattedString = formattedText
        )

        _currentResult.value = updatedResult
        _toastMessage.value = "$cleanName added to selection"
    }

    fun rerollSinglePicture(index: Int) {
        val current = _currentResult.value ?: return
        val currentList = current.selectedPictures.toMutableList()
        if (index !in currentList.indices) return

        val allPics = _availablePictures.value
        if (allPics.isEmpty()) return

        val currentlySelectedIds = currentList.map { it.id }.toSet()

        var candidates = allPics.filter { it.id !in currentlySelectedIds }
        if (candidates.isEmpty()) {
            val itemToReplace = currentList[index]
            candidates = allPics.filter { it.id != itemToReplace.id }
        }
        if (candidates.isEmpty()) {
            candidates = allPics
        }

        val replacement = candidates.random().copy(isLocked = false)
        currentList[index] = replacement

        val newFormatted = NameFormatter.formatPictureNames(currentList, _formattingOptions.value)
        val updatedResult = current.copy(
            selectedPictures = currentList,
            formattedString = newFormatted
        )

        _currentResult.value = updatedResult
    }

    fun removePictureAt(index: Int): Int {
        val current = _currentResult.value ?: return _requestedCount.value
        val currentList = current.selectedPictures.toMutableList()
        if (index !in currentList.indices) return _requestedCount.value

        val removedItem = currentList.removeAt(index)
        val newCount = (_requestedCount.value - 1).coerceAtLeast(0)
        _requestedCount.value = newCount
        prefs.edit().putInt("requested_count", newCount).apply()

        val newFormatted = NameFormatter.formatPictureNames(currentList, _formattingOptions.value)
        val updatedResult = current.copy(
            countRequested = currentList.size,
            selectedPictures = currentList,
            formattedString = newFormatted
        )
        _currentResult.value = updatedResult
        return currentList.size
    }

    fun addRandomArtistToSelection(): Int {
        val current = _currentResult.value
        val currentList = current?.selectedPictures ?: emptyList()
        val allPics = _availablePictures.value

        val newCount = _requestedCount.value + 1
        _requestedCount.value = newCount
        prefs.edit().putInt("requested_count", newCount).apply()

        if (allPics.isEmpty()) {
            return newCount
        }

        val selectedIds = currentList.map { it.id }.toSet()
        val unusedPics = allPics.filter { it.id !in selectedIds }

        val newArtist = if (unusedPics.isNotEmpty()) {
            unusedPics.random().copy(isLocked = false)
        } else {
            allPics.random().copy(isLocked = false)
        }

        val newList = currentList + newArtist
        val newFormatted = NameFormatter.formatPictureNames(newList, _formattingOptions.value)
        val updatedResult = SelectionResult(
            folderName = _folderName.value,
            countRequested = newList.size,
            selectedPictures = newList,
            formattedString = newFormatted
        )
        _currentResult.value = updatedResult
        return newList.size
    }

    fun resetPassedArtists() {
        val favs = _favoriteArtistNames.value
        _swipedArtistNames.value = favs.toSet()
        prefs.edit().putStringSet("swiped_artists", favs.toSet()).apply()
        _toastMessage.value = "Reset passed artists! Un-favorited artists can be swiped again."
    }

    fun pickRandomPictures() {
        val allPics = _availablePictures.value
        if (allPics.isEmpty()) {
            _toastMessage.value = "No pictures available to select."
            return
        }

        val count = _requestedCount.value
        val allowDupes = _allowDuplicates.value
        var currentList = _currentResult.value?.selectedPictures ?: emptyList()

        if (currentList.isEmpty()) {
            currentList = loadSavedLockedPicturesFromPrefs()
        }

        // Move locked items to the first positions (#1, #2, etc.)
        val lockedItems = currentList.filter { it.isLocked }
        val usedIds = lockedItems.map { it.id }.toMutableSet()

        val neededUnlockedCount = (count - lockedItems.size).coerceAtLeast(0)

        val candidatePool = if (allowDupes) {
            allPics
        } else {
            allPics.filter { it.id !in usedIds }.shuffled()
        }.toMutableList()

        val newUnlockedList = ArrayList<PictureItem>(neededUnlockedCount)
        if (candidatePool.isNotEmpty()) {
            if (allowDupes) {
                repeat(neededUnlockedCount) {
                    newUnlockedList.add(candidatePool.random().copy(isLocked = false))
                }
            } else {
                for (i in 0 until neededUnlockedCount) {
                    if (i < candidatePool.size) {
                        newUnlockedList.add(candidatePool[i].copy(isLocked = false))
                    } else {
                        newUnlockedList.add(allPics.random().copy(isLocked = false))
                    }
                }
            }
        } else {
            repeat(neededUnlockedCount) {
                newUnlockedList.add(allPics.random().copy(isLocked = false))
            }
        }

        // Combine: locked items come first, followed by newly rolled items
        val newList = (lockedItems + newUnlockedList).take(count)

        val formattedText = NameFormatter.formatPictureNames(newList, _formattingOptions.value)

        val result = SelectionResult(
            folderName = _folderName.value,
            countRequested = newList.size,
            selectedPictures = newList,
            formattedString = formattedText
        )

        _currentResult.value = result
        saveLockedPicturesToPrefs(newList)
    }

    private fun saveLockedPicturesToPrefs(pictures: List<PictureItem>) {
        val locked = pictures.filter { it.isLocked }
        val jsonArray = org.json.JSONArray()
        for (pic in locked) {
            val obj = org.json.JSONObject().apply {
                put("id", pic.id)
                put("fileName", pic.fileName)
                put("uriString", pic.uriString)
                put("sizeBytes", pic.sizeBytes)
                put("mimeType", pic.mimeType)
                put("sourceType", pic.sourceType.name)
                put("isLocked", true)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("saved_locked_pictures", jsonArray.toString()).apply()
    }

    private fun loadSavedLockedPicturesFromPrefs(): List<PictureItem> {
        val jsonStr = prefs.getString("saved_locked_pictures", null) ?: return emptyList()
        return try {
            val jsonArray = org.json.JSONArray(jsonStr)
            val list = mutableListOf<PictureItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val sourceType = runCatching {
                    PictureSourceType.valueOf(obj.optString("sourceType", PictureSourceType.WEB_GALLERY.name))
                }.getOrDefault(PictureSourceType.WEB_GALLERY)
                list.add(
                    PictureItem(
                        id = obj.optString("id", "saved_$i"),
                        fileName = obj.optString("fileName", ""),
                        uriString = obj.optString("uriString", ""),
                        sizeBytes = obj.optLong("sizeBytes", 0L),
                        mimeType = obj.optString("mimeType", "image/*"),
                        sourceType = sourceType,
                        isLocked = true
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun copyResultToClipboard(context: Context, customText: String? = null) {
        val textToCopy = customText ?: _currentResult.value?.formattedString
        if (textToCopy.isNullOrBlank()) {
            _toastMessage.value = "Nothing to copy."
            return
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Selected Picture Names", textToCopy)
        clipboard.setPrimaryClip(clip)
        _toastMessage.value = "Copied to clipboard! 📋"
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    // NovelAI Generator Methods
    fun updateNaiApiKey(key: String) {
        val cleanKey = key.trim()
        _naiApiKey.value = cleanKey
        prefs.edit().putString("nai_api_key", cleanKey).apply()
    }

    fun updateNaiMainPrompt(prompt: String) {
        _naiMainPrompt.value = prompt
        prefs.edit().putString("nai_main_prompt", prompt).apply()
    }

    fun updateNaiUc(uc: String) {
        _naiUc.value = uc
        prefs.edit().putString("nai_uc", uc).apply()
    }

    fun updateNaiCharacter(character: String) {
        _naiCharacter.value = character
        prefs.edit().putString("nai_character", character).apply()
    }

    fun updateNaiModel(model: String) {
        _naiModel.value = model
        prefs.edit().putString("nai_model", model).apply()
    }

    fun updateNaiResolution(width: Int, height: Int) {
        _naiWidth.value = width
        _naiHeight.value = height
        prefs.edit().putInt("nai_width", width).putInt("nai_height", height).apply()
    }

    fun updateNaiSeed(seed: Long) {
        _naiSeed.value = seed
        prefs.edit().putLong("nai_seed", seed).apply()
    }

    fun updateNaiScale(scale: Double) {
        _naiScale.value = scale
        prefs.edit().putFloat("nai_scale", scale.toFloat()).apply()
    }

    fun resetNaiSettingsToDefault() {
        _naiMainPrompt.value = defaultNaiMainPrompt
        _naiUc.value = defaultNaiUc
        _naiCharacter.value = defaultNaiCharacter
        _naiModel.value = "nai-diffusion-4-5-full"
        _naiWidth.value = 832
        _naiHeight.value = 1216
        _naiSeed.value = defaultNaiSeed
        _naiScale.value = 4.0

        prefs.edit()
            .putString("nai_main_prompt", defaultNaiMainPrompt)
            .putString("nai_uc", defaultNaiUc)
            .putString("nai_character", defaultNaiCharacter)
            .putString("nai_model", "nai-diffusion-4-5-full")
            .putInt("nai_width", 832)
            .putInt("nai_height", 1216)
            .putLong("nai_seed", defaultNaiSeed)
            .putFloat("nai_scale", 4.0f)
            .apply()

        _toastMessage.value = "NovelAI settings reset to defaults."
    }

    fun generateNovelAiImage(
        artistTags: String,
        mainPrompt: String = _naiMainPrompt.value,
        uc: String = _naiUc.value,
        character: String = _naiCharacter.value,
        overrideSeed: Long? = null
    ) {
        val key = _naiApiKey.value.trim()
        if (key.isBlank()) {
            _naiError.value = "NovelAI API Key is missing. Please enter your API key in Settings."
            return
        }

        viewModelScope.launch {
            _naiGenerating.value = true
            _naiError.value = null

            val targetSeed = overrideSeed ?: _naiSeed.value

            val result = NovelAiGenerator.generateImage(
                apiKey = key,
                artistTags = artistTags,
                mainPrompt = mainPrompt,
                ucPrompt = uc,
                characterPrompt = character,
                model = _naiModel.value,
                width = _naiWidth.value,
                height = _naiHeight.value,
                scale = _naiScale.value,
                seed = targetSeed
            )

            _naiGenerating.value = false
            result.fold(
                onSuccess = { genResult ->
                    _lastGeneratedArtistTags.value = artistTags
                    _lastGeneratedSeed.value = genResult.actualSeed
                    _naiGeneratedBitmap.value = genResult.bitmap
                },
                onFailure = { error ->
                    _naiError.value = error.message ?: "Failed to generate image."
                }
            )
        }
    }

    fun restoreGeneratedImage(bitmap: Bitmap?, artistTags: String, seed: Long) {
        _naiGeneratedBitmap.value = bitmap
        _lastGeneratedArtistTags.value = artistTags
        _lastGeneratedSeed.value = seed
    }

    fun clearNaiGenerationState() {
        _naiGeneratedBitmap.value = null
        _naiError.value = null
        _naiGenerating.value = false
        _lastGeneratedArtistTags.value = null
        _lastGeneratedSeed.value = null
    }

    // --- FAVORITE MIXES MANAGEMENT ---
    fun saveFavoriteMix(
        pictures: List<PictureItem>,
        bitmap: Bitmap?,
        seed: Long? = null,
        tags: List<String> = emptyList()
    ): String {
        if (pictures.isEmpty()) {
            _toastMessage.value = "Cannot save empty artist mix."
            return ""
        }

        val mixId = UUID.randomUUID().toString()
        var savedImagePath: String? = null

        if (bitmap != null) {
            try {
                val app = getApplication<Application>()
                val mixDir = File(app.filesDir, "favorite_mix_images")
                if (!mixDir.exists()) mixDir.mkdirs()
                val imgFile = File(mixDir, "mix_$mixId.png")
                FileOutputStream(imgFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                }
                savedImagePath = imgFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val mixNumber = _favoriteMixes.value.size + 1
        val title = "Mix#$mixNumber"

        val newMix = FavoriteMix(
            id = mixId,
            title = title,
            pictures = pictures,
            imagePath = savedImagePath,
            seed = seed,
            tags = tags.distinct(),
            timestamp = System.currentTimeMillis()
        )

        val updatedList = listOf(newMix) + _favoriteMixes.value
        _favoriteMixes.value = updatedList
        saveFavoriteMixesToPrefs(updatedList)
        saveBackupToDisk()

        val toastText = "$title saved in Favorite Mixes"
        _toastMessage.value = toastText
        return title
    }

    fun updateFavoriteMixTitle(mixId: String, newTitle: String) {
        val current = _favoriteMixes.value
        val updatedList = current.map { mix ->
            if (mix.id == mixId) mix.copy(title = newTitle.trim()) else mix
        }
        _favoriteMixes.value = updatedList
        saveFavoriteMixesToPrefs(updatedList)
        saveBackupToDisk()
        _toastMessage.value = "Updated mix name!"
    }

    fun removeFavoriteMix(mixId: String) {
        val current = _favoriteMixes.value
        val target = current.firstOrNull { it.id == mixId }
        if (target?.imagePath != null) {
            runCatching { File(target.imagePath).delete() }
        }
        val updatedList = current.filterNot { it.id == mixId }
        _favoriteMixes.value = updatedList
        saveFavoriteMixesToPrefs(updatedList)
        saveBackupToDisk()
        _toastMessage.value = "Mix removed from Favorite Mixes"
    }

    fun restoreFavoriteMix(mix: FavoriteMix) {
        restorePicturesList(mix.pictures)
        _toastMessage.value = "Restored mix with ${mix.pictures.size} artists"
    }

    fun restorePicturesList(pictures: List<PictureItem>) {
        val formattedText = NameFormatter.formatPictureNames(pictures, _formattingOptions.value)
        val count = pictures.size
        _requestedCount.value = count
        prefs.edit().putInt("requested_count", count).apply()
        _currentResult.value = SelectionResult(
            folderName = _folderName.value,
            countRequested = count,
            selectedPictures = pictures,
            formattedString = formattedText
        )
    }

    private fun saveFavoriteMixesToPrefs(list: List<FavoriteMix>) {
        try {
            val array = JSONArray()
            for (mix in list) {
                val mixObj = JSONObject()
                mixObj.put("id", mix.id)
                mixObj.put("title", mix.title)
                mixObj.put("imagePath", mix.imagePath ?: "")
                if (mix.seed != null) mixObj.put("seed", mix.seed)
                mixObj.put("timestamp", mix.timestamp)
                mixObj.put("tags", JSONArray(mix.tags))

                val picsArray = JSONArray()
                for (pic in mix.pictures) {
                    val picObj = JSONObject()
                    picObj.put("id", pic.id)
                    picObj.put("fileName", pic.fileName)
                    picObj.put("uriString", pic.uriString)
                    picObj.put("sizeBytes", pic.sizeBytes)
                    picObj.put("mimeType", pic.mimeType)
                    picObj.put("sourceType", pic.sourceType.name)
                    picObj.put("isLocked", pic.isLocked)
                    picsArray.put(picObj)
                }
                mixObj.put("pictures", picsArray)
                array.put(mixObj)
            }
            prefs.edit().putString("favorite_mixes_json", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadFavoriteMixesFromPrefs(): List<FavoriteMix> {
        val jsonStr = prefs.getString("favorite_mixes_json", null) ?: return emptyList()
        return parseFavoriteMixesJson(jsonStr)
    }

    private fun parseFavoriteMixesJson(jsonStr: String): List<FavoriteMix> {
        val result = mutableListOf<FavoriteMix>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val mixObj = array.getJSONObject(i)
                val id = mixObj.optString("id", UUID.randomUUID().toString())
                val title = mixObj.optString("title", "")
                val imagePath = mixObj.optString("imagePath", "").takeIf { it.isNotBlank() }
                val seed = if (mixObj.has("seed")) mixObj.getLong("seed") else null
                val timestamp = mixObj.optLong("timestamp", System.currentTimeMillis())

                val mixTagsList = mutableListOf<String>()
                if (mixObj.has("tags")) {
                    val tagsArr = mixObj.getJSONArray("tags")
                    for (k in 0 until tagsArr.length()) {
                        mixTagsList.add(tagsArr.getString(k))
                    }
                }

                val picturesList = mutableListOf<PictureItem>()
                if (mixObj.has("pictures")) {
                    val picsArray = mixObj.getJSONArray("pictures")
                    for (j in 0 until picsArray.length()) {
                        val pObj = picsArray.getJSONObject(j)
                        val pId = pObj.optString("id", "pic_$j")
                        val fileName = pObj.optString("fileName", "")
                        val uriString = pObj.optString("uriString", "")
                        val sizeBytes = pObj.optLong("sizeBytes", 0L)
                        val mimeType = pObj.optString("mimeType", "image/*")
                        val sourceTypeStr = pObj.optString("sourceType", PictureSourceType.SAMPLE_PRESET.name)
                        val sourceType = runCatching { PictureSourceType.valueOf(sourceTypeStr) }.getOrDefault(PictureSourceType.SAMPLE_PRESET)
                        val isLocked = pObj.optBoolean("isLocked", false)

                        picturesList.add(
                            PictureItem(
                                id = pId,
                                fileName = fileName,
                                uriString = uriString,
                                sizeBytes = sizeBytes,
                                mimeType = mimeType,
                                sourceType = sourceType,
                                isLocked = isLocked
                            )
                        )
                    }
                }
                result.add(
                    FavoriteMix(
                        id = id,
                        title = title,
                        pictures = picturesList,
                        imagePath = imagePath,
                        seed = seed,
                        tags = mixTagsList,
                        timestamp = timestamp
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}
