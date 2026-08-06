package com.example.facemash.admod

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import java.util.UUID

class CollapsibleAds {
    var adView: AdView? = null
    fun loadCollapsibleAds(context: Context, view: FrameLayout) {
        adView = AdView(context)
        adView?.adUnitId = "ca-app-pub-3940256099942544/2014213617"

        val adSize = getAdSize(context as Activity, view)
        adView?.setAdSize(adSize)

        view.removeAllViews()
        view.addView(adView)

        // Cấu hình tham số Collapsible
        val extras = Bundle().apply {
            putString("collapsible", "bottom")
            putString("collapsible_request_id", UUID.randomUUID().toString())
        }

        val adRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()

        adView?.loadAd(adRequest)
    }

    fun onDestroy() {
        adView?.destroy()
        adView = null
    }

    private fun getAdSize(activity: Activity, view: View): AdSize {
        val displayMetrics = activity.resources.displayMetrics
        var adWidthPixels = view.width.toFloat()

        if (adWidthPixels <= 0) {
            adWidthPixels = displayMetrics.widthPixels.toFloat()
        }

        val density = displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }


}