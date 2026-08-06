package com.example.facemash.view

import android.app.Dialog
import android.content.ContentValues
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.OptIn
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import com.example.facemash.base.BaseFragment
import com.example.facemash.databinding.DiaglogSaveSuccessBinding
import com.example.facemash.databinding.FragmentRecordDetailBinding
import com.example.facemash.utils.Constants
import com.example.facemash.utils.PlayerInstance.pause
import com.example.facemash.utils.PlayerInstance.play
import com.example.facemash.utils.PlayerInstance.player
import com.example.facemash.utils.PlayerInstance.stop
import com.example.facemash.utils.ShareHelper
import com.example.facemash.utils.setSingleClick
import com.example.facemash.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class RecordDetailFragment : BaseFragment<FragmentRecordDetailBinding>(FragmentRecordDetailBinding::inflate) {
    private lateinit var videoPath: String
    private lateinit var videoFile: File
    private var playerListener: Player.Listener? = null

    @OptIn(UnstableApi::class)
    override fun FragmentRecordDetailBinding.initView() {
        videoPath = arguments?.getString("video_path") ?: ""
        videoFile = File(videoPath)

        if (videoPath.isNotEmpty() && videoFile.exists()) {
            player?.setVideoTextureView(videoView)
            
            playerListener = object : Player.Listener {
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    super.onVideoSizeChanged(videoSize)
                    if (videoSize.width > 0 && videoSize.height > 0) {
                        aspectRatioFrameLayout.setAspectRatio(videoSize.width.toFloat() / videoSize.height)
                    }
                }
            }
            player?.addListener(playerListener!!)
            
            play(videoPath)
        } else {
            showToast(requireContext(), "Video not found")
        }
    }

    override fun FragmentRecordDetailBinding.initListener() {
        imgBack.setSingleClick {
            navigateUp()
        }

        btnShare.setSingleClick {
            if (::videoFile.isInitialized && videoFile.exists()) {
                ShareHelper(requireContext()).shareFiles(listOf(videoFile))
            }
        }

        btnDownload.setSingleClick {
            if (::videoFile.isInitialized && videoFile.exists()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val uri = withContext(Dispatchers.IO) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            saveVideoForAndroid10AndAbove()
                        else saveVideoForAndroid9AndBelow()
                    }
                    if (uri != null) {
                        showDownloadSuccessDialog()
                    } else {
                        showToast(requireContext(), "Failed to save video")
                    }
                }
            }
        }
    }

    private fun showDownloadSuccessDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val bindingDialog = DiaglogSaveSuccessBinding.inflate(layoutInflater)
        dialog.setContentView(bindingDialog.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        bindingDialog.btnOk.setSingleClick {
            dialog.dismiss()
        }

        dialog.show()
    }

    @Suppress("DEPRECATION")
    private fun saveVideoForAndroid9AndBelow(): Uri? {
        return try {
            val moviesDir = File(
                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_MOVIES),
                Constants.GAME_RECORD_FOLDER
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
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/${Constants.GAME_RECORD_FOLDER}")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
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

    override fun initObserver() {}

    override fun onResume() {
        super.onResume()
        if (::videoPath.isInitialized && videoPath.isNotEmpty()) {
            play(videoPath)
        }
    }

    override fun onPause() {
        super.onPause()
        pause()
    }

    override fun onDestroyView() {
        playerListener?.let { player?.removeListener(it) }
        playerListener = null
        super.onDestroyView()
        stop()
    }
}
