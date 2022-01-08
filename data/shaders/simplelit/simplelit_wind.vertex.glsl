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
uniform float u_time;
uniform float u_treeHeight;
uniform float u_windAmount;
uniform float u_windSpeed;
uniform vec3 u_lightDirection;
uniform mat4 u_normalMatrix;
void lightComputeDir(in vec3 worldPos, in vec4 color, in vec4 position, out vec4 lightDir){
    float posLight = step(0.5, color.w);
    vec3 tempVec = position.xyz * sign(posLight - 0.5) - (worldPos * posLight);
    vec3 lightVec = tempVec;  
    float dist = length(tempVec);
    lightDir.w = clamp(1.0 - position.w * dist * posLight, 0.0, 1.0);
    lightDir.xyz = tempVec / vec3(dist);
}
void main() {
	
	vec3 pos = a_position;
    float angle_a = ((u_time*u_windSpeed));
    float angle_b = ((u_time*0.35*u_windSpeed));
    float a = sin(angle_a)*(a_position.y/u_treeHeight) * u_windAmount;
    float b = sin(angle_b)*(a_position.y/u_treeHeight) * u_windAmount;
    pos.x += a;
    pos.z += b;

	norm = normalize(vec4(a_normal, 1.0));
	//lightV = vec4(-0.2, -0.8, 0.0, 1.0);
    v_texCoord0 = a_texCoord0;
    gl_Position = u_projViewTrans * u_worldTrans * vec4(pos, 1.0);
	vert = vec4(pos, 1.0);
	
	lightV = vec4(u_lightDirection, 1.0)*u_worldTrans;
	
}