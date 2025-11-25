package org.game.invaders

import org.joml.Matrix4f
import org.lwjgl.opengl.GL30
import kotlin.math.roundToInt

class MissileRenderer(
    private val shader: MissilelShader,
    private var windowWidth: Int,
    private var windowHeight: Int
) {
    private data class Quad(val vao: Int, val vbo: Int)

    private lateinit var quad: Quad

    var ballSize = (windowHeight * Constants.BALL_HEIGHT_RATIO).roundToInt()
        private set

    init {
        updateWindowSize(windowWidth, windowHeight)
    }



    fun cleanup() {
        GL30.glDeleteVertexArrays(quad.vao)
        GL30.glDeleteBuffers(quad.vbo)
        shader.cleanup()
    }

    fun updateWindowSize(w: Int, h: Int) {
        shader.rebuild()
        windowWidth = w
        windowHeight = h
        ballSize = (windowHeight * Constants.BALL_HEIGHT_RATIO).roundToInt()
        buildGeometry()
    }

    private fun buildGeometry() {
        val s = ballSize.toFloat()
        val vertices = floatArrayOf(
            0f, 0f,
            s, 0f,
            s, s,
            s, s,
            0f, s,
            0f, 0f
        )

        val vao = GL30.glGenVertexArrays()
        val vbo = GL30.glGenBuffers()

        GL30.glBindVertexArray(vao)
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo)
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices, GL30.GL_STATIC_DRAW)

        GL30.glEnableVertexAttribArray(0)
        GL30.glVertexAttribPointer(0, 2, GL30.GL_FLOAT, false, 2 * Float.SIZE_BYTES, 0)

        GL30.glBindVertexArray(0)

        quad = Quad(vao, vbo)
    }

    fun render(ballX: Float, ballY: Float) {
        shader.use()
        val diameter = ballSize.toFloat()

        val proj = Matrix4f().ortho2D(0f, windowWidth.toFloat(), 0f, windowHeight.toFloat())
            .get(FloatArray(16))

        shader.setUniformMat4("uProjection", proj)
        shader.setUniformVec2("uBallPos", ballX, ballY)
        shader.setUniformVec2("uBallSize", diameter, diameter)
        shader.setUniformVec3("uColor", Constants.BALL_COLOR_R, Constants.BALL_COLOR_G, Constants.BALL_COLOR_B)

        GL30.glBindVertexArray(quad.vao)
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6)
        GL30.glBindVertexArray(0)
    }
}