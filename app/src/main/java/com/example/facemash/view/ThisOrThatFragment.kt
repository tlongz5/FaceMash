package com.example.facemash.view

import android.content.Context
import android.content.res.Resources
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.createBitmap
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.model.Question
import com.example.facemash.R
import com.example.facemash.databinding.FragmentThisOrThatBinding
import com.example.facemash.view.main.MainViewModel
import com.example.facemash.base.BaseFragment
import com.example.facemash.model.Game
import com.example.facemash.utils.Constants
import com.example.facemash.utils.DimenExt
import com.example.facemash.utils.PlayerInstance
import com.example.facemash.utils.PreferenceUtils
import com.example.facemash.utils.hide
import com.example.facemash.utils.logd
import com.example.facemash.utils.setAssetImageToDrawable
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

class ThisOrThatFragment : BaseFragment<FragmentThisOrThatBinding>(FragmentThisOrThatBinding::inflate) {
    private val viewModel: MainViewModel by activityViewModels()

    private var isRecording = false
    private var isStartGame = false
    private var isAnswering = false
    private var isMusicStopped = false

    private var currentCategory: String = ""
    private var currentGame: Game? = null
    private var questions: List<Question> = listOf()
    private var currentQuestionIndex = 0

    private var currentLeftRotation = 0f
    private var currentRightRotation = 0f

    private var currentLeftTranslationX = 0f
    private var currentRightTranslationX = 0f

    private var currentLeftTranslationY = 0f
    private var currentRightTranslationY = 0f

    private val tiltThreshold = 5f

