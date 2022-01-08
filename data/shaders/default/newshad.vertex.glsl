

attribute vec3 a_position;
uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;

#ifdef shadowMapFlag
uniform mat4 u_shadowMapProjViewTrans;
varying vec3 v_shadowMapUv;
#define separateAmbientFlag
#endif //shadowMapFlag


void main() {
	#ifdef textureFlag
		v_texCoords0 = a_texCoord0;
	#endif // textureFlag
	
	#if defined(colorFlag)
		v_color = a_color;
	#endif // colorFlag
		
	
	
	

	#ifdef skinningFlag
		vec4 pos = u_worldTrans * skinning * vec4(a_position, 1.0);
	#else
		vec4 pos = u_worldTrans * vec4(a_position, 1.0);
	#endif
		
	gl_Position = u_projViewTrans * pos;
		
	#ifdef shadowMapFlag
		vec4 spos = u_shadowMapProjViewTrans * pos;
		v_shadowMapUv.xy = (spos.xy / spos.w) * 0.5 + 0.5;
		v_shadowMapUv.z = min(spos.z * 0.5 + 0.5, 0.998);
	#endif //shadowMapFlag
	
	
}