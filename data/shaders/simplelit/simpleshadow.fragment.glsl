
uniform sampler2D u_shadowMap0;
uniform sampler2D u_shadowMap1;
uniform sampler2D u_shadowMap2;
uniform sampler2D u_shadowMap3;


varying vec2 v_texCoord0;
varying vec4 norm;
varying vec4 vert;
varying vec4 lightV;
uniform int u_flipY;
uniform sampler2D u_normalMap;
uniform sampler2D u_leafTexture;
uniform vec3 u_lightColor;
uniform float u_alphaDiscard;
uniform float u_fogStart;
uniform float u_fogEnd;
uniform vec3 u_fogColor;
uniform int u_useNormalMap;
uniform vec4 u_splits;
uniform int renderSplits;
varying vec4 v_shadowMapUv0;
varying vec4 v_shadowMapUv1;
varying vec4 v_shadowMapUv2;
varying vec4 v_shadowMapUv3;

uniform vec3 u_lightPosition;
varying vec4 v_worldPosition;

uniform vec3 u_ranges;
varying float v_camDistance;
float random(vec4 seed4) {
	float dot_product = dot(seed4, vec4(12.9898,78.233,45.164,94.673));
    return fract(sin(dot_product) * 43758.5453);
}



float getShadowness(int idx, vec2 offset)
{	
	float result = 1.0;
	vec4 depth;
    const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 160581375.0);
	
	if (idx == 0) { // shadow map 0
		depth = texture2D(u_shadowMap0, v_shadowMapUv0.xy + offset);
		if (depth.z < 0.98) { // this test allows us to completely remove super far away objects.
			result = step(v_shadowMapUv0.z+0.001, dot(depth, bitShifts));
		}
	} else if (idx == 1) {// shadow map 1
		depth = texture2D(u_shadowMap1, v_shadowMapUv1.xy + offset);
		if (depth.z < 0.98) {
			result = step(v_shadowMapUv1.z+0.001, dot(depth, bitShifts));
		}
	} else if (idx == 2) {// shadow map 2
		depth = texture2D(u_shadowMap2, v_shadowMapUv2.xy + offset);
		if (depth.z < 0.98) {
			result = step(v_shadowMapUv2.z+0.001, dot(depth, bitShifts));
		}
	} else { // shadow map 3
		depth = texture2D(u_shadowMap3, v_shadowMapUv3.xy + offset);
		if (depth.z < 0.98) {
			result = step(v_shadowMapUv3.z+0.001, dot(depth, bitShifts));
		}
	}

	return result;
}

float getShadow(int idx) 
{
	vec2 pcf0 = vec2(0.0003, 0.0004);
	vec2 pcf1 = vec2(0.0002, -0.0001);
	vec2 pcf2 = vec2(-0.0001, -0.0006);
	vec2 pcf3 = vec2(-0.0001, 0.0005);

	float shadowVal = ( getShadowness(idx, vec2(0.0, 0.0)) + getShadowness(idx, pcf0) +
			getShadowness(idx, pcf1) +
			getShadowness(idx, pcf2) +
			getShadowness(idx, pcf3)) * 0.20;
	
	return shadowVal ;
}

void main() {

	if (gl_FragColor.a < u_alphaDiscard) {
		discard;
	}
	vec3 wPosNoY = vec3(v_worldPosition.x, 0.0, v_worldPosition.z);
	vec3 lPosNoY = vec3(u_lightPosition.x, 0.0, u_lightPosition.z);
	float dist = gl_FragCoord.z/gl_FragCoord.w;//distance(wPosNoY, lPosNoY); 
	int index = 0;
	vec3 splitColor = vec3(0.0);
	if (dist < u_ranges.x) {
		
			splitColor = vec3(0.0, 1.0, 0.0);
		
		
		index = 0;
	} else if (dist > u_ranges.x && dist < u_ranges.y){
		
		splitColor = vec3(1.0, 0.0, 0.0);
		index = 1;
	} else if (dist > u_ranges.y && dist < u_ranges.z){
		
		splitColor = vec3(0.0, 0.0, 1.0);
		
		index = 2;
	} else if (dist > u_ranges.z) {
		splitColor = vec3(1.0, 1.0, 0.0);
		index = 3;
	}
	float shadowCol = getShadow(index);
	//if (renderSplits == 1) {
		gl_FragColor = vec4(splitColor, 1.0);
	//} else {
		//gl_FragColor = vec4(1.0);
	//}
	gl_FragColor *= shadowCol;
	//gl_FragColor.a = 1.0-shadowCol;
	
		
	
		
	
	
}