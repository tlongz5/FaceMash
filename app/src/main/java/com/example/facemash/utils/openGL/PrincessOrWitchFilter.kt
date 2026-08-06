package com.example.facemash.utils.openGL

import android.graphics.Bitmap
import android.graphics.RectF
import android.opengl.GLES20
import android.opengl.GLUtils
import com.otaliastudios.cameraview.filter.BaseFilter
import com.otaliastudios.cameraview.filter.OneParameterFilter
import kotlin.concurrent.Volatile
import kotlin.math.abs
import kotlin.math.cos


class PrincessOrWitchFilter : BaseFilter(), OneParameterFilter {
    private var parameter1 = 0f

    private var viewWidth = 0f
    private var viewHeight = 0f

    class SharedState {
        @Volatile
        var isOverlayVisible: Boolean = false
        val princessRect: FloatArray = floatArrayOf(0f, 0f, 0f, 0f)
        val princessRot: FloatArray = floatArrayOf(0f, 0f, 0f)
        val witchRect: FloatArray = floatArrayOf(0f, 0f, 0f, 0f)
        val witchRot: FloatArray = floatArrayOf(0f, 0f, 0f)

        @Volatile
        var logicWidth: Float = 1080f

        @Volatile
        var logicHeight: Float = 1920f

        @Volatile
        var overlayTexPrincess: Int = 0

        @Volatile
        var overlayTexWitch: Int = 0

        var pendingBmpPrincess: Bitmap? = null
        var pendingBmpWitch: Bitmap? = null
    }

    private var state = SharedState()

    // Vị trí các biến Uniforms trong GLSL
    private var locViewSize = -1
    private var locLogicSize = -1
    private var locShowOverlay = -1
    private var locPrincessTex = -1
    private var locWitchTex = -1
    private var locPrincessRect = -1
    private var locWitchRect = -1
    private var locPrincessRot = -1
    private var locWitchRot = -1

    override fun setParameter1(value: Float) {
        this.parameter1 = value
    }

    override fun getParameter1(): Float {
        return parameter1
    }

    override fun getFragmentShader(): String {
        return FRAGMENT_SHADER
    }

    override fun onCreate(programHandle: Int) {
        super.onCreate(programHandle)
        locViewSize = GLES20.glGetUniformLocation(programHandle, "uViewSize")
        locLogicSize = GLES20.glGetUniformLocation(programHandle, "uLogicSize")
        locShowOverlay = GLES20.glGetUniformLocation(programHandle, "uShowOverlay")

        locPrincessTex = GLES20.glGetUniformLocation(programHandle, "uPrincessTex")
        locWitchTex = GLES20.glGetUniformLocation(programHandle, "uWitchTex")

        locPrincessRect = GLES20.glGetUniformLocation(programHandle, "uPrincessRect")
        locWitchRect = GLES20.glGetUniformLocation(programHandle, "uWitchRect")

        locPrincessRot = GLES20.glGetUniformLocation(programHandle, "uPrincessRot")
        locWitchRot = GLES20.glGetUniformLocation(programHandle, "uWitchRot")
    }

    override fun setSize(width: Int, height: Int) {
        super.setSize(width, height)
        viewWidth = width.toFloat()
        viewHeight = height.toFloat()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (state.overlayTexPrincess != 0) {
            GLES20.glDeleteTextures(1, intArrayOf(state.overlayTexPrincess), 0)
            state.overlayTexPrincess = 0
        }
        if (state.overlayTexWitch != 0) {
            GLES20.glDeleteTextures(1, intArrayOf(state.overlayTexWitch), 0)
            state.overlayTexWitch = 0
        }
    }

    fun setOverlayBitmaps(bmpPrincess: Bitmap?, bmpWitch: Bitmap?) {
        state.pendingBmpPrincess = bmpPrincess
        state.pendingBmpWitch = bmpWitch
    }

