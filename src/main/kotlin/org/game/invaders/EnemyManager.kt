package org.game.invaders

import org.game.invaders.utilities.CollisionUtilities.missileHitsEnemy
import org.game.invaders.utilities.loadTextureFromResource
import kotlin.math.roundToInt

class EnemyManager(
    private var windowWidth: Int,
    private var windowHeight: Int
) {

    // Track enemies
    data class Enemy (
        var enemyX: Int,
        var enemyY: Int,
        var index: Int,
        var isActive: Boolean,
        val renderer: EnemyRenderer,
    )

    private val enemies = mutableListOf<Enemy>()
    private var enemyWidth = (windowWidth * Constants.ENEMY_WIDTH_RATIO).roundToInt()
    private var enemyHeight = (windowHeight * Constants.ENEMY_HEIGHT_RATIO).roundToInt()

    private var enemyTexture0: Int = 0
    private var enemyTexture1: Int = 0
    private var enemyTexture2: Int = 0
    private var enemyTexture3: Int = 0
    private var enemyTexture4: Int = 0
    private var enemyTexture5: Int = 0

    fun rebuildEnemies() {
        loadEnemyTextures()
        enemies.clear()
        val margin = 3
        val frameWidth = (windowWidth - 2 * Constants.DIVIDE_LINE_RATIO * windowWidth).roundToInt()
        val spacer = (frameWidth / (Constants.ENEMY_COLUMN_COUNT * 1.0f) - enemyWidth).roundToInt()
        val startPosition = ((windowWidth - frameWidth) / 2.0f).roundToInt()
        var enemyY = (Constants.ENEMY_START_RATIO * windowHeight).roundToInt()
        for (i in 0 ..< Constants.ENEMY_ROW_COUNT) {
            var enemyX = startPosition + margin

            (0 ..<Constants.ENEMY_COLUMN_COUNT).forEach { _ ->
                val enemy = Enemy(
                    enemyX,
                    enemyY,
                    index = i,
                    true,
                    EnemyRenderer(EnemyShader(), windowWidth, windowHeight, getEnemyTexture(i))
                )
                enemies.add(enemy)
                enemyX += enemyWidth + spacer
            }
            enemyY += enemyHeight
        }
    }

    fun onWindowResize(newWindowWidth: Int, newWindowHeight: Int) {
        loadEnemyTextures()
        val scaleX = 1f * newWindowWidth / windowWidth
        val scaleY = 1f * newWindowHeight / windowHeight

        enemyWidth = (scaleX * windowWidth * Constants.ENEMY_WIDTH_RATIO).roundToInt()
        enemyHeight = (scaleY * windowHeight * Constants.ENEMY_HEIGHT_RATIO).roundToInt()

        for (enemy in enemies) {
            enemy.renderer.updateWindowSize(newWindowWidth, newWindowHeight, getEnemyTexture(enemy.index))
            enemy.enemyX = (enemy.enemyX * scaleX).roundToInt()
            enemy.enemyY = (enemy.enemyY * scaleY).roundToInt()
        }
        windowWidth = newWindowWidth
        windowHeight= newWindowHeight
    }

    fun render() {
        enemies.forEach {
            if(it.isActive)
                it.renderer.render(it.enemyX, it.enemyY)
        }
    }

    fun hasCollision(missileX: Int, missileY: Int, missileWidth: Int, missileHeight: Int): Boolean {
        enemies.forEach {
            if (it.isActive && missileHitsEnemy(
                    missileX.toFloat(),
                    missileY.toFloat(),
                    missileWidth.toFloat(),
                    missileHeight.toFloat(),
                    it.enemyX.toFloat(),
                    it.enemyY.toFloat(),
                    enemyWidth.toFloat(),
                    enemyHeight.toFloat()
                )
            ) {
                it.isActive = false
                return true // Can Collide only with one enemy per check
            }
        }
        return false
    }

    fun cleanUp() {
        enemies.forEach { it.renderer.cleanup() }
    }

    private fun getEnemyTexture(enemyRow: Int): Int {
        return when(enemyRow) {
            0 -> enemyTexture0
            1 -> enemyTexture1
            2 -> enemyTexture2
            3 -> enemyTexture3
            4 -> enemyTexture4
            else -> enemyTexture5
        }
    }

    private fun loadEnemyTextures(){
        enemyTexture0 = loadTextureFromResource("/images/enemy0.png")
        enemyTexture1 = loadTextureFromResource("/images/enemy1.png")
        enemyTexture2 = loadTextureFromResource("/images/enemy2.png")
        enemyTexture3 = loadTextureFromResource("/images/enemy3.png")
        enemyTexture4 = loadTextureFromResource("/images/enemy4.png")
        enemyTexture5 = loadTextureFromResource("/images/enemy5.png")
    }
}