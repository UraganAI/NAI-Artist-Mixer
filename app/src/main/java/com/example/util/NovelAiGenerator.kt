package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

object NovelAiGenerator {

    data class GenerationResult(
        val bitmap: Bitmap,
        val actualSeed: Long
    )

    suspend fun generateImage(
        apiKey: String,
        artistTags: String,
        mainPrompt: String,
        ucPrompt: String,
        characterPrompt: String = "",
        model: String = "nai-diffusion-4-5-full",
        width: Int = 832,
        height: Int = 1216,
        scale: Double = 4.0,
        steps: Int = 28,
        sampler: String = "k_euler_ancestral",
        seed: Long = -1L
    ): Result<GenerationResult> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank()) {
            return@withContext Result.failure(Exception("NovelAI API Key is missing. Please set your API key in Settings."))
        }

        // Build base prompt string: artist tag mix ALWAYS goes at the very start of the prompt
        val fullInput = buildFullPrompt(artistTags, mainPrompt, characterPrompt, model)

        val actualSeed = if (seed >= 0L) seed else kotlin.random.Random.nextLong(1L, 2147483647L)

        try {
            val url = URL("https://image.novelai.net/ai/generate-image")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 60000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $cleanKey")
                setRequestProperty("User-Agent", "NAIArtistMixerApp/1.0")
            }

            val parametersJson = JSONObject().apply {
                put("width", width)
                put("height", height)
                put("scale", scale)
                put("sampler", sampler)
                put("steps", steps)
                put("n_samples", 1)
                put("uc", ucPrompt)
                put("negative_prompt", ucPrompt)
                put("add_original_image", true)
                put("cfg_rescale", 0.0)
                put("noise_schedule", "karras")
                put("params_version", 1)

                if (model == "nai-diffusion-3" || model == "nai-diffusion-furry-3") {
                    put("sm", false)
                    put("sm_dyn", false)
                    put("dynamic_thresholding", false)
                    put("controlnet_strength", 1.0)
                    put("legacy", false)
                    put("uncond_scale", 1.0)
                } else {
                    // NovelAI V4 and V4.5 prompt structure with character captions
                    val charCaptionsArray = org.json.JSONArray()
                    val cleanChar = characterPrompt.trim()
                    if (cleanChar.isNotBlank()) {
                        val charObj = JSONObject().apply {
                            put("char_caption", cleanChar)
                            put("centers", org.json.JSONArray().apply {
                                put(JSONObject().apply {
                                    put("x", 0.5)
                                    put("y", 0.5)
                                })
                            })
                        }
                        charCaptionsArray.put(charObj)
                    }

                    val v4PromptObj = JSONObject().apply {
                        put("caption", JSONObject().apply {
                            put("base_caption", fullInput)
                            put("char_captions", charCaptionsArray)
                        })
                        put("use_coords", false)
                        put("use_order", true)
                    }

                    val v4NegPromptObj = JSONObject().apply {
                        put("caption", JSONObject().apply {
                            put("base_caption", ucPrompt)
                            put("char_captions", org.json.JSONArray())
                        })
                        put("use_coords", false)
                        put("use_order", true)
                    }

                    put("v4_prompt", v4PromptObj)
                    put("v4_negative_prompt", v4NegPromptObj)
                    put("v4prompt", v4PromptObj)
                    put("v4negative_prompt", v4NegPromptObj)
                    put("use_coords", false)
                }

                put("seed", actualSeed)
            }

            val requestBody = JSONObject().apply {
                put("input", fullInput)
                put("model", model)
                put("action", "generate")
                put("parameters", parametersJson)
            }

            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val zipStream = ZipInputStream(connection.inputStream)
                var entry = zipStream.nextEntry
                var imageBytes: ByteArray? = null

                while (entry != null) {
                    if (!entry.isDirectory && (
                                entry.name.endsWith(".png", ignoreCase = true) ||
                                        entry.name.endsWith(".jpg", ignoreCase = true) ||
                                        entry.name.endsWith(".webp", ignoreCase = true)
                                )) {
                        val buffer = ByteArrayOutputStream()
                        val data = ByteArray(4096)
                        var count: Int
                        while (zipStream.read(data, 0, 4096).also { count = it } != -1) {
                            buffer.write(data, 0, count)
                        }
                        imageBytes = buffer.toByteArray()
                        break
                    }
                    entry = zipStream.nextEntry
                }
                zipStream.close()

                if (imageBytes != null && imageBytes.isNotEmpty()) {
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    if (bitmap != null) {
                        Result.success(GenerationResult(bitmap, actualSeed))
                    } else {
                        Result.failure(Exception("Failed to decode generated image bitmap."))
                    }
                } else {
                    Result.failure(Exception("NovelAI server returned response, but no image entry was found in the archive."))
                }
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                val errorMsg = when (responseCode) {
                    401 -> "Unauthorized (401): Invalid NovelAI API Key. Please check your key in Settings."
                    402 -> "Payment Required (402): Insufficient Anlas or active subscription required on your NovelAI account."
                    400 -> "Bad Request (400): Invalid request parameters or prompt structure. $errorText"
                    429 -> "Too Many Requests (429): Rate limited by NovelAI. Please wait a moment."
                    else -> "NovelAI API Error ($responseCode): ${errorText.ifBlank { "Failed to generate image." }}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error while connecting to NovelAI: ${e.localizedMessage ?: "Connection failed"}"))
        }
    }

    /**
     * Helper to construct the final prompt sent to NovelAI.
     * The artist tag mix ALWAYS goes at the very start of the main prompt.
     * Ensures artist tags end with a comma so tags do not fuse with the main prompt.
     */
    fun buildFullPrompt(
        artistTags: String,
        mainPrompt: String,
        characterPrompt: String = "",
        model: String = ""
    ): String {
        val sb = StringBuilder()
        
        var cleanArtist = artistTags.trim()
        if (cleanArtist.isNotBlank()) {
            if (!cleanArtist.endsWith(",")) {
                cleanArtist += ","
            }
            sb.append(cleanArtist).append(" ")
        }
        
        var cleanMain = mainPrompt.trim()
        if (cleanMain.isNotBlank()) {
            sb.append(cleanMain)
        }
        
        if ((model == "nai-diffusion-3" || model == "nai-diffusion-furry-3") && characterPrompt.isNotBlank()) {
            var cleanChar = characterPrompt.trim()
            if (sb.isNotEmpty() && !sb.endsWith(" ") && !sb.endsWith(",")) {
                sb.append(", ")
            } else if (sb.isNotEmpty() && !sb.endsWith(" ")) {
                sb.append(" ")
            }
            sb.append(cleanChar)
        }
        
        return sb.toString().trim()
    }

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String = "NAI_ArtistMixer"): Uri? {
        return try {
            val filename = "${title}_${System.currentTimeMillis()}.png"
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NAIArtistMixer")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { os ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
            }
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareBitmap(context: Context, bitmap: Bitmap) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "shared_nai_image.png")
            FileOutputStream(file).use { os ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            }
            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share NovelAI Image"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun copyBitmapToClipboard(context: Context, bitmap: Bitmap) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "copied_nai_image.png")
            FileOutputStream(file).use { os ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            }
            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newUri(context.contentResolver, "NovelAI Image", contentUri)
            clipboard.setPrimaryClip(clip)
            android.widget.Toast.makeText(context, "Image copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Failed to copy image to clipboard.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
