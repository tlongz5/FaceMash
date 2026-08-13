package com.example.facemash.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.activityViewModels
import com.example.facemash.R
import com.example.facemash.base.BaseFragment
import com.example.facemash.databinding.DialogPickerThisOrThatBinding
import com.example.facemash.databinding.FragmentFilterBinding
import com.example.facemash.utils.setSingleClick
import com.example.facemash.view.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilterFragment : BaseFragment<FragmentFilterBinding>(FragmentFilterBinding::inflate) {
    private val viewModel: MainViewModel by activityViewModels()

    override fun FragmentFilterBinding.initView() {
    }

    override fun FragmentFilterBinding.initListener() { btnLipPose.setSingleClick {
        viewModel.dataMusicFromAssets.value!!.find { it.name == "Lip Puzzle" }?.let { viewModel.setSelectedMusic(it) }
        navigateTo(R.id.lipPoseFragment)
    }
        btnThisOrThat.setSingleClick {
            showDialogPickThisOrThat()
        }
        btnRanking.setSingleClick {
            viewModel.dataMusicFromAssets.value!!.find { it.name == "APT" }?.let { viewModel.setSelectedMusic(it) }
            navigateTo(R.id.rankingFragment)
        }
        btnPrincessOrWitch.setSingleClick {
            viewModel.dataMusicFromAssets.value!!.find { it.name == "APT" }?.let { viewModel.setSelectedMusic(it) }
            navigateTo(R.id.princessOrWitchFragment)
        }
        btnHandCard.setSingleClick {
            viewModel.dataMusicFromAssets.value!!.find { it.name == "APT" }?.let { viewModel.setSelectedMusic(it) }
            navigateTo(R.id.handCardFragment)
        }
        btnBack.setSingleClick {
            navigateUp()
        }
    }

    private fun showDialogPickThisOrThat() {
        val dialog = Dialog(requireContext())
        val bindingDialog = DialogPickerThisOrThatBinding.inflate(layoutInflater)
        dialog.setContentView(bindingDialog.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bindingDialog.btnMusic.setSingleClick {
            viewModel.dataMusicFromAssets.value?.find { it.name == "APT" }?.let { viewModel.setSelectedMusic(it) }
            navigateTo(R.id.thisOrThatFragment, Bundle().apply {
                putString("category", "Music")
            })
            dialog.dismiss()
        }
        bindingDialog.btnLogo.setSingleClick {
            viewModel.dataMusicFromAssets.value?.find { it.name == "APT" }?.let { viewModel.setSelectedMusic(it) }
            navigateTo(R.id.thisOrThatFragment, Bundle().apply {
                putString("category", "Logo")
            })
            dialog.dismiss()
        }
        bindingDialog.btnMath.setSingleClick {
            viewModel.dataMusicFromAssets.value?.find { it.name == "APT" }?.let { viewModel.setSelectedMusic(it) }
            navigateTo(R.id.thisOrThatFragment, Bundle().apply {
                putString("category", "Math")
            })
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun initObserver() {
    }
}