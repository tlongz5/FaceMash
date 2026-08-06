package com.example.facemash.view.language

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.navigation.NavOptions
import com.example.facemash.R
import com.example.facemash.databinding.FragmentLanguageBinding
import com.example.facemash.base.BaseFragment
import com.example.facemash.di.App
import com.example.facemash.utils.Helper
import com.example.facemash.utils.PreferenceUtils
import com.example.facemash.utils.setSingleClick
import com.example.facemash.utils.Constants
import com.example.facemash.utils.setAppLocale

class LanguageFragment : BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate) {
    private lateinit var languageAdapter: LanguageAdapter
    private var changed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            Helper.languageList.forEach { it.isSelected = false }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun FragmentLanguageBinding.initView() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        val selectedLanguage = Helper.languageList.find { it.isSelected }
        if (selectedLanguage != null) {
            changed = true
            btnDone.isEnabled = true
            btnDone.alpha = 1.0f
            btnDone.setBackgroundResource(R.drawable.bg_button_selected)
            btnDone.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            val selectedIndex = Helper.languageList.indexOf(selectedLanguage)
            languageAdapter = LanguageAdapter { curPosition, nextPosition ->
                onLanguageSelected(curPosition, nextPosition)
            }
            languageAdapter.curPosition = selectedIndex
        } else {
            btnDone.isEnabled = false
            btnDone.alpha = 0.6f
            languageAdapter = LanguageAdapter { curPosition, nextPosition ->
                onLanguageSelected(curPosition, nextPosition)
            }
        }

        rvLanguage.adapter = languageAdapter
        rvLanguage.layoutManager = GridLayoutManager(requireContext(), 2)

        languageAdapter.submitList(Helper.languageList)


        (requireActivity().application as App).collapsibleAds.loadCollapsibleAds(requireActivity(), binding.adCollapsibleView)

    }

    private fun onLanguageSelected(curPosition: Int?, nextPosition: Int) {
        if (!changed) {
            changed = true
            binding.btnDone.isEnabled = true
            binding.btnDone.alpha = 1.0f
            binding.btnDone.setBackgroundResource(R.drawable.bg_button_selected)
            binding.btnDone.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
        if (curPosition != null) Helper.languageList[curPosition].isSelected = false
        Helper.languageList[nextPosition].isSelected = true
        languageAdapter.notifyDataSetChanged()
    }

    override fun FragmentLanguageBinding.initListener() {
        btnDone.setSingleClick {
            val selectedLanguage = Helper.languageList.firstOrNull { it.isSelected } ?: return@setSingleClick
            val languageCode = selectedLanguage.languageCode

            PreferenceUtils.setValue(Constants.LANGUAGE_CODE, languageCode)

            (requireActivity().application as App).interAds.showInterAds(requireActivity())
            
            val fromSplash = arguments?.getBoolean("from_splash", false) ?: false
            if (fromSplash) {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.languageFragment, true)
                    .build()
                navController.navigate(R.id.introFragment, null, navOptions)
            } else {
                navController.popBackStack()
            }
            
            // Apply locale after navigation command to ensure state is saved with new destination
            requireContext().setAppLocale(languageCode)
        }
    }

    override fun initObserver() {

    }

    override fun onDestroyView() {
        (requireActivity().application as App).collapsibleAds.onDestroy()
        super.onDestroyView()
    }
}
