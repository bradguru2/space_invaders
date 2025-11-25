#version 330 core
in vec2 TexCoord;
out vec4 FragColor;
uniform sampler2D fontTexture;
uniform vec3 textColor;
void main() {
    float alpha = texture(fontTexture, TexCoord).r;
    FragColor = vec4(textColor, alpha);
}