    private var isAnswered = false
    private var index = 0
    private var score = 0

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    )

    private val thisOrThatAdapter = ThisOrThatAdapter { game, _ ->
        if (game.questions.isNotEmpty()) {
            currentGame = game
            questions = game.questions
            currentQuestionIndex = 0
            resetGameView()
            updateQuestion()

            // Auto update music based on game ID
            if (game.category.equals("Music", ignoreCase = true)) {
                viewModel.dataMusicFromAssets.value?.find { it.id == game.id }?.let { music ->
                    viewModel.setSelectedMusic(music)
                }
            } else if (game.category.equals("Math", ignoreCase = true)) {
                viewModel.dataMusicFromAssets.value?.find { it.id == "apt" }?.let { music ->
                    viewModel.setSelectedMusic(music)
                }
            }
        }
    }

    override fun FragmentThisOrThatBinding.initView() {
        setUpSound()
        setupView()
        setupCameraView()
        rvItem.adapter = thisOrThatAdapter
    }

    private fun setUpSound() {
        val music = viewModel.selectedMusic.value
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

    override fun FragmentThisOrThatBinding.initListener() {
        btnBack.setSingleClick {
            navController.navigateUp()
        }
        btnMusic.setSingleClick {
            PlayerInstance.pause()
            navigateTo(R.id.songPickerFragment)
        }
        btnExitMusic.setSingleClick {
            tvMusic.setText(R.string.add_sound)
            PreferenceUtils.setValue("USE_SONG_DEFAULT", true)
            isMusicStopped = true
            PlayerInstance.stop()
            layoutExitMusic.hide()
        }
        imgFilter.setSingleClick {
            showTypePickerDialog()
        }
        tvFilter.setSingleClick {
            showTypePickerDialog()
        }

        binding.tvTap.setSingleClick {
            binding.tvTap.hide()
            startRecording()
        }
    }

    private fun showTypePickerDialog() {
        val dialog = android.app.Dialog(requireContext())
        val bindingDialog = com.example.facemash.databinding.DialogPickerThisOrThatBinding.inflate(layoutInflater)
        dialog.setContentView(bindingDialog.root)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        bindingDialog.btnMusic.setSingleClick {
            updateCategoryList("Music")
            dialog.dismiss()
        }
        bindingDialog.btnLogo.setSingleClick {
            updateCategoryList("Logo")
            dialog.dismiss()
        }
        bindingDialog.btnMath.setSingleClick {
            updateCategoryList("Math")
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateCategoryList(category: String) {
        currentCategory = category
        val allGames = viewModel.gamesData.value ?: emptyList()
        val filteredGames = when (category) {
            "Logo" -> {
                val logoList = allGames.filter {
                    it.category.equals("Logo", ignoreCase = true) ||
                    it.type.equals("Logo", ignoreCase = true) ||
                    it.name.contains("Logo", ignoreCase = true)
                }
                if (logoList.isNotEmpty()) {
                    logoList
                } else {
                    listOf(
                        Game(id = "logo1", categoryItem = "Logo App", name = "Logo", type = "Logo", category = "Logo",
                            questions = listOf(
                                Question(trueAnswer = "T1A", falseAnswer = "F1A"),
                                Question(trueAnswer = "T1B", falseAnswer = "F1B"),
                                Question(trueAnswer = "T1C", falseAnswer = "F1C")
                            )
                        ),
                        Game(id = "logo2", categoryItem = "Logo Food", name = "Logo", type = "Logo", category = "Logo",
                            questions = listOf(
                                Question(trueAnswer = "T2A", falseAnswer = "F2A"),
                                Question(trueAnswer = "T2B", falseAnswer = "F2B"),
                                Question(trueAnswer = "T2C", falseAnswer = "F2C")
                            )
                        ),
                        Game(id = "logo3", categoryItem = "Logo Car", name = "Logo", type = "Logo", category = "Logo",
                            questions = listOf(
                                Question(trueAnswer = "T3A", falseAnswer = "F3A"),
                                Question(trueAnswer = "T3B", falseAnswer = "F3B"),
                                Question(trueAnswer = "T3C", falseAnswer = "F3C")
                            )
                        ),
                        Game(id = "logo4", categoryItem = "Logo Brand", name = "Logo", type = "Logo", category = "Logo",
                            questions = listOf(
                                Question(trueAnswer = "T1D", falseAnswer = "F1D")
                            )
                        )
                    )
                }
            }
            "Math" -> {
                val mathList = allGames.filter {
                    it.category.equals("Math", ignoreCase = true) ||
                    it.type.equals("Math", ignoreCase = true)
                }
                if (mathList.isNotEmpty()) mathList else listOf(
                    Game(id = "math1", categoryItem = "Addition", name = "Math", type = "Text", category = "Math",
                        questions = listOf(Question(question = "1 + 1 = ?", trueAnswer = "2", falseAnswer = "3"))
                    ),
                    Game(id = "math2", categoryItem = "Substraction", name = "Math", type = "Text", category = "Math",
                        questions = listOf(Question(question = "5 - 2 = ?", trueAnswer = "3", falseAnswer = "2"))
                    )
                )
            }
            else -> {
                allGames.filter {
                    it.category.equals("Music", ignoreCase = true) &&
                    it.type.equals("Text", ignoreCase = true)
                }
            }
        }

        val listToDisplay = if (filteredGames.isNotEmpty()) filteredGames else allGames
        thisOrThatAdapter.submitList(listToDisplay)

        val selectedGame = listToDisplay.firstOrNull()
        if (selectedGame != null && selectedGame.questions.isNotEmpty()) {
            currentGame = selectedGame
            questions = selectedGame.questions
            currentQuestionIndex = 0
            resetGameView()
            updateQuestion()

            // Initial music setup
            if (selectedGame.category.equals("Music", ignoreCase = true)) {
                viewModel.dataMusicFromAssets.value?.find { it.id == selectedGame.id }?.let { music ->
                    viewModel.setSelectedMusic(music)
                }
            } else if (selectedGame.category.equals("Math", ignoreCase = true)) {
                viewModel.dataMusicFromAssets.value?.find { it.id == "apt" }?.let { music ->
                    viewModel.setSelectedMusic(music)
                }
            }
        }
    }

    override fun initObserver() {
        viewModel.selectedMusic.observe(viewLifecycleOwner) { musicData ->
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

        binding.root.setSingleClick {
            if (isRecording) return@setSingleClick
            else {
                binding.tvTap.hide()
                startRecording()
            }
        }

        val category = arguments?.getString("category") ?: "Music"
        if (currentCategory.isEmpty()) {
            currentCategory = category
        }
        viewModel.gamesData.observe(viewLifecycleOwner){ games ->
            if (games.isNotEmpty()) {
                updateCategoryList(currentCategory)
            }
        }
    }

    private fun updateQuestion() {
        if (questions.isNotEmpty() && currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]
            val leftAnswer: String
            val rightAnswer: String
            if (question.trueSide == 0) {
                leftAnswer = question.trueAnswer
                rightAnswer = question.falseAnswer
            } else {
                leftAnswer = question.falseAnswer
                rightAnswer = question.trueAnswer
            }

            val isLogoGame = currentGame?.type.equals("Logo", ignoreCase = true) ||
                    currentGame?.category.equals("Logo", ignoreCase = true) ||
                    currentGame?.name?.contains("Logo", ignoreCase = true) == true

            if (isLogoGame) {
                binding.quizGame.tvLogoQuestion.show()
                binding.quizGame.viewQuestion.root.visibility = View.INVISIBLE
                binding.quizGame.capyLeft.hide()
                binding.quizGame.capyRight.hide()

                binding.quizGame.viewAnswerLeft.txtAnswer.hide()
                binding.quizGame.viewAnswerLeft.icon1.hide()
                binding.quizGame.viewAnswerLeft.icon2.hide()
                binding.quizGame.viewAnswerLeft.icon3.hide()
                binding.quizGame.viewAnswerLeft.icon4.hide()
                binding.quizGame.viewAnswerLeft.imgLogo.show()
                val leftPath = if (leftAnswer.endsWith(".webp")) "logo/$leftAnswer" else "logo/$leftAnswer.webp"
                binding.quizGame.viewAnswerLeft.imgLogo.setImageDrawable(
                    setAssetImageToDrawable(requireContext(), leftPath)
                )

                binding.quizGame.viewAnswerRight.txtAnswer.hide()
                binding.quizGame.viewAnswerRight.icon1.hide()
                binding.quizGame.viewAnswerRight.icon2.hide()
                binding.quizGame.viewAnswerRight.icon3.hide()
                binding.quizGame.viewAnswerRight.icon4.hide()
                binding.quizGame.viewAnswerRight.imgLogo.show()
                val rightPath = if (rightAnswer.endsWith(".webp")) "logo/$rightAnswer" else "logo/$rightAnswer.webp"
                binding.quizGame.viewAnswerRight.imgLogo.setImageDrawable(
                    setAssetImageToDrawable(requireContext(), rightPath)
                )
            } else {
                binding.quizGame.tvLogoQuestion.hide()
                binding.quizGame.viewQuestion.root.show()
                binding.quizGame.capyLeft.show()
                binding.quizGame.capyRight.show()

                binding.quizGame.viewQuestion.txtQuestion.text = question.question
                binding.quizGame.viewQuestion.txtQuestion.show()

                binding.quizGame.viewAnswerLeft.imgLogo.hide()
                binding.quizGame.viewAnswerLeft.icon1.hide()
                binding.quizGame.viewAnswerLeft.icon2.hide()
                binding.quizGame.viewAnswerLeft.icon3.hide()
                binding.quizGame.viewAnswerLeft.icon4.hide()
                binding.quizGame.viewAnswerLeft.txtAnswer.text = leftAnswer
                binding.quizGame.viewAnswerLeft.txtAnswer.show()

                binding.quizGame.viewAnswerRight.imgLogo.hide()
                binding.quizGame.viewAnswerRight.icon1.hide()
                binding.quizGame.viewAnswerRight.icon2.hide()
                binding.quizGame.viewAnswerRight.icon3.hide()
                binding.quizGame.viewAnswerRight.icon4.hide()
                binding.quizGame.viewAnswerRight.txtAnswer.text = rightAnswer
                binding.quizGame.viewAnswerRight.txtAnswer.show()
            }
        }
    }

    private fun setupView() {
        binding.btnStop.setSingleClick {
            if (!isRecording) return@setSingleClick
            stopRecording()
        }

        binding.cameraView.addCameraListener(object : CameraListener() {
            override fun onVideoRecordingStart() {
                super.onVideoRecordingStart()
                isRecording = true

                PlayerInstance.player?.seekTo(0)
                PlayerInstance.player?.play()

                binding.btnStop.visibility = View.VISIBLE

                showToast(requireContext(), "Recording started")

            }

            override fun onVideoRecordingEnd() {
                super.onVideoRecordingEnd()
                isRecording = false
                isStartGame = false

//                binding.btnStart.visibility = View.VISIBLE
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
                val eulerZ = face.headEulerAngleZ

                val viewLeft = binding.quizGame.viewAnswerLeft.root
                val viewRight = binding.quizGame.viewAnswerRight.root

                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    if (eulerZ > tiltThreshold) {
                        handleRightBeforeStart(eulerZ, viewLeft, viewRight)
                    } else if (eulerZ < -tiltThreshold) {
                        handleLeftBeforeStart(eulerZ, viewLeft, viewRight)
                    }else {
                        // Đầu đang ở giữa
                        if (isStartGame && isAnswering) {
                            // Nếu game đang chạy và NGƯỜI CHƠI VỪA TRẢ LỜI XONG -> Next câu hỏi
                            nextQuestion(viewLeft, viewRight)
                        } else {
                            // Nếu chưa trả lời gì cả thì chỉ giữ UI ở trạng thái cân bằng
                            resetViews(viewLeft, viewRight)
                        }
                    }


//                        if (eulerZ > tiltThreshold) {
//                            // Nghiêng sang phải
//                            handleRightBeforeStart(eulerZ, viewLeft, viewRight)
//                        } else if (eulerZ < -tiltThreshold) {
//                            // Nghiêng sang trái
//                            handleLeftBeforeStart(eulerZ, viewLeft, viewRight)
//                        } else {
//                            resetViews(viewLeft, viewRight)
//                        }

                }


            }

        }

    }
    private fun resetGameView() {
        lifecycleScope.launch(Dispatchers.Main) {
            isAnswered = false
            index = 0
            score = 0

            binding.quizGame.viewAnswerLeft.root.setBackgroundResource(R.drawable.bg_item_quiz)
            binding.quizGame.viewAnswerRight.root.setBackgroundResource(R.drawable.bg_item_quiz)
            binding.quizGame.viewAnswerLeft.root.rotation = 0f
            binding.quizGame.viewAnswerLeft.root.translationX = 0f
            binding.quizGame.viewAnswerRight.root.rotation = 0f
            binding.quizGame.viewAnswerRight.root.translationX = 0f
            binding.quizGame.viewAnswerLeft.root.translationY = 0f
            binding.quizGame.viewAnswerRight.root.translationY = 0f

        }
    }



    private fun resetViews(viewLeft: View, viewRight: View) {
        isAnswering = false
        binding.quizGame.viewAnswerLeft.root.setBackgroundResource(R.drawable.bg_item_quiz)
        binding.quizGame.viewAnswerRight.root.setBackgroundResource(R.drawable.bg_item_quiz)

        viewLeft.translationX = 0f
        viewLeft.translationY = 0f
        viewLeft.rotation = 0f

        viewRight.translationX = 0f
        viewRight.translationY = 0f
        viewRight.rotation = 0f

        currentLeftRotation = 0f
        currentLeftTranslationX = 0f
        currentLeftTranslationY = 0f
        currentRightRotation = 0f
        currentRightTranslationX = 0f
        currentRightTranslationY = 0f
    }

    private fun handleLogic(
        eulerZ: Float,
        viewLeft: View,
        viewRight: View
    ) {
        if (isAnswering) return

        if (eulerZ > tiltThreshold * 3) {
            handleChoice(false, viewLeft, viewRight)
        } else if (eulerZ < -tiltThreshold * 3) {
            handleChoice(true, viewLeft, viewRight)
        } else if (eulerZ > tiltThreshold) {
            handleRightBeforeStart(eulerZ, viewLeft, viewRight)
        } else if (eulerZ < -tiltThreshold) {
            handleLeftBeforeStart(eulerZ, viewLeft, viewRight)
        } else {
            resetViews(viewLeft, viewRight)
        }
    }

    private fun handleChoice(isLeft: Boolean, viewLeft: View, viewRight: View) {
        isAnswering = true
        viewLifecycleOwner.lifecycleScope.launch {
            if (isLeft) {
                if (questions[currentQuestionIndex].trueSide == 0) {
                    binding.quizGame.viewAnswerLeft.root.setBackgroundResource(R.drawable.bg_item_quiz_correct)
                    ++score
                }
                else binding.quizGame.viewAnswerLeft.root.setBackgroundResource(R.drawable.bg_item_quiz_in_correct)
            } else {
                if (questions[currentQuestionIndex].trueSide == 1) {
                    binding.quizGame.viewAnswerRight.root.setBackgroundResource(R.drawable.bg_item_quiz_correct)
                    ++score
                }
                else binding.quizGame.viewAnswerRight.root.setBackgroundResource(R.drawable.bg_item_quiz_in_correct)
            }
        }
    }

//    private fun handleLeftBeforeStart(
//        eulerZ: Float,
//        viewLeft: View,
//        viewRight: View
//    ) {
//
//    }
//
//    private fun handleRightBeforeStart(
//        eulerZ: Float,
//        viewLeft: View,
//        viewRight: View
//    ) {
//        val alpha = 0.8f
//        currentRightRotation = alpha * (eulerZ/0.8f - currentRightRotation)
//        currentRightTranslationX = alpha *
//
//    }

    private fun handleRightBeforeStart(rotation: Float, viewLeft: View, viewRight: View) {
//        val targetRotation = rotation * 1.25f
//        val targetTransX = DimenExt.getDpf(rotation) * 2.22f
//        val targetTransY = DimenExt.getDpf(rotation) * 2.0f
//
//        // Áp dụng thẳng vào View
//        viewRight.rotation = targetRotation
//        viewRight.translationX = targetTransX
//        viewRight.translationY = targetTransY

        val alpha = 0.8f
        currentRightRotation += alpha * ((rotation / 0.8f) - currentRightRotation)
        currentRightTranslationX += alpha * ((DimenExt.getDpf(rotation) / 0.45f) - currentRightTranslationX)
        currentRightTranslationY += alpha * ((DimenExt.getDpf(rotation) / 0.5f) - currentRightTranslationY)
        viewRight.rotation = currentRightRotation
        viewRight.translationX = currentRightTranslationX
        viewRight.translationY = currentRightTranslationY

        // Reset View Right
        viewLeft.rotation = 0f
        viewLeft.translationX = 0f
        viewLeft.translationY = 0f

//        viewRight.rotation = rotation
//        viewRight.translationX = rotation/90f * resources.getDimensionPixelSize(R.dimen._200sdp)
//        viewRight.translationY = rotation/90f * resources.getDimensionPixelSize(R.dimen._100sdp)
//        viewLeft.translationX = 0f
//        viewLeft.translationY = 0f
//        viewLeft.rotation = 0f

        if (rotation>15f && isStartGame && !isAnswering){
            handleChoice(false, viewLeft, viewRight)
        }
    }

    private fun handleLeftBeforeStart(rotation: Float, viewLeft: View, viewRight: View) {
        // Khuếch đại biên độ trực tiếp, không dùng nội suy (alpha)
//        val targetRotation = rotation * 1.25f
//        val targetTransX = DimenExt.getDpf(rotation) * 2.22f
//        val targetTransY = -DimenExt.getDpf(rotation) * 2.0f
//
//        // Áp dụng thẳng vào View
//        viewLeft.rotation = targetRotation
//        viewLeft.translationX = targetTransX
//        viewLeft.translationY = targetTransY

        val alpha = 0.8f
        currentLeftRotation += alpha * ((rotation / 0.8f) - currentLeftRotation)
        currentLeftTranslationX += alpha * ((DimenExt.getDpf(rotation) / 0.45f) - currentLeftTranslationX)
        currentLeftTranslationY += alpha * ((-DimenExt.getDpf(rotation) / 0.5f) - currentLeftTranslationY)
        viewLeft.rotation = currentLeftRotation
        viewLeft.translationX = currentLeftTranslationX
        viewLeft.translationY = currentLeftTranslationY

        // Reset View Right
        viewRight.rotation = 0f
        viewRight.translationX = 0f
        viewRight.translationY = 0f

//        viewLeft.rotation = rotation
//        viewLeft.translationX = rotation/90f * resources.getDimensionPixelSize(R.dimen._200sdp)
//        viewLeft.translationY = -rotation/90f * resources.getDimensionPixelSize(R.dimen._100sdp)
//        viewRight.translationX = 0f
//        viewRight.translationY = 0f
//        viewRight.rotation = 0f

        if (rotation<-15f && isStartGame && !isAnswering){
            handleChoice(true, viewLeft, viewRight)
        }
    }

    private fun nextQuestion(viewLeft: View, viewRight: View) {
        currentQuestionIndex++

        if (currentQuestionIndex < questions.size) {
            updateQuestion()
            // Gọi resetViews để đưa UI về giữa và mở khóa isAnswering = false
            resetViews(viewLeft, viewRight)
        } else {
            binding.quizGame.root.hide()

            binding.bgScore.show()
            binding.tvScore.show()

            val scoreText = (score.toFloat() / questions.size * 100).toInt()
            binding.tvScore.text = "$scoreText"

            // Kết thúc game
            isStartGame = false
            isAnswering = false
        }
    }



//    private fun handleLeftBeforeStart(rotation: Float, viewLeft: View, viewRight: View) {
//        val alpha = 0.8f
//        currentLeftRotation += alpha * ((-rotation / 0.8f) - currentLeftRotation)
//        currentLeftTranslationX += alpha * ((-DimenExt.getDpf(rotation) / 0.45f) - currentLeftTranslationX)
//        currentLeftTranslationY += alpha * ((DimenExt.getDpf(rotation) / 0.5f) - currentLeftTranslationY)
//        viewLeft.rotation = currentLeftRotation
//        viewLeft.translationX = currentLeftTranslationX
//        viewLeft.translationY = currentLeftTranslationY
//        viewRight.translationX = 0f
//        viewRight.translationY = 0f
//        viewRight.rotation = 0f
//    }

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
        val videoDirectory = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES), Constants.GAME_RECORD_FOLDER)
        if (!videoDirectory.exists() && !videoDirectory.mkdirs()) {
            showToast(requireContext(), "Failed to create directory")
            return
        }

        val videoFile = File(videoDirectory,"test_${System.currentTimeMillis()}.mp4")

        try {
            this.lifecycleScope.launch {
                binding.tvTap.hide()
                binding.imgIcon.hide()
                binding.tvMusic.hide()
                binding.imgFilter.hide()
                binding.btnMusic.hide()
                binding.tvFilter.hide()
                binding.rvItem.hide()
                binding.btnBack.hide()


                binding.layoutCountdown.show()
                delay(1000.milliseconds)
                binding.tvCount.text = "2"
                delay(1000.milliseconds)
                binding.tvCount.text = "1"
                delay(1000.milliseconds)
                binding.layoutCountdown.hide()

                //start record
                currentQuestionIndex = 0
                isAnswering = false
                updateQuestion()
                isStartGame=true
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

    override fun onResume() {
        super.onResume()
        if (!isMusicStopped) {
            PlayerInstance.player?.play()
        }
    }

    override fun onPause() {
        super.onPause()
        PlayerInstance.pause()
    }



    override fun onDestroyView() {
        binding.cameraView.clearFrameProcessors()
        super.onDestroyView()
        PlayerInstance.stop()
    }

}