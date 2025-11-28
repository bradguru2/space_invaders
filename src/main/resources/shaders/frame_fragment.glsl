#version 330 core
const float DIVIDE_LINE_RATIO=0.10; // In Sync with Constants
out vec4 FragColor;
uniform vec3 uColor;
in vec2 vPos;
in mat4 region;

void main() {
    float width = 2.0 / region[0][0]; // Translating back from UV into width
    float rightUV = 1 - DIVIDE_LINE_RATIO;
    float leftUV = DIVIDE_LINE_RATIO;
    if (vPos.x > rightUV * width && vPos.x < (rightUV + 0.01) * width) discard; // Right dividing-line
    if (vPos.x > (leftUV - 0.01) * width && vPos.x < leftUV * width) discard; // Left dividing-line
    FragColor = vec4(uColor, 1.0);
}