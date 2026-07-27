package com.example.facemash.view

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.facemash.R
import com.example.facemash.databinding.FragmentRankingBinding
import com.example.facemash.view.main.MainViewModel
import com.example.facemash.base.BaseFragment
import com.example.facemash.utils.MusicPlayerInstance
import com.example.facemash.utils.hide
import com.example.facemash.utils.logd
import com.example.facemash.utils.playMusic
import com.example.facemash.utils.setAssetImageToDrawable
import com.example.facemash.utils.setSingleClick
import com.example.facemash.utils.show
import com.example.facemash.utils.showToast
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.AudioCodec
import com.otaliastudios.cameraview.size.AspectRatio
import com.otaliastudios.cameraview.size.SizeSelectors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.text.toFloat
import kotlin.time.Duration.Companion.milliseconds

class RankingFragment : BaseFragment<FragmentRankingBinding>(FragmentRankingBinding::inflate) {
    private var isRecording = false
    private var isCountDown = false
    private val viewModel: MainViewModel by activityViewModels()
    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    )

    private var randomPos = 0

    private var listToShow = mutableListOf<Drawable?>()

    private var timer: CountDownTimer? = null

    // Các giá trị đã được làm mượt
    private var smoothedX = 0f
    private var smoothedY = 0f
    private var smoothedScale = 1f

    // Hệ số mượt (0.1 -> 0.3 là tốt nhất).
// Càng nhỏ càng mượt nhưng sẽ có độ trễ nhẹ, càng lớn càng nhạy nhưng dễ giật.
    private val SMOOTHING_FACTOR = 0.4f

    private val rankingAdapter = RankingAdapter { _, position ->
        val curPos = position%viewModel.firstElementsList.size
        listToShow = viewModel.ranking[curPos].map{ setAssetImageToDrawable(requireContext(),it)}.toMutableList()
        binding.quizGame.setImageDrawable(setAssetImageToDrawable(requireContext(),viewModel.multipliedList[position]))
    }

    override fun FragmentRankingBinding.initView() {
        initMusic()
        setupView()
        setupCameraView()

        listToShow = viewModel.ranking[0].map{ setAssetImageToDrawable(requireContext(),it)}.toMutableList()
        rvItem.adapter = rankingAdapter
        rankingAdapter.submitList(viewModel.multipliedList)
        quizGame.setImageDrawable(listToShow[0])
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

                val imageWidth = frame.size.width
                val imageHeight = frame.size.height

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
                val eulerZ = face.headEulerAngleZ

                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    setPositionLayoutGame(rotation,boundingBox,imageWidth,imageHeight)

                }


            }

        }

    }

