package com.example.facemash.view

import androidx.fragment.app.activityViewModels
import com.example.facemash.R
import com.example.facemash.base.BaseFragment
import com.example.facemash.databinding.FragmentFilterBinding
import com.example.facemash.utils.setSingleClick
import com.example.facemash.view.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilterFragment : BaseFragment<FragmentFilterBinding>(FragmentFilterBinding::inflate) {
    private val viewModel: MainViewModel by activityViewModels()

    override fun FragmentFilterBinding.initView() {
    }

    override fun FragmentFilterBinding.initListener() {
        btnLipPose.setSingleClick {
            viewModel.dataMusicFromAssets.value?.find { it.name == "Lip Puzzle" }?.let { viewModel.setSelectedMusic(it) }
            navigateTo(R.id.lipPoseFragment)
        }
        btnThisOrThat.setSingleClick {
            viewModel.dataMusicFromAssets.value?.find { it.name == "APT" }?.let { viewModel.setSelectedMusic(it) }
            navigateTo(R.id.thisOrThatFragment)
        }
        btnRanking.setSingleClick {
            viewModel.dataMusicFromAssets.value?.find { it.name == "APT" }?.let { viewModel.setSelectedMusic(it) }
            navigateTo(R.id.rankingFragment)
        }
        btnPrincessOrWitch.setSingleClick {
            viewModel.dataMusicFromAssets.value?.find { it.name == "APT" }?.let { viewModel.setSelectedMusic(it) }
            navigateTo(R.id.princessOrWitchFragment)
        }
    }

    override fun initObserver() {
    }
}