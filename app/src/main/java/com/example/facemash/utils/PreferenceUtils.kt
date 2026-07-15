package com.example.magicface.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceUtils {
    var sharePreferences : SharedPreferences? = null
    const val PREF_KEY = "MAGIC_FACE"

    fun init(context: Context) {
        sharePreferences = context.applicationContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
    }

    fun edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = sharePreferences?.edit()
        operation(editor?: return)
        editor?.apply()
    }

    fun SharedPreferences.clear() {
        edit().clear().apply()
    }

    fun setValue(key: String, value: Any?) {
        val editor = sharePreferences?.edit()?:return
        when(value) {
            is String -> editor.putString(key, value)?.apply()
            is Int -> editor.putInt(key, value)?.apply()
            is Boolean -> editor.putBoolean(key, value)?.apply()
            is Float -> editor.putFloat(key, value)?.apply()
            is Long -> editor.putLong(key, value)?.apply()
            else -> loge(BASE_TAG,"Unsupported type")
        }
        editor.apply()
    }

    fun removeValue(key: String) {
        val editor = sharePreferences?.edit()?:return
        editor.remove(key)
        editor.apply()
    }

    inline fun <reified T> getValue(key: String, defaultValue: T): T {
        val prefs = sharePreferences ?: return defaultValue
        return when (defaultValue) {
            is String -> (prefs.getString(key, defaultValue as String) ?: defaultValue) as T
            is Int -> prefs.getInt(key, defaultValue as Int) as T
            is Boolean -> prefs.getBoolean(key, defaultValue as Boolean) as T
            is Float -> prefs.getFloat(key, defaultValue as Float) as T
            is Long -> prefs.getLong(key, defaultValue as Long) as T
            else -> defaultValue
        }
    }

}