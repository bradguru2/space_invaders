package org.game.invaders

import org.joml.Matrix4f
import org.lwjgl.opengl.GL30

class PlayerRenderer(private val shader: PlayerShader, private var windowWidth: Int, private var windowHeight: Int) {
    private data class Quad(val vao: Int, val vbo: Int, var vertexCount: Int)
    private lateinit var paddle: Quad

    var paddleHeight = windowHeight * Constants.PADDLE_HEIGHT_RATIO
        private set
    var paddleState = Constants.NORMAL_PADDLE_RATIO
        private set
    private var playerWidth = windowWidth * paddleState

    init {
        buildGeometry() // Initial paddle position at roughly center
    }

    fun cleanup() {
        listOf(paddle).forEach { quad ->
            GL30.glDeleteVertexArrays(quad.vao)
            GL30.glDeleteBuffers(quad.vbo)
        }
        shader.cleanup()
    }

    fun updatePaddleState(newPaddleState: Float) {
        paddleState = newPaddleState
        playerWidth = windowWidth * newPaddleState
        shader.rebuild()
        buildGeometry()
    }

    fun playerSize(): Float {
        return playerWidth
    }

    fun updateWindowSize(w: Int, h: Int, s: Float) {
        shader.rebuild()
        windowWidth = w
        windowHeight = h
        paddleHeight = windowHeight * Constants.PADDLE_HEIGHT_RATIO
        playerWidth = windowWidth * s
        buildGeometry()
    }

    fun render(paddleX: Int) {
        shader.use()
        // Projection for Window Coordinates
        val proj =
            Matrix4f().ortho2D(0f, windowWidth.toFloat(), 0f, windowHeight.toFloat()).get(FloatArray(16))
        shader.setUniformVec2("uPaddlePos", paddleX.toFloat(), Constants.BOTTOM_FRAME_RATIO * windowHeight)
        shader.setUniformMat4("uProjection", proj)
        shader.setUniformVec3("uColor", Constants.PADDLE_COLOR_R, Constants.PADDLE_COLOR_G, Constants.PADDLE_COLOR_B)

        // Draw paddle
        GL30.glBindVertexArray(paddle.vao)
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6)

        GL30.glBindVertexArray(0)
    }

    private fun buildQuad(w: Float, h: Float): Quad {
        val vertices = floatArrayOf(
            // Triangle 1
            0f,      0f,
            w,       0f,
            w,       h,

            // Triangle 2
            w,       h,
            0f,      h,
            0f,      0f,
        )

        val vao = GL30.glGenVertexArrays()
        val vbo = GL30.glGenBuffers()
        GL30.glBindVertexArray(vao)
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo)
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices, GL30.GL_STATIC_DRAW)

        GL30.glEnableVertexAttribArray(0)
        GL30.glVertexAttribPointer(
            0,
            2,
            GL30.GL_FLOAT,
            false,
            2 * java.lang.Float.BYTES , // sizeOf(Vertex)
            0,
        )

        GL30.glBindVertexArray(0)
        return Quad(vao, vbo, 6)
    }

    private fun buildGeometry() {
        paddle = buildQuad(playerWidth, paddleHeight)
    }
}
