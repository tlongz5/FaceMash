package com.example.facemash
import androidx.recyclerview.widget.GridLayoutManager
import com.example.facemash.databinding.FragmentLanguageBinding
import com.example.magicface.base.BaseFragment
import com.example.magicface.utils.Helper
import com.example.magicface.utils.setSingleClick

class LanguageFragment : BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate){


    private lateinit var languageAdapter: LanguageAdapter
    private var changed = false

    override fun FragmentLanguageBinding.initView() {
        btnDone.isEnabled = false

        languageAdapter = LanguageAdapter { curPosition,nextPosition ->
            if (!changed) {
                changed = true
                btnDone.isEnabled = true
                btnDone.setBackgroundResource(R.drawable.bg_button_selected)
                btnDone.setTextColor(resources.getColor(R.color.white))
            }
            if (curPosition != null) Helper.languageList[curPosition].isSelected = false
            Helper.languageList[nextPosition].isSelected = true
            languageAdapter.notifyDataSetChanged()
        }

        rvLanguage.adapter = languageAdapter
        rvLanguage.layoutManager = GridLayoutManager(requireContext(), 2)

        languageAdapter.submitList(Helper.languageList)
    }

    override fun FragmentLanguageBinding.initListener() {
        btnDone.setSingleClick {
            // Handle change language
            navigateTo(R.id.introFragment)
        }
    }

    override fun initObserver() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        Helper.languageList.forEach { it.isSelected = false }
        changed = false
    }


}