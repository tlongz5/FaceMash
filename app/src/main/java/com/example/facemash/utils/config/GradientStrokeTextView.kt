package com.example.facemash.utils.config // Đổi lại package theo đúng project của bạn nếu cần

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.example.facemash.R // Đảm bảo import đúng R của project

class GradientStrokeTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var strokeColor: Int = Color.BLACK
    private var strokeWidth: Float = 0f
    private var originalTextColor: ColorStateList? = null

    // Thuộc tính Gradient và Shadow
    private var gradientStartColor: Int? = null
    private var gradientEndColor: Int? = null
    private var customShadowColor: Int = Color.TRANSPARENT
    private var customShadowDx: Float = 0f
    private var customShadowDy: Float = 0f

    init {
        originalTextColor = textColors
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.GradientStrokeTextView)
            strokeColor = a.getColor(R.styleable.GradientStrokeTextView_strokeColor, strokeColor)
            strokeWidth = a.getDimension(R.styleable.GradientStrokeTextView_strokeWidth, strokeWidth)

            if (a.hasValue(R.styleable.GradientStrokeTextView_gradientStartColor)) {
                gradientStartColor = a.getColor(R.styleable.GradientStrokeTextView_gradientStartColor, 0)
            }
            if (a.hasValue(R.styleable.GradientStrokeTextView_gradientEndColor)) {
                gradientEndColor = a.getColor(R.styleable.GradientStrokeTextView_gradientEndColor, 0)
            }
            customShadowColor = a.getColor(R.styleable.GradientStrokeTextView_shadowColor, customShadowColor)
            customShadowDx = a.getDimension(R.styleable.GradientStrokeTextView_shadowDx, customShadowDx)
            customShadowDy = a.getDimension(R.styleable.GradientStrokeTextView_shadowDy, customShadowDy)
            a.recycle()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun setTextAppearance(context: Context?, resId: Int) {
        super.setTextAppearance(context, resId)
        val a = context?.obtainStyledAttributes(resId, R.styleable.GradientStrokeTextView)
        if (a != null) {
            strokeColor = a.getColor(R.styleable.GradientStrokeTextView_strokeColor, strokeColor)
            strokeWidth = a.getDimension(R.styleable.GradientStrokeTextView_strokeWidth, strokeWidth)

            gradientStartColor = if (a.hasValue(R.styleable.GradientStrokeTextView_gradientStartColor)) {
                a.getColor(R.styleable.GradientStrokeTextView_gradientStartColor, 0)
            } else null

            gradientEndColor = if (a.hasValue(R.styleable.GradientStrokeTextView_gradientEndColor)) {
                a.getColor(R.styleable.GradientStrokeTextView_gradientEndColor, 0)
            } else null

            customShadowColor = a.getColor(R.styleable.GradientStrokeTextView_shadowColor, customShadowColor)
            customShadowDx = a.getDimension(R.styleable.GradientStrokeTextView_shadowDx, customShadowDx)
            customShadowDy = a.getDimension(R.styleable.GradientStrokeTextView_shadowDy, customShadowDy)
            a.recycle()
        }
        invalidate()
    }

    fun setStrokeColor(color: Int) {
        if (this.strokeColor != color) {
            this.strokeColor = color
            invalidate()
        }
    }

    // --- HÀM BẠN YÊU CẦU ĐỂ SET MÀU TỪ KOTLIN ---
    fun setColorText(startColor: Int, endColor: Int) {
        setStrokeColor(startColor)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val hasStroke = strokeWidth > 0f
        val hasGradient = gradientStartColor != null && gradientEndColor != null
        val hasCustomShadow = customShadowDx != 0f || customShadowDy != 0f

        if (!hasStroke && !hasGradient && !hasCustomShadow) {
            super.onDraw(canvas)
            return
        }

        val states = originalTextColor ?: textColors
        val currentTextColor = states.getColorForState(drawableState, states.defaultColor)

        // Lưu lại trạng thái paint ban đầu
        val originalShader = paint.shader
        val originalStyle = paint.style
        val originalStrokeWidth = paint.strokeWidth
        val originalColor = paint.color

        // Tinh chỉnh nét vẽ để viền mượt mà (Giống Figma)
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeMiter = 10f

        // 1. VẼ KHỐI BÓNG ĐỔ (3D SHADOW)
        if (hasCustomShadow) {
            canvas.save()
            canvas.translate(customShadowDx, customShadowDy)

            // Vẽ viền của bóng
            if (hasStroke) {
                drawTextInternal(canvas, customShadowColor, Paint.Style.STROKE, strokeWidth * 2, null)
            }
            // Vẽ lõi của bóng
            drawTextInternal(canvas, customShadowColor, Paint.Style.FILL, 0f, null)

            canvas.restore()
        }

        // 2. VẼ LỚP VIỀN CHỮ (MAIN STROKE)
        if (hasStroke) {
            drawTextInternal(canvas, strokeColor, Paint.Style.STROKE, strokeWidth * 2, null)
        }

        // 3. VẼ LÕI CHỮ ĐÈ LÊN CÙNG (MAIN FILL)
        if (hasGradient) {
            val gradient = LinearGradient(
                0f, 0f, 0f, lineHeight.toFloat(),
                gradientStartColor!!, gradientEndColor!!,
                Shader.TileMode.CLAMP
            )
            drawTextInternal(canvas, Color.WHITE, Paint.Style.FILL, 0f, gradient)
        } else {
            drawTextInternal(canvas, currentTextColor, Paint.Style.FILL, 0f, null)
        }

        // Khôi phục lại Paint về ban đầu
        paint.shader = originalShader
        paint.style = originalStyle
        paint.strokeWidth = originalStrokeWidth
        paint.color = originalColor
    }

    private fun drawTextInternal(canvas: Canvas, color: Int, style: Paint.Style, sWidth: Float, shader: Shader?) {
        paint.color = color
        paint.style = style
        paint.strokeWidth = sWidth
        paint.shader = shader

        // Không dùng super.onDraw vì nó sẽ reset màu paint. Thay vào đó vẽ layout trực tiếp.
        canvas.save()
        canvas.translate(totalPaddingLeft.toFloat(), totalPaddingTop.toFloat())
        layout?.draw(canvas)
        canvas.restore()
    }
}