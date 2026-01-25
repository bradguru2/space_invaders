package org.game.invaders

import org.game.invaders.utilities.CollisionUtilities.missileHitsEnemy
import org.game.invaders.utilities.loadTextureFromResource
import kotlin.math.roundToInt
import kotlin.random.Random

class EnemyManager(
    private var windowWidth: Int,
    private var windowHeight: Int
) {
    enum class RenderState {
        Normal,
        Alternate
    }

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
    private var enemyTexture6: Int = 0
    private var enemyTexture7: Int = 0
    private var enemyTexture8: Int = 0
    private var enemyTexture9: Int = 0
    private var enemyTexture10: Int = 0
    private var enemyTexture11: Int = 0
    private var ufoTexture: Int = 0
    private var enemySpeedUV: Float = 0.toFloat()
    private var enemySpeedMap: Map<Int, Float> = mapOf()
    private var enemyDX = 1
    private var accumulate = 0.0f
    private var ufoTimer = 0.0f
    private var renderState = RenderState.Normal
    private var renderStateInterval = 1.0f
    private var renderStateTimer = 0.0f
    private var startingPoint = -1
    private var isPaused = false

    fun getMissileStartCoordinates(): Pair<Int, Int> {
        val enemy = getRandomActiveEnemy()
        val missileX = enemy.enemyX + enemyWidth / 2
        val missileY = enemy.enemyY - 3 // A bit below the enemy
        return Pair(missileX, missileY)
    }

    fun onPlayerHit() {
        isPaused = true
        // Reset UFO
        val ufo = enemies.last()
        ufo.isActive = false
        ufo.enemyX = 0
        ufoTimer = 0.0f
    }

    fun onPlayerResumed() {
        isPaused = false
    }

    fun resetStartingPoint() {
        val margin = 3
        startingPoint = (Constants.ENEMY_START_RATIO * windowHeight).roundToInt() + enemyHeight + margin
    }

    fun advanceStartingPoint() {
        val bottom = (Constants.BOTTOM_FRAME_RATIO * windowHeight).roundToInt()
        val bottomThreshold = (bottom + 2 * enemyHeight)
        val margin = 3
        if (startingPoint > bottomThreshold) {
            startingPoint -= enemyHeight + margin
        }
    }

    fun rebuildEnemies() {
        enemyCount = 0
        accumulate = 0.0f
        ufoTimer = 0.0f
        isInvaded = false
        isPaused = false
        loadEnemyTextures()
        enemies.clear()
        enemySpeedUV = 0.005f
        renderState = RenderState.Normal
        renderStateInterval = 1.0f
        renderStateTimer = 0.0f
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
        var enemyY = startingPoint
        for (i in 0 ..< Constants.ENEMY_ROW_COUNT) {
            var enemyX = startPosition

            (0 ..<Constants.ENEMY_COLUMN_COUNT).forEach { _ ->
                val enemy = Enemy(
                    enemyX,
                    enemyY,
                    index = i,
                    true,
                    EnemyRenderer(EnemyShader(), windowWidth, windowHeight)
                )
                enemies.add(enemy)
                enemyX += enemyWidth + spacer
                enemyCount++
            }
            enemyY += enemyHeight + margin
        }
        // Add UFO in last row index by itself
        enemyY = (windowHeight - windowHeight * Constants.HUD_HEIGHT_RATIO - enemyHeight - margin).roundToInt()
        enemies.add(
            Enemy(
                0,
                enemyY,
                index = Constants.ENEMY_ROW_COUNT,
                false,
                EnemyRenderer(EnemyShader(), windowWidth, windowHeight)
            )
        )
    }

    fun onWindowResize(newWindowWidth: Int, newWindowHeight: Int) {
        loadEnemyTextures()
        val scaleX = 1f * newWindowWidth / windowWidth
        val scaleY = 1f * newWindowHeight / windowHeight

        enemyWidth = (scaleX * windowWidth * Constants.ENEMY_WIDTH_RATIO).roundToInt()
        enemyHeight = (scaleY * windowHeight * Constants.ENEMY_HEIGHT_RATIO).roundToInt()
        startingPoint = (scaleY * startingPoint).roundToInt()

        for (enemy in enemies) {
            enemy.renderer.updateWindowSize(newWindowWidth, newWindowHeight)
            enemy.enemyX = (enemy.enemyX * scaleX).roundToInt()
            enemy.enemyY = (enemy.enemyY * scaleY).roundToInt()
        }
        windowWidth = newWindowWidth
        windowHeight= newWindowHeight
    }

    fun render() {
        enemies.forEach {
            if(it.isActive)
                it.renderer.render(it.enemyX, it.enemyY, getEnemyTexture(it.index))
        }
    }

    fun update(dt: Float, retroSynth: RetroSynth) {
        if (isInvaded || enemies.count() == 0 || isPaused) return
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
            if (enemy.index == Constants.ENEMY_ROW_COUNT || deltaX == 0 || !enemy.isActive) continue
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
                if (enemy.index < Constants.ENEMY_ROW_COUNT) {
                    enemy.enemyY -= enemyHeight + margin
                }
            }
        }

        when (enemyCount) {
            18 -> {
                enemySpeedUV = enemySpeedMap[enemyCount]!!
                renderStateInterval = 0.5f
            }
             9 -> {
                 enemySpeedUV = enemySpeedMap[enemyCount]!!
                 renderStateInterval = 0.25f
             }
             4 -> {
                 enemySpeedUV = enemySpeedMap[enemyCount]!!
                 renderStateInterval = 0.125f
             }
             2 -> {
                 enemySpeedUV = enemySpeedMap[enemyCount]!!
                 renderStateInterval = 0.06225f
             }
             1 -> {
                 enemySpeedUV = enemySpeedMap[enemyCount]!!
                 renderStateInterval = 0.031125f
             }
        }

        // Update render state
        renderStateTimer += dt
        if (renderStateTimer >= renderStateInterval) {
            renderState = if (renderState == RenderState.Normal) RenderState.Alternate else RenderState.Normal
            renderStateTimer = 0.0f
            retroSynth.playInvaderStep((renderStateInterval * 1000).toInt(), "drum")
        }

        // Update UFO
        val ufo = enemies.last()
        if (!ufo.isActive) {
            ufoTimer += dt
            if (ufoTimer >= Constants.UFO_SECONDS) {
                ufo.isActive = true
                ufo.enemyX = 0
                ufoTimer = Constants.UFO_SOUND_INTERVAL
            }
        }  else {
            ufoTimer += dt
            ufo.enemyX += (enemySpeedMap[9]!! * windowWidth *  dt).roundToInt()
            if (ufoTimer >= Constants.UFO_SOUND_INTERVAL) {
                retroSynth.playUfoSound(Constants.UFO_SOUND_MILLIS)
                ufoTimer = 0.0f
            }
            if (ufo.enemyX > rightMax) {
                ufo.isActive = false
                ufoTimer = 0.0f
            }
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
                return if (it.index < Constants.ENEMY_ROW_COUNT) {
                    enemyCount--
                    CollisionState.Enemy
                } else {
                    ufoTimer = 0.0f
                    CollisionState.Ufo
                }
            } else if (it.isActive && it.enemyY < bottomThreshold) {
                isInvaded = true
                deltaY = it.enemyY - bottom
            }
        }
        return if (isInvaded) {
            enemies.forEach {
                if (it.index < Constants.ENEMY_ROW_COUNT) it.enemyY -= deltaY
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
            0 -> if(renderState == RenderState.Normal) enemyTexture0 else enemyTexture6
            1 -> if(renderState == RenderState.Normal) enemyTexture1 else enemyTexture7
            2 -> if(renderState == RenderState.Normal) enemyTexture2 else enemyTexture8
            3 -> if(renderState == RenderState.Normal) enemyTexture3 else enemyTexture9
            4 -> if(renderState == RenderState.Normal) enemyTexture4 else enemyTexture10
            5 -> if(renderState == RenderState.Normal) enemyTexture5 else enemyTexture11
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
        enemyTexture6 = loadTextureFromResource("/images/enemy6.png")
        enemyTexture7 = loadTextureFromResource("/images/enemy7.png")
        enemyTexture8 = loadTextureFromResource("/images/enemy8.png")
        enemyTexture9 = loadTextureFromResource("/images/enemy9.png")
        enemyTexture10 = loadTextureFromResource("/images/enemy10.png")
        enemyTexture11 = loadTextureFromResource("/images/enemy11.png")
        ufoTexture = loadTextureFromResource("/images/ufo.png")
    }

    // Note: the bottom row is index 0
    private fun getRandomActiveEnemy(): Enemy {
        val candidates = mutableListOf<Enemy>()
        val visitedCol = mutableSetOf<Int>()

        for (i in 0 ..< Constants.ENEMY_ROW_COUNT) {
            // Check for candidates in this row
            (0 ..< Constants.ENEMY_COLUMN_COUNT).forEach { j ->
                val enemy = enemies[i * Constants.ENEMY_COLUMN_COUNT + j]
                if (enemy.isActive && !visitedCol.contains(j)) {
                    visitedCol.add(j)
                    candidates.add(enemy)
                }
            }
        }

        if (candidates.isNotEmpty()) {
            return candidates[Random.nextInt(candidates.size)]
        }

        throw IllegalStateException("No active enemies found when checking all enemy rows")
    }
}