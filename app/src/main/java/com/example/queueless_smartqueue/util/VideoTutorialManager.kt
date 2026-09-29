package com.example.queueless_smartqueue.util

import android.content.Context
import android.net.Uri
import java.io.File

object VideoTutorialManager {
    private const val CUSTOM_VIDEO_FILENAME = "custom_tutorial_video.mp4"

    fun isCustomVideoUploaded(context: Context): Boolean {
        val file = File(context.filesDir, CUSTOM_VIDEO_FILENAME)
        return file.exists() && file.length() > 0
    }

    fun getVideoUri(context: Context): Uri? {
        val customFile = File(context.filesDir, CUSTOM_VIDEO_FILENAME)
        if (customFile.exists() && customFile.length() > 0) {
            return Uri.fromFile(customFile)
        }
        val rawResId = context.resources.getIdentifier("queueless_tutorial", "raw", context.packageName)
        if (rawResId != 0) {
            return Uri.parse("android.resource://${context.packageName}/$rawResId")
        }
        return null
    }

    fun saveCustomVideo(context: Context, sourceUri: Uri): Boolean {
        return try {
            val destFile = File(context.filesDir, CUSTOM_VIDEO_FILENAME)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            destFile.exists() && destFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun resetToDefaultVideo(context: Context): Boolean {
        val file = File(context.filesDir, CUSTOM_VIDEO_FILENAME)
        return if (file.exists()) file.delete() else true
    }
}
