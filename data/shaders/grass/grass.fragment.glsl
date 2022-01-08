#ifdef GL_ES 
precision mediump float;
#endif
uniform sampler2D u_texture;
uniform vec3 m_region1;
uniform float m_slopeTileFactor;
varying vec2 v_texCoord0;
varying vec4 norm;
varying vec4 vert;
varying vec4 lightVec;
varying vec4 position;
uniform int u_yflipped;
uniform float u_alphaDiscard;
uniform float u_fogStart;
uniform float u_fogEnd;
uniform vec3 u_fogColor;
uniform float u_maxViewDistance;
uniform float u_fadeStart;
uniform sampler2D u_noiseMap;
uniform vec3 u_lightColor;
varying float camDist;
void main() {
	float dist = gl_FragCoord.z/gl_FragCoord.w; 
	float fadeFactor = (u_maxViewDistance - dist)/(u_maxViewDistance - u_fadeStart);
	/*if (fadeFactor < texture2D(u_noiseMap, v_texCoord0.xy).r){
        discard;
    }*/
	
	
	gl_FragColor = texture2D(u_texture, v_texCoord0.xy);
	if (gl_FragColor.a < u_alphaDiscard) {
		discard;
	}
	
	//vec3 n = norm.xyz;
	
    
	
	
	gl_FragColor.rgb *= u_lightColor;
	
    float fogFactor = (u_fogEnd - dist)/(u_fogEnd - u_fogStart);
    fogFactor = clamp( fogFactor, 0.0, 1.0 );
   
    gl_FragColor = mix(vec4(u_fogColor, 1.0), gl_FragColor, fogFactor);
	gl_FragColor.a *= clamp(fadeFactor, 0.0, 1.0);
}