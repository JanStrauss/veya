#version 150 core

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

in vec4 vertexPosition;
in vec4 vertexColor;
in vec2 vertexTexturePosition;
in vec3 vertexNormal;

out vec4 passColor;
out vec2 passTexturePosition;

out vec3 normal;
out vec3 position;
out vec3 cameraPosition;

void main() {
	mat4 modelInvTranspMatrix = transpose(inverse(modelMatrix));
	mat4 viewInvMatrix = inverse(viewMatrix);

	vec4 worldPosition = modelMatrix * vertexPosition;
    gl_Position = projectionMatrix * viewMatrix * worldPosition;
    
    passColor = vertexColor;
    passTexturePosition = vertexTexturePosition;
    
    normal = normalize(vec3(modelInvTranspMatrix * vec4(vertexNormal,1.0)));
    position = vec3(worldPosition);
    cameraPosition = vec3(viewInvMatrix * viewMatrix[3]);
}