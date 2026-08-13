package com.example.facemash.view

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.view.Window
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.activityViewModels
import com.example.facemash.R
import com.example.facemash.databinding.DialogPickerThisOrThatBinding
import com.example.facemash.databinding.FragmentAllGameBinding
import com.example.facemash.view.main.MainViewModel
import com.example.facemash.base.BaseFragment
import com.example.facemash.databinding.DialogPermissionBinding
import com.example.facemash.utils.setSingleClick
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllGameFragment : BaseFragment<FragmentAllGameBinding>(FragmentAllGameBinding::inflate){
    private val viewModel: MainViewModel by activityViewModels()
    private var pendingAction: (() -> Unit)? = null

    override fun FragmentAllGameBinding.initView() {

    }

    override fun FragmentAllGameBinding.initListener() {
        btnLipPose.setSingleClick {
            checkPermission {
                viewModel.dataMusicFromAssets.value!!.find { it.name == "Lip Puzzle" }?.let { viewModel.setSelectedMusic(it) }
                navigateTo(R.id.lipPoseFragment)
            }
        }
        btnThisOrThat.setSingleClick {
            showDialogPickThisOrThat()
        }
        btnRanking.setSingleClick {
            checkPermission {
                viewModel.dataMusicFromAssets.value!!.find { it.name == "APT" }?.let { viewModel.setSelectedMusic(it) }
                navigateTo(R.id.rankingFragment)
            }
        }
        btnPrincessOrWitch.setSingleClick {
            checkPermission {
                viewModel.dataMusicFromAssets.value!!.find { it.name == "APT" }?.let { viewModel.setSelectedMusic(it) }
                navigateTo(R.id.princessOrWitchFragment)
            }
        }
        btnHandCard.setSingleClick {
            checkPermission {
                viewModel.dataMusicFromAssets.value?.find { it.name == "APT" }?.let { viewModel.setSelectedMusic(it) }
                navigateTo(R.id.handCardFragment)
            }
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
            checkPermission {
                viewModel.dataMusicFromAssets.value?.find { it.name == "APT" }?.let { viewModel.setSelectedMusic(it) }
                navigateTo(R.id.thisOrThatFragment, Bundle().apply {
                    putString("category", "Music")
                })
            }
            dialog.dismiss()
        }
        bindingDialog.btnLogo.setSingleClick {
            checkPermission {
                viewModel.dataMusicFromAssets.value?.find { it.name == "APT" }?.let { viewModel.setSelectedMusic(it) }
                navigateTo(R.id.thisOrThatFragment, Bundle().apply {
                    putString("category", "Logo")
                })
            }
            dialog.dismiss()
        }
        bindingDialog.btnMath.setSingleClick {
            checkPermission {
                viewModel.dataMusicFromAssets.value?.find { it.name == "APT" }?.let { viewModel.setSelectedMusic(it) }
                navigateTo(R.id.thisOrThatFragment, Bundle().apply {
                    putString("category", "Math")
                })
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun initObserver() {

    }

    private fun checkPermission(action: () -> Unit) {
        pendingAction = action
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        requestPermission.launch(permissions)
    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var allGranted = true
        permissions.forEach { (key, granted) ->
            if (!granted) {
                allGranted = false
                if (!shouldShowRequestPermissionRationale(key)) {
                    showPermissionDialog()
                }
            }
        }

        if (allGranted) {
            pendingAction?.invoke()
            pendingAction = null
        }
    }

    private fun showPermissionDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogPermissionBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.btnLater.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSetting.setOnClickListener {
            dialog.dismiss()
            openAppSettings()
        }

        dialog.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

}
