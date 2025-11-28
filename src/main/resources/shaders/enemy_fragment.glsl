#version 330 core
out vec4 FragColor;
uniform vec3 uColor;
uniform float bottomMargin;   // e.g. 0.1 means 10% of brick height
in vec2 vUV;

void main() {
    if (vUV.y < bottomMargin)
        discard;  // cut off the bottom area

    FragColor = vec4(uColor, 1.0);
}