    fun updateHeadPosition(
        faceBoundingBox: RectF,
        rotationZ: Float,
        rotationY: Float,
        logicWidth: Float,
        logicHeight: Float
    ) {
        state.logicWidth = logicWidth
        state.logicHeight = logicHeight

        val yawRad = Math.toRadians(rotationY.toDouble())
        val cosYaw = abs(cos(yawRad)).toFloat()

        // Kích thước mũ (thu nhỏ lại một chút để trông vừa vặn hơn)
        val baseSize = faceBoundingBox.width() * 0.85f * cosYaw

        // Tọa độ đã được Activity lật và bù trừ chuẩn xác
        val centerX = faceBoundingBox.centerX()
        val topOfHead = faceBoundingBox.top

        val pivotX = centerX
        val pivotY = topOfHead

        // Góc nghiêng Z
        val angleRad = Math.toRadians(rotationZ.toDouble()).toFloat()

        // Tỷ lệ chuẩn của ảnh (414 width : 478 height)
        val imageAspectRatio = 478f / 414f

        // Princess (Vương miện)
        val princessWidth = baseSize * 0.95f
        val princessHeight = princessWidth * imageAspectRatio
        val targetPrincessX = centerX - princessWidth / 2f
        val targetPrincessY = topOfHead - princessHeight * 0.98f

        // Witch (Mũ phù thủy)
        val witchWidth = baseSize * 0.95f
        val witchHeight = witchWidth * imageAspectRatio
        val targetWitchX = centerX - witchWidth / 2f
        val targetWitchY = topOfHead - witchHeight * 0.98f

        if (!state.isOverlayVisible) {
            // Lần đầu thấy mặt -> Gán ngay vị trí target không cần LERP
            state.princessRect[0] = targetPrincessX
            state.princessRect[1] = targetPrincessY
            state.princessRect[2] = princessWidth
            state.princessRect[3] = princessHeight

            state.princessRot[0] = pivotX
            state.princessRot[1] = pivotY
            state.princessRot[2] = angleRad

            state.witchRect[0] = targetWitchX
            state.witchRect[1] = targetWitchY
            state.witchRect[2] = witchWidth
            state.witchRect[3] = witchHeight

            state.witchRot[0] = pivotX
            state.witchRot[1] = pivotY
            state.witchRot[2] = angleRad
        } else {
            // Đã hiển thị -> Áp dụng LERP (0.35f) để làm mượt chuyển động xoay/di chuyển đầu
            val alpha = 0.35f

            state.princessRect[0] += alpha * (targetPrincessX - state.princessRect[0])
            state.princessRect[1] += alpha * (targetPrincessY - state.princessRect[1])
            state.princessRect[2] += alpha * (princessWidth - state.princessRect[2])
            state.princessRect[3] += alpha * (princessHeight - state.princessRect[3])

            state.princessRot[0] += alpha * (pivotX - state.princessRot[0])
            state.princessRot[1] += alpha * (pivotY - state.princessRot[1])
            state.princessRot[2] += alpha * (angleRad - state.princessRot[2])

            state.witchRect[0] += alpha * (targetWitchX - state.witchRect[0])
            state.witchRect[1] += alpha * (targetWitchY - state.witchRect[1])
            state.witchRect[2] += alpha * (witchWidth - state.witchRect[2])
            state.witchRect[3] += alpha * (witchHeight - state.witchRect[3])

            state.witchRot[0] += alpha * (pivotX - state.witchRot[0])
            state.witchRot[1] += alpha * (pivotY - state.witchRot[1])
            state.witchRot[2] += alpha * (angleRad - state.witchRot[2])
        }

        state.isOverlayVisible = true
    }

    fun hideOverlay() {
        state.isOverlayVisible = false
    }

