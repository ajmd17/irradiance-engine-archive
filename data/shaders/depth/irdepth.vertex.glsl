precision mediump float;

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
varying float v_depth;
uniform mat4 u_lightMatrix;
uniform mat4 u_projection;
uniform float u_near;
uniform float u_far;
void main() {

    v_texCoord0 = a_texCoord0;
	vec4 pos = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);
    gl_Position = pos;
	
	v_depth = pos * 0.5 + 0.5;//(-pos.z-u_near) / (u_far-u_near);//(1.0-gl_Position.z)/500.0;
	
}