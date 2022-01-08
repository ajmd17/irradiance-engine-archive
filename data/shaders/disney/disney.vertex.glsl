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
uniform vec3 u_cameraPosition;
uniform vec3 u_fresnel;
uniform vec3 u_camDir;
varying vec4 wLightV;
varying vec4 vertex;
varying vec3 eyeSpaceTangent;
varying vec3 eyeSpaceBitangent;
#ifdef ENV_MAP
varying vec4 refVec;
void computeRef(in vec4 modelSpacePos){
    vec3 worldPos = (u_worldTrans * modelSpacePos).xyz;
    vec3 I = normalize( u_cameraPosition - worldPos  ).xyz;
    vec3 N = normalize( (u_worldTrans * vec4(a_normal, 0.0)).xyz );
    refVec.xyz = -reflect(I, N);
    refVec.w   = u_fresnel.x + u_fresnel.y * pow(1.0 + dot(I, N), u_fresnel.z);
}
#endif
void lightComputeDir(in vec3 worldPos, in vec4 color, in vec4 position, out vec4 lightDir){
    float posLight = step(0.5, color.w);
    vec3 tempVec = position.xyz * sign(posLight - 0.5) - (worldPos * posLight);
    vec3 lightVec = tempVec;  
    float dist = length(tempVec);
    lightDir.w = clamp(1.0 - position.w * dist * posLight, 0.0, 1.0);
    lightDir.xyz = tempVec / vec3(dist);
}
void computeTangentVectors( vec3 inVec, out vec3 uVec, out vec3 vVec )
{
    uVec = abs(inVec.x) < 0.999 ? vec3(1,0,0) : vec3(0,1,0);
    uVec = normalize(cross(inVec, uVec));
    vVec = normalize(cross(inVec, uVec));
}
void main() {
	vec4 eyeSpaceVert;
    
	norm = normalize(vec4(a_normal,1.0));
	//computeTangentVectors( eyeSpaceNormal, eyeSpaceTangent, eyeSpaceBitangent );
	vertex = u_worldTrans * vec4(a_position, 1.0);
	v_worldNormal = normalize(inverse(transpose(u_projViewTrans)) * norm).xyz;
	//lightV = vec4(-0.2, -0.8, 0.0, 1.0);
    v_texCoord0 = a_texCoord0;
    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);
	vert = normalize(vec4(u_cameraPosition, 1.0));//normalize((inverse(u_viewTrans)*vec4(0.0, 0.0, 0.0, 1.0)) - (u_worldTrans * vec4(a_position, 1.0)));//normalize((transpose(u_worldTrans))*vec4(a_position, 1.0));
	//lightComputeDir(a_position, vec4(1.0), vec4(u_lightDirection, 1.0), lightV);
	wLightV = normalize(inverse(transpose(u_worldTrans)) * vec4(u_lightDirection, 1.0));
	lightV = normalize(vec4(u_lightDirection, 1.0));
	#ifdef ENV_MAP
	computeRef(vec4(a_position, 1.0));
	#endif
}