//    private fun setPositionLayoutGame( rotationCamera: Int,box: Rect, imageWidth: Int, imageHeight: Int) {
//        val rotatedWidth = if (rotationCamera == 90 || rotationCamera == 270) imageHeight else imageWidth
//        val rotatedHeight = if (rotationCamera == 90 || rotationCamera == 270) imageWidth else imageHeight
//        val viewWidth = binding.cameraView.width.toFloat()
//        val viewHeight = binding.cameraView.height.toFloat()
//        val scale = maxOf(
//            viewHeight/rotatedHeight.toFloat(),
//            viewWidth/rotatedWidth.toFloat()
//        )
//
//        // khi centerCrop, ảnh bị lệch ra 1 khoảng, nên cần phải dịch lại
//        val dx = (viewWidth - scale*rotatedWidth)/2f
//        val dy = (viewHeight - scale*rotatedHeight)/2f
//
//        var faceCenterX = box.centerX() * scale + dx
//        faceCenterX = viewWidth -faceCenterX
//
//        val screenTop = box.top*scale+dy
//
//        val faceWidthRatio = box.width().toFloat()/rotatedWidth.toFloat()
//        val baseRatio = 0.4f
//        val scaleFactor = (faceWidthRatio/baseRatio).coerceIn(0.4f,1f)
//
//        val quizGameView = binding.quizGame
//        if (quizGameView.width == 0 || quizGameView.height == 0) return
//        quizGameView.pivotX = quizGameView.width/2f
//        quizGameView.pivotY = quizGameView.height.toFloat()
//
//        quizGameView.scaleX = scaleFactor
//        quizGameView.scaleY = scaleFactor
//        quizGameView.x = faceCenterX - quizGameView.width/2f
//        quizGameView.y = screenTop - quizGameView.height
//    }

    private fun setPositionLayoutGame(rotationCamera: Int, box: Rect, imageWidth: Int, imageHeight: Int) {
        // ... (Giữ nguyên phần tính toán scale, dx, dy, faceCenterX, screenTop hiện tại) ...

        val rotatedWidth = if (rotationCamera == 90 || rotationCamera == 270) imageHeight else imageWidth
        val rotatedHeight = if (rotationCamera == 90 || rotationCamera == 270) imageWidth else imageHeight
        val viewWidth = binding.cameraView.width.toFloat()
        val viewHeight = binding.cameraView.height.toFloat()
        val scale = maxOf(
            viewHeight/rotatedHeight.toFloat(),
            viewWidth/rotatedWidth.toFloat()
        )

        // khi centerCrop, ảnh bị lệch ra 1 khoảng, nên cần phải dịch lại
        val dx = (viewWidth - scale*rotatedWidth)/2f
        val dy = (viewHeight - scale*rotatedHeight)/2f

        var faceCenterX = box.centerX() * scale + dx
        faceCenterX = viewWidth -faceCenterX

        val screenTop = box.top*scale+dy
        val quizGameView = binding.quizGame
        if (quizGameView.width == 0 || quizGameView.height == 0) return

        val faceWidthRatio = box.width().toFloat()/rotatedWidth.toFloat()
        val baseRatio = 0.4f
        val scaleFactor = (faceWidthRatio/baseRatio).coerceIn(0.4f,1f)

        // Tọa độ mục tiêu (Target) mà ML Kit trả về
        val targetX = faceCenterX - quizGameView.width / 2f
        val targetY = screenTop - quizGameView.height
        val targetScale = scaleFactor

        // Áp dụng thuật toán làm mượt: NewValue = OldValue + Alpha * (Target - OldValue)
        if (smoothedX == 0f) {
            // Lần đầu tiên thì gán thẳng để không bị bay từ góc màn hình ra
            smoothedX = targetX
            smoothedY = targetY
            smoothedScale = targetScale
        } else {
            smoothedX += SMOOTHING_FACTOR * (targetX - smoothedX)
            smoothedY += SMOOTHING_FACTOR * (targetY - smoothedY)
            smoothedScale += SMOOTHING_FACTOR * (targetScale - smoothedScale)
        }

        // Gán các giá trị đã làm mượt vào View
        quizGameView.pivotX = quizGameView.width / 2f
        quizGameView.pivotY = quizGameView.height.toFloat()

        quizGameView.x = smoothedX
        quizGameView.y = smoothedY
        quizGameView.scaleX = smoothedScale
        quizGameView.scaleY = smoothedScale
    }

    private fun setupView() {
        binding.tvTap.setSingleClick {
            binding.tvTap.hide()
            startRecording()
        }

        binding.btnStop.setSingleClick {
            if (!isRecording) return@setSingleClick
            stopRecording()
        }

        binding.cameraView.addCameraListener(object : CameraListener() {
            override fun onVideoRecordingStart() {
                super.onVideoRecordingStart()
                isRecording = true

                playMusic(viewModel.selectedMusic.value!!)
                binding.btnStop.visibility = View.VISIBLE

                showToast(requireContext(), "Recording started")
                startSpinningEffect()

            }



            override fun onVideoRecordingEnd() {
                super.onVideoRecordingEnd()
                isRecording = false

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

    private fun startSpinningEffect(){
        isCountDown = true
        timer?.cancel()
        timer = object : CountDownTimer(2000,50){
            override fun onTick(millisUntilFinished: Long) {
                randomPos = (0 until listToShow.size).random()
                val drawable = listToShow[randomPos]
                binding.quizGame.setImageDrawable(drawable)
            }
            override fun onFinish() {
                val finalPos = listToShow.size-1
                binding.quizGame.setImageDrawable(listToShow[finalPos])
                isCountDown= false
            }
        }
        timer?.start()
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
                binding.imgIcon.hide()
                binding.tvMusic.hide()
                binding.btnMusic.hide()
                binding.tvFilter.hide()
                binding.rvItem.hide()
                binding.imgRanking.hide()


                binding.layoutCountdown.show()
                delay(1000.milliseconds)
                binding.tvCount.text = "2"
                delay(1000.milliseconds)
                binding.tvCount.text = "1"
                delay(1000.milliseconds)
                binding.layoutCountdown.hide()

                //start record
//                currentQuestionIndex = 0
//                isAnswering = false
//                updateQuestion()
//                isStartGame=true
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

    override fun FragmentRankingBinding.initListener() {

    }

    override fun initObserver() {
        binding.root.setSingleClick {
            if (isRecording) return@setSingleClick
            else {
                startRecording()
            }
        }

        with(binding){
            tv1.setSingleClick {
                handleLogicView(tv1)
            }
            tv2.setSingleClick {
                handleLogicView(tv2)
            }
            tv3.setSingleClick {
                handleLogicView(tv3)
            }
            tv4.setSingleClick {
                handleLogicView(tv4)
            }
            tv5.setSingleClick {
                handleLogicView(tv5)
            }
            tv6.setSingleClick {
                handleLogicView(tv6)
            }
            tv7.setSingleClick {
                handleLogicView(tv7)
            }
            tv8.setSingleClick {
                handleLogicView(tv8)
            }
            tv9.setSingleClick {
                handleLogicView(tv9)
            }
            tv10.setSingleClick {
                handleLogicView(tv10)
            }

        }
    }

    private fun handleLogicView(tv: TextView) {
        if (isRecording && !isCountDown) {
            tv.text = ""
            tv.isEnabled = false
            tv.background = listToShow.last()
            listToShow.removeLastOrNull()
            if (listToShow.isNotEmpty()) startSpinningEffect()
            else binding.quizGame.setImageDrawable(null)
        }
    }

    override fun onDestroyView() {
        timer?.cancel()
        binding.cameraView.clearFrameProcessors()
        super.onDestroyView()
        MusicPlayerInstance.stop()
    }

}