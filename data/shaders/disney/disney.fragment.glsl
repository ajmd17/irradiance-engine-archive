varying vec4 norm;
varying vec4 lightV;
varying vec4 vert;
varying vec2 v_texCoord0;
uniform vec4 u_albedo;
varying vec4 wLightV;
varying vec3 eyeSpaceTangent;
varying vec3 eyeSpaceBitangent;
#ifdef DIFFUSE_MAP
uniform sampler2D u_diffuseTexture;
#endif
#ifdef ENV_MAP
uniform samplerCube u_environmentCubemap;
varying vec4 refVec;
#endif
float Kd = .1;
float Ks = .9;
float specwidth = 0.2;
float F0 = 0.094;
float kurtAlpha = 0.5;
const float PI = 3.14159265358979323846;

#ifdef ENV_MAP
vec4 envMap() {
	vec4 finTex;
	vec4 texCoord = refVec;
	finTex = vec4(textureCube(u_environmentCubemap, texCoord));
	return finTex;
}
#endif

float Beckmann(float t, float m)
{
    float M = m*m;
    float T = t*t;
    return exp((T-1)/(M*T)) / (PI*M*T*T);
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
float SchlickFresnel(float f0, float f90, float u)
{
    return f0 + (f90-f0) * pow(1-u, 5);
}
float geometricShadowingSchlickBeckmann(float NdotV, float k)
{
	return NdotV / (NdotV * (1.0 - k) + k);
}
float GGX(float NdotH, float alphaG)
{
    return alphaG*alphaG / (PI * sqr(NdotH*NdotH*(alphaG*alphaG-1.0) + 1.0));
}
float geometricShadowingSmith(float NdotL, float NdotV, float k)
{
	return geometricShadowingSchlickBeckmann(NdotL, k) * geometricShadowingSchlickBeckmann(NdotV, k);
}
float Gaussian(float c, float thetaH)
{
    return exp(-thetaH*thetaH/(c*c));
}
void main() {
	vec4 N = normalize(norm);
	vec4 L = normalize(-lightV);
	vec4 V = normalize(vert);
	vec3 R = reflect(-normalize(wLightV),N);
	gl_FragColor = u_albedo;
	#ifdef ENV_MAP
		gl_FragColor *= envMap();
	#endif
	#ifdef DIFFUSE_MAP
		gl_FragColor *= texture2D(u_diffuseTexture, v_texCoord0);
	#endif
	float NdotL = max(0.1,dot(N.xyz,L.xyz));
    float NdotV = dot(N.xyz,V.xyz);
    vec4 H = normalize(-normalize(wLightV)+V);
    float NdotH = (N,H);
    float LdotH = dot(L.xyz,H.xyz);
	float VdotH = dot(V.xyz, H.xyz);
	
	float RdotV =  max(0.0, dot(R, V));
	vec3 reflectDir = reflect(lightV, N);
    float specAngle = max(dot(reflectDir, V), 0.0);
	
	float specPower = 25.0;
	specPower *= specPower;
	float specular = pow(dot(R, V), specPower);
	
    float phongNormFactor = (specPower + 2.0) / (2.0 * PI);
	float blinnPhongNormFactor = (specPower + 2.0) / (2.0);
	
    
	
	float D = NormalizedTrowbridgeReitz(NdotH, specwidth);
	float F = SchlickFresnel(F0, 1, LdotH);
	float G;
	
    
	
	
	//gl_FragColor *= vec4(NdotL);
	//gl_FragColor += clamp(vec4(specular*blinnPhongNormFactor)*NdotL*u_albedo, 0.0, 0.4);
	//gl_FragColor = vec4(specular);
	
	//gl_FragColor = vec4(D*10.0);
	float gamma = 2.2;
	gl_FragColor.rgb = pow( gl_FragColor.rgb, vec3( 1.0 / gamma ) );
	
}