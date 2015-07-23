#version 330 core

uniform sampler2DArray textureData;
uniform sampler2D shadowMap;

uniform vec3 cameraPosition;

uniform vec3 lightPosition;
uniform vec3 lightColor;
uniform vec3 lightFactors;

uniform bool colorSwitch;
uniform bool aoSwitch;

in vec4 passColor;
in vec3 passTexturePosition;
in vec4 fragPosLightSpace;
in vec3 normal;
in vec3 position;

in float ambiantOcc;

out vec4 fragColor;

float ShadowCalculation(vec4 fragPosLightSpace)
{
    // perform perspective divide
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    // Transform to [0,1] range
    projCoords = projCoords * 0.5 + 0.5;
    // Get closest depth value from light's perspective (using [0,1] range fragPosLight as coords)
    float closestDepth = texture(shadowMap, projCoords.xy).r;
    // Get depth of current fragment from light's perspective
    float currentDepth = projCoords.z;
    // Calculate bias (based on depth map resolution and slope)
    vec3 normal = normalize(normal);
    vec3 lightDir = normalize(lightPosition - position);
    float bias = max(0.05 * (1.0 - dot(normal, lightDir)), 0.005);
    // Check whether current frag pos is in shadow
     float shadow = currentDepth - bias > closestDepth  ? 1.0 : 0.0;
    // PCF
//    float shadow = 0.0;
//    vec2 texelSize = 1.0 / textureSize(shadowMap, 0);
//    for(int x = 0; x <= 0; ++x)
//    {
//        for(int y = 0; y <= 0; ++y)
//        {
//            float pcfDepth = texture(shadowMap, projCoords.xy + vec2(x, y) * texelSize).r;
//            shadow += currentDepth - bias > pcfDepth  ? 1.0 : 0.0;
//        }
//    }
//    shadow /= 1.0;

    // Keep the shadow at 0.0 when outside the far_plane region of the light's frustum.
    if(projCoords.z > 1.0)
        shadow = 0.0;

    return shadow;
}


vec3 BlinnPhong(vec3 V, vec3 N, vec3 L, vec3 color, vec3 lightColor, vec3 lightFactors, float shadow){
	vec3 h = normalize(V + L);
	float ka = lightFactors.x;										// ambient
	float kd = lightFactors.y * max(0.0, dot(L, N));				// diffuse
	float ks = lightFactors.z * pow(max(dot(N, h), 0.0), 11.0);		// specular

	return vec3(ka-shadow) * color + vec3(kd) * color + ks * lightColor;
}



float norm(float x){
return x/3.0;
}

void main() {
	vec3 V = normalize(cameraPosition - position);
	vec3 N = normal;
	vec3 L = normalize(lightPosition - position);

	vec4 texColor = texture(textureData, vec3(passTexturePosition.xy, passTexturePosition.z+0.25));

	vec3 baseColor = vec3(texColor.rgb);
	if(colorSwitch){
		baseColor = vec3(passColor.rgb);
    }

	// dirty workaround, water is only transparent surface and should have specular reflections
	vec3 adaptedLightFactors = lightFactors;
	if(texColor.a == 1.0){
		adaptedLightFactors.z = 0;
	}

	if(aoSwitch){
			adaptedLightFactors.x = 0.25 + 0.75*(adaptedLightFactors.x * norm(ambiantOcc));
	}


//	float shadow = ShadowCalculation(fragPosLightSpace);
//
//
//
//	vec3 lighting = BlinnPhong(V, N, L, baseColor, lightColor, adaptedLightFactors, shadow);
        // Ambient
        float ambient = adaptedLightFactors.x;
        // Diffuse
        vec3 lightDir = normalize(lightPosition - position);
        float diff = max(dot(lightDir, normal), 0.0);
        vec3 diffuse = diff * lightColor;
        // Specular
        vec3 viewDir = normalize(cameraPosition - position);
        vec3 reflectDir = reflect(-lightDir, normal);

        vec3 halfwayDir = normalize(lightDir + viewDir);
        float spec  =  adaptedLightFactors.z* pow(max(dot(normal, halfwayDir), 0.0), 32.0);
        vec3 specular =  spec * lightColor;
        // Calculate shadow
        float shadow =  ShadowCalculation(fragPosLightSpace);
        vec3 lighting = (ambient + (0.75 - shadow) * (diffuse + specular)) * baseColor;

	fragColor = vec4(lighting, texColor.a);
}