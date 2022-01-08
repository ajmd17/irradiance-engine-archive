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
varying vec3 v_worldNormal;
varying vec4 wPos;
void lightComputeDir(in vec3 worldPos, in vec4 color, in vec4 position, out vec4 lightDir){
    float posLight = step(0.5, color.w);
    vec3 tempVec = position.xyz * sign(posLight - 0.5) - (worldPos * posLight);
    vec3 lightVec = tempVec;  
    float dist = length(tempVec);
    lightDir.w = clamp(1.0 - position.w * dist * posLight, 0.0, 1.0);
    lightDir.xyz = tempVec / vec3(dist);
}
void main() {
	
	norm = normalize(transpose(inverse(u_worldTrans))*vec4(a_normal, 1.0));
	v_worldNormal = normalize(inverse(transpose(u_projViewTrans)) * norm).xyz;
	//lightV = vec4(-0.2, -0.8, 0.0, 1.0);
	vec4 g_position = u_worldTrans * vec4(a_position, 1.0); 
	wPos = u_worldTrans * vec4(a_position, 1.0);
    v_texCoord0 = a_texCoord0;
    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);
	vert = normalize((inverse(u_viewTrans) * vec4(0.0, 0.0, 0.0, 1.0))-g_position);
	//lightComputeDir(a_position, vec4(1.0), vec4(u_lightDirection, 1.0), lightV);
	lightV = normalize(u_worldTrans*vec4(u_lightDirection, 1.0));
}