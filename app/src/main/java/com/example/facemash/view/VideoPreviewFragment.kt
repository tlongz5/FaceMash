package com.example.facemash.view

import android.content.ContentValues
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.lifecycle.lifecycleScope
import com.example.facemash.R
import com.example.facemash.databinding.FragmentVideoPreviewBinding
import com.example.facemash.base.BaseFragment
import com.example.facemash.utils.Constants
import com.example.facemash.utils.PlayerInstance.pause
import com.example.facemash.utils.PlayerInstance.play
import com.example.facemash.utils.PlayerInstance.player
import com.example.facemash.utils.PlayerInstance.stop
import com.example.facemash.utils.setSingleClick
import com.example.facemash.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class VideoPreviewFragment : BaseFragment<FragmentVideoPreviewBinding>(FragmentVideoPreviewBinding::inflate){
    private lateinit var videoPath: String
    private lateinit var videoFile : File
    override fun FragmentVideoPreviewBinding.initView() {
        videoPath = arguments?.getString("video_path")!!
        videoFile = File(videoPath)
        if(!videoFile.exists()) {
            showToast(requireContext(), "Video not found")
            return
        }

        videoPreview.player = player
        play(videoPath)
    }

    override fun FragmentVideoPreviewBinding.initListener() {
        btnBack.setSingleClick {
            navigateUp()
        }
        btnNext.setSingleClick {
            navigateTo(R.id.myDownloadFragment, Bundle().apply {
                putString("video_path", videoPath)
            })
        }

//        btnSave.setOnClickListener {
//            viewLifecycleOwner.lifecycleScope.launch{
//                val uri = withContext(Dispatchers.IO){
//                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
//                        saveVideoForAndroid10AndAbove()
//                    else saveVideoForAndroid9AndBelow()
//                }
//                if(uri == null) showToast(requireContext(), "Failed to save video")
//                else showToast(requireContext(), "Video saved successfully")
//
//            }
//        }
    }

    //DEBUGGGGGGGGGGG
    @Suppress("DEPRECATION")
    private fun saveVideoForAndroid9AndBelow() : Uri?{
        return try {
            val moviesDir = File(
                requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_MOVIES),
                Constants.GAME_RECORD_FOLDER
            )
            if (!moviesDir.exists() && !moviesDir.mkdirs()) {
                return null
            }
            val videoFile = File(moviesDir, "video_${System.currentTimeMillis()}.mp4")
            videoFile.outputStream().use { outputStream ->
                videoFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            MediaScannerConnection.scanFile(
                requireContext(),
                arrayOf(videoPath),
                arrayOf("video/mp4"),
                null
            )

            Uri.fromFile(videoFile)
        }catch (e: Exception){
            e.printStackTrace()
            null
        }
    }

    private fun saveVideoForAndroid10AndAbove() : Uri?{
        val resolver = requireContext().contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, videoFile.name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/${Constants.GAME_RECORD_FOLDER}")

            // Add more metadata if needed
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }

        val collection = MediaStore.Video.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val videoUri = resolver.insert(collection, values)
        if(videoUri == null) {
//            showToast(requireContext(), "Failed to save video")
            return null
        }

        return try{
            resolver.openOutputStream(videoUri).use { outputStream ->
                if(outputStream == null) {
//                    showToast(requireContext(), "Failed to save video")
                    return null
                }
                videoFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(videoUri, values, null, null)
//            showToast(requireContext(), "Video saved successfully")

            videoUri
        }catch (e:Exception) {
            e.printStackTrace()
            resolver.delete(videoUri, null, null)
//            showToast(requireContext(), "Failed to save video")
            null
        }
    }

    override fun initObserver() {

    }

    override fun onResume() {
        super.onResume()
        if (::videoPath.isInitialized) {
            play(videoPath)
        }
    }

    override fun onPause() {
        super.onPause()
        pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stop()
    }

}