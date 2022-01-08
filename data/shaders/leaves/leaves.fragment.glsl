#ifdef GL_ES 
precision mediump float;
#endif

varying vec2 v_texCoord0;
varying vec4 norm;
varying vec4 vert;
varying vec4 lightV;
varying vec4 position;
uniform sampler2D u_leafTexture;
uniform int u_flipY;
uniform float u_alphaDiscard;
void main() {
	if (u_flipY == 1) {
		gl_FragColor = texture2D(u_leafTexture, vec2(v_texCoord0.x, -v_texCoord0.y));
	} else {
		gl_FragColor = texture2D(u_leafTexture, vec2(v_texCoord0.x, v_texCoord0.y));
	}
	if (gl_FragColor.a < u_alphaDiscard) {
		discard;
	}
	vec3 n = normalize(1.0-norm.xyz);
	vec3 l = normalize(-lightV.xyz);
	vec3 v = normalize(vert.xyz);
	vec3 h = normalize(l-n);
	float nDotl = max(0.0, dot(n, l));
	float lDotv = max(0.0, dot(l, v));
	//gl_FragColor.rgb *= nDotl+lDotv/2.0;
	float dist = gl_FragCoord.z / gl_FragCoord.w; 

	vec4 fogColor = vec4(1.0, 0.9, 0.8, 1.0);
	float fogEnd = 500.0;
	float fogStart = 100.0;
	float fogFactor = (fogEnd - dist)/(fogEnd - fogStart);
	fogFactor = clamp( fogFactor, 0.0, 1.0 );
	
	//gl_FragColor = mix(fogColor, gl_FragColor, fogFactor);
}