attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;
uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;


uniform float u_time;
uniform float u_windSpeed;
uniform float u_windAmount;
uniform float u_maxViewDistance;
uniform float u_fadeStart;
uniform vec3 u_cameraPosition;

varying vec4 vert;
varying vec2 v_texCoord0;
varying vec4 norm;
varying vec4 lightVec;
varying vec4 position;
uniform int u_yflipped;
varying float camDist;

void main() {
	
	position = u_worldTrans * vec4(a_position, 0.0);
	norm = vec4(1.0);
	vec3 pos = a_position;
	
	v_texCoord0 = a_texCoord0.xy;
	float time = u_time * u_windSpeed;
	float txY = 1.0-a_texCoord0.y;
	float angle = (time + pos.x*0.75) * 0.5;
    float angle_b = ((time*0.55*u_windSpeed));
    float a = sin(angle)*txY * u_windAmount;
    pos.xz += a;
	vec4 worldPos = vec4(pos, 1.0) * u_worldTrans;
	camDist = distance(u_cameraPosition.xz, worldPos.xz);
	
	
	//v_texCoord0.z = clamp((u_maxViewDistance - dist)/(u_fadeStart),0.0,1.0);
   
    gl_Position = u_projViewTrans * u_worldTrans * vec4(pos, 1.0);
	//lightComputeDir(a_position, vec4(1.0), vec4(0.0, 0.0, 0.0, 1.0), lightVec);
}