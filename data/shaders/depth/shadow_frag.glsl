#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif


varying MED vec2 v_texCoords0;
uniform sampler2D u_diffuseTexture;
uniform float u_alphaTest;
uniform int u_useDiffuse;

uniform vec3 u_cameraPosition;
varying HIGH float v_depth;
uniform float near;
uniform float far;
varying vec4 v_pos;
void main() {
	//if (distance(v_pos.xyz, u_cameraPosition) < near) {
		//discard;
	//}
	
	
	if (u_useDiffuse == 1) {
		vec4 tex = texture2D(u_diffuseTexture, v_texCoords0);
			if (tex.r > 0.1 || tex.g > 0.1 || tex.b > 0.1) {
				if (tex.a < 0.1) {
					discard;
				}
			}
	}
	
		HIGH float depth = v_depth;
		const HIGH vec4 bias = vec4(1.0 / 255.0, 1.0 / 255.0, 1.0 / 255.0, 0.0);
		HIGH vec4 color = vec4(depth, fract(depth * 255.0), fract(depth * 65025.0), fract(depth * 160581375.0));
		gl_FragColor = color - (color.yzww * bias);
		
	
}

