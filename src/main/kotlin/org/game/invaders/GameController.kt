package org.game.invaders

/** TODO need shared ALC DC import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.game.invaders.utilities.MusicUtilities **/
import org.game.invaders.utilities.CollisionUtilities.missileHitsPlayer
import org.lwjgl.glfw.GLFW.GLFW_KEY_A
import org.lwjgl.glfw.GLFW.GLFW_KEY_D
import org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT
import org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT
import org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE
import org.lwjgl.glfw.GLFW.GLFW_PRESS
import org.lwjgl.glfw.GLFW.glfwGetKey
import org.lwjgl.glfw.GLFW.glfwPollEvents
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.glfw.GLFW.glfwWindowShouldClose
import org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT
import org.lwjgl.opengl.GL11.glClear
import kotlin.collections.plusAssign
import kotlin.dec
import kotlin.math.roundToInt
import kotlin.times

class GameController(window: Long, width:Int, height:Int) {

    private var playerRenderer = PlayerRenderer(PlayerShader(), width, height)
    private val missileRenderer = MissileRenderer(MissilelShader(), width, height)
    private val retroSynth = RetroSynth()
    private var hudRenderer = HudRenderer(RetroFont(), HudShader(), width, height)
    private var frameRenderer = FrameRenderer(FrameShader(), width, height)
    private var enemyManager = EnemyManager(width, height)
    private var score = 0
    private var ships = 3
    private var isLevelEvent = true
    private var level = 0
    private var gameWindow = window
    private var windowWidth = width
    private var windowHeight = height
    private var gameOver = false
    private var playerX = windowWidth / 2
    private var enemyCount = Constants.ENEMY_COLUMN_COUNT * Constants.ENEMY_ROW_COUNT
    private var frameWidth = (windowWidth * Constants.SIDE_FRAME_RATIO).roundToInt()
    private var divideLineWidth = (windowWidth * Constants.DIVIDE_LINE_RATIO).roundToInt()

    private var missileX = 0
    private var missileY = 0
    private var lastTime = System.currentTimeMillis()
    private var deltaTime = 0f
    private var playerSpeed = 600f  // pixels per second
    private var isBallReleased = true
    private var ballDX = 0f
    private var missileDY = 0f
    private var ballSpeedUV = 0.5f
    private var isMoveLeft: Boolean = false
    private var isMoveRight: Boolean = false
    private var isSpaceKey: Boolean = false

    fun execute() {
        // TODO need shared ALC DC val job = MusicUtilities.playAsync (CoroutineScope(Dispatchers.IO),"/some.mp3")
        while (!glfwWindowShouldClose(gameWindow) && !gameOver) {
            deltaTime = computeDeltaTime()
            if (isLevelEvent) {
                level++
                initLevel()
                isLevelEvent = false
            }
            pollInput()
            update()
            handleCollisions()
            render()
            glfwSwapBuffers(gameWindow)
            glfwPollEvents()
        }
        cleanup()
        // TODO need shared ALC DC MusicUtilities.stopPlayback()
        // TODO need shared ALC DC runBlocking { job.cancelAndJoin() } // Wait for cleanup
    }

    fun onResizeWindow(newWindow: Long,newWidth: Int, newHeight: Int) {
        gameWindow = newWindow
        val scaleX = 1f * newWidth / windowWidth
        val scaleY = 1f * newHeight / windowHeight
        windowWidth = newWidth
        windowHeight = newHeight

        // Update state as needed
        frameWidth = (windowWidth * Constants.SIDE_FRAME_RATIO).roundToInt()
        divideLineWidth = (windowWidth * Constants.DIVIDE_LINE_RATIO).roundToInt()

        enemyManager.onWindowResize(newWidth, newHeight)

        missileRenderer.updateWindowSize(windowWidth, windowHeight)
        missileX = (missileX * scaleX).roundToInt()
        missileY = (missileY * scaleY).roundToInt()
        ballDX *= scaleX
        missileDY *= scaleY

        playerX = (playerX * scaleX).roundToInt()
        hudRenderer.updateWindowSize(windowWidth, windowHeight)
        frameRenderer.updateWindowSize(windowWidth, windowHeight)
        playerRenderer.updateWindowSize(windowWidth, windowHeight, playerRenderer.paddleState)
    }

    private fun initLevel() {
        // Create enemies, set positions, assign shaders
        enemyCount = Constants.ENEMY_COLUMN_COUNT * Constants.ENEMY_ROW_COUNT
        playerX = ((windowWidth - playerRenderer.playerSize()) / 2).roundToInt()
        enemyManager.rebuildEnemies()
        initMissile()
    }

