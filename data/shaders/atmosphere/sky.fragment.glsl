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
uniform sampler2D m_StarMap;

varying vec3 vBackDirection;
varying vec4 vRayleighColor;
varying vec4 vMieColor; 
uniform float m_PhasePrefix1;
uniform float m_PhasePrefix2;
uniform float m_PhasePrefix3;
uniform vec3 u_sunPosition;
varying float alphaVal;
uniform float u_globalTime;
void main() {
	float stars_scale = 3.2;
	//
	
	
	vec3 n = norm.xyz;
    gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	float dist = gl_FragCoord.z / gl_FragCoord.w; 
    float fCos = dot(u_sunPosition, vBackDirection) / length(vBackDirection);
	float fMiePhase = m_PhasePrefix1 * (1.0 + fCos * fCos) / pow(m_PhasePrefix2 - m_PhasePrefix3 * fCos, 1.5);
   
	vec4 sunColor = vMieColor * fMiePhase;
	vec4 skyColor = vRayleighColor;
	vec4 finalSkyColor = sunColor + skyColor;
	vec4 starsColor = texture2D(m_StarMap, v_texCoord0*stars_scale+(u_globalTime*0.005))*(1.0-vRayleighColor.a);
    gl_FragColor = sunColor + skyColor + starsColor;
	//if (sunColor.r < 0.1 && sunColor.g < 0.1 && sunColor.b < 0.1) {
		gl_FragColor.a = alphaVal;
	//}
	
}