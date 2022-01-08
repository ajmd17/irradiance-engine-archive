

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
varying vec4 v_depth;
uniform mat4 u_lightMatrix;
uniform mat4 u_projection;


uniform float u_time;
uniform float u_treeHeight;
uniform float u_windAmount;
uniform float u_windSpeed;

void main() {

	vec3 pos = a_position;
    float angle_a = ((u_time*u_windSpeed));
    float angle_b = ((u_time*0.35*u_windSpeed));
    float a = sin(angle_a)*(a_position.y/u_treeHeight) * u_windAmount;
    float b = sin(angle_b)*(a_position.y/u_treeHeight) * u_windAmount;
    pos.x += a;
    pos.z += b;

    v_texCoord0 = a_texCoord0;
    gl_Position = u_projViewTrans * u_worldTrans * vec4(pos, 1.0);
	

	
}