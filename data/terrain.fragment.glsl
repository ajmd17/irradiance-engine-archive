#ifdef GL_ES 
precision mediump float;
#endif
uniform sampler2D m_slopeColorMap;
uniform sampler2D m_region1ColorMap;
uniform sampler2D m_region2ColorMap;
uniform vec3 m_region1;
uniform float m_slopeTileFactor;
varying vec2 v_texCoord0;
varying vec4 norm;
varying vec4 vert;
varying vec4 lightVec;
varying vec4 position;
uniform vec3 u_lightColor;
uniform float u_fogStart;
uniform float u_fogEnd;
uniform vec3 u_fogColor;
uniform sampler2D u_normalMap;
uniform sampler2D u_alphaMap1;
vec4 getTerrainCoords() {
	vec4 p = position / 256;
	return p;
}

vec4 GenerateTerrainColor() {

    float height = position.y;
    vec4 p = getTerrainCoords();
    vec3 blend = abs( norm.xyz );
    blend = (blend -0.2) * 0.7;
    blend = normalize(max(blend, 0.00001));      // Force weights to sum to 1.0 (very important!)
    float b = (blend.x + blend.y + blend.z);
    blend /= vec3(b, b, b);

   

    float m_regionMin = 0.0;
    float m_regionMax = 0.0;
    float m_regionRange = 0.0;
    float m_regionWeight = 0.0;

    vec4 slopeCol1 = texture2D(m_slopeColorMap, p.xz * m_slopeTileFactor);
    vec4 slopeCol2 = texture2D(m_slopeColorMap, p.xy * m_slopeTileFactor);   
	
	vec4 terrainColor1 = mix(slopeCol1, texture2D(m_region1ColorMap, p.xz * m_region1.z), norm.y);
	vec4 terrainColor2 = mix(slopeCol1, texture2D(m_region2ColorMap, p.xz * m_region1.z), norm.y);
	vec4 alphaMap = texture2D(u_alphaMap1, p.xz * m_region1.z);
	
    vec4 terrainColor = texture2D(m_region1ColorMap, p.xz * m_region1.z );
	return mix(terrainColor1, terrainColor2, alphaMap.r);

}
void main() {
	vec3 n = normalize(norm.xyz);
	//#ifdef NORMAL_MAP
		//vec4 p = getTerrainCoords();
		
		//n = 1.0-normalize(2.0*texture2D(u_normalMap,v_texCoord0).rgb-1.0);
	//#endif
	
	vec3 l = normalize(lightVec.xyz);
	vec3 v = normalize(vert.xyz);
	vec3 h = normalize(l - n);
	float lDoth = max(0.0, dot(l, h));
	float nDotl = max(0.0, dot(n, l));
	
	
	
	
    gl_FragColor = GenerateTerrainColor()*lDoth;
	gl_FragColor.rgb *= u_lightColor;
	float dist = gl_FragCoord.z / gl_FragCoord.w; 
	
    
	

    float fogFactor = (u_fogEnd - dist)/(u_fogEnd - u_fogStart);
    fogFactor = clamp( fogFactor, 0.0, 0.86 );
   
    gl_FragColor = mix(vec4(u_fogColor, 1.0), gl_FragColor, fogFactor);
}