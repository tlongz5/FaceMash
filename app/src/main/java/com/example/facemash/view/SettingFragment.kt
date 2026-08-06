package com.example.facemash.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import com.example.facemash.R
import com.example.facemash.databinding.DialogRateBinding
import com.example.facemash.databinding.FragmentSettingBinding
import com.example.facemash.base.BaseFragment
import com.example.facemash.databinding.LayoutNativeAdSettingBinding
import com.example.facemash.di.App
import com.example.facemash.utils.openAppInStore
import com.example.facemash.utils.sendMail
import com.example.facemash.utils.setSingleClick
import com.example.facemash.utils.shareApp
import com.google.android.gms.ads.nativead.NativeAdView

class SettingFragment: BaseFragment<FragmentSettingBinding>(FragmentSettingBinding::inflate) {
    override fun FragmentSettingBinding.initView() {
        (requireActivity().application as App).nativeAds.loadNativeAds(requireContext()) { nativeAd ->
            val adBinding = LayoutNativeAdSettingBinding.inflate(layoutInflater)
            val adView = adBinding.root 
            adView.headlineView = adBinding.adHeadline
            adView.bodyView = adBinding.adBody
            adView.callToActionView = adBinding.adCallToAction
            adView.iconView = adBinding.adAppIcon
            adView.mediaView = adBinding.adMedia

            adBinding.adHeadline.text = nativeAd.headline
            adBinding.adBody.text = nativeAd.body
            adBinding.adCallToAction.text = nativeAd.callToAction
            adBinding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)

            adView.setNativeAd(nativeAd)

            adContainer.removeAllViews()
            adContainer.addView(adView)
        }
    }

    override fun FragmentSettingBinding.initListener() {
        btnBack.setSingleClick {
            navigateUp()
        }

        linearLayout.setSingleClick {
            showRateDialog()
        }
        linearLayout2.setSingleClick {
            requireContext().shareApp()
        }
        linearLayout3.setSingleClick {
            requireContext().sendMail()
        }
        linearLayout4.setSingleClick {
            navigateTo(R.id.languageFragment)
        }
        linearLayout5.setSingleClick {
            navigateTo(R.id.webViewFragment)
        }
        linearLayout6.setSingleClick {
            navigateTo(R.id.webViewFragment)
        }
    }

    private fun showRateDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogRateBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val stars = listOf<ImageView>(
            dialogBinding.ivStar1,
            dialogBinding.ivStar2,
            dialogBinding.ivStar3,
            dialogBinding.ivStar4,
            dialogBinding.ivStar5
        )

        var currentRating = 0

        stars.forEachIndexed { index, imageView ->
            imageView.setSingleClick {
                currentRating = index + 1
                updateStars(stars, currentRating)
            }
        }

        dialogBinding.btnCancel.setSingleClick {
            dialog.dismiss()
        }

        dialogBinding.btnRate.setSingleClick {
            requireContext().openAppInStore()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateStars(stars: List<ImageView>, rating: Int) {
        stars.forEachIndexed { index, imageView ->
            if (index < rating) {
                imageView.setColorFilter(Color.parseColor("#FFCC00"))
            } else {
                imageView.setColorFilter(Color.parseColor("#DEDEDE"))
            }
        }
    }

    override fun initObserver() {

    }

}