#version 150 core

uniform sampler2D textureData;

in vec4 passColor;
in vec2 passTexturePosition;

out vec4 fragColor;

vec3 BlinnPhong(vec3 V, vec3 N, vec3 L,
                vec3 color, vec3 lightColor) 
{
  vec3 h = normalize(V + L);
  float kd = max(0.0, dot(L, N)); // diffuse
  float ka = 0.0; //ambient
  float ks = 0.9 * pow(max(dot(N, h), 0.0), 30.0);

  return vec3(kd + ka) * color + ks * lightColor;
}

void main() {
   // fragColor = passColor;
    fragColor = texture(textureData, passTexturePosition);
}