package com.example.magicface.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.example.magicface.utils.checkIfFragmentAttached

abstract class BaseFragment<T: ViewBinding>(
    private val bindingInflater: (
        layoutInflater: LayoutInflater,
        parent: ViewGroup?,
        attachToParent: Boolean) -> T)
    : Fragment() {
    protected val navController by lazy { findNavController() }
    private var _binding: T? = null
    protected val binding get() = _binding!!

    protected fun navigateUp() = checkIfFragmentAttached { navController.navigateUp() }
    protected fun navigateTo(resId: Int,
                             args: Bundle? = null,
                             navOptions: NavOptions? = null
                    ) = checkIfFragmentAttached { navController.navigate(resId,args,navOptions) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = bindingInflater.invoke(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.initView()
        binding.initListener()
        initObserver()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    abstract fun T.initView()

    abstract fun T.initListener()

    abstract fun initObserver()
}