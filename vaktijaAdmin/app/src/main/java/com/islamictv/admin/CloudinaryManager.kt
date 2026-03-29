package com.islamictv.admin

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class CloudinaryManager(private val context: Context) {

    // REPLACE THESE WITH YOUR CLOUDINARY CREDENTIALS
    private val cloudName = "dyrtwtnr1"
    private val uploadPreset = "islamic_tv"     // ← YOUR UPLOAD PRESET NAME

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Upload image to Cloudinary
     * Returns the secure URL of uploaded image
     */
    suspend fun uploadImage(uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Get file from URI
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                file.outputStream().use { output ->
                    inputStream?.copyTo(output)
                }
                inputStream?.close()

                // Build multipart request
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        file.name,
                        file.asRequestBody("image/jpeg".toMediaType())
                    )
                    .addFormDataPart("upload_preset", uploadPreset)
                    .addFormDataPart("folder", "announcements")
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                    .post(requestBody)
                    .build()

                // Execute request
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                // Clean up temp file
                file.delete()

                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    val secureUrl = json.getString("secure_url")
                    val publicId = json.getString("public_id")

                    Log.d("Cloudinary", "Upload successful: $secureUrl")
                    Log.d("Cloudinary", "Public ID: $publicId")

                    Result.success(secureUrl)
                } else {
                    Log.e("Cloudinary", "Upload failed: ${response.code} - $responseBody")
                    Result.failure(Exception("Upload failed: ${response.code}"))
                }
            } catch (e: Exception) {
                Log.e("Cloudinary", "Upload error", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Delete image from Cloudinary
     * Note: Requires authentication, so we'll skip deletion for unsigned uploads
     * Images can be manually deleted from Cloudinary dashboard if needed
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // For unsigned uploads, we can't delete programmatically
                // Images need to be deleted from Cloudinary dashboard
                // Or you can implement signed uploads with API secret
                Log.d("Cloudinary", "Delete requested for: $imageUrl")
                Log.d("Cloudinary", "Manual deletion required from dashboard")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("Cloudinary", "Delete error", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Extract public_id from Cloudinary URL for deletion
     */
    private fun extractPublicId(url: String): String? {
        return try {
            // URL format: https://res.cloudinary.com/cloud_name/image/upload/v123/folder/image_id.jpg
            val parts = url.split("/upload/")
            if (parts.size == 2) {
                val afterUpload = parts[1].split("/").drop(1).joinToString("/")
                afterUpload.substringBeforeLast(".")
            } else null
        } catch (e: Exception) {
            null
        }
    }
}