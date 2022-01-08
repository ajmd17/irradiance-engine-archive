attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;
uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;
uniform mat4 u_viewTrans;
varying vec4 vert;
varying vec2 v_texCoord0;
varying vec4 norm;
varying vec4 lightV;
attribute vec3 inNormal;
uniform vec3 u_lightDirection;


varying vec4 v_worldPosition;

varying vec4 v_shadowMapUv0;
varying vec4 v_shadowMapUv1;
varying vec4 v_shadowMapUv2;
varying vec4 v_shadowMapUv3;

varying mat4 biasMatrix;
uniform vec3 u_lightPosition;

uniform mat4 u_shadowMapProjViewTrans0;
uniform mat4 u_shadowMapProjViewTrans1;
uniform mat4 u_shadowMapProjViewTrans2;
uniform mat4 u_shadowMapProjViewTrans3;

varying float v_camDistance;
void main() {
	
	
    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);

	//lightComputeDir(a_position, vec4(1.0), vec4(u_lightDirection, 1.0), lightV);
	
	vec3 wPosNoY = vec3(v_worldPosition.x, 0.0, v_worldPosition.z);
	vec3 lPosNoY = vec3(u_lightPosition.x, 0.0, u_lightPosition.z);
	v_camDistance = distance(wPosNoY, lPosNoY);
	
	v_worldPosition = u_worldTrans * vec4(a_position, 1.0);
	
	vec4 spos0 = u_shadowMapProjViewTrans0 * u_worldTrans * vec4(a_position, 1.0);
	v_shadowMapUv0 = spos0;
	v_shadowMapUv0 *=0.5;
	v_shadowMapUv0 += 0.5;
	//v_shadowMapUv0.xyz /= v_shadowMapUv0.w;
	
	vec4 spos1 = u_shadowMapProjViewTrans1 * u_worldTrans * vec4(a_position, 1.0);
	v_shadowMapUv1 = spos1;
	v_shadowMapUv1 *=0.5;
	v_shadowMapUv1 += 0.5;
	//v_shadowMapUv1.xyz /= v_shadowMapUv1.w;
	
	vec4 spos2 = u_shadowMapProjViewTrans2 * u_worldTrans * vec4(a_position, 1.0);
	v_shadowMapUv2 = spos2;
	v_shadowMapUv2 *=0.5;
	v_shadowMapUv2 += 0.5;
	//v_shadowMapUv2.xyz /= v_shadowMapUv2.w;
	
	vec4 spos3 = u_shadowMapProjViewTrans3 * u_worldTrans * vec4(a_position, 1.0);
	v_shadowMapUv3 = spos3;
	v_shadowMapUv3 *=0.5;
	v_shadowMapUv3 += 0.5;
	//v_shadowMapUv3.xyz /= v_shadowMapUv3.w;
	
	//v_shadowMapUv.z = min(spos.z * 0.5 + 0.5, 0.998);
	
}