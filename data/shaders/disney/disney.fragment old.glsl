varying vec4 norm;
varying vec4 lightV;
varying vec4 vert;
varying vec2 v_texCoord0;
uniform vec4 u_albedo;
varying vec4 wLightV;
#ifdef DIFFUSE_MAP
uniform sampler2D u_diffuseTexture;
#endif
#ifdef ENV_MAP
uniform samplerCube u_environmentCubemap;
varying vec4 refVec;
#endif
float Kd = .1;
float Ks = .5;
float specwidth = 0.02;
float F0 = 0.01;
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
	float NdotL = dot(N.xyz,L.xyz);
    float NdotV = dot(N.xyz,V.xyz);
    vec4 H = normalize(L+V);
    float NdotH = dot(N.xyz,H.xyz);
    float LdotH = dot(L.xyz,H.xyz);
	float VdotH = dot(V.xyz, H.xyz);
	
	float RdotV =  max(0.0, dot(R, V));
	vec3 reflectDir = reflect(lightV, N);
    float specAngle = max(dot(reflectDir, V), 0.0);
    float specular = pow(dot(R, V), 55.0);
	
	float D;
	float F = SchlickFresnel(F0, 1, LdotH);
	float G;
	
    
	vec3 f0 = vec3(0.971519,0.959915,0.915324);
	float specPower = (0.2);
	
    specPower *= specPower;
	float phongSpec = pow(RdotV,specPower);
	float blinnPhongSpec = pow(NdotH,specPower);
    float phongNormFactor = (specPower + 2.0) / (2.0 * PI);
	float blinnPhongNormFactor = (specPower + 2.0) / (2.0 );
	D = GGX(NdotH, 0.01);
    G = NdotL * NdotV;
	vec3 ambient = 0.20 * u_albedo.rgb;
	vec3 diffuseColor = u_albedo.rgb;
    vec3 specularColor = vec3(1.0,1.0,1.0)-diffuseColor.rrr;
	float metalness = 0.2;
    metalness = max (metalness,0.);
    metalness = min (metalness,1.);
    diffuseColor = mix(u_albedo.rgb, vec3(0,0,0), metalness);
    ambient = 0.20 * diffuseColor;
    vec3 minf0 = vec3(0.04,0.04,0.04);
    f0 = mix(minf0, f0, metalness);
    //F = f0 + (1.0 - f0) * pow( (1.0 - LdotH), 5.0);
    vec3 specBRDF = (F * G * D) / ((4.0 * NdotL * NdotV) + 0.001);
    vec3 blinnPhongMetalness = diffuseColor * NdotL + f0 * specBRDF;
	
	//gl_FragColor *= vec4(NdotL); //vec4(blinnPhongMetalness, 1.0);
	//gl_FragColor = clamp(vec4(NdotL*NdotV), 0.0, 1.0);
	gl_FragColor *= vec4(NdotL);
	if (NdotL >= 0.0) {
		gl_FragColor += clamp(vec4(specular*NdotL), 0.0, 1.0);
	}
	
	float gamma = 1.3;
	gl_FragColor.rgb = pow( gl_FragColor.rgb, vec3( 1.0 / gamma ) );
	
}