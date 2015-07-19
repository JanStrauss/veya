#version 150 core

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

in vec4 vertexPosition;
in vec4 vertexColor;
in vec3 vertexTexturePosition;
in vec3 vertexNormal;
in float vertexAO;

out vec4 passColor;
out vec3 passTexturePosition;

out vec3 normal;
out vec3 position;

out float ambiantOcc;


void main() {
	mat4 modelInvTranspMatrix = transpose(inverse(modelMatrix));

	vec4 worldPosition = modelMatrix * vec4(vertexPosition.xyz, 1.0);
    gl_Position = projectionMatrix * viewMatrix * worldPosition;
    
    passColor = vertexColor;
    passTexturePosition = vertexTexturePosition;
    
    normal = normalize(vec3(modelInvTranspMatrix * vec4(vertexNormal,1.0)));
    position = vec3(worldPosition);

    ambiantOcc = vertexAO;
}