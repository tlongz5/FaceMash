package com.example.facemash.view.detail

import android.Manifest
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.media3.common.Player
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.facemash.R
import com.example.facemash.databinding.FragmentDetailBinding
import com.example.facemash.view.main.MainViewModel
import com.example.facemash.base.BaseFragment
import com.example.facemash.utils.Constants
import com.example.facemash.utils.PlayerInstance

class DetailFragment : BaseFragment<FragmentDetailBinding>(FragmentDetailBinding::inflate){
    private val mainViewModel: MainViewModel by activityViewModels()

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var cnt=0
        permissions.forEach { (key, value) ->
            if(!value){
                if(!shouldShowRequestPermissionRationale(key)){

                    return@registerForActivityResult
                }
            }else ++cnt

            if(cnt==2){
                // navigate to màn khác
//                navigateTo(resId = R.id.)
            }

        }

//        val cameraGranted = permissions[android.Manifest.permission.CAMERA] == true
//        val recordAudioGranted = permissions[android.Manifest.permission.RECORD_AUDIO] == true
//        if (cameraGranted && recordAudioGranted) {
//            // Permission is granted
//
//        } else {
//            showToast(requireContext(),"Full permissions are required to perform this action")
//        }
    }

    private fun checkPermission() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        requestPermission.launch(permissions)
    }

    private val pageCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            binding.viewPager.post {
                playVideoAtPosition(position)
            }
        }
    }

    private fun playVideoAtPosition(position: Int) {
        PlayerInstance.stop()
        val videoUrl = mainViewModel.trendingData.value!![position].video?:return

        val recyclerView = binding.viewPager.getChildAt(0) as RecyclerView

        val holder = recyclerView.findViewHolderForAdapterPosition(position)
                as DetailAdapter.DetailViewHolder

//        holder.binding.playerView.player = PlayerInstance.player
        
        PlayerInstance.play(videoUrl)
    }

    override fun FragmentDetailBinding.initView() {
        val startPosition = arguments?.getInt("position") ?: 0
        val type = arguments?.getString("type") ?: ""

        val videoList = if(type == Constants.FilterType.ALL_FILTER) mainViewModel.trendingData.value!!
                    else mainViewModel.trendingData.value!!.filter { it.filterType == type }

        viewPager.adapter = DetailAdapter(videoList = videoList){
            // navigateTo màn khác
            checkPermission()
        }
        viewPager.setCurrentItem(startPosition, false)
        viewPager.registerOnPageChangeCallback(pageCallback)
        viewPager.post {
            playVideoAtPosition(startPosition)
        }

        PlayerInstance.player?.addListener(object : Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when(playbackState){
                    Player.STATE_BUFFERING -> {
                        binding.animLoading.visibility = View.VISIBLE
                        binding.animLoading.playAnimation()
                    }
                    Player.STATE_READY -> {
                        binding.animLoading.visibility = View.GONE
                    }
                    Player.STATE_ENDED -> {
                        binding.animLoading.visibility = View.GONE
                    }
                    Player.STATE_IDLE -> {
                        binding.animLoading.visibility = View.GONE
                    }
                }
            }
        })
    }

    override fun FragmentDetailBinding.initListener() {
        btnBack.setOnClickListener {
            navigateUp()
        }
    }

    override fun initObserver() {

    }

    override fun onDestroyView() {
        binding.viewPager.unregisterOnPageChangeCallback(pageCallback)
        super.onDestroyView()
    }

}