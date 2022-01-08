#ifdef GL_ES 
precision mediump float; 
#endif 
uniform samplerCube u_environmentCubemap; 
varying vec2 v_texCoord0; 
varying vec3 v_cubeMapUV; 
varying vec4 refVec;
varying vec4 norm;
varying vec4 wNorm;
varying vec4 lightVec;
varying vec4 vert;
uniform float u_specularFactor;
uniform vec4 u_albedo;
uniform float u_roughness;
uniform sampler2D u_diffuseTexture;
uniform vec3 u_cameraPosition;
uniform float u_globalTime;
uniform float u_textureScale;
#ifdef NORMAL_MAP
uniform sampler2D u_normalMap;
#endif
const float PI = 3.14159265358979323846;
float F0 = .257;
float SchlickFresnel(float f0, float f90, float u)
{
    return f0 + (f90-f0) * pow(1.0-u, 5.0);
}

vec4 desaturate(vec4 color, float amount)
{
	vec4 gray = vec4(dot(vec4(0.2125,0.7154,0.0721, 1.0), color));
	return vec4(mix(color, gray, amount));
}

vec4 blurTexCube() {
	vec4 finTex;      
	float dist = gl_FragCoord.z/gl_FragCoord.w;
	if (dist < 100.0) {
		for (int x = -3; x < 3; x++) {
			for (int y = -3; y < 3; y++) {
				//vec4 texCoord = mix(refVec, refVec + vec4(x*0.015*u_roughness, 0.0, y*0.01*u_roughness, 0.0), clamp(1.0-dist, 0.3, 0.7));
					vec4 texCoord = refVec + vec4(x*0.0025, y*0.0025, y*0.0025, 0.0) ;
					
					finTex += vec4(textureCube(u_environmentCubemap, texCoord).rgb, 1.0);
			}
		}
		finTex /= 36.0;
		return finTex; 
	} else {
		return vec4(1.0);
	}
}
float sqr(float x)
{
	return x*x;
}
float NormalizedTrowbridgeReitz(float costhetaH, float w)
{
	float w2 = w*w;
	return w2 / (PI * sqr( costhetaH*costhetaH * (w2 - 1.0) + 1.0 ));
}

void main() {      
	float roughness = 0.1;
	float metallic = 0.4;
	vec4 lighting;
	vec2 newTexCoord = (v_texCoord0 * u_textureScale) + vec2(u_globalTime*0.03, u_globalTime*0.05);
	vec3 n;
	#ifndef NORMAL_MAP
	n = normalize(norm.xyz);
	#endif
	#ifdef NORMAL_MAP
	vec4 normalColor = texture2D(u_normalMap, newTexCoord);
	n = normalize((normalColor.rgb * 2.0)-1.0);
	#endif
	vec3 l = normalize(-lightVec.xyz);
	vec3 v = normalize(vert.xyz);
	vec3 h = normalize(v+l);
	
	float nDotl = max(0.0, dot(n, l));   
	float nDotv = max(0.0, dot(n, v));   
	float lDoth = max(0.0, dot(l, h));
	float nDoth = max(0.0, dot(n, h));
	
	
	gl_FragColor = mix(u_albedo, vec4(1.0), metallic);
	// range: 0.01-0.5
	float D = NormalizedTrowbridgeReitz(nDoth, roughness*0.5);
	float Ks = 1.0-(metallic * 0.5);
	float G = 1.0;
	float F = SchlickFresnel(metallic, 1.0, lDoth);
	
	
	
	
	
	lighting = vec4(nDotl);
	
	vec4 diffuseColor = u_albedo;
	lighting = nDotl;
	
	vec4 spec = vec4(max(D, 0.0));//vec4(pow(max((Ks*D*G*F), 0.0), 1.0/2.0));
	//diffuseColor = mix(lighting*diffuseColor, diffuseColor, 1.0-metallic);
	//diffuseColor *= lighting;
	#ifdef DIFFUSE_MAP
	diffuseColor *= texture2D(u_diffuseTexture, newTexCoord);
	#endif
	#ifdef ENV_MAP
	vec4 reflection = blurTexCube()*diffuseColor*(1.0-(roughness*metallic));
	//gl_FragColor = reflection;   
	diffuseColor = mix(reflection, diffuseColor, 1.0-metallic);
	spec *= reflection.r;
	#endif
	//diffuseColor = mix(diffuseColor, vec4(0.0, 0.0, 0.0, 1.0), metallic);
	gl_FragColor = vec4(diffuseColor.rgb + spec.rgb, 0.8);
	//float gamma = 2.2;
	//gl_FragColor.rgb = pow( gl_FragColor.rgb, vec3( 1.0 / gamma ) );
} 
