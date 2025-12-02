package org.game.invaders

import org.game.invaders.utilities.loadTextureFromResource
import org.joml.Matrix4f
import org.lwjgl.opengl.GL30

class PlayerRenderer(private val shader: PlayerShader, private var windowWidth: Int, private var windowHeight: Int) {
    private data class Quad(val vao: Int, val vbo: Int, var vertexCount: Int)
    private lateinit var player: Quad
    private var normalTexture: Int

    var playerHeight = windowHeight * Constants.PLAYER_HEIGHT_RATIO
        private set
    var playerState = Constants.NORMAL_PLAYER_RATIO
        private set
    var playerWidth = windowWidth * playerState
        private set

    init {
        buildGeometry() // Initial paddle position at roughly center
        normalTexture = loadTextureFromResource("/images/player.png")
    }

    fun cleanup() {
        listOf(player).forEach { quad ->
            GL30.glDeleteVertexArrays(quad.vao)
            GL30.glDeleteBuffers(quad.vbo)
        }
        shader.cleanup()
    }

    fun updatePlayerState(newPaddleState: Float) {
        playerState = newPaddleState
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
        playerHeight = windowHeight * Constants.PLAYER_HEIGHT_RATIO
        playerWidth = windowWidth * s
        buildGeometry()
        normalTexture = loadTextureFromResource("/images/player.png")
    }

    fun render(playerX: Int) {
        shader.use()
        // Projection for Window Coordinates
        val proj =
            Matrix4f().ortho2D(0f, windowWidth.toFloat(), 0f, windowHeight.toFloat()).get(FloatArray(16))

        // Set uniform parameters for shader
        shader.setUniformVec2("uPlayerPos", playerX.toFloat(), Constants.BOTTOM_FRAME_RATIO * windowHeight)
        shader.setUniformMat4("uProjection", proj)
        shader.setUniformVec3("uColor", Constants.PADDLE_COLOR_R, Constants.PADDLE_COLOR_G, Constants.PADDLE_COLOR_B)
        shader.setUniformVec2("uSize", playerWidth, playerHeight)
        shader.setUniformInt("uTex", 0) // sampler uses texture unit 0

        // Draw player
        GL30.glBindVertexArray(player.vao)
        GL30.glActiveTexture(GL30.GL_TEXTURE0)
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, normalTexture)
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
        player = buildQuad(playerWidth, playerHeight)
    }
}
