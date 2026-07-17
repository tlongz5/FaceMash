package com.example.facemash.utils.config

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import androidx.core.graphics.toColorInt
import com.example.facemash.R
import kotlin.math.tan

class GradientStrokeTextView : androidx.appcompat.widget.AppCompatTextView {
    private var startColor = Color.BLUE
    private var endColor = Color.GREEN
    private var angle = 0f
    private var isUseOnlyStroke = false
    private var strokeWidth = 4f
    private val listColor by lazy { ArrayList<String>() }
    private val rotationColorMatrix by lazy { Matrix() }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {

    }

    fun setColorText(startColor: Int, endColor: Int) {
        this.startColor = startColor
        this.endColor = endColor
        invalidate()
    }

    fun setListColor(listColor: List<String>) {
        this.listColor.clear()
        this.listColor.addAll(listColor)
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        paint.shader = LinearGradient(
            0f, 0f, 0f, lineHeight.toFloat(),
            "#FFFFFF".toColorInt(),
            "#FFFFFF".toColorInt(),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.FILL
        super.onDraw(canvas)

        paint.shader = LinearGradient(
            0f, 0f, 0f, lineHeight.toFloat(),
            "#000000".toColorInt(),
            "#000000".toColorInt(),
            Shader.TileMode.CLAMP
        )
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        super.onDraw(canvas)
    }
}