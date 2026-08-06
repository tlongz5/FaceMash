package com.example.facemash.utils

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.fragment.app.Fragment
import com.example.baseproject.model.MusicData
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Locale
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.net.toUri
import com.example.facemash.R

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun View?.show() {
    this?.visibility = View.VISIBLE
}

fun View?.hide() {
    this?.visibility = View.GONE
}

// check if fragment is attached
fun Fragment.checkIfFragmentAttached(operation: Context.() -> Unit) {
    if (isAdded && context != null) {
        operation(requireContext())
    }
}

fun View.setSingleClick(
    clickSpendTime: Long = 500L,
    execution: () -> Unit
) {
    setOnClickListener(object : View.OnClickListener {
        var lastClickTime: Long = 0
        override fun onClick(p0: View?) {
            if (SystemClock.elapsedRealtime() - lastClickTime < clickSpendTime) {
                return
            }
            lastClickTime = SystemClock.elapsedRealtime()
            execution.invoke()
        }
    })
}

fun getJsonFromRaw(context: Context, @RawRes resourceId: Int): String? {
    return try {
        // Mở file từ res/raw và đọc toàn bộ nội dung thành String
        context.resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun playMusic(music: MusicData){
    try {
        MusicPlayerInstance.stop()
        Log.d("SongPickerFragment", "Selected music: 1")
        if (music.url.isNotEmpty()) {
            Log.d("SongPickerFragment", "Selected music: ok")
            MusicPlayerInstance.play(music.url)
        }
    }catch (e: Exception){
        e.printStackTrace()
    }
}

fun getImagesFromAsset(context: Context): List<List<String>> {
    val imagePaths = mutableListOf<MutableList<String>>()
    try {
        // Lấy tất cả tên file trong thư mục "ranking" của assets
        val files = context.assets.list("ranking")
        if (files != null) {
            var imageList = mutableListOf<String>()
            for (fileName in files) {
                if (imageList.size==10) {
                    imagePaths.add(imageList)
                    imageList = mutableListOf()
                }
                // Lưu lại đường dẫn đầy đủ để dùng cho hàm open() ở trên
                val filePath = "ranking/$fileName"
                imageList.add(filePath)
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return imagePaths
}

fun setAssetImageToDrawable(context: Context, fileName: String): Drawable? {
    return try {
        val inputStream = context.assets.open(fileName)
        val drawable = Drawable.createFromStream(inputStream, null)
        inputStream.close()
        drawable
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun Context.setAppLocale(languageCode: String): Context {
    val localeList = LocaleListCompat.forLanguageTags(languageCode)
    AppCompatDelegate.setApplicationLocales(localeList)

    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    resources.updateConfiguration(config, resources.displayMetrics)
    return createConfigurationContext(config)
}

fun Context.openAppInStore() {
    val intent = Intent(Intent.ACTION_VIEW)
    try {
        val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(intent)
    }catch (e: Exception){
        val webIntent = Intent(Intent.ACTION_VIEW,
            "https://play.google.com/store/apps/details?id=$packageName".toUri())
        this.startActivity(webIntent)
    }

}

fun Context.shareApp() {
    val intent = Intent(Intent.ACTION_SEND)
    val shareBody = "Check out this amazing app: https://play.google.com/store/apps/details?id=$packageName"
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_SUBJECT, "Share App")
    intent.putExtra(Intent.EXTRA_TEXT, shareBody)
    this.startActivity(intent)
}

fun Context.sendMail() {
    val osVersion = Build.VERSION.RELEASE
    val country = resources.configuration.locales[0].displayCountry
    val appVersion = try {
        packageManager.getPackageInfo(packageName, 0).versionName
    } catch (e: Exception) {
        "Unknown"
    }

    val subject = getString(R.string.subject_feedback)
    val body = "$subject\n\nOs Version: $osVersion\nCountry: $country\nApp Version: $appVersion\n${getString(R.string.message_email)}"

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("thangnd99308@gmail.com"))
        putExtra(Intent.EXTRA_TEXT, body)
    }

    try {
        startActivity(Intent.createChooser(intent, "Send Email"))
    } catch (e: Exception) {
        showToast(this, "No email client found")
    }
}
