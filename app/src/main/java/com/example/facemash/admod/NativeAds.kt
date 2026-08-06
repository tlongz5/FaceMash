package com.example.facemash.admod

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

class NativeAds {
    private var nativeAd: NativeAd? = null

    fun loadNativeAds(context: Context, onAdLoaded: (NativeAd) -> Unit) {
        val adLoader = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { ad ->
                // Nếu Fragment/Activity đã bị destroy thì giải phóng ad mới tải về ngay
                if (nativeAd != null) {
                    nativeAd?.destroy()
                }
                nativeAd = ad
                onAdLoaded(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    // Xử lý lỗi load quảng cáo
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        // QUAN TRỌNG: Phải gọi loadAd thì nó mới chạy
        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun onDestroy() {
        nativeAd?.destroy()
        nativeAd = null
    }
}
