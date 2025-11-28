#version 330 core

layout (location = 0) in vec2 aPos;   // 0..size quad

uniform mat4 uProjection;
uniform vec2 uBallPos;
uniform vec2 uBallSize;
out vec2 vUV;

out vec2 vLocalPos;   // pass local coordinates directly

void main() {
    vUV = aPos / uBallSize;         // derive UVs (0..1)

    vec2 worldPos = aPos + uBallPos;
    gl_Position = uProjection * vec4(worldPos, 0.0, 1.0);
}
