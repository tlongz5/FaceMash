
package com.example.facemash

import android.graphics.ImageFormat
import android.media.Image
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.facemash.databinding.FragmentLipPoseBinding
import com.example.magicface.base.BaseFragment
import com.example.magicface.utils.setSingleClick
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import com.otaliastudios.cameraview.controls.AudioCodec
import com.otaliastudios.cameraview.frame.Frame
import kotlin.compareTo
import kotlin.text.format

class LipPoseFragment : BaseFragment<FragmentLipPoseBinding>(FragmentLipPoseBinding::inflate) {
    val detector = FaceMeshDetection.getClient(
        FaceMeshDetectorOptions.Builder()
            .setUseCase(FaceMeshDetectorOptions.FACE_MESH)
            .build()
    )

    override fun FragmentLipPoseBinding.initView() {
        setupCameraView()
    }

    fun setupCameraView() {
        binding.cameraView.apply {
            setRequestPermissions(false)
            setLifecycleOwner(viewLifecycleOwner)
            audioBitRate = 320000
            audioCodec = AudioCodec.AAC
            videoBitRate = 5000000
            snapshotMaxHeight = 1080
            snapshotMaxWidth = 720

            addFrameProcessor { frame: Frame ->
                try {
//                    if (frame.dataClass != Image::class.java ) return@addFrameProcessor

                    val image = frame.getData<Image>()

                    //Yuv_420_888 la dinh dang tra ve cua Camera2
//                    if (image.format != ImageFormat.YUV_420_888) return@addFrameProcessor
//                    if(image.height <= 0 || image.width <= 0) return@addFrameProcessor

                    val rotation = frame.rotationToView
                    val inputImage = InputImage.fromMediaImage(image, rotation)

                    detector.process(inputImage)
                        .addOnSuccessListener { faceMeshes ->
                            if (faceMeshes.isEmpty()) return@addOnSuccessListener

                            val faceMesh = faceMeshes[0]


                        }

                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    override fun FragmentLipPoseBinding.initListener() {
        btnBack.setSingleClick {
            navigateUp()
        }
        btnStart.setSingleClick {
        }
        btnStop.setSingleClick {
        }

        btnAddSound.setSingleClick {

        }
        btnAnswerQuestion.setSingleClick {

        }
    }

    override fun initObserver() {

    }

}