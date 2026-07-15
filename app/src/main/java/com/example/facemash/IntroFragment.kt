package com.example.facemash

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.facemash.databinding.FragmentIntroBinding
import com.example.magicface.base.BaseFragment
import com.example.magicface.model.IntroPager
import com.example.magicface.utils.Helper.introList
import com.example.magicface.utils.setSingleClick

class IntroFragment : BaseFragment<FragmentIntroBinding>(FragmentIntroBinding::inflate){
    private val introAdapter = IntroAdapter()
    private var count =0

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
                    dotIndicator.setCurrentItem(position)
                }
            }
        )
    }

    override fun FragmentIntroBinding.initListener() {
        btnNext.setSingleClick {
            ++count
//            if (count>3) navigateTo(l)
            viewPager.currentItem =count
        }
    }

    override fun initObserver() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}