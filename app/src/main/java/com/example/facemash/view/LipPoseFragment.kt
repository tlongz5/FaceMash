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
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.example.facemash.R
import androidx.core.graphics.createBitmap
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.facemash.databinding.FragmentLipPoseBinding
import com.example.facemash.model.Point
import com.example.facemash.view.main.MainViewModel
import com.example.magicface.base.BaseFragment
import com.example.magicface.utils.MusicPlayerInstance
import com.example.magicface.utils.hide
import com.example.magicface.utils.logd
import com.example.magicface.utils.playMusic
import com.example.magicface.utils.setSingleClick
import com.example.magicface.utils.show
import com.example.magicface.utils.showToast
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.facemesh.FaceMesh
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.AudioCodec
import com.otaliastudios.cameraview.size.AspectRatio
import com.otaliastudios.cameraview.size.SizeSelectors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class LipPoseFragment : BaseFragment<FragmentLipPoseBinding>(FragmentLipPoseBinding::inflate) {
    private val viewModel: MainViewModel by activityViewModels()

    private var listLipPose = mutableListOf<Bitmap>()
    private val activeFallingLips = mutableListOf<ImageView>()

    private var countFalling = 0

    private var lastCaptureTime = 0L

    val options = FaceMeshDetectorOptions.Builder()
        .setUseCase(FaceMeshDetectorOptions.FACE_MESH)
        .build()

    val detector = FaceMeshDetection.getClient(options)

    private var isRecording = false
    private var isStartGame1 = false
    private var isStartGame2 = false

    private var score = 0

    override fun FragmentLipPoseBinding.initView() {
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

    override fun FragmentLipPoseBinding.initListener() {
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

                viewLifecycleOwner.lifecycleScope.launch {
                    binding.imgScan.animate()
                        .translationY(getScreenSize(requireContext()).second.toFloat())
                        .setDuration(2500)
                        .setInterpolator(LinearInterpolator())
                        .withEndAction {
                            binding.imgLip.visibility = View.GONE
                        }
                        .start()
                    delay(2500.milliseconds)

                    binding.imgScan.animate()
                        .translationY(0f)
                        .setDuration(2500)
                        .setInterpolator(LinearInterpolator())
                        .withEndAction {
                            binding.imgScan.visibility = View.GONE
                        }
                        .start()
                    delay(2500.milliseconds)

                    isStartGame1 = false
                    isStartGame2 = true
                    // Start spawning lips
                    startSpawningLips()
                }

                binding.btnStart.visibility = View.GONE
                binding.btnStop.visibility = View.VISIBLE

                showToast(requireContext(), "Recording started")

                viewLifecycleOwner.lifecycleScope.launch {
                    binding.imgScan.visibility = View.VISIBLE
                    binding.imgScan.animate()
                        .translationY(getScreenSize(requireContext()).second.toFloat())
                        .setDuration(2500)
                        .start()

                    delay(2500.milliseconds)

                    binding.imgScan.animate()
                        .translationY(0f)
                        .setDuration(2500)
                        .withEndAction {
                            binding.imgScan.visibility = View.GONE
                        }
                        .start()
                }
            }

            override fun onVideoRecordingEnd() {
                super.onVideoRecordingEnd()
                isRecording = false
                isStartGame1 = false
                isStartGame2 = false
                countFalling = 0
                
                // Clear active lips
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    activeFallingLips.forEach { 
                        binding.main.removeView(it)
                    }
                    activeFallingLips.clear()
                }

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

                val faceMeshes = try {
                    Tasks.await(detector.process(inputImage))
                } catch (e: Exception) {
                    return@addFrameProcessor
                }

                if (faceMeshes.isEmpty()) {
                    return@addFrameProcessor
                }

                val faceMesh = faceMeshes[0]
                val upperLipTop = faceMesh.getPoints(FaceMesh.UPPER_LIP_TOP)
                val lowerLipBottom = faceMesh.getPoints(FaceMesh.LOWER_LIP_BOTTOM)

                if (upperLipTop.isEmpty() || lowerLipBottom.isEmpty()) return@addFrameProcessor

                // Phase 1: Capture multiple lips
                if (isStartGame1 && listLipPose.size < 10) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastCaptureTime > 200) {
                        val fullLip = upperLipTop + lowerLipBottom.reversed()
                        val originalBitmap = convertNv21ToBitmap(data, frame.size.width, frame.size.height, rotation)
                        if (originalBitmap != null) {
                            val resultBitmap = createBitmap(originalBitmap.width, originalBitmap.height)
                            val canvas = Canvas(resultBitmap)
                            val path = Path()
                            val firstPoint = fullLip[0].position
                            path.moveTo(firstPoint.x, firstPoint.y)
                            for (i in 1 until fullLip.size) {
                                val point = fullLip[i].position
                                path.lineTo(point.x, point.y)
                            }
                            path.close()

                            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                            paint.shader = BitmapShader(originalBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                            canvas.drawPath(path, paint)
                            val bounds = RectF()
                            path.computeBounds(bounds, true)
                            val left = bounds.left.toInt()
                            val top = bounds.top.toInt()
                            val width = bounds.width().toInt()
                            val height = bounds.height().toInt()
                            if (width > 0 && height > 0) {
                                val finalLipBitmap = Bitmap.createBitmap(resultBitmap, left, top, width, height)
                                listLipPose.add(finalLipBitmap)
                                lastCaptureTime = currentTime
//                                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
//                                    binding.imgLip.setImageBitmap(finalLipBitmap)
//                                    binding.imgLip.visibility = View.VISIBLE
//                                    binding.imgLip.translationY = 0f
//                                    binding.imgLip.animate().translationY(200f).setDuration(300).withEndAction {
//                                        binding.imgLip.visibility = View.GONE
//                                    }.start()
//                                }
                            }
                        }
                    }
                }

                // Phase 2: Game Collision Detection
                if (isStartGame2) {
                    val (screenWidth, screenHeight) = getScreenSize(requireContext())
                    
                    // User lip center in image coordinates
                    var sumX = 0f
                    var sumY = 0f
                    val allPoints = upperLipTop + lowerLipBottom
                    allPoints.forEach {
                        sumX += it.position.x
                        sumY += it.position.y
                    }
                    val avgX = sumX / allPoints.size
                    val avgY = sumY / allPoints.size
                    
                    // Image to Screen coordinates conversion
                    val isRotated = rotation == 90 || rotation == 270
                    val imgW = if (isRotated) frame.size.height else frame.size.width
                    val imgH = if (isRotated) frame.size.width else frame.size.height
                    
                    val userLipX = avgX * (screenWidth.toFloat() / imgW)
                    val userLipY = avgY * (screenHeight.toFloat() / imgH)

                    // Check collision with all active falling lips
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        val iterator = activeFallingLips.iterator()
                        while (iterator.hasNext()) {
                            val lipView = iterator.next()
                            if (lipView.visibility != View.VISIBLE) continue

                            // View's center on screen
                            val viewX = lipView.x + lipView.width / 2f
                            val viewY = lipView.y + lipView.height / 2f

                            val dist = hypot(userLipX - viewX, userLipY - viewY)
                            
                            // Threshold depends on lip size, let's say 100px or based on lip width
                            if (dist < 150f) { 
                                score++
                                lipView.visibility = View.GONE
                                // iterator.remove() // We'll let animation endAction clean up or clean up here
                                showToast(requireContext(), "Score: $score")
                            }
                        }
                    }
                }
            }

        }

    }

    private fun startSpawningLips() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            while (isStartGame2 && countFalling<10) {
                spawnOneLip()
                ++countFalling
                delay(1500.milliseconds) // Spawn every 1.5 seconds
            }

            delay(2500.milliseconds)

            binding.bgScore.visibility = View.VISIBLE
            binding.tvScore.visibility = View.VISIBLE

            val scoreText = score*10
            binding.tvScore.text = "$scoreText"

        }
    }

    private fun spawnOneLip() {
        if (listLipPose.isEmpty()) return
        
        val (screenWidth, screenHeight) = getScreenSize(requireContext())
        
        val lipView = ImageView(requireContext())
        lipView.setImageBitmap(listLipPose.random())
        
        // Add to layout
        binding.main.addView(lipView)

        // Add to current active lips
        activeFallingLips.add(lipView)

        val startX = (Math.random() * (screenWidth - 200)).toFloat()
        lipView.x = startX
        lipView.y = -200f
        
        lipView.animate()
            .translationY(screenHeight.toFloat() + 200f)
            .setDuration(4000)
            .setInterpolator(LinearInterpolator())
            .withEndAction {
                binding.main.removeView(lipView)
                activeFallingLips.remove(lipView)
            }
            .start()
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
                binding.imgLipPose.hide()
                binding.imgIcon.hide()
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
                isStartGame1=true
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
        activeFallingLips.clear()
        super.onDestroyView()
        MusicPlayerInstance.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.close()
    }
}