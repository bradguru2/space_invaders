package org.game.invaders

import org.lwjgl.opengl.*
import org.lwjgl.system.MemoryUtil
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import kotlin.math.roundToInt

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
class RetroFont() {
    val textureId: Int
    private val vaoId: Int
    private val vboId: Int
    private val atlasWidth: Int
    private val atlasHeight: Int
    // Build this once alongside the atlas so you know where each code point lives.
    // same list you used when drawing the atlas
    private val codePoints: List<Int> = buildList {
        addAll(0x0020..0x024F)   // Latin + Latin-1 + Latin Extended-A/B
        addAll(0x1E00..0x1EFF)   // Latin Extended Additional (Vietnamese precomposed)
        addAll(0x0370..0x03FF)   // Greek
        addAll(0x0400..0x04FF)   // Cyrillic
        addAll(0x0530..0x058F)   // Armenian
        addAll(0x10A0..0x10FF)   // Georgian (Mkhedruli)
        addAll(0x1C90..0x1CBF)   // Georgian Extended (Mtavruli) – optional
        addAll(0x0590..0x05FF)   // Hebrew
        addAll(0x0600..0x06FF)   // Arabic
        addAll(0x0750..0x077F)   // Arabic Supplement
        addAll(0x0E00..0x0E7F)   // Thai
        addAll(0x0900..0x097F)   // Devanagari
        addAll(0x3040..0x30FF)   // Hiragana/Katakana
        //addAll(0xAC00..0xD7A3)   // Hangul syllables
        addAll(listOf(0xC810, 0xC218, 0xBC30, 0xB4E4)) // Hangul minimal
        addAll(0x1200..0x137F)   // Ethiopic (Amharic/Tigrinya)
        addAll(listOf(0x5F97, 0x5206, 0x8239, 0x96BB, 0x53EA)) // minimal CJK
        // new Indic/SE Asia ranges
        addAll(0x0980..0x09FF) // Bengali
        addAll(0x0B80..0x0BFF) // Tamil
        addAll(0x0C00..0x0C7F) // Telugu
        addAll(0x0C80..0x0CFF) // Kannada
        addAll(0x0A80..0x0AFF) // Gujarati
        addAll(0x0A00..0x0A7F) // Gurmukhi (Punjabi)
        addAll(0x1780..0x17FF) // Khmer
        addAll(0x1000..0x109F) // Myanmar (Burmese)
        addAll(0x0D80..0x0DFF) // Sinhala
    }

    private val glyphIndex: Map<Int, Int> = buildMap {
        var i = 0
        for (cp in codePoints) put(cp, i++)
    }

    val glyphWidth: Int = DEFAULT_GLYPH_WIDTH    // TODO: adjust glyph width (px)
    val glyphHeight: Int = DEFAULT_GLYPH_HEIGHT  // TODO: adjust glyph height (px)
    private val cols: Int = GLYPH_COLUMNS
    private var rows: Int

    init {
        val neededRows = (codePoints.size + cols - 1) / cols
        rows = neededRows                      // or max(neededRows, GLYPH_ROWS) if you want a minimum

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

        val glyphCount = text.codePointCount(0, text.length)
        val vertsPerChar = 6
        val floatsPerVert = 4
        val fb = MemoryUtil.memAllocFloat(glyphCount * vertsPerChar * floatsPerVert)

        var penX = xStart
        val penY = yStart
        val glyphTexW = glyphWidth.toFloat() / atlasWidth.toFloat()
        val glyphTexH = glyphHeight.toFloat() / atlasHeight.toFloat()

        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            i += Character.charCount(cp)

            val idx = glyphIndex[cp]
            if (idx == null) {
                penX += glyphWidth // unknown glyph
                continue
            }

            val gx = idx % cols
            val gy = idx / cols

            val u0 = gx * glyphTexW
            val u1 = u0 + glyphTexW
            val v0 = 1f - (gy + 1) * glyphTexH
            val v1 = 1f - gy * glyphTexH

            val x0 = penX
            val y0 = penY
            val x1 = penX + glyphWidth
            val y1 = penY + glyphHeight

            fb.put(x0).put(y0).put(u0).put(v0)
            fb.put(x1).put(y0).put(u1).put(v0)
            fb.put(x1).put(y1).put(u1).put(v1)
            fb.put(x1).put(y1).put(u1).put(v1)
            fb.put(x0).put(y1).put(u0).put(v1)
            fb.put(x0).put(y0).put(u0).put(v0)

            penX += glyphWidth
        }

        fb.flip()
        GL30.glBindVertexArray(vaoId)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId)
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, fb)
        MemoryUtil.memFree(fb)

        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, glyphCount * vertsPerChar)
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
        val graphics = image.createGraphics()

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)

        graphics.color = Color.BLACK
        graphics.fillRect(0, 0, atlasWidth, atlasHeight)

        // Choose a mono font with wide coverage; try fallbacks if needed
        val baseFontName = "Noto Sans Mono"
        val fallbackFontNames = listOf("DejaVu Sans Mono", "Ubuntu Mono", "Monospaced")
        val fontSize = (glyphHeight * Constants.FONT_SCALE).toInt()

        fun pickFontFor(cp: Int): Font {
            val allNames = listOf(baseFontName) + fallbackFontNames
            for (name in allNames) {
                val f = Font(name, Font.PLAIN, fontSize)
                if (f.canDisplay(cp)) return f
            }
            // last resort: use first fallback even if it can’t display
            return Font(fallbackFontNames.last(), Font.PLAIN, fontSize)
        }

        var cx = 0
        var cy = 0
        for (cp in codePoints) {
            val s = String(Character.toChars(cp))
            graphics.font = pickFontFor(cp)

            val fm = graphics.fontMetrics
            val x = cx * glyphWidth
            val y = cy * glyphHeight

            val glyphW = fm.stringWidth(s)
            val glyphH = fm.descent + fm.ascent
            val glyphX = x + (glyphWidth - glyphW) / 2
            val glyphY = y + (glyphHeight - glyphH) / 2 + fm.ascent

            graphics.color = Color.WHITE
            graphics.clipRect(x, y, glyphWidth, glyphHeight)
            graphics.drawString(s, glyphX, glyphY - 6)
            graphics.clip = null
            cx++
            if (cx >= cols) {
                cx = 0
                cy++
                //if (cy * glyphHeight >= atlasHeight) break // avoid overflow
            }
        }

        applyScanlines(image)
        graphics.dispose()
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
        private const val DEFAULT_GLYPH_WIDTH = 30
        private const val DEFAULT_GLYPH_HEIGHT = 40
        private const val GLYPH_COLUMNS = 16
    }
}
