package com.example.facemash.view

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.facemash.R
import com.example.facemash.base.BaseFragment
import com.example.facemash.databinding.FragmentWebViewBinding
import com.example.facemash.utils.Constants

class WebViewFragment : BaseFragment<FragmentWebViewBinding>(FragmentWebViewBinding::inflate) {
    private var isDestroyView = false
    private fun safeSettings(webSettings: WebSettings) {
        webSettings.loadsImagesAutomatically = true
    }


    private fun updateUI() {
        if (isDestroyView) return
        binding.ivNext.isEnabled = binding.webView.canGoForward()
        binding.ivPrev.isEnabled = binding.webView.canGoBack()
    }


    inner class AUMWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest
        ): Boolean {
            val url = request.url.toString()
            if (url.startsWith("http://")) {
                openUrl(url)
                return true
            }
            return false
        }


        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            updateUI()
        }


        override fun onPageFinished(view: WebView, url: String?) {


        }


        private fun openUrl(url: String) {
            val hadPrefix = url.startsWith("http", true)
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse(if (hadPrefix) url else "https://$url"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            try {
                context?.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }


    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun FragmentWebViewBinding.initView() {
        val url = Constants.URL_TERM_AND_POLICY
        if (isDestroyView) return
        binding.webView.settings.javaScriptEnabled = true
        safeSettings(binding.webView.settings)
        binding.webView.webViewClient = AUMWebViewClient()
        binding.webView.loadUrl(url)
        updateUI()
        binding.ivClose.setOnClickListener {
            navigateUp()
        }
        binding.ivReload.setOnClickListener {
            binding.webView.reload()
        }
        binding.ivNext.setOnClickListener {
            binding.webView.goForward()
            updateUI()
        }
        binding.ivPrev.setOnClickListener {
            binding.webView.goBack()
        }
    }


    override fun FragmentWebViewBinding.initListener() {


    }


    override fun initObserver() {


    }


    override fun onDestroyView() {
        isDestroyView = true
        super.onDestroyView()
    }


}
