package org.game.invaders

import org.joml.Matrix4f
import org.lwjgl.opengl.GL30
import kotlin.math.roundToInt

class FrameRenderer(
    private val shader: FrameShader,
    private var windowWidth: Int,
    private var windowHeight: Int,
) {
    private data class Quad(val vao: Int, val vbo: Int, var vertexCount: Int)
    private lateinit var bottom: Quad
    private lateinit var top: Quad
    var startTopY: Int = 0
        private set

    private var hudHeight = windowHeight * Constants.HUD_HEIGHT_RATIO

    init {
        buildGeometry()
    }

    fun cleanup() {
        listOf(bottom).forEach { quad ->
            GL30.glDeleteVertexArrays(quad.vao)
            GL30.glDeleteBuffers(quad.vbo)
        }
        shader.cleanup()
    }

    fun updateWindowSize(w: Int, h: Int) {
        shader.rebuild()
        windowWidth = w
        windowHeight = h
        hudHeight = windowHeight * Constants.HUD_HEIGHT_RATIO
        buildGeometry()
    }

    fun render() {
        shader.use()
        // Projection for Window Coordinates
        val proj =
            Matrix4f().ortho2D(0f, windowWidth.toFloat(), 0f, windowHeight.toFloat()).get(FloatArray(16))
        shader.setUniformMat4("uProjection", proj)
        // set frame color
        shader.setUniformVec3("uColor", Constants.FRAME_COLOR_R, Constants.FRAME_COLOR_G, Constants.FRAME_COLOR_B)

        // Draw Bottom
        GL30.glBindVertexArray(bottom.vao)
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6)
        // Draw Top
        GL30.glBindVertexArray(top.vao)
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6)

        GL30.glBindVertexArray(0)
    }

    private fun buildQuad(x: Float, y: Float, w: Float, h: Float): Quad {
        val vertices = floatArrayOf(
            // Triangle 1
            x,       y,
            x + w,   y,
            x + w,   y + h,

            // Triangle 2
            x + w,   y + h,
            x,       y + h,
            x,       y,
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
            2 * java.lang.Float.BYTES, // sizeOf(Vertex)
            0,
        )

        GL30.glBindVertexArray(0)
        return Quad(vao, vbo, 4)
    }

    private fun buildGeometry() {
        val bottomHeight = (windowHeight * Constants.BOTTOM_FRAME_RATIO).roundToInt()
        val sideWidth = (windowWidth * Constants.SIDE_FRAME_RATIO).roundToInt()
        startTopY =(windowHeight - hudHeight).roundToInt()

        bottom = buildQuad(0f,  0.0f, windowWidth.toFloat(), bottomHeight + 0.0f)
        top = buildQuad(0f, windowHeight - hudHeight - 1, windowWidth.toFloat(), 1.0f)
    }
}