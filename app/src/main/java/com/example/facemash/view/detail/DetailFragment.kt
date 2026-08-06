package com.example.facemash.view.detail

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.media3.common.Player
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.facemash.R
import com.example.facemash.databinding.DialogPermissionBinding
import com.example.facemash.databinding.FragmentDetailBinding
import com.example.facemash.view.main.MainViewModel
import com.example.facemash.base.BaseFragment
import com.example.facemash.model.TrendingData
import com.example.facemash.utils.Constants
import com.example.facemash.utils.PlayerInstance
import androidx.core.graphics.drawable.toDrawable

class DetailFragment : BaseFragment<FragmentDetailBinding>(FragmentDetailBinding::inflate){
    private val mainViewModel: MainViewModel by activityViewModels()
    private var currentVideoList: List<TrendingData> = listOf()
    private var lastPlayedPosition = -1
    private var playerListener: Player.Listener? = null

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var allGranted = true
        
        permissions.forEach { (key, granted) ->
            if (!granted) {
                allGranted = false
                if (!shouldShowRequestPermissionRationale(key)) {
//                    isPermanentlyDenied = true
                    showPermissionDialog()
                }
            }
        }

        if (allGranted) {
            // Chỉ khi đồng ý tất cả mới chuyển màn
            val hashtag = bindingOrNull?.viewPager?.let { currentVideoList[it.currentItem].hasTag }
            handleNavigateToChallenge(hashtag)
        }
//        else if (isPermanentlyDenied) {
//            // Chỉ hiện dialog Setting khi bị từ chối vĩnh viễn
//            showPermissionDialog()
//        } else {
//            // Người dùng từ chối nhưng chưa chọn "Don't ask again"
//            com.example.facemash.utils.showToast(requireContext(), "Permissions are required to continue")
//        }
    }

    private fun showPermissionDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogPermissionBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.btnLater.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSetting.setOnClickListener {
            dialog.dismiss()
            openAppSettings()
        }

        dialog.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun handleNavigateToChallenge(hashtag: String?) {
        // Logic điều hướng dựa trên hashtag ở đây
        when(hashtag){
            "#Lip_Poses" -> {
                mainViewModel.dataMusicFromAssets.value!!.find { it.name == "Lip Puzzle" }?.let { mainViewModel.setSelectedMusic(it) }
                navigateTo(R.id.lipPoseFragment)
            }
            "#Ranking_Challenge" -> {
                mainViewModel.dataMusicFromAssets.value!!.find { it.name == "APT" }?.let { mainViewModel.setSelectedMusic(it) }
                navigateTo(R.id.rankingFragment)
            }
            "#Math_Challenge" -> {
                mainViewModel.dataMusicFromAssets.value!!.find { it.name == "APT" }?.let { mainViewModel.setSelectedMusic(it) }
                navigateTo(R.id.thisOrThatFragment,Bundle().apply {
                    putString("category", "Math")
                })
            }
            "#Logo_Challenge" -> {
                mainViewModel.dataMusicFromAssets.value!!.find { it.name == "APT" }?.let { mainViewModel.setSelectedMusic(it) }
                navigateTo(R.id.thisOrThatFragment,Bundle().apply {
                    putString("category", "Logo")
                })
            }
            "#Song_Challenge" -> {
                mainViewModel.dataMusicFromAssets.value!!.find { it.name == "APT" }?.let { mainViewModel.setSelectedMusic(it) }
                navigateTo(R.id.thisOrThatFragment,Bundle().apply {
                    putString("category", "Music")
                })
            }
            else -> {
                // Danh sách 4 loại challenge ngẫu nhiên
                val randomOption = (1..4).random()
                
                when(randomOption) {
                    1 -> { // Lip Pose
                        mainViewModel.dataMusicFromAssets.value?.find { it.name == "Lip Puzzle" }?.let { mainViewModel.setSelectedMusic(it) }
                        navigateTo(R.id.lipPoseFragment)
                    }
                    2 -> { // Ranking
                        mainViewModel.dataMusicFromAssets.value?.find { it.name == "APT" }?.let { mainViewModel.setSelectedMusic(it) }
                        navigateTo(R.id.rankingFragment)
                    }
                    3 -> { // Princess or Witch
                        mainViewModel.dataMusicFromAssets.value?.find { it.name == "APT" }?.let { mainViewModel.setSelectedMusic(it) }
                        navigateTo(R.id.princessOrWitchFragment)
                    }
                    4 -> { // This or That (với category ngẫu nhiên)
                        val randomCategory = listOf("Math", "Logo", "Music").random()
                        mainViewModel.dataMusicFromAssets.value?.find { it.name == "APT" }?.let { mainViewModel.setSelectedMusic(it) }
                        navigateTo(R.id.thisOrThatFragment, Bundle().apply {
                            putString("category", randomCategory)
                        })
                    }
                }
            }

        }
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
            bindingOrNull?.viewPager?.post {
                playVideoAtPosition(position)
            }
        }
    }

    private fun playVideoAtPosition(position: Int) {
        val recyclerView = bindingOrNull?.viewPager?.getChildAt(0) as? RecyclerView ?: return

        // Gỡ player khỏi vị trí cũ
        if (lastPlayedPosition != -1) {
            val lastHolder = recyclerView.findViewHolderForAdapterPosition(lastPlayedPosition) as? DetailAdapter.DetailViewHolder
            lastHolder?.binding?.playerView?.player = null
        }

        PlayerInstance.stop()
        if (currentVideoList.isEmpty() || position >= currentVideoList.size) return
        val videoUrl = currentVideoList[position].video ?: return

        val holder = recyclerView.findViewHolderForAdapterPosition(position) as? DetailAdapter.DetailViewHolder

        if (holder != null) {
            holder.binding.playerView.player = PlayerInstance.player
            PlayerInstance.play(videoUrl)
            lastPlayedPosition = position
        } else {
            bindingOrNull?.viewPager?.postDelayed({
                val retryHolder = recyclerView.findViewHolderForAdapterPosition(position) as? DetailAdapter.DetailViewHolder
                retryHolder?.let {
                    it.binding.playerView.player = PlayerInstance.player
                    PlayerInstance.play(videoUrl)
                    lastPlayedPosition = position
                }
            }, 100)
        }
    }

    override fun FragmentDetailBinding.initView() {
        val startPosition = arguments?.getInt("position") ?: 0
        val type = arguments?.getString("type") ?: ""

        currentVideoList = if(type == Constants.FilterType.ALL_FILTER) mainViewModel.trendingData.value!!
                    else mainViewModel.trendingData.value!!.filter { it.filterType == type }

        viewPager.adapter = DetailAdapter(videoList = currentVideoList){
            checkPermission()
        }
        viewPager.setCurrentItem(startPosition, false)
        viewPager.registerOnPageChangeCallback(pageCallback)
        viewPager.post {
            playVideoAtPosition(startPosition)
        }

        playerListener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                val binding = bindingOrNull ?: return
                when (playbackState) {
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
        }
        PlayerInstance.player?.addListener(playerListener!!)
    }

    override fun FragmentDetailBinding.initListener() {
        btnBack.setOnClickListener {
            navigateUp()
        }
        
    }

    override fun initObserver() {
        
    }

    override fun onResume() {
        super.onResume()
        if (currentVideoList.isNotEmpty()) {
            bindingOrNull?.viewPager?.let {
                playVideoAtPosition(it.currentItem)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        PlayerInstance.pause()
    }

    override fun onStop() {
        super.onStop()
        PlayerInstance.stop()
    }

    override fun onDestroyView() {
        bindingOrNull?.viewPager?.unregisterOnPageChangeCallback(pageCallback)
        playerListener?.let { PlayerInstance.player?.removeListener(it) }
        playerListener = null
        super.onDestroyView()
    }

}