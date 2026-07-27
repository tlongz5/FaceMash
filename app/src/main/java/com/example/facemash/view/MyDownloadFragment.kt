package com.example.facemash.view

import android.content.ContentValues
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.facemash.R
import com.example.facemash.databinding.FragmentMyDownloadBinding
import com.example.facemash.view.main.MainViewModel
import com.example.facemash.base.BaseFragment
import com.example.facemash.utils.PlayerInstance.play
import com.example.facemash.utils.PlayerInstance.player
import com.example.facemash.utils.PlayerInstance.stop
import com.example.facemash.utils.ShareHelper
import com.example.facemash.utils.setSingleClick
import com.example.facemash.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class MyDownloadFragment : BaseFragment<FragmentMyDownloadBinding>(FragmentMyDownloadBinding::inflate) {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var videoPath: String
    private lateinit var videoFile: File
    private lateinit var moreGameAdapter: MoreGameAdapter

    override fun FragmentMyDownloadBinding.initView() {
        videoPath = arguments?.getString("video_path") ?: ""
        videoFile = File(videoPath)
        if (!videoFile.exists()) {
            showToast(requireContext(), "Video not found")
            return
        }

        videoPreview.player = player
        player?.stop()
        player?.clearMediaItems()
        play(videoPath)

        moreGameAdapter = MoreGameAdapter { game ->
            when(game.second){
                "Ranking" -> {navigateTo(R.id.rankingFragment)}
                "This Or That" -> {navigateTo(R.id.thisOrThatFragment)}
                "Lip Pose" -> {navigateTo(R.id.lipPoseFragment)}
                "Princess Or Witch" -> {navigateTo(R.id.princessOrWitchFragment)}
            }
        }
        rvMoreGames.adapter = moreGameAdapter
    }

    override fun FragmentMyDownloadBinding.initListener() {
        btnBack.setSingleClick {
            navigateUp()
        }
        btnHome.setSingleClick {
            navigateTo(R.id.homeFragment)
        }
        btnShare.setSingleClick {
            if (videoFile.exists()) {
                ShareHelper.shareFiles(requireContext(), listOf(videoFile))
            } else {
                showToast(requireContext(), "File not found")
            }
        }
        btnDownload.setSingleClick {
            viewLifecycleOwner.lifecycleScope.launch {
                val uri = withContext(Dispatchers.IO) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        saveVideoForAndroid10AndAbove()
                    else saveVideoForAndroid9AndBelow()
                }
                if (uri == null) showToast(requireContext(), "Failed to save video")
                else showToast(requireContext(), "Video saved successfully")
            }
        }
    }

    override fun initObserver() {
        viewModel.gamesData.observe(viewLifecycleOwner) {
            moreGameAdapter.submitList(viewModel.game)
        }
    }

    @Suppress("DEPRECATION")
    private fun saveVideoForAndroid9AndBelow(): Uri? {
        return try {
            val moviesDir = File(
                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_MOVIES),
                "FaceMash"
            )
            if (!moviesDir.exists() && !moviesDir.mkdirs()) {
                return null
            }
            val destinationFile = File(moviesDir, "video_${System.currentTimeMillis()}.mp4")
            videoFile.inputStream().use { inputStream ->
                destinationFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            MediaScannerConnection.scanFile(
                requireContext(),
                arrayOf(destinationFile.absolutePath),
                arrayOf("video/mp4"),
                null
            )

            Uri.fromFile(destinationFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveVideoForAndroid10AndAbove(): Uri? {
        val resolver = requireContext().contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "video_${System.currentTimeMillis()}.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/FaceMash")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }

        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val videoUri = resolver.insert(collection, values) ?: return null

        return try {
            resolver.openOutputStream(videoUri).use { outputStream ->
                if (outputStream == null) return null
                videoFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(videoUri, values, null, null)
            videoUri
        } catch (e: Exception) {
            e.printStackTrace()
            resolver.delete(videoUri, null, null)
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stop()
    }
}
