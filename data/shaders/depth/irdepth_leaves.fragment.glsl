

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
varying vec4 v_depth;
uniform sampler2D u_diffuseTexture;
uniform int u_flipY;
uniform float u_alphaDiscard;
uniform int u_discard;
vec4 pack_depth(const in float depth){
    vec4 bit_shift =
        vec4(256.0*256.0*256.0, 256.0*256.0, 256.0, 1.0);
    vec4 bit_mask  =
        vec4(0.0, 1.0/256.0, 1.0/256.0, 1.0/256.0);
    vec4 res = fract(depth * bit_shift);
    res -= res.xxyz * bit_mask;
    return res;
}
void main() {
		if (u_discard == 1) {
			discard;
		}
		vec4 color;
		if (u_flipY == 1) {
		//vec4 color = texture2D(u_diffuseTexture, v_texCoord0);
		color = texture2D(u_diffuseTexture, vec2(v_texCoord0.x, v_texCoord0.y));
		} else {
		
		color = texture2D(u_diffuseTexture, vec2(v_texCoord0.x, - v_texCoord0.y));
		}
		
		if (color.a < u_alphaDiscard) {
			discard;
		}
		gl_FragColor = pack_depth(gl_FragCoord.z);
		
		
		
}