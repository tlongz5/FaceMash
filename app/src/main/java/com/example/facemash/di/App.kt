package com.example.facemash.di

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.facemash.admod.AppOpenAdManager
import com.example.facemash.admod.CollapsibleAds
import com.example.facemash.admod.InterAds
import com.example.facemash.admod.NativeAds
import com.example.facemash.utils.MusicPlayerInstance
import com.example.facemash.utils.PlayerInstance
import com.example.facemash.utils.PreferenceUtils
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application(){
    lateinit var appOpenAdManager: AppOpenAdManager
    lateinit var interAds: InterAds
    lateinit var collapsibleAds: CollapsibleAds
    lateinit var nativeAds: NativeAds
    override fun onCreate() {
        super.onCreate()
        PreferenceUtils.init(this)
        PlayerInstance.initPlayer(this)
        MusicPlayerInstance.initPlayer(this)
//        MobileAds.initialize(this)
//        appOpenAdManager = AppOpenAdManager(this)
//        registerActivityLifecycleCallbacks(appOpenAdManager)
////        ProcessLifecycleOwner.get().lifecycle.addObserver(appOpenAdManager)
//
//        interAds = InterAds(this)
//        interAds.loadInterAds()
//        collapsibleAds = CollapsibleAds()
//        nativeAds = NativeAds()
    }
}