#version 330 core

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

layout(location = 0) in vec4 v;
layout(location = 1) in vec4 vc;

out vec4 fvc;


void main() {

	fvc = vc;

	gl_Position = projectionMatrix * viewMatrix * modelMatrix * v;

}
