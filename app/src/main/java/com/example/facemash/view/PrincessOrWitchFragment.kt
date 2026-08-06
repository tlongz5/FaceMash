//package com.example.facemash.view
//
//import android.app.ActivityManager
//import android.content.Context
//import android.content.Context.ACTIVITY_SERVICE
//import android.graphics.BitmapFactory
//import android.graphics.SurfaceTexture
//import android.os.Bundle
//import android.util.Log
//import android.view.Surface
//import android.view.View
//import androidx.annotation.OptIn
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ExperimentalGetImage
//import androidx.camera.core.ImageAnalysis
//import androidx.camera.core.Preview
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.activityViewModels
//import com.example.facemash.R
//import com.example.facemash.databinding.FragmentPrincessOrWitchBinding
//import com.example.facemash.view.main.MainViewModel
//import com.example.facemash.base.BaseFragment
//import com.example.facemash.utils.hide
//import com.example.facemash.utils.logd
//import com.example.facemash.utils.openGL.CustomFaceFilter
//import com.example.facemash.utils.openGL.MyCameraRenderer
//import com.example.facemash.utils.playMusic
//import com.example.facemash.utils.setSingleClick
//import com.example.facemash.utils.showToast
//import com.google.common.util.concurrent.ListenableFuture
//import com.google.mlkit.vision.common.InputImage
//import com.google.mlkit.vision.face.FaceDetection
//import com.google.mlkit.vision.face.FaceDetectorOptions
//import com.otaliastudios.cameraview.CameraException
//import com.otaliastudios.cameraview.CameraListener
//import com.otaliastudios.cameraview.VideoResult
//import com.otaliastudios.cameraview.frame.Frame
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
//class PrincessOrWitchFragment : BaseFragment<FragmentPrincessOrWitchBinding>(FragmentPrincessOrWitchBinding::inflate) {
//    private val viewModel: MainViewModel by activityViewModels()
//
//    private val customFaceFilter = CustomFaceFilter()
//
//    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
//    private lateinit var bgExecutor: ExecutorService
//
//    private var rendererSet = false
//    private var princessTexId = -1
//    private var witchTexId = -1
//
//    private val myRenderer = MyCameraRenderer()
//
//    private val detector = FaceDetection.getClient(
//        FaceDetectorOptions.Builder()
//            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
//            .build()
//    )
//
//    override fun FragmentPrincessOrWitchBinding.initView() {
//        bgExecutor = Executors.newSingleThreadExecutor()
//        initMusic()
////        setupOpenGLES()
//        setupView()
//    }
//
//    private fun setupView() {
////        binding.tvTap.setSingleClick {
////            binding.tvTap.hide()
////            startRecording()
////        }
////
////        binding.btnStop.setSingleClick {
////            if (!isRecording) return@setSingleClick
////            stopRecording()
////        }
//
//// 1. Gán Custom Filter cho CameraView
//        binding.cameraView.filter = customFaceFilter
//        // 2. Load Bitmap cho sticker truyền vào Filter
//        val princessBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_crown)
//        val witchBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_witch_hat)
//        customFaceFilter.setStickerBitmaps(princessBitmap, witchBitmap)
//
//        binding.cameraView.addFrameProcessor { frame ->
//            processFrameWithMlKit(frame)
//        }
//
//        binding.cameraView.addCameraListener(object : CameraListener() {
//            override fun onVideoRecordingStart() {
//                super.onVideoRecordingStart()
////                isRecording = true
//
//                playMusic(viewModel.selectedMusic.value!!)
//                binding.btnStop.visibility = View.VISIBLE
//
//                showToast(requireContext(), "Recording started")
////                startSpinningEffect()
//
//            }
//
//
//
//            override fun onVideoRecordingEnd() {
//                super.onVideoRecordingEnd()
////                isRecording = false
//
//                binding.btnStop.visibility = View.GONE
//            }
//
//            override fun onVideoTaken(result: VideoResult) {
//                super.onVideoTaken(result)
//                // save video
//                val videoPath = result.file.absolutePath
//                navigateTo(R.id.videoPreviewFragment, Bundle().apply {
//                    putString("video_path", videoPath)
//                })
//            }
//
//            override fun onCameraError(exception: CameraException) {
//                super.onCameraError(exception)
//                // when no permission granted
//                showToast(requireContext(), "Camera error. Bug: $exception")
//                logd("Camera error. Bug: $exception")
//                Log.e("TAGhkhhkhk", "Camera error", exception)
//            }
//        })
//
//
//    }
//
////    private fun setupOpenGLES() {
////        val activityManager = requireActivity().getSystemService(ACTIVITY_SERVICE) as ActivityManager
////        val configurationInfo = activityManager.deviceConfigurationInfo
////        val supportsEs32 = configurationInfo.reqGlEsVersion >= 0x30002
////        if (supportsEs32) {
////            binding.myGLSurfaceView.setEGLContextClientVersion(3)
////            binding.myGLSurfaceView.setRenderer(myRenderer)
////
////            myRenderer.surfaceReadyCallback = { surfaceTexture ->
////                binding.myGLSurfaceView.queueEvent {
////                    val princessBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_crown)
////                    val witchBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_witch_hat)
////
////                    princessTexId = myRenderer.loadStickerTexture(princessBitmap)
////                    witchTexId = myRenderer.loadStickerTexture(witchBitmap)
////
////                    Log.d("TAGhkhhkhk", "princessTexId: $princessTexId, witchTexId: $witchTexId ")
////                }
////
////                startCamera(surfaceTexture)
////                rendererSet = true
////            }
////        } else {
////            showToast(requireContext(), "OpenGLES 3.2 not supported")
////        }
////    }
//
//    private fun processFrameWithMlKit(frame: Frame) {
//        if (frame.dataClass != ByteArray::class.java) return
//        val data = frame.getData<ByteArray>()
//        val rotation = frame.rotationToUser
//        val size = frame.size
//        val inputImage = InputImage.fromByteArray(
//            data,
//            size.width,
//            size.height,
//            rotation,
//            InputImage.IMAGE_FORMAT_NV21
//        )
//        detector.process(inputImage)
//            .addOnSuccessListener { faces ->
//                if (faces.isEmpty()) {
//                    customFaceFilter.updateFaceData(0f, 0f, 0f, 0f, faceDetected = false)
//                    return@addOnSuccessListener
//                }
//                val face = faces[0]
//                val box = face.boundingBox
//                val isRotated = rotation == 90 || rotation == 270
//                val actualImgW = if (isRotated) size.height else size.width
//                val actualImgH = if (isRotated) size.width else size.height
//                val centerXPixel = box.centerX().toFloat()
//                val centerYPixel = box.centerY().toFloat()
//                // Quy đổi sang hệ tọa độ NDC OpenGL (-1..1)
//                val glX = 1f - (centerXPixel / actualImgW.toFloat()) * 2f
//                val glY = 1f - (centerYPixel / actualImgH.toFloat()) * 2f
//                val glScale = (box.width().toFloat() / actualImgW.toFloat()) * 1.3f
//                customFaceFilter.updateFaceData(glX, glY, glScale, -face.headEulerAngleZ, faceDetected = true)
//            }
//            .addOnFailureListener {
//                // Xử lý lỗi nếu cần
//            }
//    }
//
////    @OptIn(ExperimentalGetImage::class)
////    private fun startCamera(surfaceTexture: SurfaceTexture) {
////        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
////        cameraProviderFuture.addListener({
////            val cameraProvider = try { cameraProviderFuture.get() } catch (e: Exception) { null } ?: return@addListener
////
////            val preview = Preview.Builder().build()
////            preview.setSurfaceProvider { request ->
////                myRenderer.setCameraResolution(request.resolution.width, request.resolution.height)
////                surfaceTexture.setDefaultBufferSize(request.resolution.width, request.resolution.height)
////                val surface = Surface(surfaceTexture)
////                request.provideSurface(surface, ContextCompat.getMainExecutor(requireContext())) {
////                    surface.release()
////                }
////            }
////
////            val imageAnalysis = ImageAnalysis.Builder()
////                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
////                .build()
////
////            imageAnalysis.setAnalyzer(bgExecutor) { imageProxy ->
////                val mediaImage = imageProxy.image
////                if (mediaImage == null) {
////                    imageProxy.close()
////                    return@setAnalyzer
////                }
////
////                val rotation = imageProxy.imageInfo.rotationDegrees
////                val image = InputImage.fromMediaImage(mediaImage, rotation)
////
////                detector.process(image)
////                    .addOnSuccessListener { faces ->
////                        if (faces.isEmpty()) {
////                            myRenderer.stickerTextureIdLeft = -1
////                            myRenderer.stickerTextureIdRight = -1
////                            return@addOnSuccessListener
////                        }
////
////                        val face = faces[0]
////                        val box = face.boundingBox
////
////                        val isRotated = rotation == 90 || rotation == 270
////                        val actualImgW = if (isRotated) imageProxy.height else imageProxy.width
////                        val actualImgH = if (isRotated) imageProxy.width else imageProxy.height
////
////                        val centerXPixel = box.centerX().toFloat()
////                        val centerYPixel = box.centerY().toFloat()
////
////                        val glX = 1f - (centerXPixel / actualImgW.toFloat()) * 2f
////                        val glY = 1f - (centerYPixel / actualImgH.toFloat()) * 2f
////                        val glScale = (box.width().toFloat() / actualImgW.toFloat()) * 1.3f
////
////                        myRenderer.stickerTextureIdLeft = princessTexId
////                        customFaceFilter.stickerTextureIdRight = witchTexId
////                        customFaceFilter.updateFaceData(glX, glY, glScale, -face.headEulerAngleZ)
////                    }
////                    .addOnCompleteListener {
////                        imageProxy.close()
////                    }
////            }
////
////            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
////            try {
////                cameraProvider.unbindAll()
////                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageAnalysis)
////            } catch (e: Exception) {
////                e.printStackTrace()
////            }
////        }, ContextCompat.getMainExecutor(requireContext()))
////    }
//
//    private fun initMusic() {
//        viewModel.selectedMusic.observe(viewLifecycleOwner) { musicData ->
//            if (musicData != null) {
//                playMusic(musicData)
//                binding.tvMusic.text = musicData.name
//            } else {
//                binding.tvMusic.text = "No Sound"
//                binding.layoutExitMusic.visibility = android.view.View.GONE
//            }
//        }
//    }
//
//    override fun FragmentPrincessOrWitchBinding.initListener() {
//        btnMusic.setSingleClick { navigateTo(R.id.songPickerFragment) }
//        btnExitMusic.setSingleClick { viewModel.setSelectedMusic(null) }
//        imgFilter.setSingleClick { navigateTo(R.id.homeFragment) }
//    }
//
//    override fun onPause() {
//        super.onPause()
////        if (rendererSet) binding.myGLSurfaceView.onPause()
//    }
//
//    override fun onResume() {
//        super.onResume()
////        if (rendererSet) binding.myGLSurfaceView.onResume()
//    }
//
//    override fun initObserver() {}
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        detector.close()
//    }
//}

