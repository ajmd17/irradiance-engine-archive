#ifdef GL_ES 
precision mediump float; 
#endif 
uniform samplerCube u_environmentCubemap; 
varying vec2 v_texCoord0; 
varying vec3 v_cubeMapUV; 
uniform vec3 u_lightColor;
varying vec4 norm;
varying vec4 wNorm;
varying vec4 lightVec;
varying vec4 vert;
varying vec3 v_tangent;
varying vec3 v_binormal;
varying vec4 v_worldNormal;
varying vec3 v_worldPos;

uniform vec3 u_fresnel;
uniform float u_specularFactor;
uniform vec4 u_albedo;
uniform float u_roughness;
uniform float u_metallic;
uniform sampler2D u_diffuseTexture;
uniform vec3 u_cameraPosition;
uniform float u_F0;
#ifdef NOISE_MAP
uniform sampler2D u_noiseMap;
uniform float u_grimeAmount;
#endif
#ifdef ENV_MAP
uniform float u_envMapBound;
uniform vec3 u_envMapPos;
#endif
#ifdef NORMAL_MAP
uniform sampler2D u_normalMap;
#endif NORMAL_MAP

vec4 refVec;

const float PI = 3.14159265358979323846;
float F0 = .257;
float SchlickFresnel(float f0, float f90, float u)
{
    return f0 + (f90-f0) * pow(1.0-u, 5.0);
}
#ifdef ENV_MAP
vec3 LocalCorrect(vec3 origVec, vec3 bboxMin, vec3 bboxMax, vec3 vertexPos, vec3 cubemapPos)
{
    // Find the ray intersection with box plane
    vec3 invOrigVec = vec3(1.0,1.0,1.0)/origVec;
    vec3 intersecAtMaxPlane = (bboxMax - vertexPos) * invOrigVec;
    vec3 intersecAtMinPlane = (bboxMin - vertexPos) * invOrigVec;
    // Get the largest intersection values (we are not intersted in negative values)
    vec3 largestIntersec = max(intersecAtMaxPlane, intersecAtMinPlane);
    // Get the closest of all solutions
   float Distance = min(min(largestIntersec.x, largestIntersec.y), largestIntersec.z);
    // Get the intersection position
    vec3 IntersectPositionWS = vertexPos + origVec * Distance;
    // Get corrected vector
    vec3 localCorrectedVec = IntersectPositionWS - cubemapPos;
    return localCorrectedVec;
}
#endif
vec4 desaturate(vec4 color, float amount)
{
	vec4 gray = vec4(dot(vec4(0.2125,0.7154,0.0721, 1.0), color));
	return vec4(mix(color, gray, amount));
}
float cookTorranceG(float NdotL, float NdotV, float LdotH, float NdotH)
{
    return min(1, 2 * (NdotH / LdotH) * min(NdotL, NdotV));
}
#ifdef ENV_MAP
vec4 blurTexCube() {
	vec4 finTex;      
	float dist = gl_FragCoord.z/gl_FragCoord.w;
	
		/*for (int x = -1; x < 1; x++) {
			for (int y = -1; y < 1; y++) {
				for (int z = -1; z < 1; z++) {*/
					vec4 texCoord;
					if (u_envMapBound > 0.0) {
						texCoord = vec4(LocalCorrect(refVec.xyz, vec3(-u_envMapBound), vec3(u_envMapBound), v_worldPos.xyz, u_envMapPos),1.0);
					} else {
						texCoord = refVec;
					}//refVec + vec4(x*0.007, y*0.005, z*0.009, 0.0);
					finTex += vec4(textureCube(u_environmentCubemap, texCoord).rgb, 1.0);
				/*}
				//vec4 texCoord = mix(refVec, refVec + vec4(x*0.015*u_roughness, 0.0, y*0.01*u_roughness, 0.0), clamp(1.0-dist, 0.3, 0.7));
					
			}
		}*/
		//finTex /= 8.0;
		return finTex; 
	
}
#endif
float sqr(float x)
{
	return x*x;
}
float NormalizedTrowbridgeReitz(float costhetaH, float w)
{
	float w2 = w*w;
	return w2 / (PI * sqr( costhetaH*costhetaH * (w2 - 1.0) + 1.0 ));
}
void computeRef(vec4 wNorm){
        

        vec3 I = normalize( u_cameraPosition - v_worldPos  ).xyz;
        vec3 N = normalize( wNorm.xyz );

        refVec.xyz = -reflect(I, N);
        refVec.w   = u_fresnel.x + u_fresnel.y * pow(1.0 + dot(I, N), u_fresnel.z);
}
float duerG(vec3 L, vec3 V, vec3 N)
{
    vec3 LplusV = L + V;
    return dot(LplusV,LplusV) / (PI * pow(dot(LplusV,N), 4));
}

void main() {      
	float roughness = u_roughness;
	float metallic = u_metallic;
	vec4 lighting;
	vec3 n;
	
	
	
	#ifndef NORMAL_MAP
		computeRef(norm);
		n = normalize(norm.xyz);
	#endif
	#ifdef NORMAL_MAP
		
		vec4 normalColor = texture2D(u_normalMap, v_texCoord0);
		n = normalize((normalColor.rgb * 2.0)-1.0);
		computeRef(vec4(n, 1.0));
	#endif

	vec3 l = normalize(-lightVec.xyz);
	vec3 v = normalize(vert.xyz);
	vec3 h = normalize(v+l);
	
	float nDotl = max(dot(n, l), 0.0);   
	float nDotv = max(dot(n, v), 0.0);   
	float lDoth = max(dot(l, h), 0.0);
	float nDoth = max(dot(n, h), 0.0);
	
	
	vec4 diffuseColor = mix(u_albedo, vec4(1.0), metallic);
	// range: 0.01-0.5
	float D = NormalizedTrowbridgeReitz(nDoth, roughness);
	float Ks = 1.0-(metallic * 0.5);
	float G = cookTorranceG(nDotl, nDotv, lDoth, nDoth);//duerG(l, v, h);
	float F = SchlickFresnel(u_F0, 1.0, lDoth);
	
	
	
	
	
	lighting = vec4(nDotl);
	lighting.rgb *= u_lightColor;
	#ifdef DIFFUSE_MAP
	diffuseColor *= texture2D(u_diffuseTexture, v_texCoord0);
	#endif
	vec4 spec = vec4(max((Ks*D*G*F), 0.0))/2.;//vec4(pow(max((Ks*D*G*F), 0.0), 1.0/2.0));
	diffuseColor *= mix(lighting, diffuseColor, clamp(1.0-roughness, 0.0, 1.0));
	vec4 noise = vec4(1.0);
	#ifdef NOISE_MAP
	noise = mix(vec4(1.0), texture2D(u_noiseMap, v_texCoord0), u_grimeAmount);
	#endif
	
	
	#ifdef ENV_MAP
	vec4 reflection = blurTexCube()*F;
	reflection = mix(diffuseColor, reflection, metallic)*clamp(noise.r, 0.0, 1.0);
	//gl_FragColor = reflection;   
	//reflection *= spec;
	diffuseColor = mix(reflection, diffuseColor,roughness);
	
	#endif
	spec *= noise;
	spec.rgb *= u_lightColor;
	gl_FragColor = /*vec4(v_tangent, 1.0);*/clamp(diffuseColor + spec, 0.0, 1.0);
	
} 
