package com.example.facemash.utils.openGL

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyCameraRenderer : GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private var viewW = 0
    private var viewH = 0

    private var targetX = 0f
    private var targetY = 0f
    private var targetScale = 0f
    private var targetRotate = 0f

    private var smoothedX = 0f
    private var smoothedY = 0f
    private var smoothedScale = 0f
    private var smoothedRotate = 0f

    private var surfaceTexture: SurfaceTexture? = null
    private var cameraTextureId: Int = -1
    var stickerTextureIdLeft: Int = -1
    var stickerTextureIdRight: Int = -1

    private val projectionMatrix = FloatArray(16)

    private var updateSurface = false
    var surfaceReadyCallback: ((SurfaceTexture) -> Unit)? = null

    private val textureTransformMatrix = FloatArray(16)
    private val stickerMatrix = FloatArray(16)

    private lateinit var cameraDrawer: CameraDrawer
    private lateinit var imageDrawer: ImageDrawer

    fun loadStickerTexture(bitmap: Bitmap): Int {
        return imageDrawer.loadTexture(bitmap)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT)

        val st = surfaceTexture ?: return
        
        synchronized(this) {
            if (updateSurface) {
                st.updateTexImage()
                st.getTransformMatrix(textureTransformMatrix)
                updateSurface = false
            }
        }



        // 2. Vẽ Sticker (Nếu có texture)
        if (stickerTextureIdLeft != -1 && stickerTextureIdRight != -1) {
            val screenRatio = viewW.toFloat()/viewH.toFloat()

            val alpha = 0.5f
            smoothedX += alpha * (targetX - smoothedX)
            smoothedY += alpha * (targetY - smoothedY)
            smoothedScale += alpha * (targetScale - smoothedScale)
            smoothedRotate += alpha * (targetRotate - smoothedRotate)

            Matrix.setIdentityM(stickerMatrix, 0)
            // Lưu ý: targetX/Y phải là tọa độ GL (-1 đến 1)
            Matrix.translateM(stickerMatrix, 0, smoothedX, smoothedY + 0.6f, 0f)
            Matrix.rotateM(stickerMatrix, 0, smoothedRotate, 0f, 0f, 1f)
            Matrix.scaleM(stickerMatrix, 0, smoothedScale, smoothedScale * screenRatio * ( 478.toFloat()/414), 1f)

//            Matrix.scaleM(stickerMatrix, 0, smoothedScale, smoothedScale / screenRatio, 1f)

            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

            cameraDrawer.draw(cameraTextureId, textureTransformMatrix, isMirror = true,stickerTextureIdLeft, stickerTextureIdRight, stickerMatrix)

//            imageDrawer.draw()
            
            GLES20.glDisable(GLES20.GL_BLEND)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES32.glViewport(0, 0, width, height)
        this.viewW = width
        this.viewH = height


//        // Tính toán ma trận chiếu để sửa méo cho Sticker
//        val ratio = width.toFloat() / height.toFloat()
//        Matrix.setIdentityM(projectionMatrix, 0)
//        // Hệ tọa độ bây giờ: X từ -ratio đến ratio, Y từ -1 đến 1.
//        // Giúp 1 đơn vị X = 1 đơn vị Y (không bị méo)
//        Matrix.orthoM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, -1f, 1f)
//
//        // Đừng quên gọi update cho camera background như cũ
//        cameraDrawer.updateMVPMatrix(width, height, cameraW, cameraH)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        imageDrawer = ImageDrawer()
        cameraDrawer = CameraDrawer(imageDrawer)
        cameraTextureId = cameraDrawer.createCameraTexture()
        surfaceTexture = SurfaceTexture(cameraTextureId)
        surfaceTexture?.setOnFrameAvailableListener(this)

        surfaceReadyCallback?.invoke(surfaceTexture!!)
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        synchronized(this) {
            updateSurface = true
        }
    }

    fun updateFaceData(x: Float, y: Float, scale: Float, rotate: Float) {
        this.targetX = x
        this.targetY = y
        this.targetScale = scale
        this.targetRotate = rotate
    }

    fun setCameraResolution(cameraW: Int, cameraH: Int) {
        if (viewW > 0 && viewH > 0) {
            cameraDrawer.updateMVPMatrix(viewW, viewH, cameraW, cameraH)
        }
    }
}
