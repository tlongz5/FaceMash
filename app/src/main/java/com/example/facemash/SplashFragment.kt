package com.example.facemash

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.facemash.databinding.FragmentSplashBinding
import com.example.magicface.base.BaseFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SplashFragment : BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate){
    override fun FragmentSplashBinding.initView() {
        // load
        findNavController().navigate(R.id.languageFragment)

        viewLifecycleOwner.lifecycleScope.launch {
            loading1.visibility = View.VISIBLE
            loading2.visibility = View.GONE
            loading1.playAnimation()
            delay(3750.milliseconds)

            loading2.playAnimation()
            loading2.visibility = View.VISIBLE
            loading1.visibility = View.GONE
            delay(2500.milliseconds)

            findNavController().navigate(R.id.languageFragment)
        }
    }

    override fun FragmentSplashBinding.initListener() {
    }

    override fun initObserver() {
    }

}