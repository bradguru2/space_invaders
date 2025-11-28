#version 330 core
layout(location = 0) in vec2 aPos;
uniform vec2 uEnemyPos;
uniform mat4 uProjection;
out vec2 vUV;
uniform vec2 uSize;  // (w, h)

void main() {
    vUV = aPos / uSize;         // derive UVs (0..1)
    gl_Position = uProjection * vec4(aPos + uEnemyPos, 0.0f, 1.0);
}