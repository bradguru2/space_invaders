#version 330 core

in vec2 vUV;
out vec4 FragColor;

uniform vec3 uColor;

void main()
{
    vec2 centered = vUV - vec2(0.5);
    if (dot(centered, centered) > 0.33) discard;

    FragColor = vec4(uColor, 1.0);
}
