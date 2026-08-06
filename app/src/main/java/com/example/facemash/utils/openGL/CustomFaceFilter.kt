package com.example.facemash.utils.openGL

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.Matrix
import com.otaliastudios.cameraview.filter.BaseFilter

class CustomFaceFilter : BaseFilter() {
    override fun getFragmentShader(): String {
        return """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        
        varying vec2 vTextureCoord;
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
            vec2 uv = vTextureCoord;
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
        
                bool isSkinCb = (cb >= 0.30 && cb <= 0.50);
                bool isSkinCr = (cr >= 0.52 && cr <= 0.70);
                bool isNotTooDark = (y >= 0.17);
        
                if (isSkinCb && isSkinCr && isNotTooDark) {
                    vec3 greenYCbCr = ycbcr;
                    greenYCbCr.y = 0.30; 
                    greenYCbCr.z = 0.25; 
                
                    vec3 blendedYCbCr = mix(ycbcr, greenYCbCr, 0.4);
                    gl_FragColor = vec4(ycbcr2rgb(blendedYCbCr), texColor.a);
                    return;
                }
            }
            gl_FragColor = texColor;
        }
    """.trimIndent()
    }

    override fun getVertexShader(): String {
        return """
            attribute vec4 aPosition;
            attribute vec2 aTextureCoord; 
            varying vec2 vTextureCoord;
            uniform mat4 uSTMatrix;
            uniform mat4 uMVPMatrix;
            
            void main() {
                gl_Position = uMVPMatrix * aPosition;
                vec4 tex = uSTMatrix * vec4(aTextureCoord, 0.0, 1.0);
                vTextureCoord = tex.xy;
            }
        """.trimIndent()
    }

    private var hasFace = false

    private var targetX = 0f
    private var targetY = 0f
    private var targetScale = 0f
    private var targetRotate = 0f

    private var smoothedX = 0f
    private var smoothedY = 0f
    private var smoothedScale = 0f
    private var smoothedRotate = 0f

    var stickerTextureIdLeft: Int = -1
    var stickerTextureIdRight: Int = -1

    private var pendingPrincessBitmap: Bitmap? = null
    private var pendingWitchBitmap: Bitmap? = null

    private val projectionMatrix = FloatArray(16)

    private var updateSurface = false

    private val textureTransformMatrix = FloatArray(16)
    private val stickerMatrix = FloatArray(16)

    private lateinit var imageDrawer: ImageDrawer

    fun loadStickerTexture(bitmap: Bitmap): Int {
        return imageDrawer.loadTexture(bitmap)
    }

    override fun onCreate(programHandle: Int) {
        super.onCreate(programHandle)
        imageDrawer = ImageDrawer()
    }

    fun setStickerBitmaps(princess: Bitmap, witch: Bitmap) {
        this.pendingPrincessBitmap = princess
        this.pendingWitchBitmap = witch
    }

    override fun onDraw(timestampUs: Long) {
        super.onDraw(timestampUs)
//        if (stickerTextureIdLeft != -1 && stickerTextureIdRight != -1) {
//            // Logic làm mượt
//            val alpha = 0.2f
//            smoothedX += alpha * (targetX - smoothedX)
//            smoothedY += alpha * (targetY - smoothedY)
//            smoothedScale += alpha * (targetScale - smoothedScale)
//            smoothedRotate += alpha * (targetRotate - smoothedRotate)
//
//            // Tính ma trận (Lưu ý: tọa độ NDC -1..1)
//            Matrix.setIdentityM(stickerMatrix, 0)
//            Matrix.translateM(stickerMatrix, 0, smoothedX, smoothedY + 0.6f, 0f)
//            Matrix.rotateM(stickerMatrix, 0, smoothedRotate, 0f, 0f, 1f)
//            // Bạn có thể lấy viewW/viewH từ cameraView.width/height truyền vào
//            val screenRatio = 1080f / 2400f // Demo, nên truyền từ ngoài vào
//            Matrix.scaleM(
//                stickerMatrix,
//                0,
//                smoothedScale,
//                smoothedScale * screenRatio * (478f / 414f),
//                1f
//            )
//
//            GLES20.glEnable(GLES20.GL_BLEND)
//            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
//
//            imageDrawer.draw(stickerTextureIdLeft, stickerTextureIdRight, stickerMatrix)
//
//            GLES20.glDisable(GLES20.GL_BLEND)
//        }
//
    }

    override fun onPostDraw(timestampUs: Long) {
        super.onPostDraw(timestampUs)

        if (stickerTextureIdLeft == -1 && pendingPrincessBitmap != null) {
            stickerTextureIdLeft = imageDrawer.loadTexture(pendingPrincessBitmap!!)
            pendingPrincessBitmap = null
        }
        if (stickerTextureIdRight == -1 && pendingWitchBitmap != null) {
            stickerTextureIdRight = imageDrawer.loadTexture(pendingWitchBitmap!!)
            pendingWitchBitmap = null
        }
        if (!hasFace) return

        if (stickerTextureIdLeft != -1 && stickerTextureIdRight != -1) {
            val alpha = 0.5f
            smoothedX += alpha * (targetX - smoothedX)
            smoothedY += alpha * (targetY - smoothedY)
            smoothedScale += alpha * (targetScale - smoothedScale)
            smoothedRotate += alpha * (targetRotate - smoothedRotate)

            // Setup ma trận (giả sử tỷ lệ màn hình là 16:9 tạm thời, bạn có thể truyền tỷ lệ thật vào)
            Matrix.setIdentityM(stickerMatrix, 0)
            Matrix.translateM(stickerMatrix, 0, smoothedX, smoothedY + 0.6f, 0f)
            Matrix.rotateM(stickerMatrix, 0, smoothedRotate, 0f, 0f, 1f)
            Matrix.scaleM(stickerMatrix, 0, smoothedScale, smoothedScale * (9f/16f) * (478f/414f), 1f)

            // Bật Blend để sticker có nền trong suốt không bị đen
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

            imageDrawer.draw(stickerTextureIdLeft, stickerTextureIdRight, stickerMatrix)

            GLES20.glDisable(GLES20.GL_BLEND)
        }
    }

    fun updateFaceData(x: Float, y: Float, scale: Float, rotate: Float, faceDetected: Boolean) {
        this.hasFace = faceDetected
        if(faceDetected){
            this.targetX = x
            this.targetY = y
            this.targetScale = scale
            this.targetRotate = rotate
        }
    }

//    fun setCameraResolution(cameraW: Int, cameraH: Int) {
//        if (viewW > 0 && viewH > 0) {
//            cameraDrawer.updateMVPMatrix(viewW, viewH, cameraW, cameraH)
//        }
//    }


}