package com.example.facemash.admod

import android.app.Activity
import android.content.Context
import com.example.facemash.utils.logd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardAd: BaseAd<RewardedAd>() {
    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917" // Sample ID

    override fun load(context: Context) {
        super.load(context)
        if(adLoading || isAdReady()) return

        adLoading = true
        RewardedAd.load(
            context,
            AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    super.onAdLoaded(ad)
                    this@RewardAd.ad = ad
                    adCreatedTime = System.currentTimeMillis()
                    logd("rewardAds","onAdLoaded: ok")
                    adLoading = false
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    adLoading = false
                    logd("rewardAds","onAdFailedToLoad: ${p0.message}")
                }
            }
        )
    }

    override fun show(activity: Activity) {
        super.show(activity)
        val rewardAd = ad
        if(adShowing || rewardAd == null) return

        adShowing = true

        rewardAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                ad = null

                //chuan bi ads moi
                logd( "rewardAds","onAdShowedFullScreenContent : ok")
            }
            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                adShowing = false
                load(activity)
                logd("rewardAds","onAdDismissedFullScreenContent")
            }

            override fun onAdClicked() {
                super.onAdClicked()
                logd("rewardAds","onAdClicked")
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
                load(activity)
                logd("rewardAds","onAdFailedToShowFullScreenContent: ${p0.message}")
                adShowing = false
            }

            override fun onAdImpression() {
                super.onAdImpression()
                logd("rewardAds","onAdImpression")
            }
        }

        rewardAd.show(activity, object : OnUserEarnedRewardListener {
            override fun onUserEarnedReward(p0: RewardItem) {
                logd("rewardAds","onUserEarnedReward: ${p0.amount} ${p0.type}")
            }
        })
    }
}