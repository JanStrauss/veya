#version 150 core

uniform sampler2DArray textureData;

uniform vec3 cameraPosition;

uniform vec3 lightPosition;
uniform vec3 lightColor;
uniform vec3 lightFactors;

uniform bool colorSwitch;

in vec4 passColor;
in vec3 passTexturePosition;

in vec3 normal;
in vec3 position;

in float ambiantOcc;

out vec4 fragColor;

vec3 BlinnPhong(vec3 V, vec3 N, vec3 L, vec3 color, vec3 lightColor, vec3 lightFactors){
	vec3 h = normalize(V + L);
	float ka = lightFactors.x;										// ambient
	float kd = lightFactors.y * max(0.0, dot(L, N));				// diffuse
	float ks = lightFactors.z * pow(max(dot(N, h), 0.0), 11.0);		// specular

	return vec3(ka) * color + vec3(kd) * color + ks * lightColor;
}

float norm(float x){
return x/3.0;
}

void main() {
	vec3 V = normalize(cameraPosition - position);
	vec3 N = normal;
	vec3 L = normalize(lightPosition - position);

	vec4 texColor = texture(textureData, vec3(passTexturePosition.xy, int(passTexturePosition.z)));

	vec3 baseColor = vec3(texColor.rgb);
	if(colorSwitch){
		baseColor = vec3(passColor.rgb);
    }

	// dirty workaround, water is only transparent surface and should have specular reflections
	vec3 adaptedLightFactors = lightFactors;
	if(texColor.a == 1.0){
		adaptedLightFactors.z = 0;
	}

	adaptedLightFactors.x = 0.1 + 0.9*(adaptedLightFactors.x * norm(ambiantOcc));

	fragColor = vec4(BlinnPhong(V, N, L, baseColor, lightColor, adaptedLightFactors), texColor.a);
}