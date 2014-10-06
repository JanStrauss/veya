#version 330 core


layout(std140, binding = 0) uniform MVP {

	mat4 p;
	mat4 v;
	mat4 m;

} mvp;


layout(location = 0) in vec4 vertex_position;
layout(location = 1) in vec4 vertex_color;


out vec4 fvc;


void main() {

	fvc = vertex_color;

	gl_Position = mvp.p * mvp.v * mvp.m * vertex_position;

}