package com.example.facemash.utils.openGL

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class ImageDrawer {
    private val vs2D = """
        attribute vec4 aPosition;
        attribute vec2 aTextureCoord;
        varying vec2 vTextureCoord;
        varying vec4 vPosition;
        uniform mat4 uMVPMatrix;
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vPosition = gl_Position;
            vTextureCoord = aTextureCoord;
        }
    """.trimIndent()

    private val fs2D = """
        precision mediump float;
        varying vec2 vTextureCoord; 
        uniform sampler2D uTextureLeft;
        uniform sampler2D uTextureRight;
        
        varying vec4 vPosition;
        
        void main() {
            if (vPosition.x < 0.0) {
                gl_FragColor = texture2D(uTextureLeft, vTextureCoord);
            } else {
                gl_FragColor = texture2D(uTextureRight, vTextureCoord);
            }
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

    private val mProgram: Int
    private val aPositionHandle: Int
    private val aTextureCoordHandle: Int
    private val uMVPMatrixHandle: Int

    init {
        mProgram = createProgram()
        aPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
        aTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord")
        uMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
    }

    fun loadTexture(bitmap: Bitmap): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return textures[0]
    }

    private fun createProgram(): Int {
        val vertex = loadShader(GLES20.GL_VERTEX_SHADER, vs2D)
        val fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, fs2D)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertex)
        GLES20.glAttachShader(program, fragment)
        GLES20.glLinkProgram(program)
        GLES20.glDeleteShader(vertex)
        GLES20.glDeleteShader(fragment)
        return program
    }

    private fun loadShader(type: Int, code: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)
        return shader
    }

    fun draw(textureIdLeft: Int, textureIdRight: Int, mvpMatrix: FloatArray) {
        GLES20.glUseProgram(mProgram)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)

        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)
        GLES20.glEnableVertexAttribArray(aTextureCoordHandle)
        GLES20.glVertexAttribPointer(aTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 8, textureBuffer)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIdLeft)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgram, "uTextureLeft"), 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIdRight)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgram, "uTextureRight"), 1)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glDisableVertexAttribArray(aTextureCoordHandle)

        // Reset state
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glUseProgram(0)
    }
}
