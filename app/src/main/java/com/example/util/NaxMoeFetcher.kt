package com.example.util

import android.content.Context
import com.example.model.PictureItem
import com.example.model.PictureSourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

object NaxMoeFetcher {
    private const val LOOSE_BASE_URL = "https://cdn.zele.st/data/NAX/Images/danbooru-artist-tags-2-v4.5/"
    private const val CONSTRAINED_BASE_URL = "https://cdn.zele.st/data/NAX/Images/danbooru-artist-tags-v4.5/"

    @Volatile
    private var cachedLoosePictures: List<PictureItem>? = null

    @Volatile
    private var cachedConstrainedPictures: List<PictureItem>? = null

    suspend fun fetchLooseGalleryPictures(context: Context? = null): List<PictureItem> = withContext(Dispatchers.IO) {
        cachedLoosePictures?.let { return@withContext it }

        if (context != null) {
            val list = loadJsonAsset(context, "danbooru_artists.json", LOOSE_BASE_URL, "nax_loose")
            if (list.isNotEmpty()) {
                cachedLoosePictures = list
                return@withContext list
            }
        }

        val fallback = getFallbackPictures(LOOSE_BASE_URL, "nax_loose_fb")
        cachedLoosePictures = fallback
        return@withContext fallback
    }

    suspend fun fetchConstrainedGalleryPictures(context: Context? = null): List<PictureItem> = withContext(Dispatchers.IO) {
        cachedConstrainedPictures?.let { return@withContext it }

        if (context != null) {
            val list = loadJsonAsset(context, "danbooru_artists_constrained.json", CONSTRAINED_BASE_URL, "nax_const")
            if (list.isNotEmpty()) {
                cachedConstrainedPictures = list
                return@withContext list
            }
        }

        val fallback = getFallbackPictures(CONSTRAINED_BASE_URL, "nax_const_fb")
        cachedConstrainedPictures = fallback
        return@withContext fallback
    }

    private fun loadJsonAsset(context: Context, fileName: String, baseUrl: String, idPrefix: String): List<PictureItem> {
        val list = mutableListOf<PictureItem>()
        try {
            context.assets.open(fileName).use { inputStream ->
                val jsonStr = inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonStr)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val artist = obj.getString("a")
                    val rawImg = obj.getString("i")
                    val doubleEncodedImg = rawImg.replace("%", "%25")
                    list.add(
                        PictureItem(
                            id = "${idPrefix}_$i",
                            fileName = artist,
                            uriString = baseUrl + doubleEncodedImg,
                            sourceType = PictureSourceType.WEB_GALLERY
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun getFallbackPictures(baseUrl: String, idPrefix: String): List<PictureItem> {
        return FALLBACK_DATA.mapIndexed { index, (artist, imgPath) ->
            PictureItem(
                id = "${idPrefix}_${index + 1}",
                fileName = artist,
                uriString = baseUrl + imgPath.replace("%", "%25"),
                sourceType = PictureSourceType.WEB_GALLERY
            )
        }
    }

    private val FALLBACK_DATA = listOf(
        Pair("@@@ (eckzahn)", "%40%40%40%20%28eckzahn%29.jpg"),
        Pair("@est@", "%40est%40.jpg"),
        Pair("^jj^", "%5Ejj%5E.jpg"),
        Pair(".sin", ".sin.jpg"),
        Pair("0 (znanimo)", "0%20%28znanimo%29.jpg"),
        Pair("0-den", "0-den.jpg"),
        Pair("0002koko", "0002koko.jpg"),
        Pair("00047", "00047.jpg"),
        Pair("00kashian00", "00kashian00.jpg"),
        Pair("013 (hamsasuke)", "013%20%28hamsasuke%29.jpg"),
        Pair("0202ase", "0202ase.jpg"),
        Pair("0321smith", "0321smith.jpg"),
        Pair("0725akaba", "0725akaba.jpg"),
        Pair("0930erina", "0930erina.jpg"),
        Pair("0jae", "0jae.jpg"),
        Pair("0lightsource", "0lightsource.jpg")
    )
}
