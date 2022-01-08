 attribute vec3 a_position; 
 attribute vec3 a_normal; 
 attribute vec2 a_texCoord0; 
 uniform mat4 u_worldTrans; 
 varying vec2 v_texCoord0; 
 varying vec3 v_cubeMapUV; 
 uniform mat4 u_projViewTrans;
 uniform vec3 u_fresnel;
 uniform vec3 u_lightDirection;
 uniform vec3 u_cameraPosition;
 varying vec4 norm;
 varying vec4 wNorm;
 varying vec4 refVec;
 varying vec4 lightVec;
 varying vec4 vert;
 uniform float u_globalTime;
 void computeRef(in vec4 modelSpacePos){
        vec3 worldPos = (u_worldTrans * modelSpacePos).xyz;

        vec3 I = normalize( u_cameraPosition - worldPos  ).xyz;
        vec3 N = normalize( (u_worldTrans * vec4(a_normal, 0.0)).xyz );

        refVec.xyz = -reflect(I, N);
        refVec.w   = u_fresnel.x + u_fresnel.y * pow(1.0 + dot(I, N), u_fresnel.z);
}
 void main() { 
     v_texCoord0 = a_texCoord0;     
     vec4 g_position = u_worldTrans * vec4(a_position, 1.0); 
     v_cubeMapUV = normalize(a_position); 
     computeRef(vec4(a_position, 1.0));
     wNorm = normalize(inverse(transpose(u_worldTrans))*vec4(a_normal, 0.0));
     norm = normalize(vec4(a_normal, 1.0));
     lightVec = normalize(u_worldTrans * vec4(u_lightDirection, 1.0));
	 vec4 apos = vec4(a_position, 1.0);
     gl_Position = u_projViewTrans * u_worldTrans * apos; 
      vert = normalize((u_projViewTrans * vec4(0.0, 0.0, 0.0, 1.0))-g_position);
     //vert /= vert.w;
 } 
