#version 330 core

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 lightSpaceMatrix;

layout(location = 0) in vec4 vertexPosition;
layout(location = 1) in vec4 vertexColor;
layout(location = 2) in vec3 vertexTexturePosition;
layout(location = 3) in vec3 vertexNormal;
layout(location = 4) in float vertexAO;

out vec4 passColor;
out vec3 passTexturePosition;

out vec3 normal;
out vec3 position;
out vec4 fragPosLightSpace;
out float ambiantOcc;


void main() {
	mat4 modelInvTranspMatrix = transpose(inverse(modelMatrix));

	vec4 worldPosition = modelMatrix * vec4(vertexPosition.xyz, 1.0);
    gl_Position = projectionMatrix * viewMatrix * worldPosition;
    
    passColor = vertexColor;
    passTexturePosition = vertexTexturePosition;
    
    normal = normalize(vec3(modelInvTranspMatrix * vec4(vertexNormal,1.0)));
    position = vec3(worldPosition);

	fragPosLightSpace = lightSpaceMatrix * worldPosition;

    ambiantOcc = vertexAO;
}