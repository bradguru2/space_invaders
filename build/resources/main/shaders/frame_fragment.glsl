#version 330 core
out vec4 FragColor;
uniform vec3 uColor;
in vec2 vPos;
in mat4 region;

void main() {
    float width = 2.0 / region[0][0]; // Translating back from UV into width
    if (vPos.x > 0.90 * width && vPos.x < 0.91 * width) discard; // Right dividing-line
    if (vPos.x > 0.09 * width && vPos.x < 0.10 * width) discard; // Left dividing-line
    FragColor = vec4(uColor, 1.0);
}