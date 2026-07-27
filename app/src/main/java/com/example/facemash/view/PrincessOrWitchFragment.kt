package com.example.facemash.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory.decodeByteArray
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.YuvImage
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.graphics.createBitmap
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.facemash.R
import com.example.facemash.databinding.FragmentPrincessOrWitchBinding
import com.example.facemash.view.main.MainViewModel
import com.example.facemash.base.BaseFragment
import com.example.facemash.utils.MusicPlayerInstance
import com.example.facemash.utils.hide
import com.example.facemash.utils.logd
import com.example.facemash.utils.playMusic
import com.example.facemash.utils.setSingleClick
import com.example.facemash.utils.show
import com.example.facemash.utils.showToast
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMesh
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.AudioCodec
import com.otaliastudios.cameraview.size.AspectRatio
import com.otaliastudios.cameraview.size.SizeSelectors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.hypot
import kotlin.time.Duration.Companion.milliseconds

class PrincessOrWitchFragment : BaseFragment<FragmentPrincessOrWitchBinding>(FragmentPrincessOrWitchBinding::inflate){
    private val viewModel: MainViewModel by activityViewModels()

    private var isRecording = false

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    )

    override fun FragmentPrincessOrWitchBinding.initView() {
        initMusic()
        setupCameraView()
        setupView()
    }

    private fun initMusic() {
        viewModel.selectedMusic.observe(viewLifecycleOwner){ musicData ->
            if(musicData != null) {
                playMusic(musicData)
                binding.tvMusic.text = musicData.name
            }else{
                binding.tvMusic.text = "No Sound"
                binding.layoutExitMusic.hide()
            }
        }
    }

    override fun FragmentPrincessOrWitchBinding.initListener() {
        btnMusic.setSingleClick {
            navigateTo(R.id.songPickerFragment)
        }
        btnExitMusic.setSingleClick {
            viewModel.setSelectedMusic(null)
        }

        imgLamp.setSingleClick {

        }

        imgFilter.setSingleClick {
            navigateTo(R.id.homeFragment)
        }
    }

    override fun initObserver() {

    }

    private fun setupView() {
        binding.btnStart.setOnClickListener {
            startRecording()
        }

        binding.btnStop.setOnClickListener {
            stopRecording()
        }

        binding.cameraView.addCameraListener(object : CameraListener() {
            override fun onVideoRecordingStart() {
                super.onVideoRecordingStart()
                isRecording = true

                playMusic(viewModel.selectedMusic.value!!)

                binding.btnStart.visibility = View.GONE
                binding.btnStop.visibility = View.VISIBLE

                showToast(requireContext(), "Recording started")
            }

            override fun onVideoRecordingEnd() {
                super.onVideoRecordingEnd()
                isRecording = false

                binding.btnStart.visibility = View.VISIBLE
                binding.btnStop.visibility = View.GONE
            }

            override fun onVideoTaken(result: VideoResult) {
                super.onVideoTaken(result)

                // save video
                val videoPath = result.file.absolutePath
                navigateTo(R.id.videoPreviewFragment, Bundle().apply {
                    putString("video_path", videoPath)
                })

            }

            override fun onCameraError(exception: CameraException) {
                super.onCameraError(exception)
                // when no permission granted
                showToast(requireContext(), "Camera error. Bug: $exception")
                logd("Camera error. Bug: $exception")
                Log.e("TAGhkhhkhk", "Camera error", exception)
            }
        })
    }

    private fun getScreenSize(context: Context): Pair<Int,Int>{
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels - (60*displayMetrics.density).toInt()
        return Pair(screenWidth,screenHeight)
    }

    private fun setupResolution(screenWidth: Int, screenHeight: Int) {
        val dimenWidth = SizeSelectors.minWidth(1000)
        val dimenHeight = SizeSelectors.minHeight(1000)
        val dimensions = SizeSelectors.and(dimenWidth, dimenHeight)

        val ratio = SizeSelectors.aspectRatio(AspectRatio.of(screenWidth, screenHeight), 0f)
        val result = SizeSelectors.or(
            SizeSelectors.and(ratio, dimensions),
            ratio,
            SizeSelectors.biggest()
        )
        binding.cameraView.setPreviewStreamSize(result)
    }

    private fun setupCameraView() {
        val (screenWidth, screenHeight) = getScreenSize(requireContext())

        setupResolution(screenWidth,screenHeight)
        Log.d("TAGhkhhkhk", "Come here 3" )

        binding.cameraView.apply {
            setLifecycleOwner(viewLifecycleOwner)
            audioBitRate = 320000
            audioCodec = AudioCodec.AAC
            videoBitRate = 5000000
            snapshotMaxHeight = 1080
            snapshotMaxWidth = 720
            Log.d("TAGhkhhkhk", "Come here 2" )

            addFrameProcessor { frame ->
                val data = frame.getData<ByteArray>()
                val rotation = frame.rotationToView

                val inputImage = InputImage.fromByteArray(
                    data,
                    frame.size.width,
                    frame.size.height,
                    rotation,
                    InputImage.IMAGE_FORMAT_NV21
                )

                val faces = try {
                    Tasks.await(detector.process(inputImage))
                } catch (e: Exception) {
                    return@addFrameProcessor
                }

                if (faces.isEmpty()) {
                    return@addFrameProcessor
                }

                val face = faces[0]
                val boundingBox = face.boundingBox
                val imageWidth = frame.size.width
                val imageHeight = frame.size.height
                setPositionLayoutGame(rotation,boundingBox,imageWidth,imageHeight)


            }

        }

    }

    fun setPositionLayoutGame(
        rotationCamera: Int,
        boundingBox: Rect,
        imageWidth: Int,
        imageHeight: Int
    ) {
        val rotatedWidth = if (rotationCamera == 90 || rotationCamera == 270) imageHeight else imageWidth
        val rotatedHeight = if (rotationCamera == 90 || rotationCamera == 270) imageWidth else imageHeight
        val viewWidth = binding.cameraView.width.toFloat()
        val viewHeight = binding.cameraView.height.toFloat()
        val scale = maxOf(
            viewHeight/rotatedHeight.toFloat(),
            viewWidth/rotatedWidth.toFloat()
        )

        val dx = (viewWidth - scale*rotatedWidth)/2f
        val dy = (viewHeight - scale*rotatedHeight)/2f

        var faceCenterX = boundingBox.centerX() * scale + dx
        // lay giua
        faceCenterX = viewWidth - faceCenterX

        var screenTop = boundingBox.top*scale+dy

        val faceWidthRatio = boundingBox.width().toFloat()/rotatedWidth.toFloat()
        val baseRatio = 0.4f
        val scaleFactor = (faceWidthRatio/baseRatio).coerceIn(0.4f,1f)

//        val quizGameView = binding.quizGame
//        if (quizGameView.width == 0 || quizGameView.height == 0) return



    }

    private fun convertNv21ToBitmap(data: ByteArray, width: Int, height: Int, rotation: Int): Bitmap? {
        try {
            val yuvImage = YuvImage(data, ImageFormat.NV21, width, height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
            val imageBytes = out.toByteArray()
            val bitmap = decodeByteArray(imageBytes, 0, imageBytes.size)

            if (rotation != 0) {
                val matrix = Matrix()
                matrix.postRotate(rotation.toFloat())
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun startRecording() {
        if (isRecording) return
        val videoDirectory = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES),"Test")
        if (!videoDirectory.exists() && !videoDirectory.mkdirs()) {
            showToast(requireContext(), "Failed to create directory")
            return
        }

        val videoFile = File(videoDirectory,"test_${System.currentTimeMillis()}.mp4")

        try {
            this.lifecycleScope.launch {
                binding.imgLamp.hide()
                binding.tvMusic.hide()
                binding.imgFilter.hide()
                binding.btnMusic.hide()
                binding.tvFilter.hide()


                binding.layoutCountdown.show()
                delay(1000.milliseconds)
                binding.tvCount.text = "2"
                delay(1000.milliseconds)
                binding.tvCount.text = "1"
                delay(1000.milliseconds)
                binding.layoutCountdown.hide()

                //start record
                binding.cameraView.takeVideoSnapshot(videoFile)
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        if(!isRecording) return
        binding.cameraView.stopVideo()
    }

    override fun onDestroyView() {
        binding.cameraView.clearFrameProcessors()
        super.onDestroyView()
        MusicPlayerInstance.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.close()
    }

}