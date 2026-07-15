package com.example.facemash.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseActivity<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater) -> VB
) : AppCompatActivity() {

    private var _binding: VB? = null

    protected val binding: VB
        get() = checkNotNull(_binding) {
            "Không được sử dụng binding sau khi Activity đã bị destroy"
        }

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = bindingInflater(layoutInflater)
        setContentView(binding.root)

        initView()
        initListener()
        initObserve()
        initData()
    }
    protected abstract fun initView()
    protected open fun initListener() = Unit
    protected open fun initObserve() = Unit
    protected open fun initData() = Unit

    protected fun handleBackPress(
        enabled: Boolean = true,
        action: () -> Unit
    ) {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(enabled) {
                override fun handleOnBackPressed() {
                    action()
                }
            }
        )
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}