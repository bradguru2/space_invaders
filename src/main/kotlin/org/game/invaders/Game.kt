package org.game.invaders

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWVidMode
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil


class Game {
    private var window: Long = 0
    private var width = 800
    private var height = 600
    private var fullscreen = false

    private lateinit var controller: GameController

    fun start() {
        initWindow()
        initOpenGL()
        controller = GameController(window, width, height)
        controller.execute()
        cleanup()
    }

    private fun initWindow(): Pair<Int, Int> {
        if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

        var monitor: Long = glfwGetPrimaryMonitor()
        val vid: GLFWVidMode? = glfwGetVideoMode(monitor)
        var localWidth = width
        var localHeight = height

        if (fullscreen) {
            localWidth = vid!!.width()
            localHeight = vid.height()
            glfwWindowHint(GLFW_RED_BITS, vid.redBits())
            glfwWindowHint(GLFW_GREEN_BITS, vid.greenBits())
            glfwWindowHint(GLFW_BLUE_BITS, vid.blueBits())
            glfwWindowHint(GLFW_REFRESH_RATE, vid.refreshRate())
        } else {
            monitor = MemoryUtil.NULL
        }

        window = glfwCreateWindow(localWidth, localHeight, "Breakout Clone", monitor, MemoryUtil.NULL)

        if (!fullscreen) {
            // restore windowed mode
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
            glfwSetWindowAttrib(window, GLFW_DECORATED, GLFW_TRUE)
            glfwSetWindowSize(window, width, height)
            glfwSetWindowPos(
                window,
                (vid!!.width() - width) / 2,
                (vid.height() - height) / 2
            )
        }

        glfwMakeContextCurrent(window)
        glfwSwapInterval(1) // vsync
        glfwShowWindow(window)
        GL.createCapabilities()

        // Set ESC to close window
        glfwSetKeyCallback(window) { _, key, _, action, mods ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(window, true)
            } else if (key == GLFW_KEY_ENTER && (mods and GLFW_MOD_ALT) != 0) {
                toggleFullscreen()
            }
        }

        return Pair(localWidth, localHeight)
    }

    private fun toggleFullscreen() {
        fullscreen = !fullscreen
        // Destroy current window and recreate
        glfwDestroyWindow(window)
        val (localWidth, localHeight) = initWindow()
        initOpenGL()
        controller.onResizeWindow(window, localWidth, localHeight)
    }

    private fun initOpenGL() {
        glClearColor(0f, 0f, 0f, 1f)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    private fun cleanup() {
        glfwDestroyWindow(window)
        glfwTerminate()
    }
}

fun main() {
    Game().start()
}
