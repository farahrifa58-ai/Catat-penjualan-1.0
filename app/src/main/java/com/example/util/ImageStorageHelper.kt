package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageStorageHelper {
    /**
     * Copies a selected media Uri to app internal storage so it is permanently accessible
     * and safe across app restarts without requiring runtime storage permissions.
     */
    fun saveImageLocally(context: Context, sourceUri: Uri, prefix: String): String? {
        return try {
            val contentResolver = context.contentResolver
            val imagesDir = File(context.filesDir, "images").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(imagesDir, "${prefix}_${System.currentTimeMillis()}.jpg")
            contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            destinationFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }
}
