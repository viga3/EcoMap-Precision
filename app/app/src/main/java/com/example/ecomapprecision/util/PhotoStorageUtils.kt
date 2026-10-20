package com.example.ecomapprecision.util

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PhotoStorageUtils {

    data class PhotoTarget(
        val uri: Uri,
        val filePath: String
    )

    /**
     * Creates a target Uri and File path inside Pictures/EcoMap for camera capture.
     * Uses MediaStore on Android 10+ (API 29+) so files land directly in /storage/emulated/0/Pictures/EcoMap/
     * and are immediately visible in standard File Managers and Gallery.
     */
    fun createCameraPhotoTarget(context: Context, prefix: String = "Segnalazione"): PhotoTarget? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALIAN).format(Date())
        val fileName = "EcoMap_${prefix}_${timestamp}.jpg"
        val publicPicturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val ecoMapDir = File(publicPicturesDir, "EcoMap")

        if (!ecoMapDir.exists()) {
            ecoMapDir.mkdirs()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/EcoMap")
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                val absolutePath = File(ecoMapDir, fileName).absolutePath
                return PhotoTarget(uri = uri, filePath = absolutePath)
            }
        }

        // Fallback for API < 29 or if MediaStore insert returns null
        val targetFile = File(ecoMapDir, fileName)
        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                targetFile
            )
            PhotoTarget(uri = uri, filePath = targetFile.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Copies a Uri stream from gallery into Pictures/EcoMap.
     */
    fun copyUriToAccessibleFile(context: Context, uri: Uri, prefix: String = "Galleria"): File? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALIAN).format(Date())
            val fileName = "EcoMap_${prefix}_${timestamp}.jpg"
            val publicPicturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val ecoMapDir = File(publicPicturesDir, "EcoMap")
            if (!ecoMapDir.exists()) {
                ecoMapDir.mkdirs()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/EcoMap")
                }
                val targetUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (targetUri != null) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    val targetFile = File(ecoMapDir, fileName)
                    notifyMediaScanner(context, targetFile.absolutePath)
                    return targetFile
                }
            }

            // Fallback for API < 29
            val targetFile = File(ecoMapDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            notifyMediaScanner(context, targetFile.absolutePath)
            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Triggers Android MediaScanner so new photos immediately appear in gallery and file managers.
     */
    fun notifyMediaScanner(context: Context, filePath: String) {
        try {
            MediaScannerConnection.scanFile(
                context,
                arrayOf(filePath),
                arrayOf("image/jpeg"),
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
