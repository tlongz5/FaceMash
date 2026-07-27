package com.example.facemash.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.facemash.R
import com.example.facemash.databinding.DialogPickerThisOrThatBinding
import com.example.facemash.databinding.FragmentAllGameBinding
import com.example.facemash.view.main.MainViewModel
import com.example.facemash.base.BaseFragment
import com.example.facemash.utils.setSingleClick
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllGameFragment : BaseFragment<FragmentAllGameBinding>(FragmentAllGameBinding::inflate){
    private val viewModel: MainViewModel by activityViewModels()
    override fun FragmentAllGameBinding.initView() {

    }

    override fun FragmentAllGameBinding.initListener() {
        btnLipPose.setSingleClick {
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
        btnSetting.setSingleClick {
            navigateTo(R.id.settingFragment)
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