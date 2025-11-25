#version 330 core
layout(location = 0) in vec2 aPos;
layout(location = 1) in vec2 aTexCoord;
out vec2 TexCoord;
uniform vec2 objectPos;
uniform mat4 projection;
void main() {
    gl_Position = projection * vec4(aPos + objectPos, 0.0, 1.0);
    TexCoord = aTexCoord;
}