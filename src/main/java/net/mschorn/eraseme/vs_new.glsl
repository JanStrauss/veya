#version 150 core

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

in vec4 vertexPosition;
in vec4 vertexColor;

out vec4 passColor;

void main() {
    gl_Position =  projectionMatrix * viewMatrix * modelMatrix * vertexPosition;
    passColor = vertexColor;
}