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
void main() {
	
	gl_FragColor = vec4(u_albedo);
	bool flipY = false;
	#ifdef FLIP_Y
		flipY = true;
	#endif

	
	#ifdef DIFFUSE_MAP
	vec4 tex;
	if (flipY) {
		tex = texture2D(u_diffuseTexture, vec2(v_texCoord0.x, -v_texCoord0.y));
	} else {
		tex = texture2D(u_diffuseTexture, vec2(v_texCoord0.x, v_texCoord0.y));
	}
	
	
	if (tex.a < u_alphaDiscard) {
		discard;
	}
	gl_FragColor = tex;
	#endif
	vec3 n = normalize(norm.xyz);
	vec3 nBefore = n;
	
	
	#ifdef NORMAL_MAP
		vec4 normalColor = texture2D(u_normalMap, v_texCoord0);
		n = normalize((normalColor.rgb * 2.0)-1.0);

	#endif
	
	vec3 l = normalize(-lightV.xyz);
	vec3 v = normalize(vert.xyz);
	vec3 h = normalize(l-n);
	float nDotl = max(0.0, dot(n, l));
	//float nDotv = max(0.0, dot(nBefore, v));
	
	
	
	
	
	float avg = nDotl;
	//avg /= 2.0;

	float dist = gl_FragCoord.z / gl_FragCoord.w; 
	float fogFactor = (u_fogEnd - dist)/(u_fogEnd - u_fogStart);
	fogFactor = clamp( fogFactor, 0.0, 1.0 );
	
	
	#ifdef SHININESS
	vec3 reflectDir = reflect(lightV, nBefore);
    float specAngle = max(dot(reflectDir, v), 0.0);
    float specular = pow(dot(nBefore, h), 20.0);
	#endif
	
	#ifdef SHININESS
	avg += clamp(specular, 0.0, 1.0);
	
	#endif
	
	#ifndef UNSHADED
	gl_FragColor.rgb *= vec3(avg)*u_lightColor;
	#endif
	
	
	gl_FragColor = mix(vec4(u_fogColor, 1.0), gl_FragColor, fogFactor);
	
	#ifdef TURN_RED
		gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	#endif
	#ifdef TURN_BLUE
		//gl_FragColor *= vec4(0.0, 0.0, 1.0, 1.0);
	#endif
	if (u_debugView == 1) {
		gl_FragColor *= vec4(0.5, 0.7, 0.9, 1.0);
	} else if (u_debugView == 0) {
		
	}
	
	//gl_FragColor.rgb = vec3(1.0, 0.0, 0.0);
}