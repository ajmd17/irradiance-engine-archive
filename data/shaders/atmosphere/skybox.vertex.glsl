attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;
uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;

varying vec2 v_texCoord0;
varying vec3 v_cubeMapUV;


void main() {

    v_texCoord0 = a_texCoord0;
	vec4 g_position = u_worldTrans * vec4(a_position, 0.0);
	v_cubeMapUV = normalize(g_position.xyz);
    gl_Position = u_projViewTrans * g_position;
	
}