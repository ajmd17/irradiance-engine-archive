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

float rho = .9;
float sigma = 30;
const float PI = 3.14159265358979323846;
vec3 BRDF( vec3 L, vec3 V, vec3 N)
{
    float VdotN = dot(V,N);
    float LdotN = dot(L,N);
    float theta_r = acos (VdotN);
    float sigma2 = pow(sigma*PI/180,2);

    float cos_phi_diff = dot( normalize(V-N*(VdotN)), normalize(L - N*(LdotN)) );
    float theta_i = acos (LdotN);
    float alpha = max (theta_i, theta_r);
    float beta = min (theta_i, theta_r);
    if (alpha > PI/2) return vec3(0);

    float C1 = 1.0 - 0.5 * sigma2 / (sigma2 + 0.33);
    float C2 = 0.45 * sigma2 / (sigma2 + 0.09);
    if (cos_phi_diff >= 0) C2 *= sin(alpha);
    else C2 *= (sin(alpha) - pow(2.0*beta/PI,3.0));
    float C3 = 0.125 * sigma2 / (sigma2+0.09) * pow ((4.0*alpha*beta)/(PI*PI),2.0);
    float L1 = rho/PI * (C1 + cos_phi_diff * C2 * tan(beta) + (1.0 - abs(cos_phi_diff)) * C3 * tan((alpha+beta)/2.0));
    float L2 = 0.17 * rho*rho / PI * sigma2/(sigma2+0.13) * (1.0 - cos_phi_diff*(4.0*beta*beta)/(PI*PI));
    return vec3(L1 + L2);
}


void main() {
	
	gl_FragColor = vec4(u_albedo);
	bool flipY = false;
	#ifdef FLIP_Y
		flipY = true;
	#endif

	
	
	vec3 n = normalize(norm.xyz);
	
	
	
	
	vec3 l = normalize(-lightV.xyz);
	vec3 v = normalize(vert.xyz);
	vec3 h = normalize(v+l);
	float dist = gl_FragCoord.z / gl_FragCoord.w; 

	float fogFactor = (u_fogEnd - dist)/(u_fogEnd - u_fogStart);
	fogFactor = clamp( fogFactor, 0.0, 1.0 );
	
	
	gl_FragColor *= vec4(BRDF(l, v, n), 1.0);
	
	
	gl_FragColor = mix(vec4(u_fogColor, 1.0), gl_FragColor, fogFactor);
	
}