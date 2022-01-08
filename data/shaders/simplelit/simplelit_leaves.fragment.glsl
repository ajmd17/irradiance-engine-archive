#ifdef GL_ES 
precision mediump float;
#endif

varying vec2 v_texCoord0;
varying vec4 norm;
varying vec4 vert;
varying vec4 lightV;
uniform int u_flipY;
uniform sampler2D u_normalMap;
uniform sampler2D u_leafTexture;
uniform float u_alphaDiscard;
uniform float u_fogStart;
uniform float u_fogEnd;
uniform vec3 u_fogColor;
uniform vec3 u_lightColor;
void main() {
	
	bool flipY = false;
	#ifdef FLIP_Y
		flipY = true;
	#endif

	
	//#ifdef DIFFUSE_MAP

	
	
	//if (flipY) {
		gl_FragColor = texture2D(u_leafTexture, vec2(v_texCoord0.x, -v_texCoord0.y));
	//} else {
		//gl_FragColor = texture2D(u_leafTexture, vec2(v_texCoord0.x, v_texCoord0.y));
	//}
	
	if (gl_FragColor.a < u_alphaDiscard) {
		discard;
	}
	//#endif
	vec3 n = normalize(norm.xyz);
	vec3 l = normalize(-lightV.xyz);
	vec3 v = normalize(vert.xyz);
	vec3 h = normalize(l-n);
	float nDotl = max(0.0, dot(n, l));
	float lDotv = max(0.0, dot(l, v));
	//gl_FragColor.rgb *= clamp(vec3(lDotv), 0.0,1.0)*u_lightColor;

	float dist = gl_FragCoord.z / gl_FragCoord.w; 

	float fogFactor = (u_fogEnd - dist)/(u_fogEnd - u_fogStart);
	fogFactor = clamp( fogFactor, 0.0, 1.0 );
	
	
	//gl_FragColor = mix(vec4(u_fogColor, 1.0), gl_FragColor, fogFactor);
}