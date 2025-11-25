package org.game.invaders

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.nio.FloatBuffer

/**
 * RetroFont
 *
 * - Procedurally generates a bitmap font atlas using Java2D (Monospaced font).
 * - Uploads the atlas as a single-channel GL texture (GL_R8).
 * - Renders text using a dynamic VBO bound to a VAO; expects the active shader to:
 *     - be already used (shader.use())
 *     - have a 'projection' mat4 uniform set
 *     - sample the font texture from texture unit 0 via a sampler2D named "fontTexture"
 *     - read vertex attributes: location 0 = vec2 position, location 1 = vec2 texcoord
 *
 * TODO markers indicate default values you can change later.
 */
class RetroFont(
    val glyphWidth: Int = DEFAULT_GLYPH_WIDTH,    // TODO: adjust glyph width (px)
    val glyphHeight: Int = DEFAULT_GLYPH_HEIGHT,  // TODO: adjust glyph height (px)
    private val cols: Int = GLYPH_COLUMNS,
    private val rows: Int = GLYPH_ROWS,
    private val firstChar: Int = FIRST_CHAR,
) {
    val textureId: Int
    private val vaoId: Int
    private val vboId: Int
    val atlasWidth: Int
    val atlasHeight: Int

    init {
        atlasWidth = glyphWidth * cols
        atlasHeight = glyphHeight * rows

        val atlas = createAtlasBitmap()
        textureId = uploadAtlasToTexture(atlas)

        // create VAO/VBO for streaming quads (pos.xy + tex.u,tex.v)
        val vao = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(vao)

        val vbo = GL15.glGenBuffers()
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)

        // reserve buffer space for a reasonable maximum (change if needed)
        val reserveBytes = 4096 * 4 * java.lang.Float.BYTES // floats for many glyphs
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, reserveBytes.toLong(), GL15.GL_DYNAMIC_DRAW)

        // vertex layout: (location = 0) vec2 position, (location = 1) vec2 texcoord
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 4 * java.lang.Float.BYTES, 0)
        GL20.glEnableVertexAttribArray(0)
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 4 * java.lang.Float.BYTES, 2L * java.lang.Float.BYTES)
        GL20.glEnableVertexAttribArray(1)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GL30.glBindVertexArray(0)

        vaoId = vao
        vboId = vbo
    }

    /**
     * Render text at absolute screen coordinates (origin at bottom-left).
     * The caller must:
     *  - have the appropriate shader bound (shader.use())
     *  - have set the projection matrix uniform
     *  - have set the shader's text color uniform (if any)
     *
     * The font texture must be bound to texture unit 0 (this method binds it automatically).
     */
    fun renderText(xStart: Float, yStart: Float, text: String) {
        if (text.isEmpty()) return

        val chars = text.toCharArray()
        val vertsPerChar = 6 // two triangles per glyph
        val floatsPerVert = 4 // x, y, u, v
        val totalFloats = chars.size * vertsPerChar * floatsPerVert
        val fb: FloatBuffer = MemoryUtil.memAllocFloat(totalFloats)

        var penX = xStart
        val penY = yStart

        val glyphTexW = glyphWidth.toFloat() / atlasWidth.toFloat()
        val glyphTexH = glyphHeight.toFloat() / atlasHeight.toFloat()

        for (ch in chars) {
            val code = ch.code
            if (code < firstChar || code >= firstChar + (cols * rows)) {
                penX += glyphWidth // unknown -> advance
                continue
            }
            val idx = code - firstChar
            val gx = idx % cols
            val gy = idx / cols

            val u0 = gx * glyphTexW
            val u1 = u0 + glyphTexW
            // v=0 is bottom in OpenGL; top row of atlas is row 0 in Java2D
            val v0 = 1.0f - (gy + 1) * glyphTexH
            val v1 = 1.0f - gy * glyphTexH


            val x0 = penX
            val y0 = penY
            val x1 = penX + glyphWidth
            val y1 = penY + glyphHeight

            // triangle 1
            fb.put(x0).put(y0).put(u0).put(v0)
            fb.put(x1).put(y0).put(u1).put(v0)
            fb.put(x1).put(y1).put(u1).put(v1)
            // triangle 2
            fb.put(x1).put(y1).put(u1).put(v1)
            fb.put(x0).put(y1).put(u0).put(v1)
            fb.put(x0).put(y0).put(u0).put(v0)

            // advance pen for fixed-width font
            penX += glyphWidth
        }

        fb.flip()

        GL30.glBindVertexArray(vaoId)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId)
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, fb)
        MemoryUtil.memFree(fb)

        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)

        val vertexCount = chars.size * vertsPerChar
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount)

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GL30.glBindVertexArray(0)
    }

    /**
     * Simple fixed-width measurement
     */
    fun getTextWidth(text: String): Float {
        return text.length * glyphWidth.toFloat()
    }

    fun cleanup() {
        GL11.glDeleteTextures(textureId)
        GL30.glDeleteVertexArrays(vaoId)
        GL15.glDeleteBuffers(vboId)
    }

    // ---------------------------
    // Bitmap generation + upload
    // ---------------------------

    private fun createAtlasBitmap(): BufferedImage {
        val image = BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()

        // crisp pixel look (no antialias)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)

        // fill background black
        g.color = Color.BLACK
        g.fillRect(0, 0, atlasWidth, atlasHeight)

        // TODO: Change base font if you want a different stroke (default is Monospaced bold)
        val fontSize = (glyphHeight * 0.85f).toInt()
        val font = Font("Monospaced", Font.BOLD, fontSize)
        g.font = font
        g.color = Color.WHITE

        var cx = 0
        var cy = 0
        for (code in firstChar until (firstChar + cols * rows)) {
            val ch = code.toChar().toString()
            val x = cx * glyphWidth
            val y = cy * glyphHeight

            val fm = g.fontMetrics
            val glyphX = x + (glyphWidth - fm.charWidth(code)) / 2
            val glyphY = y + ((glyphHeight - fm.height) / 2) + fm.ascent

            g.drawString(ch, glyphX, glyphY)

            cx++
            if (cx >= cols) {
                cx = 0
                cy++
            }
        }

        // apply scanlines for retro feel (default mild intensity)
        applyScanlines(image)

        g.dispose()
        return image
    }

    private fun applyScanlines(img: BufferedImage) {
        val intensity = 0.00f // lower = darker scanline, adjust to taste
        for (row in 1 until img.height step 2) {

            for (col in 0 until img.width) {
                val rgba = img.getRGB(col, row)
                val a = rgba ushr 24 and 0xff
                var r = rgba ushr 18 and 0xff

                // reduce only the red channel
                r = (r * intensity).toInt().coerceIn(0, 255)

                val nrgb = (a shl 24) or (r shl 16) or (r shl 8) or r
                img.setRGB(col, row, nrgb)

            }
        }
    }


    private fun uploadAtlasToTexture(img: BufferedImage): Int {
        // convert to single-channel red bytes
        val bytes = ByteArray(atlasWidth * atlasHeight)
        var bi = 0

        // Open GL expects the texture data bottom-to-top but Java2D is top-to-bottom
        for (y in atlasHeight - 1 downTo 0) {   // iterate bottom-to-top
            for (x in 0 until atlasWidth) {
                val rgba = img.getRGB(x, y)
                val r = (rgba ushr 16) and 0xff
                bytes[bi++] = r.toByte()
            }
        }

        val buffer: ByteBuffer = MemoryUtil.memAlloc(bytes.size)
        buffer.put(bytes)
        buffer.flip()

        val tex = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex)

        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1)
        // TODO: Change filtering as desired
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            GL30.GL_R8,
            atlasWidth,
            atlasHeight,
            0,
            GL11.GL_RED,
            GL11.GL_UNSIGNED_BYTE,
            buffer
        )
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)

        MemoryUtil.memFree(buffer)
        return tex
    }

    companion object {
        // TODO: tweak these defaults (one place to change)
        private const val DEFAULT_GLYPH_WIDTH = 32
        private const val DEFAULT_GLYPH_HEIGHT = 40
        private const val GLYPH_COLUMNS = 16
        private const val GLYPH_ROWS = 6
        private const val FIRST_CHAR = 32
    }
}
