package com.example.magicface.utils

import android.content.Context
import android.os.Environment
import java.io.File

object VideoStorage {

    private const val VIDEO_FOLDER = "FilterQuiz"

    fun getVideoDirectory(context: Context): File? {
        val moviesDirectory =
            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                ?: return null

        val videoDirectory = File(moviesDirectory, VIDEO_FOLDER)

        if (!videoDirectory.exists() && !videoDirectory.mkdirs()) {
            return null
        }

        return videoDirectory
    }

    fun createVideoFile(context: Context): File? {
        val directory = getVideoDirectory(context) ?: return null

        return File(
            directory,
            "filter_quiz_${System.currentTimeMillis()}.mp4"
        )
    }

    fun getAllVideos(context: Context): List<File> {
        val directory = getVideoDirectory(context) ?: return emptyList()

        return directory.listFiles()
            ?.filter { file ->
                file.isFile &&
                        file.extension.equals("mp4", ignoreCase = true) &&
                        file.length() > 0
            }
            ?.sortedByDescending { file ->
                file.lastModified()
            }
            .orEmpty()
    }
}