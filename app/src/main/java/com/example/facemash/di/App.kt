package com.example.facemash.di

import android.app.Application
import com.example.magicface.utils.MusicPlayerInstance
import com.example.magicface.utils.PlayerInstance
import com.example.magicface.utils.PreferenceUtils
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