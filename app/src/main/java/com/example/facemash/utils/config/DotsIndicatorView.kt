package com.example.facemash.utils.config


import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.facemash.R
import kotlin.math.abs
import kotlin.math.max

class DotsIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var selectedColor: Int = ContextCompat.getColor(context, R.color.primary)
    private var unselectedColor: Int = ContextCompat.getColor(context, R.color.gray_light)
    private var numDots: Int = 3
    private var dotRadius = 18f
    private var dotSpacing = 24f

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Biến dùng chung để vẽ hình chữ nhật bo góc (hình thoi kéo dãn)
    private val rectF = RectF()

    private var currentPosition = 0
    private var positionOffset = 0f

    fun setDotCount(count: Int) {
        numDots = max(1, count)
        requestLayout()
        invalidate()
    }

    // Nhận dữ liệu vuốt trực tiếp từ ViewPager2
    fun setScrollPosition(position: Int, positionOffset: Float) {
        this.currentPosition = position
        this.positionOffset = positionOffset
        invalidate() // Vẽ lại liên tục
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = ((dotRadius * 2 * numDots) + (dotSpacing * (numDots - 1)) + paddingLeft + paddingRight).toInt()
        val height = ((dotRadius * 2) + paddingTop + paddingBottom).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            val cy = height / 2f

            // 1. Vẽ tất cả các chấm nền tĩnh màu xám (Unselected)
            dotPaint.color = unselectedColor
            dotPaint.alpha = 255 // Đảm bảo nền hiển thị rõ hoàn toàn
            for (i in 0 until numDots) {
                val cx = paddingLeft + dotRadius + i * (dotRadius * 2 + dotSpacing)
                drawCircle(cx, cy, dotRadius, dotPaint)
            }

            // 2. Vẽ chấm màu hồng/tím biến dạng thành hình thoi/nhộng kéo dãn khi vuốt dở
            if (numDots > 0) {
                // Tọa độ tâm X của mốc hiện tại (mốc cũ)
                val leftCx = paddingLeft + dotRadius + currentPosition * (dotRadius * 2 + dotSpacing)

                // Tọa độ tâm X của mốc tiếp theo (mốc mới)
                val rightCx = if (currentPosition < numDots - 1) {
                    leftCx + (dotRadius * 2 + dotSpacing)
                } else {
                    leftCx
                }

                // --- TÍNH TOÁN KÉO GIÃN HÌNH THOI ---
                // Khi positionOffset tăng từ 0.0 -> 0.5: Cạnh phải của hình thoi sẽ giãn nhanh sang phải trước
                // Khi positionOffset tăng từ 0.5 -> 1.0: Cạnh trái sẽ co nhanh sang phải để hoàn thiện hình tròn mới
                val startX: Float
                val endX: Float

                if (positionOffset < 0.5f) {
                    startX = leftCx - dotRadius
                    // Cạnh phải dãn rộng dần sang mốc mới
                    endX = leftCx + dotRadius + (positionOffset * 2) * (dotRadius * 2 + dotSpacing)
                } else {
                    // Cạnh trái bắt đầu co lại về phía mốc mới
                    startX = leftCx - dotRadius + ((positionOffset - 0.5f) * 2) * (dotRadius * 2 + dotSpacing)
                    endX = rightCx + dotRadius
                }

                // --- TÍNH TOÁN ĐỘ XUYÊN THẤU (ALPHA FADE) ---
                // Giảm độ xuyên thấu nhiều nhất (mờ đi) khi ở chính giữa khoảng vuốt (offset = 0.5)
                // Công thức: Đạt cực tiểu (ví dụ ~35% Opacity, tương đương Alpha = 90) tại offset = 0.5
                val minAlpha = 90f // Trị số Alpha tối thiểu (0 -> 255)
                val alphaFactor = abs(positionOffset - 0.5f) * 2f // Chạy từ 1.0 -> 0.0 -> 1.0
                val calculatedAlpha = minAlpha + (255f - minAlpha) * alphaFactor

                // Gán màu và độ mờ cho Paint
                dotPaint.color = selectedColor
                dotPaint.alpha = calculatedAlpha.toInt().coerceIn(0, 255)

                // Vẽ hình thoi/nhộng kéo dãn bằng RectF bo góc tuyệt đối (bán kính = dotRadius)
                rectF.set(startX, cy - dotRadius, endX, cy + dotRadius)
                drawRoundRect(rectF, dotRadius, dotRadius, dotPaint)
            }
        }
    }

    fun setCurrentItem(position: Int) {
        currentPosition = position
        positionOffset = 0f
        invalidate()
    }
}