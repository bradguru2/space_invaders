package org.game.invaders

import org.lwjgl.opengl.GL11
import org.joml.Matrix4f
import org.lwjgl.opengl.GL13
import java.util.Locale
import java.util.ResourceBundle


class HudRenderer(
    private var font: RetroFont,
    private val shader: HudShader,
    private var windowWidth: Int,
    private var windowHeight: Int,
) {
    private val resourceBundle: ResourceBundle
    private val locale: Locale

    init {
        val localeString = System.getenv("LOCALE") ?: Locale.getDefault().toLanguageTag()
        locale = Locale.forLanguageTag(localeString) // BCP-47 standard parser
        resourceBundle = ResourceBundle.getBundle("strings.messages", locale)
    }

    fun updateWindowSize(w: Int, h: Int) {
        shader.rebuild()
        font = RetroFont() // Rebuild it to be safe
        windowWidth = w
        windowHeight = h
    }

    // Caller sends unlocalized text and it starts at bottom of window
    fun renderStatus(text: String) {
        val hudHeight = (windowHeight * Constants.HUD_HEIGHT_RATIO).toInt()
        val hudY = 0
        val localLocale =
            if (locale.language != "cn" && locale.language != "ko") locale else Locale.forLanguageTag("en-US")

        shader.use()

        // Projection for Window Coordinates
        val proj =
            Matrix4f().ortho2D(0f, windowWidth.toFloat(), 0f, windowHeight.toFloat()).get(FloatArray(16))
        shader.setUniformMat4("projection", proj)

        // objectPos in the Hud shader is used as an offset; we render absolute coords so set to zero
        shader.setUniformVec3("objectPos", 0f, 0f, 0f)

        // bind font texture to texture unit 0 (shader must sample from unit 0)
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, font.textureId)

        // set text color (using HudRegion defaults)
        shader.setUniformVec3("textColor", Constants.TEXT_COLOR_R, Constants.TEXT_COLOR_G, Constants.TEXT_COLOR_B)

        //val translatedText = resourceBundle.getString("foo")
        val startX = (windowWidth - font.getTextWidth(text)) / 2.0f

        // Vertical align inside HUD (centered)
        val textY = hudY + (hudHeight - font.glyphHeight) / 2f

        // Render centered score
        font.renderText(startX, textY, text)

        // unbind texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
    }

    fun render(score: Int, ships: Int) {
        val hudHeight = (windowHeight * Constants.HUD_HEIGHT_RATIO).toInt()
        val hudY = windowHeight - hudHeight

        shader.use()

        // Projection for Window Coordinates
        val proj =
            Matrix4f().ortho2D(0f, windowWidth.toFloat(), 0f, windowHeight.toFloat()).get(FloatArray(16))
        shader.setUniformMat4("projection", proj)

        // objectPos in the Hud shader is used as an offset; we render absolute coords so set to zero
        shader.setUniformVec3("objectPos", 0f, 0f, 0f)

        // bind font texture to texture unit 0 (shader must sample from unit 0)
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, font.textureId)
        // If your shader expects a uniform sampler location to be set once, set it at shader init time.
        // Here we assume sampler2D fontTexture defaults to unit 0.

        // set text color (using HudRegion defaults)
        shader.setUniformVec3("textColor", Constants.TEXT_COLOR_R, Constants.TEXT_COLOR_G, Constants.TEXT_COLOR_B)

        // Compose texts
        val scoreResource = resourceBundle.getString("score")
        val shipResource = resourceBundle.getString("ships")
        val shipsText = "$shipResource: $ships"
        val scoreText = "$scoreResource: $score"  // per your selection (option 1)

        val rightPadding = Constants.RIGHT_PADDING_PX
        val scoreX = rightPadding

        // Vertical align inside HUD (centered)
        val textY = hudY + (hudHeight - font.glyphHeight) / 2f

        // Render centered score
        font.renderText(scoreX, textY, scoreText)

        // Right-justified balls
        val shipsWidth = font.getTextWidth(shipsText)

        val shipsX = windowWidth - shipsWidth - rightPadding
        font.renderText(shipsX, textY, shipsText)

        // unbind texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
    }


    fun cleanup() {
// no buffers owned here
    }
}