package org.game.invaders

import org.game.invaders.utilities.loadTextureFromResource
import org.joml.Matrix4f
import org.lwjgl.opengl.GL30
import kotlin.math.roundToInt

class EnemyRenderer(
    private val shader:
    EnemyShader,
    private var windowWidth: Int,
    private var windowHeight: Int,
    private var enemyTexture: Int,
) {
    private data class Quad(val vao: Int, val vbo: Int, var vertexCount: Int)
    private lateinit var enemy: Quad

    private var enemyHeight = (windowHeight * Constants.ENEMY_HEIGHT_RATIO).roundToInt()
    private var enemyWidth = (windowWidth * Constants.ENEMY_WIDTH_RATIO).roundToInt()

    init {
        buildGeometry() // Initial paddle position at roughly center
    }

    fun cleanup() {
        listOf(enemy).forEach { quad ->
            GL30.glDeleteVertexArrays(quad.vao)
            GL30.glDeleteBuffers(quad.vbo)
        }
        shader.cleanup()
    }

    fun updateWindowSize(w: Int, h: Int, newEnemyTexture: Int) {
        shader.rebuild()
        windowWidth = w
        windowHeight = h
        enemyHeight = (h * Constants.PLAYER_HEIGHT_RATIO).roundToInt()
        enemyWidth = (w * Constants.ENEMY_WIDTH_RATIO).roundToInt()
        buildGeometry()
        enemyTexture = newEnemyTexture
    }

    fun render(enemyX: Int, enemyY: Int) {
        shader.use()

        // Projection for Window Coordinates
        val proj =
            Matrix4f().ortho2D(0f, windowWidth.toFloat(), 0f, windowHeight.toFloat()).get(FloatArray(16))
        shader.setUniformVec2("uEnemyPos", enemyX.toFloat(), enemyY.toFloat()) // Y flipped
        shader.setUniformMat4("uProjection", proj)
        shader.setUniformVec3("uColor", 1.0f, 1.0f, 1.0f)
        shader.setUniformVec2("uSize", enemyWidth.toFloat(), enemyHeight.toFloat())
        shader.setUniformFloat("bottomMargin", Constants.ENEMY_MARGIN_RATIO)
        shader.setUniformInt("uTex", 0) // sampler uses texture unit 0

        // Draw Enemy
        GL30.glBindVertexArray(enemy.vao)
        GL30.glActiveTexture(GL30.GL_TEXTURE0)
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, enemyTexture)
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
        enemy = buildQuad(enemyWidth + 0.0f, enemyHeight + 0.0f)
    }
}
