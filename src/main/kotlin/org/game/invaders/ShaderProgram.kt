package org.game.invaders

import org.game.invaders.utilities.loadResourceString
import org.lwjgl.opengl.GL30
import kotlin.properties.Delegates

abstract class ShaderProgram(private val vertexSource: String, private val fragmentSource: String) {
    private var programId by Delegates.notNull<Int>()

    init {
        rebuild()
    }

    fun rebuild() {
        val vertexId = compileShader(loadResourceString(vertexSource), GL30.GL_VERTEX_SHADER)
        val fragmentId = compileShader(loadResourceString(fragmentSource), GL30.GL_FRAGMENT_SHADER)
        programId = GL30.glCreateProgram()
        GL30.glAttachShader(programId, vertexId)
        GL30.glAttachShader(programId, fragmentId)
        GL30.glLinkProgram(programId)
        GL30.glValidateProgram(programId)
        GL30.glDeleteShader(vertexId)
        GL30.glDeleteShader(fragmentId)
    }

    fun use() = GL30.glUseProgram(programId)

    fun setUniformInt(name: String, newValue: Int) {
        val location = GL30.glGetUniformLocation(programId, name)
        GL30.glUniform1i(location, newValue)
    }

    fun setUniformFloat(name: String, newValue: Float) {
        val location = GL30.glGetUniformLocation(programId, name)
        GL30.glUniform1f(location, newValue)
    }

    fun setUniformVec2(name: String, x: Float, y: Float) {
        val location = GL30.glGetUniformLocation(programId, name)
        GL30.glUniform2f(location, x, y)
    }
    
    fun setUniformVec3(name: String, x: Float, y: Float, z: Float) {
        val location = GL30.glGetUniformLocation(programId, name)
        GL30.glUniform3f(location, x, y, z)
    }

    fun setUniformMat4(name: String, matrix: FloatArray) {
        val location = GL30.glGetUniformLocation(programId, name)
        GL30.glUniformMatrix4fv(location, false, matrix)
    }

    fun cleanup() = GL30.glDeleteProgram(programId)

    private fun compileShader(source: String, type: Int): Int {
        val shaderId = GL30.glCreateShader(type)
        GL30.glShaderSource(shaderId, source)
        GL30.glCompileShader(shaderId)
        val success = GL30.glGetShaderi(shaderId, GL30.GL_COMPILE_STATUS)
        if (success == 0) {
            throw RuntimeException("Error compiling shader: " + GL30.glGetShaderInfoLog(shaderId))
        }
        return shaderId
    }
}

