attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;
uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;
uniform sampler2D m_slopeColorMap;
uniform sampler2D m_region1ColorMap;
uniform vec3 m_region1;
uniform float m_slopeTileFactor;
varying vec4 vert;
varying vec2 v_texCoord0;
varying vec4 norm;
varying vec4 lightVec;
varying vec4 position;
varying vec3 vBackDirection;
varying vec4 vRayleighColor;
varying vec4 vMieColor; 
uniform vec3 u_cameraPosition;
uniform float u_cameraHeight2;
uniform vec3 u_sunPosition;
uniform vec3 u_sunColor;
uniform float u_Km4PI;
uniform float u_Kr4PI;
uniform float u_KmESun;
uniform vec3 u_invWavelengths;

varying float alphaVal;

const float outerRadius = 100.0 * 1.025;
const float innerRadius = 100.0;
const float densityScale = 0.25;

int nSamples = 4;
float fSamples = 4.0;

float scale( float fCos ) {
    float x = 1.0 - fCos;
    return densityScale * exp(-0.00287 + x*(0.459 + x*(3.83 + x*(-6.80 + x*5.25))));
}
float g  = -0.3;
float g2 =  0.09;
// Calculates the Mie phase function
float getMiePhase(float fCos, float fCos2, float g, float g2)
{
    return 1.5 * ((1.0 - g2) / (2.0 + g2)) * (1.0 + fCos2) / pow(abs(1.0 + g2 - 2.0*g*fCos), 1.5);
}
// Calculates the Rayleigh phase function
float getRayleighPhase(float fCos2)
{
    //return 1.0;
    return 0.75 + 0.75*fCos2;
}
// Returns the near intersection point of a line and a sphere
float getNearIntersection(vec3 v3Pos, vec3 v3Ray, float fDistance2, float fRadius2)
{
    float B = 2.0 * dot(v3Pos, v3Ray);
    float C = fDistance2 - fRadius2;
    float fDet = max(0.0, B*B - 4.0 * C);
    return 0.5 * (-B - sqrt(fDet));
}
// Returns the far intersection point of a line and a sphere
float getFarIntersection(vec3 v3Pos, vec3 v3Ray, float fDistance2, float fRadius2)
{
    float B = 2.0 * dot(v3Pos, v3Ray);
    float C = fDistance2 - fRadius2;
    float fDet = max(0.0, B*B - 4.0 * C);
    return 0.5 * (-B + sqrt(fDet));
}

void main() {
	
	position = u_worldTrans * vec4(a_position, 0.0);
	norm = vec4(1.0);
	vert = vec4(a_position, 1.0);
    v_texCoord0 = a_texCoord0;
	
	
	float fScale = 1.0 / (outerRadius - innerRadius);
    float scaleDepth = densityScale;  
    float scaleOverScaleDepth = fScale / scaleDepth; 
	vec4 colorDay = vec4(128.0/255.0,166.0/255.0,208.0/255.0, 1.0);
    vec3 v3Pos = a_position.xyz;
    /*vec3 v3Ray = v3Pos - u_cameraPosition;
    float fFar = length(v3Ray);
    v3Ray /= fFar;
    float len = length(v3Pos);
	
	float fNear = getNearIntersection(u_cameraPosition, v3Ray, u_cameraHeight2, outerRadius*outerRadius);
    fFar -= fNear;
	
	vec3 v3Start = u_cameraPosition;
	
	
	float fInvScaleDepth = 1.0;// / m_AverageDensityScale;
    float fStartDepth = exp(-fInvScaleDepth);
    float fStartAngle = dot(v3Ray, v3Start) / outerRadius;
    float fStartOffset = fStartDepth*scale(fStartAngle);
	
	float fSampleLength = fFar / fSamples;
    float fScaledLength = fSampleLength * fScale;
    vec3 v3SampleRay = v3Ray * fSampleLength;
    vec3 v3SamplePoint = v3Start + v3SampleRay * 0.5;
    vec3 v3Attenuate;
    vec3 v3FrontColor = vec3(0.4, 0.7, 0.0);
	
	for(int i=0; i<4; i++)
    {
        float fHeight = length(v3Start);
        float fDepth = exp(scaleOverScaleDepth * fStartOffset);
        float fLightAngle = dot(u_sunPosition, v3SamplePoint) / fHeight;
        float fCameraAngle = dot(v3Ray, v3SamplePoint) / fHeight;
        float fScatter = (fStartOffset + fDepth*(scale(fLightAngle) - scale(fCameraAngle)));
        v3Attenuate = exp(-fScatter * (u_invWavelengths * u_Kr4PI + u_Km4PI));

        v3FrontColor += v3Attenuate * (fDepth * fScaledLength);

        v3SamplePoint += v3SampleRay;
       
    }*/
	//v3FrontColor /= 4.0;
	vec4 twilight = vec4(204.0/255.0, 135.0/255.0, 93.0/255.0, 1.0); // Horizon during sunrise
    vec4 blue = vec4(226.0/255.0, 214.0/255.0, 186.0/255.0, 1.0); //Horizon during day
	
	if (u_sunPosition.y > 0.0) {
        blue *= clamp(u_sunPosition.y*5.0, 0.5, 1.0);
        twilight = mix(twilight, blue, clamp(u_sunPosition.y*5.0, 0.0, 1.0));
    } else {
        blue *= 0.5;
        twilight = mix(twilight, blue, clamp(-u_sunPosition.y*5.0, 0.0, 1.0));
    }
	
	
	vec3 dir = normalize(v3Pos);
    float dist = length(dir);
    dir /= dist;
    vBackDirection = -dir;
	
	
	float fCos = dot(u_sunPosition, vBackDirection) / length(vBackDirection);
    float fCos2 = fCos*fCos;
	
	
	
	vMieColor.rgb = u_sunColor * u_KmESun * getMiePhase(fCos, fCos2, g, g2);
	vMieColor.a = 1.0;
	vRayleighColor = vec4(mix(twilight.rgb, colorDay.rgb, a_position.y+0.25), 1.0);
	//vRayleighColor *= clamp(getRayleighPhase(fCos2), 0.5, 1.0);
	vRayleighColor *= clamp(u_sunPosition.y+0.5, 0.2, 1.0);
    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);
	
	alphaVal = clamp(a_position.y*4.0, 0.0, 1.0);
	
}