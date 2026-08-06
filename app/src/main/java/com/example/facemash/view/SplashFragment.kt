package com.example.facemash.view

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavOptions
import com.example.facemash.R
import com.example.facemash.databinding.FragmentSplashBinding
import com.example.facemash.base.BaseFragment
import com.example.facemash.utils.PreferenceUtils
import com.google.android.gms.ads.AdRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SplashFragment : BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate){
    override fun FragmentSplashBinding.initView() {
//        val isFirstLaunch = PreferenceUtils.getValue("is_first_launch", true)
//        val navOptions = NavOptions.Builder()
//            .setPopUpTo(R.id.splashFragment, true)
//            .build()
//
//        if (isFirstLaunch) {
//            findNavController().navigate(R.id.languageFragment, null, navOptions)
//        } else {
//            findNavController().navigate(R.id.homeFragment, null, navOptions)
//        }


        viewLifecycleOwner.lifecycleScope.launch {
            loading1.visibility = View.VISIBLE
            loading2.visibility = View.GONE
            loading1.playAnimation()
            delay(3750.milliseconds)

            loading2.playAnimation()
            loading2.visibility = View.VISIBLE
            loading1.visibility = View.GONE
            delay(2500.milliseconds)

            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.splashFragment, true)
                .build()
            val bundle = Bundle().apply {
                putBoolean("from_splash", true)
            }
            findNavController().navigate(R.id.languageFragment, bundle, navOptions)
        }

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

    }

    override fun FragmentSplashBinding.initListener() {
    }

    override fun initObserver() {
    }

}