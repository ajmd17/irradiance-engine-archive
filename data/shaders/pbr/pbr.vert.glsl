 attribute vec3 a_position; 
 attribute vec3 a_normal; 
 attribute vec2 a_texCoord0;
 attribute vec3 a_tangent; 
 uniform mat4 u_worldTrans; 
 varying vec2 v_texCoord0; 
 varying vec3 v_cubeMapUV; 
 uniform mat4 u_projViewTrans;
 uniform vec3 u_fresnel;
 uniform vec3 u_lightDirection;
 uniform vec3 u_cameraPosition;
 
 varying vec4 v_worldNormal;
 varying vec3 v_worldPos;
 varying vec3 v_tangent;
 varying vec3 v_binormal;
 varying vec4 norm;
 varying vec4 wNorm;
 varying vec4 lightVec;
 varying vec4 vert;
 uniform mat4 u_viewTrans;
 void findTangents(vec3 n) {


	vec3 c1 = cross( n, vec3(0.0, 0.0, 1.0) ); 
	vec3 c2 = cross( n, vec3(0.0, 1.0, 0.0) ); 

	if( length(c1)>length(c2) )
	{
		v_tangent = c1;	
	}
	else
	{
		v_tangent = c2;	
	}
	v_tangent = normalize(vec4(inverse(transpose(u_worldTrans))*vec4(v_tangent, 0.0)).xyz);
	
 }
 void main() { 
     v_texCoord0 = a_texCoord0;     
	 
     vec4 g_position = u_worldTrans * vec4(a_position, 1.0); 
     v_cubeMapUV = normalize(a_position); 
     
     wNorm = normalize(inverse(transpose(u_worldTrans))*vec4(a_normal, 0.0));
	 
     norm = normalize(inverse(transpose(u_worldTrans))*vec4(a_normal, 0.0));
	 findTangents(a_normal);
	 v_binormal = vec3(0., norm.z, -norm.y);//normalize(cross(norm.xyz, v_tangent.xyz));
	 v_tangent = cross(norm.xyz, v_binormal);
	 //v_tangent = normalize(v_tangent - norm.xyz * dot(norm.xyz, v_tangent));
	 vec3 lightDirection = vec4(vec4(u_lightDirection, 0.0)).xyz;
	 lightVec = vec4(lightDirection, 0.0);
	 
     lightVec.x = dot(lightDirection, v_tangent);
	 lightVec.y = dot(lightDirection, v_binormal);
	 lightVec.z = dot(lightDirection, norm);
	 lightVec.w = 0.0;
	 lightVec = normalize(vec4(u_lightDirection, 0.0));
     gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0); 
	// vert = vec4(TBNMatrix * vec4((inverse(u_viewTrans) * vec4(0.0, 0.0, 0.0, 1.0))).xyz, 1.0);
     vert = normalize((inverse(u_viewTrans) * vec4(0.0, 0.0, 0.0, 1.0))-g_position);
	 

	/*\ vert.x = dot(vert.xyz, v_tangent);
	 vert.y = dot(vert.xyz, v_binormal);
	 vert.z = dot(vert.xyz, norm);
	 vert.w = 1.0;*/
	 
	 v_worldNormal = u_worldTrans * vec4(a_normal, 0.0);
	 v_worldPos = u_worldTrans * vec4(a_position, 1.0); 
     //vert /= vert.w;
 } 
