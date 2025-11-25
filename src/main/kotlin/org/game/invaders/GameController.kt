package org.game.invaders

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
import kotlin.math.abs
import kotlin.math.roundToInt

class GameController(window: Long, width:Int, height:Int) {
    // Track bricks
    data class Brick (
        var brickX: Int,
        var brickY: Int,
        var isActive: Boolean,
        var brickColor: Triple<Float, Float, Float>,
        val renderer: EnemyRenderer,
    )
    val brickColorArray = arrayOf(
        Triple(0.75f, 0f, 0f),
        Triple(0f, 0.75f, 0f),
        Triple(0f, 0f, 0.75f),
        Triple(0.75f, 0.75f, 0.75f),
        Triple(0.75f, 0f, 0.75f),
        Triple(0.75f, 0.75f, 0f),
        Triple(0f, 0.75f, 0.75f),
        Triple(0.633f, 0.333f, 0.333f)
    )

    private var playerRenderer = PlayerRenderer(PlayerShader(), width, height)
    private val missileRenderer = MissileRenderer(MissilelShader(), width, height)
    private val bricks = mutableListOf<Brick>() // assign BrickShader per brick
    private val retroSynth = RetroSynth()
    private var hudRenderer = HudRenderer(RetroFont(), HudShader(), width, height)
    private var frameRenderer = FrameRenderer(FrameShader(), width, height)
    private var score = 0
    private var balls = 3
    private var isLevelEvent = true
    private var level = 0
    private var gameWindow = window
    private var windowWidth = width
    private var windowHeight = height
    private var gameOver = false
    private var playerX = windowWidth / 2
    private var brickCount = Constants.BRICK_COLUMN_COUNT * Constants.BRICK_ROW_COUNT
    private var brickColorIndex = -1
    private var brickWidth = (windowWidth * Constants.BRICK_WIDTH_RATIO).roundToInt()
    private var frameWidth = (windowWidth * Constants.SIDE_FRAME_RATIO).roundToInt()
    private var divideLineWidth = (windowWidth * Constants.DIVIDE_LINE_RATIO).roundToInt()
    private var brickHeight = (windowHeight * Constants.BRICK_HEIGHT_RATIO).roundToInt()
    private var ballX = 0
    private var ballY = 0
    private var lastTime = System.currentTimeMillis()
    private var deltaTime = 0f
    private var playerSpeed = 600f  // pixels per second
    private var isBallReleased = true
    private var ballDX = 0f
    private var ballDY = 0f
    private var ballSpeedUV = 0.5f
    private var isMoveLeft: Boolean = false
    private var isMoveRight: Boolean = false
    private var isSpaceKey: Boolean = false

    fun execute() {
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
    }

    fun onResizeWindow(newWindow: Long,newWidth: Int, newHeight: Int) {
        gameWindow = newWindow
        val scaleX = 1f * newWidth / windowWidth
        val scaleY = 1f * newHeight / windowHeight
        windowWidth = newWidth
        windowHeight = newHeight

        // Update state as needed
        brickWidth = (windowWidth * Constants.BRICK_WIDTH_RATIO).roundToInt()
        frameWidth = (windowWidth * Constants.SIDE_FRAME_RATIO).roundToInt()
        brickHeight = (windowHeight * Constants.BRICK_HEIGHT_RATIO).roundToInt()
        divideLineWidth = (windowWidth * Constants.DIVIDE_LINE_RATIO).roundToInt()

        for (brick in bricks) {
            brick.renderer.updateWindowSize(windowWidth, windowHeight)
            brick.brickX = (brick.brickX * scaleX).roundToInt()
            brick.brickY = (brick.brickY * scaleY).roundToInt()
        }

        missileRenderer.updateWindowSize(windowWidth, windowHeight)
        ballX = (ballX * scaleX).roundToInt()
        ballY = (ballY * scaleY).roundToInt()
        ballDX *= scaleX
        ballDY *= scaleY

        playerX = (playerX * scaleX).roundToInt()
        hudRenderer.updateWindowSize(windowWidth, windowHeight)
        frameRenderer.updateWindowSize(windowWidth, windowHeight)
        playerRenderer.updateWindowSize(windowWidth, windowHeight, playerRenderer.paddleState)
    }

    private fun initLevel() {
        // Create bricks, set positions, assign shaders
        brickCount = Constants.BRICK_COLUMN_COUNT * Constants.BRICK_ROW_COUNT
        playerX = ((windowWidth - playerRenderer.playerSize()) / 2).roundToInt()
        if(++brickColorIndex>4) brickColorIndex = 0
        rebuildBricks() // Unnecessary but works
        initBall()
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

    fun aabbOverlap(
        ax: Float, ay: Float, aw: Float, ah: Float,
        bx: Float, by: Float, bw: Float, bh: Float
    ): Boolean {
        return ax < bx + bw &&
                ax + aw > bx &&
                ay < by + bh &&
                ay + ah > by
    }

    fun ballHitsLeftWall(ballX: Float, frameLeftX: Float): Boolean =
        ballX <= frameLeftX

    fun ballHitsRightWall(ballX: Float, ballSize: Float, frameRightX: Float): Boolean =
        ballX + ballSize >= frameRightX

    fun ballHitsTopWall(ballY: Float, ballSize: Float, topFrameY: Float): Boolean =
        ballY + ballSize >= topFrameY

    fun ballHitsBrick(
        ballX: Float, ballY: Float, ballSize: Float,
        brickX: Float, brickY: Float, brickW: Float, brickH: Float
    ): Boolean {
        return aabbOverlap(
            ballX, ballY, ballSize, ballSize,
            brickX, brickY, brickW, brickH
        )
    }

    fun ballHitsPaddle() : Boolean {
        return aabbOverlap(
            ballX.toFloat(),
            ballY.toFloat(),
            missileRenderer.ballSize.toFloat(),
            missileRenderer.ballSize.toFloat(),
            playerX.toFloat(),
            Constants.PADDLE_MARGIN,
            playerRenderer.playerSize(),
            playerRenderer.paddleHeight
        )
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
            ballDY = -(ballSpeedUV * windowHeight)
            retroSynth.playSquareBeep(freq = 880f, durationMs = 250)
        }

        if (isBallReleased) {
            ballX+=(ballDX*deltaTime).roundToInt()
            ballY+=(ballDY*deltaTime).roundToInt()
        }

        playerX = (playerX + velocityX * deltaTime).roundToInt()

        val playerWidth = playerRenderer.playerSize().roundToInt()

        // Clamp paddle to window boundaries
        playerX = playerX.coerceIn(
            divideLineWidth,                           // left wall
            windowWidth - divideLineWidth - playerWidth // right wall
        )
    }

