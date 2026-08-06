package com.example.facemash.admod


import android.app.Activity
import android.app.Application
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterAds(private val application: Application) {
    var interstitialAd: InterstitialAd? = null
    val adRequest = AdRequest.Builder().build()

    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    fun loadInterAds(){
        InterstitialAd.load(
            application,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                }
            }
        )
    }

    fun showInterAds(activity: Activity){
        if(interstitialAd != null){
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    interstitialAd = null
                }
                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    interstitialAd = null
                }
            }
            interstitialAd?.show(activity)
        }else{
            loadInterAds()
        }
    }
}