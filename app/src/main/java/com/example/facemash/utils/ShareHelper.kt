package com.example.facemash.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.facemash.BuildConfig
import com.example.facemash.R
import java.io.File

class ShareHelper(private val context: Context) {
    fun shareFiles(files: List<File>) {
        try {
            val videoUris = files.map { file ->
                FileProvider.getUriForFile(
                    context,
                    BuildConfig.CONTENT_PROVIDER_AUTHORITY,
                    file
                )
            }

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND_MULTIPLE
                type = "video/mp4"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(videoUris))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(
                Intent.createChooser(shareIntent, "Share videos using")
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
            val shareMessage = "Check out this app: https://play.google.com/store/apps/details?id=${context.packageName}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            context.startActivity(Intent.createChooser(shareIntent, "Share app via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
