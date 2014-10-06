#version 150 core

uniform sampler2D textureData;

in vec4 passColor;
in vec2 passTexturePosition;

out vec4 fragColor;

void main() {
   // fragColor = passColor;
    fragColor = texture(textureData, passTexturePosition);
}