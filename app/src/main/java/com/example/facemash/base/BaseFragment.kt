package com.example.facemash.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.example.facemash.utils.checkIfFragmentAttached
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseFragment<T: ViewBinding>(
    private val bindingInflater: (
        layoutInflater: LayoutInflater,
        parent: ViewGroup?,
        attachToParent: Boolean) -> T)
    : Fragment() {
    protected val navController by lazy { findNavController() }
    private var _binding: T? = null
    protected val binding get() = _binding!!
    protected val bindingOrNull get() = _binding

    protected fun navigateUp() = checkIfFragmentAttached { 
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            navController.navigateUp() 
        }
    }
    
    protected fun navigateTo(resId: Int,
                             args: Bundle? = null,
                             navOptions: NavOptions? = null
                    ) = checkIfFragmentAttached { 
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            navController.navigate(resId,args,navOptions)
        }
    }

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