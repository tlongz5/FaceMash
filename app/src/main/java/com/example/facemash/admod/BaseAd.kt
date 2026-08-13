package com.example.facemash.admod

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback

abstract class BaseAd<V> {
    var ad: V? = null

    var adLoading = false
    var adShowing = false

    var adExpirationTime = 1000L*60*60*4
    var adCreatedTime = 0L

    open fun load(context: Context){}
    open fun show(activity: Activity){
        if(activity.isFinishing || activity.isDestroyed) return

    }

    protected fun isAdReady(): Boolean{
        return ad != null && !isAdExpired()
    }

    protected fun isAdExpired(): Boolean{
        return (System.currentTimeMillis() - adCreatedTime) > adExpirationTime
    }

    protected fun createFullScreenCallback(
        context: Context,
        onAdShow: (() -> Unit)? = null,
        onAdClosed: (() -> Unit)? = null
    ): FullScreenContentCallback{
        return object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                ad = null
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                onAdClosed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)

            }
        }
    }


}