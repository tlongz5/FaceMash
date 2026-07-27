package com.example.facemash.di

import android.app.Application
import com.example.facemash.utils.MusicPlayerInstance
import com.example.facemash.utils.PlayerInstance
import com.example.facemash.utils.PreferenceUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application(){
    override fun onCreate() {
        super.onCreate()
        PreferenceUtils.init(this)
        PlayerInstance.initPlayer(this)
        MusicPlayerInstance.initPlayer(this)
    }
}