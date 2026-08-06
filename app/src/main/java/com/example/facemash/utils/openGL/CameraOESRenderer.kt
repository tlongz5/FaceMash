package com.example.facemash.utils.openGL

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.opengl.GLES10.glViewport
import android.opengl.GLES11Ext
import android.opengl.GLES32
import android.opengl.GLSurfaceView.Renderer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraOESRenderer(private val context: Context) : Renderer {
    // Dữ liệu đỉnh
    var textureId = 0
    var surfaceTexture: SurfaceTexture? = null
    var surfaceReadyCallback: ((SurfaceTexture) -> Unit)? = null

    private var mirrorX = 1 // 1 = mirror X (selfie), 0 = no mirror

    // GL program & uniforms
    private var program = 0
    private var uTexMatrix = 0
    private var uMirror = 0

    // Transform matrix from SurfaceTexture
    private val texMatrix = FloatArray(16)

    private var viewWidth = 0
    private var viewHeight = 0
    private var cameraWidth = 0
    private var cameraHeight = 0

    private lateinit var vertexBuffer: FloatBuffer

    // overlay ảnh trên đầu
    private var overlayTexPrincess = 0
    private var overlayTexWitch = 0
    private var pendingBmpPrincess: Bitmap? = null
    private var pendingBmpWitch: Bitmap? = null
    @Volatile private var isOverlayVisible = false

    private lateinit var overlayVertexBufferPrincess: FloatBuffer
    private lateinit var overlayVertexBufferWitch: FloatBuffer
    private var program2D = 0

    private val overlayTexCoords = floatArrayOf(
        0f, 0f, 0f, 1f,
        1f, 0f, 1f, 1f
    )

    private lateinit var overlayTexBuffer: FloatBuffer

    private val defaultCoords = floatArrayOf(
        -1f, 1f, 0f, 0f,
        -1f, -1f, 0f, 1f,
        1f, 1f, 1f, 0f,
        1f, -1f, 1f, 1f
    )

    private val vertexShaderCode = """
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        uniform mat4 uTexMatrix;

        void main() {
            gl_Position = aPosition;
            vec4 tex = uTexMatrix * vec4(aTexCoord, 0.0, 1.0);
            vTexCoord = tex.xy;
        }
    """.trimIndent()

    private val vs2DCode = """
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        void main() {
            gl_Position = aPosition;
            vTexCoord = aTexCoord;
        }
    """.trimIndent()

    private val fs2DCode = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D uTexture;
        void main() {
            gl_FragColor = texture2D(uTexture, vTexCoord);
        }
    """.trimIndent()

    /*
     * 1. CHUYỂN ĐỔI KHÔNG GIAN MÀU (Color Space Conversion - RGB to HSV):
     *    - Chuyển màu điểm ảnh từ RGB sang HSV (Hue - Saturation - Value) bằng hàm rgb2hsv().
     *    - HSV giúp tách biệt bản chất màu sắc (Hue) khỏi độ sáng (Value) và độ đậm nhạt (Saturation),
     *      giúp nhận diện màu da chính xác bất kể điều kiện ánh sáng môi trường.
     *
     * 2. LỌC VÙNG MÀU DA NGƯỜI (Skin Color Masking / Thresholding):
     *    Kiểm tra điểm ảnh có thỏa mãn 3 điều kiện màu da đặc trưng:
     *    - Hue (Tông màu): H ∈ [0.0, 0.12] (Đỏ sang Cam/Vàng) HOẶC H ∈ [0.95, 1.0] (Đỏ thẫm/Hồng).
     *    - Saturation (Độ bão hòa): S ∈ [0.15, 0.75] (Loại bỏ màu quá xám nhạt hoặc quá rực rỡ).
     *    - Value (Độ sáng): V >= 0.20 (Loại bỏ vùng bóng tối đen nặng).
     *
     * 3. BIẾN ĐỔI MÀU BẰNG HSV (Color Manipulation & Reconstruction):
     *    - Nếu điểm ảnh thỏa mãn điều kiện màu da:
     *      + Thay đổi giá trị Tông màu (Hue): H_mới = 0.333 (tương ứng 120° - Màu Xanh Lá Cây).
     *      + Tăng nhẹ độ rực rỡ (Saturation) thêm 20% để màu xanh hiển thị rõ nét hơn.
     *      + Giữ nguyên độ sáng V gốc để không làm mất chi tiết khối/bóng trên mặt.
     *    - Chuyển đổi bộ màu (H_mới, S_mới, V_gốc) ngược lại về RGB (hsv2rgb) để xuất ra màn hình.
     */
    private val fragmentShaderCode = """
    #extension GL_OES_EGL_image_external : require
    #extension GL_OES_EGL_image_external_essl3 : require
    precision mediump float;
    
    varying vec2 vTexCoord;
    uniform samplerExternalOES sTexture;
    uniform int uMirror;
    
    vec3 rgb2ycbcr(vec3 rgb) {
        float y  =  0.2990 * rgb.r + 0.5870 * rgb.g + 0.1140 * rgb.b;
        float cb = -0.1687 * rgb.r - 0.3313 * rgb.g + 0.5000 * rgb.b + 0.5;
        float cr =  0.5000 * rgb.r - 0.4187 * rgb.g - 0.0813 * rgb.b + 0.5;
        return vec3(y, cb, cr);
    }
    
    vec3 ycbcr2rgb(vec3 ycbcr) {
        float y  = ycbcr.x;
        float cb = ycbcr.y - 0.5;
        float cr = ycbcr.z - 0.5;
    
        float r = y + 1.4020 * cr;
        float g = y - 0.3441 * cb - 0.7141 * cr;
        float b = y + 1.7720 * cb;
    
        return clamp(vec3(r, g, b), 0.0, 1.0);
    }
    
    void main() {
        vec2 uv = vTexCoord;
        if (uMirror == 1) {
            uv.x = 1.0 - uv.x;
        }
    
        vec4 texColor = texture2D(sTexture, uv);
    
        if (uv.y <= 0.5) {
            vec3 rgb = texColor.rgb;
            vec3 ycbcr = rgb2ycbcr(rgb);
    
            float y  = ycbcr.x; 
            float cb = ycbcr.y; 
            float cr = ycbcr.z; 
    
            bool isSkinCb = (cb >= 0.30 && cb <= 0.48);
            bool isSkinCr = (cr >= 0.52 && cr <= 0.70);
            bool isNotTooDark = (y >= 0.17); // Loại bỏ vùng bóng tối đậm
    
            if (isSkinCb && isSkinCr && isNotTooDark) {
                vec3 greenYCbCr = ycbcr;
                greenYCbCr.y = 0.30; 
                greenYCbCr.z = 0.25; 
            
                vec3 blendedYCbCr = mix(ycbcr, greenYCbCr, 0.3);
            
                gl_FragColor = vec4(ycbcr2rgb(blendedYCbCr), texColor.a);
                return;
            }
        }
    
        gl_FragColor = texColor;
    }
""".trimIndent()

    fun setCameraSize(width: Int, height: Int) {
        if (cameraWidth != width || cameraHeight != height) {
            cameraWidth = width
            cameraHeight = height
            updateVertexCoordinates()
        }
    }

    fun setOverlayBitmaps(bmpPrincess: Bitmap, bmpWitch: Bitmap) {
        pendingBmpPrincess = bmpPrincess
        pendingBmpWitch = bmpWitch
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        textureId = generateOESTexture()
        surfaceTexture = SurfaceTexture(textureId)

        surfaceReadyCallback?.invoke(surfaceTexture!!)

        program = compileShader(vertexShaderCode, fragmentShaderCode)
        uTexMatrix = GLES32.glGetUniformLocation(program, "uTexMatrix")
        uMirror = GLES32.glGetUniformLocation(program, "uMirror")

        GLES32.glEnable(GLES32.GL_BLEND)
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA)
        program2D = compileShader(vs2DCode, fs2DCode)

        vertexBuffer = ByteBuffer.allocateDirect(defaultCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply { put(defaultCoords); position(0) }

        overlayVertexBufferPrincess = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        overlayVertexBufferWitch = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        overlayTexBuffer = ByteBuffer.allocateDirect(8 * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(overlayTexCoords); position(0)
            }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        viewWidth = width
        viewHeight = height
        updateVertexCoordinates()
    }

    private fun updateVertexCoordinates() {
        if (viewWidth <= 0 || viewHeight <= 0 || cameraWidth <= 0 || cameraHeight <= 0) return

        // Do màn hình dọc, kích thước camera thực tế đổi chiều W và H
        val imageW = Math.min(cameraWidth, cameraHeight).toFloat()
        val imageH = Math.max(cameraWidth, cameraHeight).toFloat()

        val viewAspect = viewWidth.toFloat() / viewHeight.toFloat()
        val imageAspect = imageW / imageH

        var scaleX = 1.0f
        var scaleY = 1.0f

        if (imageAspect > viewAspect) {
            // Ảnh rộng hơn View -> Phóng to chiều ngang để phủ kín View
            scaleX = imageAspect / viewAspect
        } else {
            // Ảnh dài hơn View -> Phóng to chiều dọc
            scaleY = viewAspect / imageAspect
        }

        // Fix coords with scale
        val coords = floatArrayOf(
            -1f * scaleX, 1f * scaleY, 0f, 0f,
            -1f * scaleX, -1f * scaleY, 0f, 1f,
            1f * scaleX, 1f * scaleY, 1f, 0f,
            1f * scaleX, -1f * scaleY, 1f, 1f
        )

        vertexBuffer.clear()
        vertexBuffer.put(coords)
        vertexBuffer.position(0)
    }

    fun updateHeadPosition(faceBoundingBox: RectF, rotationZ: Float, rotationY: Float) {
        if (viewWidth <= 0 || viewHeight <= 0) return

        val yawRad = Math.toRadians(rotationY.toDouble())
        val cosYaw = Math.abs(Math.cos(yawRad)).toFloat()

        val baseSize = faceBoundingBox.width() * 1.5f * cosYaw
        val centerX = faceBoundingBox.centerX()
        val bottom = faceBoundingBox.top

        val pivotX = centerX
        val pivotY = bottom

        val angleRad = Math.toRadians((rotationZ).toDouble())
        val cosA = Math.cos(angleRad).toFloat()
        val sinA = Math.sin(angleRad).toFloat()

        fun rotateAndNDC(left: Float, top: Float, right: Float, bottomPos: Float): FloatArray {
            fun rotate(x: Float, y: Float): Pair<Float, Float> {
                val dx = x - pivotX
                val dy = y - pivotY
                val rx = dx * cosA - dy * sinA
                val ry = dx * sinA + dy * cosA
                return Pair(pivotX + rx, pivotY + ry)
            }
            val (tlX, tlY) = rotate(left, top)
            val (blX, blY) = rotate(left, bottomPos)
            val (trX, trY) = rotate(right, top)
            val (brX, brY) = rotate(right, bottomPos)

            fun toNDC(x: Float, y: Float): Pair<Float, Float> {
                return Pair((x / viewWidth) * 2f - 1f, 1f - (y / viewHeight) * 2f)
            }
            val (ndcTlX, ndcTlY) = toNDC(tlX, tlY)
            val (ndcBlX, ndcBlY) = toNDC(blX, blY)
            val (ndcTrX, ndcTrY) = toNDC(trX, trY)
            val (ndcBrX, ndcBrY) = toNDC(brX, brY)

            return floatArrayOf(
                ndcTlX, ndcTlY,
                ndcBlX, ndcBlY,
                ndcTrX, ndcTrY,
                ndcBrX, ndcBrY
            )
        }

        // Princess
        val princessHeight = baseSize
        val princessWidth = baseSize
        overlayVertexBufferPrincess.clear()
        overlayVertexBufferPrincess.put(rotateAndNDC(
            centerX - princessWidth / 2f, bottom - princessHeight * 0.7f,
            centerX + princessWidth / 2f, bottom
        )).position(0)

        // Witch
        val witchHeight = baseSize * 2f
        val witchWidth = baseSize
        overlayVertexBufferWitch.clear()
        overlayVertexBufferWitch.put(rotateAndNDC(
            centerX - witchWidth / 2f, bottom - witchHeight,
            centerX + witchWidth / 2f, bottom
        )).position(0)

        isOverlayVisible = true
    }

    fun hideOverlay() {
        isOverlayVisible = false
    }

    override fun onDrawFrame(p0: GL10?) {
        val st = surfaceTexture ?: return
        st.updateTexImage()
        st.getTransformMatrix(texMatrix)

        GLES32.glUseProgram(program)
        GLES32.glUniformMatrix4fv(uTexMatrix, 1, false, texMatrix, 0)
        GLES32.glUniform1i(uMirror, mirrorX)

        drawOESTexture(textureId)

        pendingBmpPrincess?.let {
            if (!it.isRecycled) {
                if (overlayTexPrincess != 0) {
                    GLES32.glDeleteTextures(1, intArrayOf(overlayTexPrincess), 0)
                }
                overlayTexPrincess = loadTextureFromBitmap(it)
            }
            pendingBmpPrincess = null
        }
        pendingBmpWitch?.let {
            if (!it.isRecycled) {
                if (overlayTexWitch != 0) {
                    GLES32.glDeleteTextures(1, intArrayOf(overlayTexWitch), 0)
                }
                overlayTexWitch = loadTextureFromBitmap(it)
            }
            pendingBmpWitch = null
        }
        if (isOverlayVisible && overlayTexPrincess != 0 && overlayTexWitch != 0) {
            drawOverlayImage()
        }
    }

    private fun drawOESTexture(textureId: Int) {
        val aPos = GLES32.glGetAttribLocation(program, "aPosition")
        val aTex = GLES32.glGetAttribLocation(program, "aTexCoord")

        vertexBuffer.position(0)
        GLES32.glEnableVertexAttribArray(aPos)
        GLES32.glVertexAttribPointer(aPos, 2, GLES32.GL_FLOAT, false, 16, vertexBuffer)

        vertexBuffer.position(2)
        GLES32.glEnableVertexAttribArray(aTex)
        GLES32.glVertexAttribPointer(aTex, 2, GLES32.GL_FLOAT, false, 16, vertexBuffer)

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0)
        GLES32.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)

        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_STRIP, 0, 4)

        GLES32.glDisableVertexAttribArray(aPos)
        GLES32.glDisableVertexAttribArray(aTex)
    }


    private fun loadTextureFromBitmap(bitmap: Bitmap): Int {
        val tex = IntArray(1)
        GLES32.glGenTextures(1, tex, 0)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, tex[0])
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR)
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR)
        android.opengl.GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
        return tex[0]
    }

    private fun drawOverlayImage() {
        GLES32.glUseProgram(program2D)
        val aPos = GLES32.glGetAttribLocation(program2D, "aPosition")
        val aTex = GLES32.glGetAttribLocation(program2D, "aTexCoord")
        val uTex = GLES32.glGetUniformLocation(program2D, "uTexture")

        GLES32.glEnableVertexAttribArray(aTex)
        overlayTexBuffer.position(0)
        GLES32.glVertexAttribPointer(aTex, 2, GLES32.GL_FLOAT, false, 0, overlayTexBuffer)

        GLES32.glEnableVertexAttribArray(aPos)

        // Bật glScissor để cắt màn hình
        GLES32.glEnable(GLES32.GL_SCISSOR_TEST)

        // --- DRAW PRINCESS (LEFT HALF) ---
        GLES32.glScissor(0, 0, viewWidth / 2, viewHeight)

        overlayVertexBufferPrincess.position(0)
        GLES32.glVertexAttribPointer(aPos, 2, GLES32.GL_FLOAT, false, 0, overlayVertexBufferPrincess)

        GLES32.glActiveTexture(GLES32.GL_TEXTURE1)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, overlayTexPrincess)
        GLES32.glUniform1i(uTex, 1)

        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_STRIP, 0, 4)

        // --- DRAW WITCH (RIGHT HALF) ---
        GLES32.glScissor(viewWidth / 2, 0, viewWidth - (viewWidth / 2), viewHeight)

        overlayVertexBufferWitch.position(0)
        GLES32.glVertexAttribPointer(aPos, 2, GLES32.GL_FLOAT, false, 0, overlayVertexBufferWitch)

        GLES32.glActiveTexture(GLES32.GL_TEXTURE1)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, overlayTexWitch)
        GLES32.glUniform1i(uTex, 1)

        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_STRIP, 0, 4)

        // Tắt glScissor
        GLES32.glDisable(GLES32.GL_SCISSOR_TEST)
        GLES32.glDisableVertexAttribArray(aPos)
        GLES32.glDisableVertexAttribArray(aTex)
    }

    private fun compileShader(vertexShaderCode: String, fragmentShaderCode: String): Int {
        fun build(type: Int, src: String): Int {
            val shader = GLES32.glCreateShader(type)
            GLES32.glShaderSource(shader, src)
            GLES32.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES32.glGetShaderiv(shader, GLES32.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                val log = GLES32.glGetShaderInfoLog(shader)
                GLES32.glDeleteShader(shader)
                throw RuntimeException("Shader compile error: $log")
            }
            return shader
        }

        val vs = build(GLES32.GL_VERTEX_SHADER, vertexShaderCode)
        val fs = build(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode)

        val program = GLES32.glCreateProgram()
        GLES32.glAttachShader(program, vs)
        GLES32.glAttachShader(program, fs)
        GLES32.glLinkProgram(program)

        val linked = IntArray(1)
        GLES32.glGetProgramiv(program, GLES32.GL_LINK_STATUS, linked, 0)
        if (linked[0] == 0) {
            val log = GLES32.glGetProgramInfoLog(program)
            GLES32.glDeleteProgram(program)
            throw RuntimeException("Program link error: $log")
        }
        GLES32.glDeleteShader(vs)
        GLES32.glDeleteShader(fs)
        return program
    }

    private fun generateOESTexture(): Int {
        val tex = IntArray(1)
        GLES32.glGenTextures(1, tex, 0)
        GLES32.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0])
        GLES32.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES32.GL_TEXTURE_MIN_FILTER,
            GLES32.GL_LINEAR
        )
        GLES32.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES32.GL_TEXTURE_MAG_FILTER,
            GLES32.GL_LINEAR
        )
        GLES32.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES32.GL_TEXTURE_WRAP_S,
            GLES32.GL_CLAMP_TO_EDGE
        )
        GLES32.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES32.GL_TEXTURE_WRAP_T,
            GLES32.GL_CLAMP_TO_EDGE
        )
        return tex[0]
    }
}