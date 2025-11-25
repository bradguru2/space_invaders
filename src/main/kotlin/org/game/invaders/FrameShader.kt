package org.game.invaders

class FrameShader : ShaderProgram(
    vertexSource = Constants.FRAME_VERTEX_SHADER_PATH,
    fragmentSource = Constants.FRAME_FRAGMENT_SHADER_PATH,
)