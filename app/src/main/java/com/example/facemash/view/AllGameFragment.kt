package com.example.facemash.view

import com.example.facemash.R
import com.example.facemash.databinding.FragmentAllGameBinding
import com.example.magicface.base.BaseFragment
import com.example.magicface.utils.setSingleClick

class AllGameFragment : BaseFragment<FragmentAllGameBinding>(FragmentAllGameBinding::inflate){
    override fun FragmentAllGameBinding.initView() {

    }

    override fun FragmentAllGameBinding.initListener() {
        btnLipPose.setSingleClick {
            navigateTo(R.id.lipPoseFragment)
        }
        btnThisOrThat.setSingleClick {
            navigateTo(R.id.thisOrThatFragment)
        }
        btnSetting.setSingleClick {
            navigateTo(R.id.settingFragment)
        }
    }

    override fun initObserver() {

    }

}