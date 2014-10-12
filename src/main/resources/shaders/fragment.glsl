#version 150 core

const int BLOCK_COUNT = 9;

uniform sampler2DArray textureData;

uniform vec3 lightPosition;
uniform vec3 lightColor;

uniform bool colorSwitch;

uniform int textureLookup[6 * BLOCK_COUNT];

in vec4 passColor;
in vec3 passTexturePosition;

in flat int texOffset;

in vec3 normal;
in vec3 position;
in vec3 cameraPosition;

out vec4 fragColor;

vec3 BlinnPhong(vec3 V, vec3 N, vec3 L, vec3 color, vec3 lightColor) 
{
  vec3 h = normalize(V + L);
  float ka = 0.33; 									// ambient
  float kd = max(0.0, dot(L, N)); 					// diffuse
  float ks = 0.0 * pow(max(dot(N, h), 0.0), 33.0); // specular

  return vec3(ka) * color + vec3(kd) * color + ks * lightColor;
}

void main() {
	if(colorSwitch){
		fragColor = passColor;
	} else{
		vec3 V = normalize(cameraPosition - position);
		vec3 N = normal;
		vec3 L = normalize(lightPosition - position);
		
		vec4 texColor = texture(textureData, vec3(passTexturePosition.xy, textureLookup[texOffset * 6 + int(passTexturePosition.z)]));
		
		vec3 baseColor = vec3(texColor.rgb);
		fragColor = vec4(BlinnPhong(V, N, L, baseColor, lightColor), texColor.a);
	}
}