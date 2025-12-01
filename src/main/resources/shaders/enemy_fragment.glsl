#version 330 core
in vec2 vUV;
out vec4 FragColor;
uniform vec3 uColor;
uniform sampler2D uTex;

void main() {
    vec4 texColor = texture(uTex, vUV);
    vec3 key = vec3(0.0); float tol = 0.02;
    if (length(texColor.rgb - key) < tol) discard;
    FragColor = vec4(texColor.rgb * uColor, 1.0);
}