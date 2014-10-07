#version 150 core

uniform sampler2D textureData;

uniform vec3 lightPosition;
uniform vec3 lightColor;

in vec4 passColor;
in vec2 passTexturePosition;

in vec3 normal;
in vec3 position;
in vec3 cameraPosition;

out vec4 fragColor;

vec3 BlinnPhong(vec3 V, vec3 N, vec3 L, vec3 color, vec3 lightColor) 
{
  vec3 h = normalize(V + L);
  float ka = 0.33; 									// ambient
  float kd = max(0.0, dot(L, N)); 					// diffuse
  float ks = 0.25 * pow(max(dot(N, h), 0.0), 33.0); // specular

  return vec3(ka) * color + vec3(kd) * color + ks * lightColor;
}

void main() {
	vec3 V = normalize(cameraPosition - position);
	vec3 N = normal;
	vec3 L = normalize(lightPosition - position);
	
	vec3 baseColor = vec3(texture(textureData, passTexturePosition));

	
	// fragColor = passColor;
    fragColor = vec4(BlinnPhong(V, N, L, baseColor, lightColor), 1.0);
}