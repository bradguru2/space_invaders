package org.game.invaders.utilities

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL30.glGenerateMipmap
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

fun <T:Any> T.loadResourceString(path: String): String =
    requireNotNull(this.javaClass.getResourceAsStream(path)) {
        "Resource not found: $path"
    }.bufferedReader().use { it.readText() }


fun <T:Any> T.loadTextureFromResource(resourcePath: String): Int {
    val imageData = this.javaClass.getResourceAsStream(resourcePath)?.use { it.readAllBytes() }
        ?: error("Resource not found: $resourcePath")
    val imageBuffer = MemoryUtil.memAlloc(imageData.size).put(imageData).flip()
    val textureId: Int
    try {
        MemoryStack.stackPush().use { stack ->
            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)
            val c = stack.mallocInt(1)

            STBImage.stbi_set_flip_vertically_on_load(true)
            val pixels = STBImage.stbi_load_from_memory(imageBuffer, w, h, c, 4)
                ?: error("STB load failed: ${STBImage.stbi_failure_reason()}")

            textureId = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, textureId)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, w[0], h[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glGenerateMipmap(GL_TEXTURE_2D)
            glBindTexture(GL_TEXTURE_2D, 0)

            STBImage.stbi_image_free(pixels)
        }
    } finally {
        MemoryUtil.memFree(imageBuffer)
    }
    return textureId
}
