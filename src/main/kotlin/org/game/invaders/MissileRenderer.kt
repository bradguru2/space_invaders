package org.game.invaders

import org.joml.Matrix4f
import org.joml.Random
import org.lwjgl.opengl.GL30
import kotlin.math.roundToInt

class MissileRenderer(
    private val shader: MissilelShader,
    private var windowWidth: Int,
    private var windowHeight: Int
) {
    private data class Quad(val vao: Int, val vbo: Int)

    private lateinit var quad: Quad

    var missileHeight = (windowHeight * Constants.MISSILE_HEIGHT_RATIO).roundToInt()
        private set
    var missileWidth = (windowWidth * Constants.MISSILE_WIDTH_RATIO).roundToInt()

    private val random = Random(System.currentTimeMillis())

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
        missileHeight = (windowHeight * Constants.MISSILE_HEIGHT_RATIO).roundToInt()
        missileWidth = (windowWidth * Constants.MISSILE_WIDTH_RATIO).roundToInt()
        buildGeometry()
    }

    private fun buildGeometry() {
        val h = missileHeight.toFloat()
        val w = missileWidth.toFloat()
        val vertices = floatArrayOf(
            0f, 0f,
            w, 0f,
            w, h,
            w, h,
            0f, h,
            0f, 0f,
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

    fun render(missileX: Float, missileY: Float) {
        shader.use()
        val height = missileHeight.toFloat()
        val width = missileWidth.toFloat()

        val proj = Matrix4f().ortho2D(0f, windowWidth.toFloat(), 0f, windowHeight.toFloat())
            .get(FloatArray(16))
        val nextFloat = random.nextFloat() * 0.5f // Intensity < 0.51f
        shader.setUniformMat4("uProjection", proj)
        shader.setUniformVec2("uBallPos", missileX, missileY)
        shader.setUniformVec2("uBallSize", width, height)
        shader.setUniformVec3(
            "uColor",
            Constants.MISSILE_COLOR_R + nextFloat,
            Constants.MISSILE_COLOR_G + nextFloat,
            Constants.MISSILE_COLOR_B + nextFloat,
        )

        GL30.glBindVertexArray(quad.vao)
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6)
        GL30.glBindVertexArray(0)
    }
}