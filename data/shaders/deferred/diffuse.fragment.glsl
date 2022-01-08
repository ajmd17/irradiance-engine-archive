
uniform sampler2D u_diffuseTexture;
uniform vec3 m_region1;
uniform float m_slopeTileFactor;
varying vec2 v_texCoords0;
varying vec4 norm;
varying vec3 v_normal;
varying vec4 vert;
varying vec4 lightVec;
varying vec4 position;
varying vec4 v_depth;

uniform vec4 u_albedo;

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

		gl_FragColor = u_albedo;
		
		
		
		
}