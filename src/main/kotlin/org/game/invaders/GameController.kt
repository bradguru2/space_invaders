package org.game.invaders

/** TODO need shared ALC DC import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.game.invaders.utilities.MusicUtilities **/
import org.game.invaders.utilities.CollisionUtilities.missileHitsPlayer
import org.lwjgl.glfw.GLFW.GLFW_KEY_A
import org.lwjgl.glfw.GLFW.GLFW_KEY_D
import org.lwjgl.glfw.GLFW.GLFW_KEY_F2
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
import kotlin.math.roundToInt

class GameController(window: Long, width:Int, height:Int) {

    private var playerRenderer = PlayerRenderer(PlayerShader(), width, height)
    private val missileRenderer = MissileRenderer(MissilelShader(), width, height)
    private val retroSynth = RetroSynth()
    private var hudRenderer = HudRenderer(RetroFont(), HudShader(), width, height)
    private var frameRenderer = FrameRenderer(FrameShader(), width, height)
    private var enemyManager = EnemyManager(width, height)
    private var score = 0
    private var ships = 3
    private var isLevelEvent = false
    private var level = 0
    private var gameWindow = window
    private var windowWidth = width
    private var windowHeight = height
    private var gameOver = true
    private var playerX = windowWidth / 2
    private var frameWidth = (windowWidth * Constants.SIDE_FRAME_RATIO).roundToInt()
    private var divideLineWidth = (windowWidth * Constants.DIVIDE_LINE_RATIO).roundToInt()

    private var missileX = 0
    private var missileY = -500
    private var lastTime = System.currentTimeMillis()
    private var deltaTime = 0f
    private var playerSpeedUV = 0.25f  // percentage converts to UV
    private var isMissileFired = true
    private var missileDX = 0f
    private var missileDY = 0f
    private var missileSpeedUV = 0.5f
    private var isMoveLeft: Boolean = false
    private var isMoveRight: Boolean = false
    private var isSpaceKey: Boolean = false

    fun execute() {
        // TODO need shared ALC DC val job = MusicUtilities.playAsync (CoroutineScope(Dispatchers.IO),"/some.mp3")
        while (!glfwWindowShouldClose(gameWindow)) {
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
        missileDX *= scaleX
        missileDY *= scaleY

        playerX = (playerX * scaleX).roundToInt()
        hudRenderer.updateWindowSize(windowWidth, windowHeight)
        frameRenderer.updateWindowSize(windowWidth, windowHeight)
        playerRenderer.updateWindowSize(windowWidth, windowHeight, playerRenderer.playerState)
    }

    private fun initLevel() {
        // Create enemies, set positions, assign shaders
        playerX = divideLineWidth
        enemyManager.rebuildEnemies()
    }

    private fun computeDeltaTime(): Float {
        val now = System.currentTimeMillis()
        val dt = (now - lastTime) / 1000f   // convert ms → seconds
        lastTime = now
        return dt
    }

    private fun pollInput() {
        // left movement
        if (glfwGetKey(gameWindow, GLFW_KEY_LEFT) == GLFW_PRESS
            || glfwGetKey(gameWindow, GLFW_KEY_A) == GLFW_PRESS) {
            isMoveLeft = true
        }

        // right movement
        if (glfwGetKey(gameWindow, GLFW_KEY_RIGHT) == GLFW_PRESS
            || glfwGetKey(gameWindow, GLFW_KEY_D) == GLFW_PRESS) {
            isMoveRight = true
        }

        if (!isMissileFired && glfwGetKey(gameWindow, GLFW_KEY_SPACE) == GLFW_PRESS) {
            isSpaceKey = true
        }

        if (gameOver && glfwGetKey(gameWindow, GLFW_KEY_F2) == GLFW_PRESS) {
            gameOver = false
            isLevelEvent = true
            level = 0
            score = 0
            ships = 3
        }
    }

    private fun update() {

        // Exit here if game over
        if (gameOver) return

        var velocityX = 0f

        if (isMoveLeft) {
            isMoveLeft = false
            velocityX -= playerSpeedUV * windowWidth
        }

        if (isMoveRight) {
            isMoveRight = false
            velocityX += playerSpeedUV * windowWidth
        }

        playerX += (velocityX * deltaTime).roundToInt()

        val playerWidth = playerRenderer.playerWidth.roundToInt()

        // Clamp player to window boundaries
        playerX = playerX.coerceIn(
            divideLineWidth,                            // left line
            windowWidth - divideLineWidth - playerWidth // right line
        )

        if (isMissileFired) {
            missileY += (missileDY * deltaTime).roundToInt()
        } else if (isSpaceKey) {
            fireMissile()
            isSpaceKey = false
        }

        enemyManager.update(deltaTime)
    }

    private fun handleCollisions() {
        if (gameOver) return
        val my = missileY.toFloat()
        val mw = missileRenderer.missileWidth.toFloat()
        val mh = missileRenderer.missileHeight.toFloat()
        val mx = missileX.toFloat()

        val collisionState = enemyManager.hasCollision(
            mx.roundToInt(),
            my.roundToInt(),
            mw.roundToInt(),
            mh.roundToInt(),
        )
        if (collisionState == EnemyManager.CollisionState.Enemy || collisionState == EnemyManager.CollisionState.Ufo) {
            missileDY = 0f
            missileY = -500
            isMissileFired = false
            retroSynth.playNoiseBurst(durationMs = 170)
            score += if (collisionState == EnemyManager.CollisionState.Enemy) {
                50
            } else {
                200
            }
        } else if (collisionState == EnemyManager.CollisionState.Invaded) {
            gameOver = true
        }

        if (missileHitsPlayer(
            mx,
            my,
            mw,
            mw,
            playerX.toFloat(),
            windowHeight * Constants.BOTTOM_FRAME_RATIO,
            playerRenderer.playerWidth,
            playerRenderer.playerHeight
        )) {
            missileDY = 0f
            missileY = -500
            isMissileFired = false
            ships--
            gameOver = ships <= 0
            retroSynth.playNoiseBurst(durationMs = 170)
        } else if (my <= (windowHeight * Constants.BOTTOM_FRAME_RATIO)
            || my >= (frameRenderer.startTopY - missileRenderer.missileHeight)) {
            missileDY = 0f
            missileY = -500
            isMissileFired = false
            //retroSynth.playSquareBeep(freq = 550f, durationMs = 60)
        }
        if (enemyManager.enemyCount <= 0) {
            isLevelEvent = true
        }
    }

    private fun fireMissile() {
        missileX = (playerX + playerRenderer.playerWidth / 2.0f).roundToInt()  // center horizontally
        missileY = (windowHeight * Constants.PLAYER_MISSILE_START_RATIO).roundToInt()
        isMissileFired = true
        missileDX = 0f
        missileDY = (missileSpeedUV * windowHeight)
        retroSynth.playSquareBeep(freq = 880f, durationMs = 250) // Player fire
    }

    private fun render() {
        glClear(GL_COLOR_BUFFER_BIT)
        if (!enemyManager.isInvaded) {
            playerRenderer.render(playerX)
            missileRenderer.render(missileX.toFloat(), missileY.toFloat())
        }
        enemyManager.render()
        hudRenderer.render(score, ships)
        frameRenderer.render()
        if (gameOver) hudRenderer.renderStatus("F2 to Start")
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
