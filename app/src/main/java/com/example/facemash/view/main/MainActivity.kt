package com.example.facemash.view.main

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.facemash.databinding.ActivityMainBinding
import com.example.facemash.di.App
import com.example.facemash.view.NetworkReceiver
import com.example.facemash.utils.Constants
import com.example.facemash.utils.PreferenceUtils
import com.example.facemash.utils.setAppLocale
import com.example.facemash.utils.logd
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private val networkReceiver = NetworkReceiver(this) {
        with(mainViewModel) {
            getMusicData(this@MainActivity)
            getTrendingData()
            getGamesData(this@MainActivity)
            logd("getData")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val languageCode = PreferenceUtils.getValue(Constants.LANGUAGE_CODE, "en")
        setAppLocale(languageCode)
        
        installSplashScreen()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // 1. Lấy controller điều khiển các thanh hệ thống
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // 2. Cấu hình hành vi: Vuốt từ mép màn hình để hiện lại thanh điều hướng tạm thời
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // 3. Ẩn thanh điều hướng bên dưới (Navigation Bar)
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        val manager = (application as App).appOpenAdManager
        manager.loadAd(this)

        registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkReceiver)
    }
}
