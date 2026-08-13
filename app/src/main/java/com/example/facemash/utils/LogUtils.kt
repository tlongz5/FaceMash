package com.example.facemash.utils

import android.util.Log

const val BASE_TAG = "DEBUG_APP"

fun logd(tag:String,msg: String) {
    Log.d(BASE_TAG, msg)
}

fun logd(msg: String) {
    Log.d(BASE_TAG, msg)
}

fun loge(tag:String,msg: String) {
    Log.e(tag, msg)
}