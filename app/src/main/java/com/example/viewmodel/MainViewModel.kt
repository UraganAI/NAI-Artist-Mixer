package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
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
            AppThemePreset.valueOf(prefs.getString("app_theme_preset", AppThemePreset.NOVEL_AI.name)!!)
        }.getOrDefault(AppThemePreset.NOVEL_AI)
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

    // Favorites state
    private val _favoriteArtistNames = MutableStateFlow<Set<String>>(
        prefs.getStringSet("favorite_artists", emptySet()) ?: emptySet()
    )
    val favoriteArtistNames: StateFlow<Set<String>> = _favoriteArtistNames.asStateFlow()

    private val _isNaxMoeDatabase = MutableStateFlow(false)
    val isNaxMoeDatabase: StateFlow<Boolean> = _isNaxMoeDatabase.asStateFlow()

    init {
        // Attempt automatic restoration from persistent disk backup if shared preferences is empty or missing favorites
        tryAutoRestoreFromDisk()

        // Restore saved folder or nax.moe on launch if present
        val savedUriString = prefs.getString("folder_uri", null)
        val naxMoeType = prefs.getString("nax_moe_type", null)
        val hasCompletedFirstStart = prefs.getBoolean("first_start_completed", false)

        if (!savedUriString.isNullOrBlank()) {
            val uri = Uri.parse(savedUriString)
            _isNaxMoeDatabase.value = false
            loadFromTreeUri(getApplication(), uri, isInitialLoad = true)
        } else if (naxMoeType == "constrained") {
            _isNaxMoeDatabase.value = true
            loadNaxMoeConstrainedGallery()
        } else if (naxMoeType == "loose" || prefs.getBoolean("is_nax_moe", false)) {
            _isNaxMoeDatabase.value = true
            loadNaxMoeLooseGallery()
        } else if (!hasCompletedFirstStart) {
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

        return json.toString(2)
    }

    fun importDataFromJson(jsonString: String, isAutoRestore: Boolean = false): Boolean {
        return try {
            val json = JSONObject(jsonString)

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

    fun setAppThemePreset(preset: AppThemePreset) {
        _appThemePreset.value = preset
        prefs.edit().putString("app_theme_preset", preset.name).apply()
        _toastMessage.value = "Theme updated: ${preset.displayName}"
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
            _toastMessage.value = "Matched '$artistName'! Added to favorites ❤️"
        }
        saveBackupToDisk()
    }

    private val _customPrimaryColorArgb = MutableStateFlow(
        prefs.getLong("custom_primary_argb", 0xFFFF6D00)
    )
    val customPrimaryColorArgb: StateFlow<Long> = _customPrimaryColorArgb.asStateFlow()

    private val _customBgColorArgb = MutableStateFlow(
        prefs.getLong("custom_bg_argb", 0xFF10111E)
    )
    val customBgColorArgb: StateFlow<Long> = _customBgColorArgb.asStateFlow()

    fun setCustomColors(primaryArgb: Long, bgArgb: Long) {
        _customPrimaryColorArgb.value = primaryArgb
        _customBgColorArgb.value = bgArgb
        prefs.edit()
            .putLong("custom_primary_argb", primaryArgb)
            .putLong("custom_bg_argb", bgArgb)
            .apply()
        _appThemePreset.value = AppThemePreset.CUSTOM
        prefs.edit().putString("app_theme_preset", AppThemePreset.CUSTOM.name).apply()
        _toastMessage.value = "Custom color theme applied!"
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
        } else {
            currentSet.add(artistName)
            _toastMessage.value = "Added '$artistName' to favorites ❤️"
        }
        _favoriteArtistNames.value = currentSet
        prefs.edit().putStringSet("favorite_artists", currentSet).apply()
        saveBackupToDisk()
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
        val minAllowed = maxOf(1, lockedCount)

        if (count < minAllowed) {
            _toastMessage.value = "Cannot decrease count below $lockedCount locked picture(s)"
            return
        }

        val clamped = count.coerceAtLeast(minAllowed)
        _requestedCount.value = clamped
        prefs.edit().putInt("requested_count", clamped).apply()

        val allPics = _availablePictures.value
        if (current == null || current.selectedPictures.isEmpty() || allPics.isEmpty()) {
            return
        }

        val currentList = current.selectedPictures

        val newList = if (clamped < currentList.size) {
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
                    emptyList()
                }
            }
            currentList + additions
        } else {
            currentList
        }

        if (newList != currentList) {
            val newFormatted = NameFormatter.formatPictureNames(newList, _formattingOptions.value)
            val updated = current.copy(
                countRequested = newList.size,
                selectedPictures = newList,
                formattedString = newFormatted
            )
            _currentResult.value = updated
        }
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
        val currentList = _currentResult.value?.selectedPictures ?: emptyList()

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
}
