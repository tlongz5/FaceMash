package com.example.facemash.utils.openGL

import android.opengl.GLES11Ext
import android.opengl.GLES32
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.max
import kotlin.math.min

class CameraDrawer(private val imageDrawer: ImageDrawer){
//    private val imageDrawer: ImageDrawer
    private val vertexShaderCode = """
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

    private val fragmentShaderCode = """
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

    private val vertices = floatArrayOf(
        -1f, 1f, 0f,
        -1f, -1f, 0f,
        1f, 1f, 0f,
        1f, -1f, 0f
    )

    private val textureCoords = floatArrayOf(
        0f, 0f,
        0f, 1f,
        1f, 0f,
        1f, 1f
    )

    private val indices = shortArrayOf(
        0, 1, 2,
        1, 2, 3
    )

    private var vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply { put(vertices); position(0) }
    }

    private var indexBuffer: ShortBuffer = ByteBuffer.allocateDirect(indices.size * 2).run {
        order(ByteOrder.nativeOrder())
        asShortBuffer().apply { put(indices); position(0) }
    }

    private var textureBuffer: FloatBuffer = ByteBuffer.allocateDirect(textureCoords.size * 4).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply { put(textureCoords); position(0) }
    }

    private val mvpMatrix = FloatArray(16)
    private val mProgram: Int
    private val uSTMatrixHandle: Int
    private val uMirrorHandle: Int
    private val aPositionHandle: Int
    private val aTextureCoordHandle: Int
    private val uMVPMatrixHandle: Int

    init {
        mProgram = createProgram()
        uSTMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uSTMatrix")
        uMirrorHandle = GLES32.glGetUniformLocation(mProgram, "uMirror")
        aPositionHandle = GLES32.glGetAttribLocation(mProgram, "aPosition")
        aTextureCoordHandle = GLES32.glGetAttribLocation(mProgram, "aTextureCoord")
        uMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix")
        Matrix.setIdentityM(mvpMatrix, 0)
    }

    fun updateMVPMatrix(viewW: Int, viewH: Int, cameraW: Int, cameraH: Int) {
        if (viewW <= 0 || viewH <= 0 || cameraW <= 0 || cameraH <= 0) return
        val imgW = min(cameraW, cameraH).toFloat()
        val imgH = max(cameraW, cameraH).toFloat()
        val viewAspect = viewW.toFloat() / viewH
        val imgAspect = imgW / imgH
        var scaleX = 1.0f
        var scaleY = 1.0f
        if (imgAspect > viewAspect) {
            scaleX = imgAspect / viewAspect
        } else {
            scaleY = viewAspect / imgAspect
        }
        Matrix.setIdentityM(mvpMatrix, 0)
        Matrix.orthoM(mvpMatrix, 0, -1f / scaleX, 1f / scaleX, -1f / scaleY, 1f / scaleY, -1f, 1f)
    }

    private fun createProgram(): Int {
        val vertex = loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode)
        val fragment = loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode)
        return GLES32.glCreateProgram().also {
            GLES32.glAttachShader(it, vertex)
            GLES32.glAttachShader(it, fragment)
            GLES32.glLinkProgram(it)
            GLES32.glDeleteShader(vertex)
            GLES32.glDeleteShader(fragment)
        }
    }

    private fun loadShader(type: Int, code: String): Int {
        return GLES32.glCreateShader(type).also {
            GLES32.glShaderSource(it, code)
            GLES32.glCompileShader(it)
        }
    }

    fun draw(texture: Int, stMatrix: FloatArray, isMirror: Boolean, stickerTextureIdLeft: Int, stickerTextureIdRight: Int, stickerMatrix: FloatArray) {
        GLES32.glUseProgram(mProgram)
        GLES32.glUniform1i(uMirrorHandle, if (isMirror) 1 else 0)
        GLES32.glUniformMatrix4fv(uSTMatrixHandle, 1, false, stMatrix, 0)
        GLES32.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)

        GLES32.glEnableVertexAttribArray(aPositionHandle)
        GLES32.glVertexAttribPointer(aPositionHandle, 3, GLES32.GL_FLOAT, false, 12, vertexBuffer)
        GLES32.glEnableVertexAttribArray(aTextureCoordHandle)
        GLES32.glVertexAttribPointer(aTextureCoordHandle, 2, GLES32.GL_FLOAT, false, 8, textureBuffer)

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0)
        GLES32.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture)
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, 6, GLES32.GL_UNSIGNED_SHORT, indexBuffer)

        GLES32.glDisableVertexAttribArray(aPositionHandle)
        GLES32.glDisableVertexAttribArray(aTextureCoordHandle)

        imageDrawer.draw(stickerTextureIdLeft, stickerTextureIdRight,stickerMatrix)
    }

    fun createCameraTexture(): Int {
        val textures = IntArray(1)
        GLES32.glGenTextures(1, textures, 0)
        GLES32.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])
        GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR)
        GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR)
        GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_EDGE)
        GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_EDGE)
        return textures[0]
    }
}
