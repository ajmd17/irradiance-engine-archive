

#ifdef GL_ES 
precision mediump float;
#endif
uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;
uniform sampler2D u_depthMap;
uniform sampler2D m_region1ColorMap;
uniform sampler2DShadow u_shadowMap;
uniform vec3 m_region1;
uniform float m_slopeTileFactor;
varying vec2 v_texCoord0;
varying vec4 norm;
varying vec4 vert;
varying vec4 lightVec;
varying vec4 position;
varying vec4 ShadowCoord;
varying vec4 v_position;
varying vec4 v_positionLightTrans;
uniform vec3 u_lightPosition;
uniform mat4 u_lightMatrix;

float getShadowness(vec2 offset)
{
    const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 160581375.0);
    return step(ShadowCoord.z, dot(texture2D(u_depthMap, ShadowCoord.xy + offset), bitShifts));//+(1.0/255.0));	
}
float getShadow() 
{
	return (getShadowness(vec2(0,0)));
}
void main() {
	vec3 n = norm.xyz;
	mat4 biasMat = mat4(
    0.5, 0.0, 0.0, 0.0,
    0.0, 0.5, 0.0, 0.0,
    0.0, 0.0, 0.5, 0.0,
    0.5, 0.5, 0.5, 1.0
	);
    gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	float dist = texture( u_depthMap, ShadowCoord.xy ).r; 
    
	float visibility = 0.8;
	//float depth = texture2D(u_depthMap, v_texCoord0).r;
	
	
	//visibility = texture(u_depthMap, ShadowCoord.xy).r ;
	if ( texture( u_depthMap, ShadowCoord.xy ).z  <  ShadowCoord.z){
		visibility = 0.0;
	}
	//vec3 depth = (v_positionLightTrans.xyz/v_positionLightTrans.w)*0.5+0.5;
	
	float distanceFromLight = texture2D(u_depthMap,ShadowCoord.xy).z;
	
	
	/*vec4 finalColor = vec4(1.0);
	
	
	
	
	float len = length(v_position.xyz-u_lightPosition.xyz)/500.0;
	len = texture2D(u_depthMap, depth.st).a;*/
	//float visibility = 1.0;
	//if (len < v_positionLightTrans.z) {
	//visibility = 0.0;
	//}
    //finalColor.rgb = vec3(1.0 - len);    
    //gl_FragColor = finalColor; 
	//visibility = texture(u_depthMap, ShadowCoord.xyz).r / 2.0;
	//visibility += 0.5;
	
	
    gl_FragColor = vec4(1.0);
}