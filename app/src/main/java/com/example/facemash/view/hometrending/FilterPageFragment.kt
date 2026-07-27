package com.example.facemash.view.hometrending

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.facemash.R
import com.example.facemash.databinding.FragmentFilterPageBinding
import com.example.facemash.view.main.MainViewModel
import com.example.facemash.base.BaseFragment
import com.example.facemash.utils.Constants
import com.example.facemash.utils.PlayerInstance.play

class FilterPageFragment: BaseFragment<FragmentFilterPageBinding>(FragmentFilterPageBinding::inflate) {

    private val mainViewModel: MainViewModel by activityViewModels()
    private var type: String? =null

    private val adapter = HomeTrendingFilterAdapter{trendingDataItem, position ->
        // navigate
        navigateTo(
            resId = R.id.detailFragment,
            args = Bundle().apply {
                putInt("position", position)
                putString("type", type)
            }
        )

        // load video with exo player
        play(trendingDataItem.video!!)

    }

    override fun FragmentFilterPageBinding.initView() {
        type = arguments?.getString("type")
        if (type == Constants.FilterType.ALL_FILTER){
            adapter.submitList(mainViewModel.trendingData.value)
        }else adapter.submitList(mainViewModel.trendingData.value?.filter { it.filterType == type })

        rcvTrendingData.layoutManager = GridLayoutManager(requireContext(), 2)
        rcvTrendingData.adapter = adapter
    }

    override fun FragmentFilterPageBinding.initListener() {

    }

    override fun initObserver() {
    }

    companion object{
        fun newInstance(type:String): FilterPageFragment {
            val fragment = FilterPageFragment()
            val bundle = Bundle()
            bundle.putString("type",type)
            fragment.arguments = bundle
            return fragment
        }
    }

}