package org.game.invaders

import org.game.invaders.utilities.CollisionUtilities.missileHitsEnemy
import org.game.invaders.utilities.loadTextureFromResource
import kotlin.math.roundToInt

class EnemyManager(
    private var windowWidth: Int,
    private var windowHeight: Int
) {
    enum class CollisionState {
        None,
        Enemy,
        Ufo,
        Invaded,
    }

    // Track enemies
    data class Enemy (
        var enemyX: Int,
        var enemyY: Int,
        var index: Int,
        var isActive: Boolean,
        val renderer: EnemyRenderer,
    )

    var enemyCount = 0
        private set
    var isInvaded = false
        private set
    private val enemies = mutableListOf<Enemy>()
    private var enemyWidth = (windowWidth * Constants.ENEMY_WIDTH_RATIO).roundToInt()
    private var enemyHeight = (windowHeight * Constants.ENEMY_HEIGHT_RATIO).roundToInt()

    private var enemyTexture0: Int = 0
    private var enemyTexture1: Int = 0
    private var enemyTexture2: Int = 0
    private var enemyTexture3: Int = 0
    private var enemyTexture4: Int = 0
    private var enemyTexture5: Int = 0
    private var ufoTexture: Int = 0
    private var enemySpeedUV: Float = 0.toFloat()
    private var enemySpeedMap: Map<Int, Float> = mapOf()
    private var enemyDX = 1
    private var accumulate = 0.0f

    fun rebuildEnemies() {
        isInvaded = false
        loadEnemyTextures()
        enemies.clear()
        enemySpeedUV = 0.005f
        enemySpeedMap = mapOf(
            18 to 0.050f,
             9 to 0.150f,
             4 to 0.250f,
             2 to 0.750f,
             1 to 1.0f,
        )
        enemyDX = 1
        val margin = 3
        val frameWidth = (windowWidth - 2 * Constants.DIVIDE_LINE_RATIO * windowWidth).roundToInt()
        val spacer = (frameWidth / (Constants.ENEMY_COLUMN_COUNT * 1.0f) - enemyWidth).roundToInt()
        val startPosition = (Constants.DIVIDE_LINE_RATIO * windowWidth - enemyWidth).roundToInt()
        var enemyY = (Constants.ENEMY_START_RATIO * windowHeight).roundToInt()
        for (i in 0 ..< Constants.ENEMY_ROW_COUNT) {
            var enemyX = startPosition

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
                enemyCount++
            }
            enemyY += enemyHeight + margin
        }
        // Add UFO
        enemies.add(
            Enemy(
                ((windowWidth - enemyWidth) / 2.0f).roundToInt(),
                enemyY,
                index = 6,
                false,
                EnemyRenderer(EnemyShader(), windowWidth, windowHeight, getEnemyTexture(6))
            )
        )
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

    fun update(dt: Float) {
        if (isInvaded) return
        val rightMax = (windowWidth - windowWidth * Constants.DIVIDE_LINE_RATIO).roundToInt()
        val leftMax = (windowWidth * Constants.DIVIDE_LINE_RATIO - enemyWidth).roundToInt()
        var changDirection = false

        // Move enemies
        val deltaX = (accumulate + enemySpeedUV * windowWidth * enemyDX * dt).roundToInt()
        // We only take discreet pixel steps
        if (deltaX == 0) {
            accumulate += enemySpeedUV * windowWidth * enemyDX * dt
        }
        else {
            accumulate = 0.0f
        }
        for (enemy in enemies) {
            if (enemy.index > 5 || deltaX == 0 || !enemy.isActive) continue
            if (enemy.enemyX + deltaX in leftMax..rightMax) {
                enemy.enemyX += deltaX
            } else {
                changDirection = true
            }
        }
        if (changDirection) {
            val margin = 3
            enemyDX = -enemyDX
            enemies.forEach { enemy ->
                if (enemy.index < 6) {
                    enemy.enemyY -= enemyHeight + margin
                }
            }
        }

        when (enemyCount) {
            18 -> enemySpeedUV = enemySpeedMap[enemyCount]!!
             9 -> enemySpeedUV = enemySpeedMap[enemyCount]!!
             4 -> enemySpeedUV = enemySpeedMap[enemyCount]!!
             2 -> enemySpeedUV = enemySpeedMap[enemyCount]!!
             1 -> enemySpeedUV = enemySpeedMap[enemyCount]!!
        }
    }

    fun hasCollision(missileX: Int, missileY: Int, missileWidth: Int, missileHeight: Int): CollisionState {
        if (isInvaded) return CollisionState.Invaded // If already invaded, no need to check for collision
        val bottom = (Constants.BOTTOM_FRAME_RATIO * windowHeight).roundToInt()
        val bottomThreshold = bottom + enemyHeight
        var deltaY = 0
        enemies.forEach {
            if (isInvaded) return@forEach // Continue
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
                // Can Collide only with one enemy per check
                return if (it.index < 6) {
                    enemyCount--
                    CollisionState.Enemy
                } else CollisionState.Ufo
            } else if (it.isActive && it.enemyY < bottomThreshold) {
                isInvaded = true
                deltaY = it.enemyY - bottom
            }
        }
        return if (isInvaded) {
            enemies.forEach {
                if (it.index < 6) it.enemyY -= deltaY
            }
            CollisionState.Invaded
        } else {
            CollisionState.None
        }
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
            5 -> enemyTexture5
            else ->  ufoTexture
        }
    }

    private fun loadEnemyTextures() {
        enemyTexture0 = loadTextureFromResource("/images/enemy0.png")
        enemyTexture1 = loadTextureFromResource("/images/enemy1.png")
        enemyTexture2 = loadTextureFromResource("/images/enemy2.png")
        enemyTexture3 = loadTextureFromResource("/images/enemy3.png")
        enemyTexture4 = loadTextureFromResource("/images/enemy4.png")
        enemyTexture5 = loadTextureFromResource("/images/enemy5.png")
        ufoTexture = loadTextureFromResource("/images/ufo.png")
    }

    private fun getMinMaxActiveColumns(): Pair<Int, Int> {
        var maxCol = 0
        var minCol = Constants.ENEMY_COLUMN_COUNT - 1
        for (i in 0 ..< Constants.ENEMY_ROW_COUNT) {
            (0 ..< Constants.ENEMY_COLUMN_COUNT).forEach { j ->
                val enemy = enemies[i * Constants.ENEMY_COLUMN_COUNT + j]
                if (enemy.isActive && j > maxCol) {
                    maxCol = j
                } else if (enemy.isActive && j < minCol) {
                    minCol = j
                }
            }
        }
        return Pair(minCol, maxCol)
    }
}