    private fun computeDeltaTime(): Float {
        val now = System.currentTimeMillis()
        val dt = (now - lastTime) / 1000f   // convert ms → seconds
        lastTime = now
        return dt
    }

    private fun pollInput() {
        // left movement
        if (glfwGetKey(gameWindow, GLFW_KEY_LEFT) == GLFW_PRESS) {
            isMoveLeft = true
        }

        // right movement
        if (glfwGetKey(gameWindow, GLFW_KEY_RIGHT) == GLFW_PRESS) {
            isMoveRight = true
        }

        if (!isBallReleased && glfwGetKey(gameWindow, GLFW_KEY_SPACE) == GLFW_PRESS) {
            isSpaceKey = true
        }
    }

    private fun update() {
        var velocityX = 0f

        if (glfwGetKey(gameWindow, GLFW_KEY_LEFT) == GLFW_PRESS ||
            glfwGetKey(gameWindow, GLFW_KEY_A) == GLFW_PRESS) {
            velocityX -= playerSpeed
        }

        if (glfwGetKey(gameWindow, GLFW_KEY_RIGHT) == GLFW_PRESS ||
            glfwGetKey(gameWindow, GLFW_KEY_D) == GLFW_PRESS) {
            velocityX += playerSpeed
        }

        if (!isBallReleased && isSpaceKey) {
            isBallReleased = true
            missileDY = -(ballSpeedUV * windowHeight)
            retroSynth.playSquareBeep(freq = 880f, durationMs = 250)
        }

        if (isBallReleased) {
            missileX+=(ballDX*deltaTime).roundToInt()
            missileY+=(missileDY*deltaTime).roundToInt()
        }

        playerX = (playerX + velocityX * deltaTime).roundToInt()

        val playerWidth = playerRenderer.playerSize().roundToInt()

        // Clamp paddle to window boundaries
        playerX = playerX.coerceIn(
            divideLineWidth,                           // left wall
            windowWidth - divideLineWidth - playerWidth // right wall
        )
    }

    private fun handleCollisions() {
        val missileY = missileY.toFloat()
        val missileSize = missileRenderer.missileHeight.toFloat()
        val missileX = missileX.toFloat()

        if (enemyManager.hasCollision(
                missileX.roundToInt(),
                missileY.roundToInt(),
                missileRenderer.missileWidth,
                missileRenderer.missileHeight,
            )) {
            missileDY *= -1 // reverse direction
            retroSynth.playNoiseBurst(durationMs = 170)
            score += 200
            enemyCount--
        }

        val playerSize = playerRenderer.playerSize()
        if (missileHitsPlayer(
            missileX,
            missileY,
            missileSize,
            missileSize,
            playerX.toFloat(),
            windowHeight * Constants.BOTTOM_FRAME_RATIO,
            playerSize,
            playerRenderer.playerHeight
        )) {
            missileDY*=-1 // reverse direction
        } else if (missileY < (windowHeight * Constants.BOTTOM_FRAME_RATIO)) {
            initMissile()
            ships--
            if(ships <= 0) gameOver = true
        }

        // retroSynth.playSquareBeep(freq = 550f, durationMs = 60) save for hitshield
        if (enemyCount <= 0) {
            isLevelEvent = true
            playerRenderer.updatePaddleState( Constants.NORMAL_PLAYER_RATIO)
            ballSpeedUV += 0.10f
        } else if (enemyCount <= 36) {
            playerRenderer.updatePaddleState(Constants.SMALL_PLAYER_RATIO)
        }
    }

    private fun initMissile() {
        missileX = (windowWidth / 2f - (windowHeight * Constants.MISSILE_HEIGHT_RATIO) / 2f).roundToInt()  // center horizontally
        missileY = (windowHeight * Constants.BALL_START_RATIO).roundToInt()
        ballDX = 0f
        missileDY = 0f
        isBallReleased = !isBallReleased
        isSpaceKey = false
    }

    private fun render() {
        glClear(GL_COLOR_BUFFER_BIT)
        playerRenderer.render(playerX)
        missileRenderer.render(missileX.toFloat(), missileY.toFloat())
        enemyManager.render()
        hudRenderer.render(score, ships)
        frameRenderer.render()
        hudRenderer.renderStatus("F2 to Start")
    }

    private fun cleanup() {
        retroSynth.cleanup()
        playerRenderer.cleanup()
        missileRenderer.cleanup()
        enemyManager.cleanUp()
        hudRenderer.cleanup()
        frameRenderer.cleanup()
    }
}
