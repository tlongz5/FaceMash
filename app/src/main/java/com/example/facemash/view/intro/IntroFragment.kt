package com.example.facemash.view.intro

import androidx.viewpager2.widget.ViewPager2
import androidx.navigation.NavOptions
import com.example.facemash.R
import com.example.facemash.databinding.FragmentIntroBinding
import com.example.facemash.base.BaseFragment
import com.example.facemash.utils.PreferenceUtils
import com.example.facemash.utils.setSingleClick

class IntroFragment : BaseFragment<FragmentIntroBinding>(FragmentIntroBinding::inflate) {
    private val introAdapter = IntroAdapter()
    private var count = 0

    override fun FragmentIntroBinding.initView() {
        viewPager.adapter = introAdapter

        dotIndicator.setDotCount(introAdapter.itemCount)
        dotIndicator.setCurrentItem(0)

        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    dotIndicator.setScrollPosition(position, positionOffset)
                }

                override fun onPageSelected(position: Int) {
                    count = position
                    dotIndicator.setCurrentItem(position)
                }
            }
        )
    }

    override fun FragmentIntroBinding.initListener() {
        btnNext.setSingleClick {
            ++count
            if (count == 3) {
                PreferenceUtils.setValue("is_first_launch", false)
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.introFragment, true)
                    .build()
                navigateTo(R.id.homeFragment, null, navOptions)
            } else viewPager.currentItem = count
        }
    }

    override fun initObserver() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}