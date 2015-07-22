#version 330 core

layout(location = 0) in vec4 vertexPosition;

uniform mat4 modelMatrix;
uniform mat4 lightSpaceMatrix;

void main()
{
    gl_Position = lightSpaceMatrix * modelMatrix * vec4(vertexPosition.xyz,1.0);
}