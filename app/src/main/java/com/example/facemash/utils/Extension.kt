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
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    resources.updateConfiguration(config, resources.displayMetrics)
    return createConfigurationContext(config)
}
//
//fun Context.sendMail(
//    withMessage: Boolean = true
//) {
//    val osName = Build.VERSION.RELEASE
//    val locale = this.resources.configuration.locales[0] ?: ""
//    val manager = this.packageManager
//    val info = manager.getPackageInfo(
//        this.packageName, 0
//    )
//    val version = info.versionName
//
//    val message = getString(
//        R.string.message_email, osName, locale, version
//    )
//
//    val selectorIntent = Intent(Intent.ACTION_SENDTO)
//    selectorIntent.data = Uri.parse(MAIL_TO)
//    val emailIntent = Intent(Intent.ACTION_SEND)
//    emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(RECIPIENT_EMAIL))
//    emailIntent.putExtra(
//        Intent.EXTRA_SUBJECT,
//        getString(R.string.subject_feedback)
//    )
//    if (withMessage)
//        emailIntent.putExtra(Intent.EXTRA_TEXT, message)
//    emailIntent.selector = selectorIntent
//    this.startActivity(Intent.createChooser(emailIntent, INTENT_TITLE))
//}
