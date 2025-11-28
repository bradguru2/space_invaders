#version 330 core
layout(location = 0) in vec2 aPos;
uniform vec2 uPlayerPos;
uniform mat4 uProjection;
uniform vec2 uSize; // w,h
out vec2 vUV;

void main() {
    vUV = aPos / uSize; // Convert to UV because aPos is in Window Coords and uSize in pixels
    gl_Position = uProjection * vec4(aPos + uPlayerPos, 0.0f, 1.0);
}