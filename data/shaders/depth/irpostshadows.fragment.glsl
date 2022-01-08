#ifdef GL_ES 
precision mediump float;
#endif
uniform sampler2D m_slopeColorMap;
uniform sampler2D m_region1ColorMap;
uniform vec3 m_region1;
uniform float m_slopeTileFactor;
varying vec2 v_texCoord0;
varying vec4 norm;
varying vec4 vert;
varying vec4 lightVec;
varying vec4 position;
varying float v_depth;
uniform sampler2D u_diffuseTexture;

void main() {
	vec3 n = norm.xyz;
    gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	float dist = gl_FragCoord.z; 
    
	vec4 fogColor = vec4(1.0, 0.9, 0.8, 1.0);
	float fogEnd = 500.0;
	float fogStart = 100.0;
    float fogFactor = (fogEnd - dist)/(fogEnd - fogStart);
    fogFactor = clamp( fogFactor, 0.0, 1.0 );
	float depth = v_depth;
	vec4 bias = vec4(1.0 / 255.0, 1.0 / 255.0, 1.0 / 255.0, 0.0);
	vec4 color = vec4(depth, fract(depth * 255.0), fract(depth * 65025.0), fract(depth * 160581375.0));
    gl_FragColor = vec4(1.0);
}