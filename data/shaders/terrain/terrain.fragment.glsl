#ifdef GL_ES 
precision mediump float;
#endif

varying vec2 v_texCoord0;
varying vec4 norm;
varying vec4 vert;
varying vec4 lightV;
uniform int u_flipY;
uniform sampler2D u_normalMap;
uniform sampler2D u_diffuseTexture;
uniform vec3 u_lightColor;
uniform float u_alphaDiscard;
uniform float u_fogStart;
uniform float u_fogEnd;
uniform vec3 u_fogColor;
uniform int u_useNormalMap;
uniform vec3 u_cameraPosition;
varying vec3 v_worldNormal;
uniform vec4 u_albedo;
uniform int u_debugView;
varying vec4 wPos;
uniform vec4 u_ambient;

#ifdef GRASS_TEX
uniform sampler2D u_grassTex;
#endif
#ifdef SLOPE_TEX
uniform sampler2D u_slopeTex;
#endif
#ifdef REGIONS
uniform int u_region0;
uniform int u_region1;
uniform int u_region2;
uniform int u_region3;
uniform sampler2D u_region1Tex;
uniform sampler2D u_region2Tex;
uniform sampler2D u_region3Tex;
#endif
const float texScale = 10.0;
vec4 getTerrainCoords() {
	vec4 p = wPos / 256.;
	return p;
}
void main() {
	
	gl_FragColor = vec4(u_albedo);


	
	
	vec4 p = getTerrainCoords();
		
	vec3 n = normalize(norm.xyz);
	#ifdef GRASS_TEX
	gl_FragColor = texture2D(u_grassTex, p.xz*texScale);
	#endif
	
	#ifdef REGIONS 
	
	if (wPos.y > float(u_region0)) {
		// do nothing
		if (wPos.y > float(u_region1) && wPos.y < float(u_region2)) {
			gl_FragColor = texture2D(u_region1Tex, p.xz * texScale);
		} else if (wPos.y > float(region2) && wPos.y < float(u_region3)) {
			gl_FragColor = texture2D(u_region2Tex, p.xz * texScale);
		} else if (wPos.y > float(region3)) {
			gl_FragColor = texture2D(u_region3Tex, p.xz * texScale);
		}
	}
	
	#endif
	
	#ifdef SLOPE_TEX 
	gl_FragColor = mix(texture2D(u_slopeTex, p.xz*texScale), gl_FragColor, -n.y);
	#endif
	
	
	
	vec3 l = normalize(-lightV.xyz);
	vec3 v = normalize(vert.xyz);
	vec3 h = normalize(l-n);
	float nDotl = max(0.0, dot(n, l));
	float nDotv = max(0.0, dot(n, v));
	
	
	gl_FragColor *= vec4(nDotl)*vec4(u_lightColor, 1.0)+u_ambient;

	float dist = gl_FragCoord.z / gl_FragCoord.w; 

	float fogFactor = (u_fogEnd - dist)/(u_fogEnd - u_fogStart);
	fogFactor = clamp( fogFactor, 0.0, 1.0 );
	
	
	
	
	
	gl_FragColor = mix(vec4(u_fogColor, 1.0), gl_FragColor, fogFactor);
	
	
	
	//gl_FragColor.rgb = vec3(1.0, 0.0, 0.0);
}