    private fun rebuildBricks() {
        bricks.clear()
        val totalPixels = windowWidth - 2 * frameWidth
        val calculatedPixels = Constants.BRICK_COLUMN_COUNT * brickWidth
        val margin = (totalPixels - calculatedPixels) / 2

       var brickY = (Constants.BRICK_START_RATIO * windowHeight).roundToInt()
        for (i in 0 ..< Constants.BRICK_ROW_COUNT) {
            var brickX = frameWidth + margin
            (0 ..<Constants.BRICK_COLUMN_COUNT).forEach { _ ->
                val brick = Brick(
                    brickX,
                    brickY,
                    true,
                    brickColorArray[i/2 + brickColorIndex],
                    EnemyRenderer(EnemyShader(), windowWidth, windowHeight)
                )
                bricks.add(brick)
                brickX += brickWidth
            }
            brickY += brickHeight
        }
    }

    private fun handleCollisions() {
        val ballY = ballY.toFloat()
        val ballSize = missileRenderer.ballSize.toFloat()
        val ballX = ballX.toFloat()

        bricks.forEach {
            if (it.isActive && ballHitsBrick(
                    ballX,
                    ballY,
                    ballSize,
                    it.brickX.toFloat(),
                    it.brickY.toFloat(),
                    brickWidth.toFloat(),
                    brickHeight.toFloat())) {
                ballDY*=-1 // reverse direction
                retroSynth.playNoiseBurst(durationMs = 170)
                it.isActive = false
                score+=200
                brickCount--
            }
        }

        val paddleSize = playerRenderer.playerSize()
        if (ballHitsPaddle()) {
            val halfSize =  paddleSize / 2f
            val mid = playerX + halfSize - 1
            val quarter = mid - (halfSize - 1) / 2f
            val third = mid + (halfSize - 1) / 2f
            val ballMid = ballX - 1 + ballSize / 2f

            if (ballMid == mid) {
                ballDX = 0f
            } else if (ballMid < quarter) {
                ballDX = -abs(ballDY)
            } else if (ballMid < mid) {
                ballDX = -abs(0.5f * ballDY)
            } else if (ballMid < third) {
                ballDX = abs(0.5f * ballDY)
            } else {
                ballDX = abs(ballDY)
            }

            ballDY*=-1 // reverse direction
        } else if (ballY < Constants.PADDLE_MARGIN) {
            initBall()
            balls--
            if(balls <= 0) gameOver = true
        }

        val isHitTopWall = ballHitsTopWall(ballY, ballSize, frameRenderer.startTopY.toFloat())
        val isHitLeftWall = ballHitsLeftWall(ballX, frameWidth.toFloat())
        val isHitRightWall = ballHitsRightWall(ballX, ballSize, windowWidth - frameWidth.toFloat())
        if ( isHitTopWall || isHitRightWall || isHitLeftWall ) {
            retroSynth.playSquareBeep(freq = 550f, durationMs = 60)
            // Clamp and reverse
            ballX.coerceIn(frameWidth.toFloat(), windowWidth - frameWidth - ballSize) 
            ballY.coerceIn(0f, frameRenderer.startTopY.toFloat())
            if (isHitTopWall) {
                if (ballDY >= 0)
                    ballDY*=-1
            } else {
                if (isHitRightWall && ballDX >= 0)
                    ballDX*=-1
                if (isHitLeftWall && ballDX <= 0)
                    ballDX*=-1
            }
        }

        if (brickCount <= 0) {
            isLevelEvent = true
            playerRenderer.updatePaddleState( Constants.NORMAL_PADDLE_RATIO)
            ballSpeedUV += 0.10f
        } else if (brickCount <= 36) {
            playerRenderer.updatePaddleState(Constants.SMALL_PADDLE_RATIO)
        }
    }

    private fun initBall() {
        ballX = (windowWidth / 2f - (windowHeight * Constants.BALL_HEIGHT_RATIO) / 2f).roundToInt()  // center horizontally
        ballY = (windowHeight * Constants.BALL_START_RATIO).roundToInt()
        ballDX = 0f
        ballDY = 0f
        isBallReleased = !isBallReleased
        isSpaceKey = false
    }

    private fun render() {
        glClear(GL_COLOR_BUFFER_BIT)
        playerRenderer.render(playerX)
        missileRenderer.render(ballX.toFloat(), ballY.toFloat())
        bricks.forEach {
            if(it.isActive)
                it.renderer.render(it.brickX, it.brickY, it.brickColor)
        }
        hudRenderer.render(score, balls)
        frameRenderer.render()
    }

    private fun cleanup() {
        retroSynth.cleanup()
        playerRenderer.cleanup()
        missileRenderer.cleanup()
        bricks.forEach { it.renderer.cleanup() }
        hudRenderer.cleanup()
        frameRenderer.cleanup()
    }
}
