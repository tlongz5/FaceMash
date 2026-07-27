package com.example.facemash.view.hometrending

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.facemash.R
import com.example.facemash.databinding.FragmentHomeTrendingBinding
import com.example.facemash.base.BaseFragment
import com.example.facemash.utils.Constants
import com.example.facemash.utils.logd
import com.example.facemash.view.hometrending.FilterPageFragment
import com.example.facemash.view.hometrending.HomeTrendingTagAdapter

class HomeTrendingFragment : BaseFragment<FragmentHomeTrendingBinding>(FragmentHomeTrendingBinding::inflate){
    override fun FragmentHomeTrendingBinding.initView() {
        btnSetting.setOnClickListener {
            navigateTo(resId = R.id.settingFragment)
        }

        rcvTag.adapter = HomeTrendingTagAdapter(Constants.filterType) { position ->
            viewPager.currentItem = position
            logd("position $position")
        }

        rcvTag.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        viewPager.adapter = object : FragmentStateAdapter(this@HomeTrendingFragment) {
            override fun getItemCount(): Int {
                return Constants.filterType.size
            }
            override fun createFragment(position: Int): Fragment {
                return FilterPageFragment.newInstance(Constants.filterType[position])
            }
        }

        viewPager.currentItem = 0
    }

    override fun FragmentHomeTrendingBinding.initListener() {

    }

    override fun initObserver() {

    }

}