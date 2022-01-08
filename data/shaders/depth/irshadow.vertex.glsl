

attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;
uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;
uniform sampler2D m_slopeColorMap;
uniform sampler2D m_region1ColorMap;
uniform vec3 m_region1;
uniform float m_slopeTileFactor;
varying vec4 vert;
varying vec2 v_texCoord0;
varying vec4 norm;
varying vec4 lightVec;
varying vec4 position;
varying vec4 ShadowCoord;
uniform mat4 u_lightMatrix;
uniform vec3 u_lightPosition;
varying vec4 v_position;
varying vec4 v_positionLightTrans;
uniform mat4 u_proj;
uniform mat4 u_vmInverse;
void lightComputeDir(in vec3 worldPos, in vec4 color, in vec4 position, out vec4 lightDir){
    float posLight = step(0.5, color.w);
    vec3 tempVec = position.xyz * sign(posLight - 0.5) - (worldPos * posLight);
    vec3 lightVec = tempVec;  
    float dist = length(tempVec);
    lightDir.w = clamp(1.0 - position.w * dist * posLight, 0.0, 1.0);
    lightDir.xyz = tempVec / vec3(dist);
}
void main() {
	mat4 biasMatrix = mat4(
    0.5, 0.0, 0.0, 0.0,
    0.0, 0.5, 0.0, 0.0,
    0.0, 0.0, 0.5, 0.0,
    0.5, 0.5, 0.5, 1.0
	);
	position = u_worldTrans * vec4(a_position, 0.0);
	norm = vec4(1.0);
	vert = vec4(a_position, 1.0);
    v_texCoord0 = a_texCoord0;
    
	v_position = u_worldTrans*vec4(a_position, 1.0);
	v_positionLightTrans = v_position;
	ShadowCoord = v_position * (u_lightMatrix)* biasMatrix ;//vec4(a_position, 1.0) * u_lightMatrix * biasMatrix;
	//ShadowCoord.xyz = (ShadowCoord.xyz/ShadowCoord.w) * 0.5 + 0.5 ;
	
	//ShadowCoord = u_lightMatrix * v_position * biasMatrix ;
	
	
	
	gl_Position = u_worldTrans*vec4(a_position, 1.0)*u_projViewTrans;// * u_lightMatrix;
	
	
	
	//lightComputeDir(a_position, vec4(1.0), vec4(0.0, 0.0, 0.0, 1.0), lightVec);
}