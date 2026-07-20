package com.example.facemash.view

import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.facemash.R
import com.example.facemash.databinding.FragmentSplashBinding
import com.example.magicface.base.BaseFragment
import com.example.magicface.utils.PreferenceUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SplashFragment : BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate){
    override fun FragmentSplashBinding.initView() {
        // load

        viewLifecycleOwner.lifecycleScope.launch {
            loading1.visibility = View.VISIBLE
            loading2.visibility = View.GONE
            loading1.playAnimation()
            delay(3750.milliseconds)

            loading2.playAnimation()
            loading2.visibility = View.VISIBLE
            loading1.visibility = View.GONE
            delay(2500.milliseconds)

            val isFirstLaunch = PreferenceUtils.getValue("is_first_launch", true)
            if (isFirstLaunch) {
                findNavController().navigate(R.id.languageFragment)
            } else {
                findNavController().navigate(R.id.homeFragment)
            }
        }
    }

    override fun FragmentSplashBinding.initListener() {
    }

    override fun initObserver() {
    }

}