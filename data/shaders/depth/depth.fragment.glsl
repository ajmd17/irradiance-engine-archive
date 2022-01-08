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




varying float v_depth;


void main() {
	
	
	
		float depth = v_depth;
		vec4 bias = vec4(1.0 / 255.0, 1.0 / 255.0, 1.0 / 255.0, 0.0);
		vec4 color = vec4(depth, fract(depth * 255.0), fract(depth * 65025.0), fract(depth * 160581375.0));
		gl_FragColor = texture2D(u_diffuseTexture, v_texCoords0) * color - (color.yzww * bias);
	
}