package com.example.facemash.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.RectF
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.facemash.R
import com.example.facemash.base.BaseFragment
import com.example.facemash.databinding.FragmentPrincessOrWitchBinding
import com.example.facemash.utils.PlayerInstance
import com.example.facemash.utils.PreferenceUtils
import com.example.facemash.utils.VideoStorage
import com.example.facemash.utils.hide
import com.example.facemash.utils.openGL.PrincessOrWitchFilter
import com.example.facemash.utils.setSingleClick
import com.example.facemash.utils.show
import com.example.facemash.view.main.MainViewModel
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.AudioCodec
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.size.AspectRatio
import com.otaliastudios.cameraview.size.SizeSelectors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PrincessOrWitchFragment : BaseFragment<FragmentPrincessOrWitchBinding>(
    FragmentPrincessOrWitchBinding::inflate
) {
    private val mainViewModel: MainViewModel by activityViewModels()
    private val ORIENTATIONS = SparseIntArray().apply {
        append(Surface.ROTATION_0, 0)
        append(Surface.ROTATION_90, 90)
        append(Surface.ROTATION_180, 180)
        append(Surface.ROTATION_270, 270)
    }
    private var isDestroyView = false

    private var outputFile: File? = null
    private var isClickStop = false
    private var isStartGame = false
    private var isMusicStopped = false

    private val detector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
        FaceDetection.getClient(options)
    }

    override fun FragmentPrincessOrWitchBinding.initView() {
        reInit()
        setupCameraView()
        setUpSound()
    }
    
    private fun reInit() {
        isDestroyView = false
        isClickStop = false
        isStartGame = false
        isMusicStopped = false
        outputFile = null
    }

    private fun setupCameraView() {
        val dimenWidth = SizeSelectors.minWidth(1000)
        val dimenHeight = SizeSelectors.minHeight(1000)
        val dimensions = SizeSelectors.and(dimenWidth, dimenHeight)
        val ratio = SizeSelectors.aspectRatio(AspectRatio.of(9, 16), 0f)

        val result = SizeSelectors.or(
            SizeSelectors.and(ratio, dimensions),
            ratio,
            SizeSelectors.biggest()
        )
        binding.cameraView.setPictureSize(result)
        binding.cameraView.setPreviewStreamSize(result)

        val princessOrWitchFilter = PrincessOrWitchFilter()
        princessOrWitchFilter.setOverlayBitmaps(
            BitmapFactory.decodeResource(resources, R.drawable.ic_crown),
            BitmapFactory.decodeResource(resources, R.drawable.ic_witch_hat)
        )

        binding.cameraView.apply {
            setRequestPermissions(false)
            setLifecycleOwner(viewLifecycleOwner)
            audioBitRate = 320000
            audioCodec = AudioCodec.AAC
            videoBitRate = 3000000
            snapshotMaxHeight = 1080
            snapshotMaxWidth = 720
            open()

            var isGetSizeImage = false
            var imageWidth = 1
            var imageHeight = 1
            var isProcessingFrame = false

            addFrameProcessor { frame ->
                if (isProcessingFrame) return@addFrameProcessor
                isProcessingFrame = true
                try {
                    if (isDestroyView) {
                        princessOrWitchFilter.hideOverlay()
                        return@addFrameProcessor
                    }

                    val rotation = getRotationCamera()
                    var rawWidth = 1
                    var rawHeight = 1
                    var inputImage: InputImage? = null

                    if (frame.dataClass == Image::class.java) {
                        val image = frame.getData<Image>()
                        if (image != null && image.width > 0 && image.height > 0) {
                            rawWidth = image.width
                            rawHeight = image.height
                            inputImage = InputImage.fromMediaImage(image, rotation)
                        }
                    } else if (frame.dataClass == ByteArray::class.java) {
                        val data = frame.getData<ByteArray>()
                        val size = frame.size
                        if (data != null && size.width > 0 && size.height > 0) {
                            rawWidth = size.width
                            rawHeight = size.height
                            inputImage = InputImage.fromByteArray(
                                data,
                                size.width,
                                size.height,
                                rotation,
                                InputImage.IMAGE_FORMAT_NV21
                            )
                        }
                    }

                    if (inputImage == null) {
                        princessOrWitchFilter.hideOverlay()
                        return@addFrameProcessor
                    }

                    val faces = Tasks.await(detector.process(inputImage))

                    lifecycleScope.launch(Dispatchers.Main) {
                        if (isDestroyView) return@launch
                        val safeBinding = bindingOrNull ?: return@launch

                        if (faces.isNotEmpty()) {
                            val face = faces.first()
                            val boundingBox = face.boundingBox
                            val viewWidth = safeBinding.cameraView.width.toFloat()
                            val viewHeight = safeBinding.cameraView.height.toFloat()

                            if (viewWidth > 0 && viewHeight > 0) {
                                val isRotated = rotation == 90 || rotation == 270
                                val rotWidth = if (isRotated) rawHeight else rawWidth
                                val rotHeight = if (isRotated) rawWidth else rawHeight

                                val scale = Math.max(viewWidth / rotWidth.toFloat(), viewHeight / rotHeight.toFloat())
                                val offsetX = (rotWidth * scale - viewWidth) / 2f
                                val offsetY = (rotHeight * scale - viewHeight) / 2f

                                val left = viewWidth - ((boundingBox.right * scale) - offsetX)
                                val right = viewWidth - ((boundingBox.left * scale) - offsetX)
                                val top = (boundingBox.top * scale) - offsetY
                                val bottom = (boundingBox.bottom * scale) - offsetY

                                val mappedRect = RectF(left, top, right, bottom)
                                val rotationZ = face.headEulerAngleZ
                                val rotationY = face.headEulerAngleY

                                princessOrWitchFilter.updateHeadPosition(mappedRect, rotationZ, rotationY, viewWidth, viewHeight)
                            } else {
                                princessOrWitchFilter.hideOverlay()
                            }
                        } else {
                            princessOrWitchFilter.hideOverlay()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isProcessingFrame = false
                }
            }
            filter = princessOrWitchFilter

            addCameraListener(object : CameraListener() {
                override fun onVideoTaken(result: VideoResult) {
                    super.onVideoTaken(result)
                    navigateTo(R.id.videoPreviewFragment, Bundle().apply {
                        putString("video_path", result.file.absolutePath)
                    })
                }
            })
        }

    }

    private fun getRotationCamera(): Int {
        return try {
            val cameraFacing = bindingOrNull?.cameraView?.facing ?: Facing.FRONT
            val cameraId = if (cameraFacing == Facing.BACK) "0" else "1"
            getRotationCompensation(cameraId, cameraFacing == Facing.FRONT)
        } catch (e: Exception) {
            e.printStackTrace()
            270
        }
    }

    private fun getRotationCompensation(cameraId: String, isFrontFacing: Boolean): Int {
        try {
            activity?.let { activity ->
                val orientation = ORIENTATIONS[activity.windowManager.defaultDisplay.rotation]
                val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
                val sensorOrientation = cameraManager?.getCameraCharacteristics(cameraId)
                    ?.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: return 270

                val rotation = if (isFrontFacing) {
                    (sensorOrientation + orientation) % 360
                } else {
                    (sensorOrientation - orientation + 360) % 360
                }
                return rotation
            }
            return 270
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 270
    }

    private fun isValidRGB(image: Image): Boolean {
        val plane0 = image.planes[0]
        val plane1 = image.planes[1]
        val plane2 = image.planes[2]

        if (plane0.pixelStride != 1 || plane1.pixelStride != 2 || plane2.pixelStride != 2) {
            return false
        }

        if (plane0.rowStride < image.width || plane1.rowStride < image.width / 2 || plane2.rowStride < image.width / 2) {
            return false
        }

        val x = image.width / 2
        val y = image.height / 2
        val yb = plane0.buffer[x + plane0.rowStride * y].toInt() and 0xFF
        val ub = plane1.buffer[(x / 2) + plane1.rowStride * (y / 2)].toInt() and 0xFF
        val vb = plane2.buffer[(x / 2) + plane2.rowStride * (y / 2)].toInt() and 0xFF

        val rgb = yuvToRGB(yb, ub, vb)
        return rgb.all { it in 0..255 }
    }

    private fun yuvToRGB(y: Int, u: Int, v: Int): IntArray {
        val c = y - 16
        val d = u - 128
        val e = v - 128

        val r = (1.164 * c + 1.596 * e).toInt().coerceIn(0, 255)
        val g = (1.164 * c - 0.392 * d - 0.813 * e).toInt().coerceIn(0, 255)
        val b = (1.164 * c + 2.017 * d).toInt().coerceIn(0, 255)

        return intArrayOf(r, g, b)
    }

    override fun FragmentPrincessOrWitchBinding.initListener() {
        setupTapToPlayInterception()

        btnBack.setSingleClick { onBack() }

        btnExitMusic.setSingleClick {
            tvMusic.setText(R.string.add_sound)
            PreferenceUtils.setValue("USE_SONG_DEFAULT", true)
            isMusicStopped = true
            PlayerInstance.stop()
            layoutExitMusic.hide()
        }

        btnMusic.setSingleClick {
            PlayerInstance.pause()
            navigateTo(R.id.songPickerFragment)
        }

        btnStop.setSingleClick {
            isClickStop = true
            stopRecord()
        }

        imgFilter.setSingleClick {
            navigateTo(R.id.homeFragment)
        }

        btnStart.setSingleClick { onTapToPlayClicked() }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTapToPlayInterception() {
        val interactiveViews = listOf(
            binding.btnMusic,
            binding.imgFilter,
            binding.btnStart,
            binding.btnStop,
            binding.btnBack
        )

        binding.cameraView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val x = event.rawX.toInt()
                val y = event.rawY.toInt()

                val isTouchedInteractiveView = interactiveViews.any { view ->
                    if (view.visibility == View.VISIBLE) {
                        val rect = android.graphics.Rect()
                        view.getGlobalVisibleRect(rect)
                        rect.contains(x, y)
                    } else {
                        false
                    }
                }

                if (!isTouchedInteractiveView) {
                    onTapToPlayClicked()
                }
            }
            false
        }
    }

    private fun onTapToPlayClicked() {
        if (binding.btnStart.visibility == View.VISIBLE) {
            binding.btnStart.hide()
            binding.imgFilter.hide()
            binding.btnMusic.hide()

            lifecycleScope.launch {
                binding.layoutCountdown.show()
                binding.tvCount.text = "3"
                delay(1000)
                binding.tvCount.text = "2"
                delay(1000)
                binding.tvCount.text = "1"
                delay(1000)
                binding.layoutCountdown.hide()
                startRecord()
            }
        }
    }

    override fun initObserver() {
        mainViewModel.selectedMusic.observe(viewLifecycleOwner) { musicData ->
            if (musicData != null) {
                binding.tvMusic.text = musicData.name
                binding.layoutExitMusic.show()
                isMusicStopped = false
                PlayerInstance.play(musicData.url)
            } else {
                binding.tvMusic.setText(R.string.add_sound)
                binding.layoutExitMusic.hide()
                isMusicStopped = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!binding.cameraView.isOpened) {
            binding.cameraView.open()
        }
        if (!isMusicStopped) {
            PlayerInstance.player?.play()
        }
    }

    override fun onDestroyView() {
        isDestroyView = true
        PlayerInstance.stop()
        detector.close()
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        if (binding.cameraView.isOpened) {
            binding.cameraView.close()
        }
        if (isStartGame && !isClickStop) {
            binding.cameraView.stopVideo()
        }
        PlayerInstance.pause()
    }

    private fun startRecord() {
        try {
            isStartGame = true
            outputFile = VideoStorage.createVideoFile(requireContext()) ?: return
            outputFile?.let { it ->
                lifecycleScope.launch {
                    delay(1000L)
                    withContext(Dispatchers.Main) {
                        binding.btnStop.show()
                    }
                }
                binding.cameraView.takeVideoSnapshot(it)
                binding.btnMusic.hide()
                PlayerInstance.player?.seekTo(0)
                PlayerInstance.player?.play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecord() {
        try {
            Log.d("Stop Record", "Stop Record")
            binding.cameraView.stopVideo()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpSound() {
        val music = mainViewModel.selectedMusic.value
        if (music != null) {
            binding.tvMusic.text = music.name
            isMusicStopped = false
            PlayerInstance.play(music.url)
        } else {
            binding.tvMusic.setText(R.string.add_sound)
            isMusicStopped = true
        }
        binding.tvMusic.isSelected = true
    }

    private fun onBack() {
        if (isStartGame) {
            return
        }
        navController.navigateUp()
    }
}