    override fun onPreDraw(timestampUs: Long, transformMatrix: FloatArray) {
        super.onPreDraw(timestampUs, transformMatrix)

        loadPendingTextures()

        GLES20.glUniform2f(locViewSize, viewWidth, viewHeight)
        GLES20.glUniform2f(locLogicSize, state.logicWidth, state.logicHeight)

        // LUÔN LUÔN gán uniform cho sampler để tránh lỗi xung đột (Conflict) trong OpenGL ES!
        // Mặc định các uniform sampler đều là 0, dẫn tới uPrincessTex (2D), uWitchTex (2D) và sTexture (External)
        // cùng trỏ vào TEXTURE0. Điều này là phạm luật OpenGL ES và khiến GPU từ chối render (màn hình đen)!
        GLES20.glUniform1i(locPrincessTex, 1)
        GLES20.glUniform1i(locWitchTex, 2)

        // LUÔN LUÔN bind texture nếu đã có
        if (state.overlayTexPrincess != 0 && state.overlayTexWitch != 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, state.overlayTexPrincess)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, state.overlayTexWitch)
        }

        if (state.isOverlayVisible && state.overlayTexPrincess != 0 && state.overlayTexWitch != 0) {
            GLES20.glUniform1i(locShowOverlay, 1)

            GLES20.glUniform4fv(locPrincessRect, 1, state.princessRect, 0)
            GLES20.glUniform3fv(locPrincessRot, 1, state.princessRot, 0)

            GLES20.glUniform4fv(locWitchRect, 1, state.witchRect, 0)
            GLES20.glUniform3fv(locWitchRot, 1, state.witchRot, 0)
        } else {
            GLES20.glUniform1i(locShowOverlay, 0)
        }

        // QUAN TRỌNG: Phải trả lại active texture về TEXTURE0 để CameraView vẽ khung hình camera tiếp theo!
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
    }

    private fun loadPendingTextures() {
        if (state.pendingBmpPrincess != null) {
            if (!state.pendingBmpPrincess!!.isRecycled()) {
                if (state.overlayTexPrincess != 0) GLES20.glDeleteTextures(
                    1,
                    intArrayOf(state.overlayTexPrincess),
                    0
                )
                state.overlayTexPrincess = loadTextureFromBitmap(state.pendingBmpPrincess!!)
            }
            state.pendingBmpPrincess = null
        }

        if (state.pendingBmpWitch != null) {
            if (!state.pendingBmpWitch!!.isRecycled()) {
                if (state.overlayTexWitch != 0) GLES20.glDeleteTextures(
                    1,
                    intArrayOf(state.overlayTexWitch),
                    0
                )
                state.overlayTexWitch = loadTextureFromBitmap(state.pendingBmpWitch!!)
            }
            state.pendingBmpWitch = null
        }
    }

    private fun loadTextureFromBitmap(bitmap: Bitmap): Int {
        // Dùng TEXTURE1 tạm thời để không ghi đè lên TEXTURE0 (chứa camera OES)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        val tex = IntArray(1)
        GLES20.glGenTextures(1, tex, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0) // Unbind sau khi load xong
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0) // Trả lại TEXTURE0
        bitmap.recycle()
        return tex[0]
    }

    override fun onCopy(): BaseFilter {
        val copy = super.onCopy() as PrincessOrWitchFilter
        copy.state = this.state
        return copy
    }

    companion object {
        // Shader đã được dọn dẹp, giữ nguyên vTextureCoord của CameraView
        private val FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "\n" +
                "varying vec2 vTextureCoord;\n" +
                "uniform samplerExternalOES sTexture;\n" +
                "\n" +
                "uniform vec2 uViewSize;\n" +
                "uniform vec2 uLogicSize;\n" +
                "uniform int uShowOverlay;\n" +
                "\n" +
                "uniform sampler2D uPrincessTex;\n" +
                "uniform sampler2D uWitchTex;\n" +
                "\n" +
                "// rect = (left, top, width, height)\n" +
                "uniform vec4 uPrincessRect;\n" +
                "uniform vec4 uWitchRect;\n" +
                "\n" +
                "// rot = (pivotX, pivotY, angleRad)\n" +
                "uniform vec3 uPrincessRot;\n" +
                "uniform vec3 uWitchRot;\n" +
                "\n" +
                "vec3 rgb2ycbcr(vec3 rgb) {\n" +
                "    float y  =  0.2990 * rgb.r + 0.5870 * rgb.g + 0.1140 * rgb.b;\n" +
                "    float cb = -0.1687 * rgb.r - 0.3313 * rgb.g + 0.5000 * rgb.b + 0.5;\n" +
                "    float cr =  0.5000 * rgb.r - 0.4187 * rgb.g - 0.0813 * rgb.b + 0.5;\n" +
                "    return vec3(y, cb, cr);\n" +
                "}\n" +
                "\n" +
                "vec3 ycbcr2rgb(vec3 ycbcr) {\n" +
                "    float y  = ycbcr.x;\n" +
                "    float cb = ycbcr.y - 0.5;\n" +
                "    float cr = ycbcr.z - 0.5;\n" +
                "\n" +
                "    float r = y + 1.4020 * cr;\n" +
                "    float g = y - 0.3441 * cb - 0.7141 * cr;\n" +
                "    float b = y + 1.7720 * cb;\n" +
                "\n" +
                "    return clamp(vec3(r, g, b), 0.0, 1.0);\n" +
                "}\n" +
                "\n" +
                "vec2 getTexCoord(vec2 pixelPos, vec4 rect, vec3 rot) {\n" +
                "    float pivotX = rot.x;\n" +
                "    float pivotY = rot.y;\n" +
                "    float angle = rot.z;\n" +
                "\n" +
                "    float dx = pixelPos.x - pivotX;\n" +
                "    float dy = pixelPos.y - pivotY;\n" +
                "\n" +
                "    float cosA = cos(-angle);\n" +
                "    float sinA = sin(-angle);\n" +
                "    float rx = dx * cosA - dy * sinA;\n" +
                "    float ry = dx * sinA + dy * cosA;\n" +
                "\n" +
                "    float x_unrot = rx + pivotX;\n" +
                "    float y_unrot = ry + pivotY;\n" +
                "\n" +
                "    float u = (x_unrot - rect.x) / rect.z;\n" +
                "    float v = (y_unrot - rect.y) / rect.w;\n" +
                "\n" +
                "    return vec2(u, v);\n" +
                "}\n" +
                "\n" +
                "void main() {\n" +
                "    // Dùng trực tiếp tọa độ CameraView truyền vào, không tự lật (flip) nữa\n" +
                "    vec2 uv = vTextureCoord;\n" +
                "    vec4 texColor = texture2D(sTexture, uv);\n" +
                "\n" +
                "    // Áp dụng filter xanh da (uv.y > 0.5 tương ứng nửa bên phải màn hình do CameraView xoay 90 độ)\n" +
                "    if (uv.y <= 0.5) {\n" +
                "        vec3 rgb = texColor.rgb;\n" +
                "        vec3 ycbcr = rgb2ycbcr(rgb);\n" +
                "\n" +
                "        float y  = ycbcr.x; \n" +
                "        float cb = ycbcr.y; \n" +
                "        float cr = ycbcr.z; \n" +
                "\n" +
                "        bool isSkinCb = (cb >= 0.30 && cb <= 0.48);\n" +
                "        bool isSkinCr = (cr >= 0.52 && cr <= 0.70);\n" +
                "        bool isNotTooDark = (y >= 0.17);\n" +
                "\n" +
                "        if (isSkinCb && isSkinCr && isNotTooDark) {\n" +
                "            vec3 greenYCbCr = ycbcr;\n" +
                "            greenYCbCr.y = 0.30; \n" +
                "            greenYCbCr.z = 0.25; \n" +
                "        \n" +
                "            vec3 blendedYCbCr = mix(ycbcr, greenYCbCr, 0.3);\n" +
                "            texColor = vec4(ycbcr2rgb(blendedYCbCr), texColor.a);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    vec4 finalColor = texColor;\n" +
                "\n" +
                "    // Vẽ Overlays\n" +
                "    if (uShowOverlay == 1) {\n" +
                "        vec2 androidXY = vec2(gl_FragCoord.x, uViewSize.y - gl_FragCoord.y);\n" +
                "        vec2 scale = uViewSize / uLogicSize;\n" +
                "        vec2 logicXY = androidXY / scale;\n" +
                "\n" +
                "        if (logicXY.x < uLogicSize.x / 2.0) { \n" +
                "            vec2 uvP = getTexCoord(logicXY, uPrincessRect, uPrincessRot);\n" +
                "            if (uvP.x >= 0.0 && uvP.x <= 1.0 && uvP.y >= 0.0 && uvP.y <= 1.0) {\n" +
                "                vec4 pColor = texture2D(uPrincessTex, uvP);\n" +
                "                finalColor = vec4(mix(finalColor.rgb, pColor.rgb, pColor.a), finalColor.a);\n" +
                "            }\n" +
                "        } else { \n" +
                "            vec2 uvW = getTexCoord(logicXY, uWitchRect, uWitchRot);\n" +
                "            if (uvW.x >= 0.0 && uvW.x <= 1.0 && uvW.y >= 0.0 && uvW.y <= 1.0) {\n" +
                "                vec4 wColor = texture2D(uWitchTex, uvW);\n" +
                "                finalColor = vec4(mix(finalColor.rgb, wColor.rgb, wColor.a), finalColor.a);\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    gl_FragColor = finalColor;\n" +
                "}\n"
    }
}