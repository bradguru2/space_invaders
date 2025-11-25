package org.game.invaders

import org.joml.Matrix4f
import org.lwjgl.opengl.GL30
import kotlin.math.roundToInt

class EnemyRenderer(private val shader: EnemyShader, private var windowWidth: Int, private var windowHeight:Int) {
    private data class Quad(val vao: Int, val vbo: Int, var vertexCount: Int)
    private lateinit var brick: Quad

    private var brickHeight = (windowHeight * Constants.PADDLE_HEIGHT_RATIO).roundToInt()
    private var brickWidth = (windowWidth * Constants.BRICK_WIDTH_RATIO).roundToInt()

    init {
        buildGeometry() // Initial paddle position at roughly center
    }

    fun cleanup() {
        listOf(brick).forEach { quad ->
            GL30.glDeleteVertexArrays(quad.vao)
            GL30.glDeleteBuffers(quad.vbo)
        }
        shader.cleanup()
    }

    fun updateWindowSize(w: Int, h: Int) {
        shader.rebuild()
        windowWidth = w
        windowHeight = h
        brickHeight = (h * Constants.PADDLE_HEIGHT_RATIO).roundToInt()
        brickWidth = (w * Constants.BRICK_WIDTH_RATIO).roundToInt()
        buildGeometry()
    }

    fun render(brickX: Int, brickY: Int, rgbColor: Triple<Float, Float, Float>) {
        shader.use()

        // Projection for Window Coordinates
        val proj =
            Matrix4f().ortho2D(0f, windowWidth.toFloat(), 0f, windowHeight.toFloat()).get(FloatArray(16))
        shader.setUniformVec2("uBrickPos", brickX.toFloat(), brickY.toFloat()) // Y flipped
        shader.setUniformMat4("uProjection", proj)
        shader.setUniformVec3("uColor", rgbColor.first, rgbColor.second, rgbColor.third)
        shader.setUniformVec2("uSize", brickWidth.toFloat(), brickHeight.toFloat())
        shader.setUniformFloat("bottomMargin", Constants.BRICK_MARGIN_RATIO)

        // Draw paddle
        GL30.glBindVertexArray(brick.vao)
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
        brick = buildQuad(brickWidth + 0.0f, brickHeight + 0.0f)
    }
}
