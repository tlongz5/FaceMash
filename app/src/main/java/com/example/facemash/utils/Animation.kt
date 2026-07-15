package com.example.magicface.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationSet
import androidx.core.graphics.toColorInt
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

fun View.setScaleUp(){
    val scaleX = ObjectAnimator.ofFloat(this, View.SCALE_X,  1.3f)
    val scaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y,  1.3f)

    AnimatorSet().apply {
        playTogether(scaleX, scaleY)
        duration = 300
        start()
    }
}

fun View.setScaleDown(){
    val scaleX = ObjectAnimator.ofFloat(this, View.SCALE_X,  1f)
    val scaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y,  1f)

    AnimatorSet().apply {
        playTogether(scaleX, scaleY)
        duration = 300
        start()
    }
}

fun View.setSpring(){
    val scaleX = ObjectAnimator.ofFloat(this, View.SCALE_X,  0.8f)
    val scaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y,  0.8f)
    AnimatorSet().apply {
        playTogether(scaleX, scaleY)
        duration = 200
        start()
    }

    val scaleX2 = ObjectAnimator.ofFloat(this, View.SCALE_X,  1f)
    val scaleY2 = ObjectAnimator.ofFloat(this, View.SCALE_Y,  1f)
    AnimatorSet().apply {
        playTogether(scaleX2, scaleY2)
        duration = 200
        start()
    }


}
