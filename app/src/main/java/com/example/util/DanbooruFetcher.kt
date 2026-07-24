package com.example.util

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class DanbooruPost(
    val imageUrl: String,
    val postUrl: String,
    val width: Int = 0,
    val height: Int = 0
)

object DanbooruFetcher {
    suspend fun fetchRandomArtistPost(artistName: String): DanbooruPost? = withContext(Dispatchers.IO) {
        val rawName = artistName.trim().removeSurrounding("\"")
        val cleanArtist = rawName.removePrefix("artist:").trim().lowercase().replace(" ", "_")
        if (cleanArtist.isEmpty()) return@withContext null

        // Try candidate tag queries in order of specificity
        val tagQueries = listOf(
            "artist:$cleanArtist date:..2025-02-28 order:random",
            "artist:$cleanArtist order:random",
            "$cleanArtist date:..2025-02-28 order:random",
            "$cleanArtist order:random"
        )

        for (tags in tagQueries) {
            val urlString = "https://danbooru.donmai.us/posts.json?tags=${Uri.encode(tags)}&limit=5"
            try {
                val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 6000
                    readTimeout = 6000
                    setRequestProperty("User-Agent", "NAIArtistMixerApp/1.0")
                }
                if (connection.responseCode == 200) {
                    val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(jsonText)
                    val validPosts = mutableListOf<DanbooruPost>()
                    for (i in 0 until jsonArray.length()) {
                        val post = jsonArray.getJSONObject(i)
                        var imageUrl = when {
                            post.has("large_file_url") && !post.isNull("large_file_url") -> post.getString("large_file_url")
                            post.has("file_url") && !post.isNull("file_url") -> post.getString("file_url")
                            post.has("preview_file_url") && !post.isNull("preview_file_url") -> post.getString("preview_file_url")
                            else -> null
                        }
                        if (imageUrl != null && imageUrl.startsWith("/")) {
                            imageUrl = "https://danbooru.donmai.us$imageUrl"
                        }
                        val postId = if (post.has("id") && !post.isNull("id")) post.optLong("id") else null
                        val postUrl = if (postId != null && postId > 0) {
                            "https://danbooru.donmai.us/posts/$postId"
                        } else {
                            "https://danbooru.donmai.us/posts?tags=${Uri.encode("artist:$cleanArtist")}"
                        }

                        val imgWidth = if (post.has("image_width") && !post.isNull("image_width")) post.optInt("image_width") else 0
                        val imgHeight = if (post.has("image_height") && !post.isNull("image_height")) post.optInt("image_height") else 0

                        if (imageUrl != null) {
                            validPosts.add(DanbooruPost(imageUrl = imageUrl, postUrl = postUrl, width = imgWidth, height = imgHeight))
                        }
                    }
                    if (validPosts.isNotEmpty()) {
                        return@withContext validPosts.random()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        null
    }

    suspend fun fetchRandomArtistImageUrl(artistName: String): String? {
        return fetchRandomArtistPost(artistName)?.imageUrl
    }
}


