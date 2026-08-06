package com.example.facemash.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.facemash.R
import com.example.facemash.databinding.DialogExitBinding
import com.example.facemash.databinding.FragmentHomeBinding
import com.example.facemash.view.hometrending.HomeTrendingFragment
import com.example.facemash.base.BaseFragment
import com.example.facemash.utils.setScaleDown
import com.example.facemash.utils.setScaleUp
import com.example.facemash.utils.setSingleClick
import androidx.core.graphics.drawable.toDrawable

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })
    }

    private fun showExitDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogExitBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        dialogBinding.btnCancel.setSingleClick {
            dialog.dismiss()
        }

        dialogBinding.btnOk.setSingleClick {
            dialog.dismiss()
            requireActivity().finish()
        }

        val width = (resources.displayMetrics.widthPixels * 0.80).toInt()

        // Set lại thông số cho Window của Dialog
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.show()
    }

    override fun FragmentHomeBinding.initView() {
        viewPager.adapter = object : FragmentStateAdapter(this@HomeFragment) {
            override fun getItemCount() = 3

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> HomeTrendingFragment()
                    1 -> AllGameFragment()
                    else -> RecordFragment()
                }
            }
        }
        viewPager.isUserInputEnabled = false

        viewPager.setCurrentItem(1, false)
        updateTab(1)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTab(position)
            }
        })
    }

    fun FragmentHomeBinding.updateTab(position: Int) {
        when (position) {
            0 -> {
                btnTrending.setScaleUp()
                btnAllGame.setScaleDown()
                btnRecord.setScaleDown()

                icRecord.setScaleDown()
                icTrending.setScaleUp()
                icAllGame.setScaleDown()

                tvTrending.setColorText(
                    ContextCompat.getColor(requireContext(), R.color.tv_home_selected),
                    ContextCompat.getColor(requireContext(), R.color.tv_home_selected))

                tvAllGame.setColorText(
                    ContextCompat.getColor(requireContext(), R.color.tv_home_un_selected),
                    ContextCompat.getColor(requireContext(), R.color.tv_home_un_selected))
                tvRecord.setColorText(
                    ContextCompat.getColor(requireContext(), R.color.tv_home_un_selected),
                    ContextCompat.getColor(requireContext(), R.color.tv_home_un_selected))

                bgTrending.setImageResource(R.drawable.bg_selected)
                bgAllGame.setImageResource(R.drawable.ic_2)
                bgRecord.setImageResource(R.drawable.ic_3)
            }

            1 -> {
                btnTrending.setScaleDown()
                btnAllGame.setScaleUp()
                btnRecord.setScaleDown()

                icRecord.setScaleDown()
                icTrending.setScaleDown()
                icAllGame.setScaleUp()

                tvTrending.setColorText(
                    ContextCompat.getColor(requireContext(), R.color.tv_home_un_selected),
                    ContextCompat.getColor(requireContext(), R.color.tv_home_un_selected))
                tvAllGame.setColorText(
                    ContextCompat.getColor(requireContext(), R.color.tv_home_selected),
                    ContextCompat.getColor(requireContext(), R.color.tv_home_selected))
                tvRecord.setColorText(
                    ContextCompat.getColor(requireContext(), R.color.tv_home_un_selected),
                    ContextCompat.getColor(requireContext(), R.color.tv_home_un_selected))

                bgTrending.setImageResource(R.drawable.ic_1)
                bgAllGame.setImageResource(R.drawable.bg_selected)
                bgRecord.setImageResource(R.drawable.ic_3)
            }

            2 -> {
                btnTrending.setScaleDown()
                btnAllGame.setScaleDown()
                btnRecord.setScaleUp()

                icRecord.setScaleUp()
                icTrending.setScaleDown()
                icAllGame.setScaleDown()

                tvTrending.setColorText(
                    ContextCompat.getColor(requireContext(), R.color.tv_home_un_selected),
                    ContextCompat.getColor(requireContext(), R.color.tv_home_un_selected))

                tvAllGame.setColorText(
                    ContextCompat.getColor(requireContext(), R.color.tv_home_un_selected),
                    ContextCompat.getColor(requireContext(), R.color.tv_home_un_selected))

                tvRecord.setColorText(
                    ContextCompat.getColor(requireContext(), R.color.tv_home_selected),
                    ContextCompat.getColor(requireContext(), R.color.tv_home_selected))

                bgTrending.setImageResource(R.drawable.ic_1)
                bgAllGame.setImageResource(R.drawable.ic_2)
                bgRecord.setImageResource(R.drawable.bg_selected)
            }
        }
    }

    override fun FragmentHomeBinding.initListener() {
        btnTrending.setSingleClick {
            viewPager.setCurrentItem(0,true)
        }
        btnAllGame.setSingleClick {
            viewPager.setCurrentItem(1,true)
        }
        btnRecord.setSingleClick {
            viewPager.setCurrentItem(2,true)
        }
    }

    override fun initObserver() {

    }

}