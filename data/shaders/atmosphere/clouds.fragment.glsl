#ifdef GL_ES 
precision mediump float;
#endif

varying vec2 v_texCoord0;

uniform sampler2D u_cloudMap;
uniform float u_globalTime;
uniform vec3 u_cloudColor;
float timeScale = 0.03;
float cloudScale = 0.00008;
float skyCover = 0.15;
float softness = 3.9;
float brightness = 1.0;
int noiseOctaves = 8;
float curlStrain = 0.3;

float saturate(float num)
{
    return clamp(num,0.0,1.0);
}

float noise(vec2 uv)
{
    return texture2D(u_cloudMap,uv).r;
}

vec2 rotate(vec2 uv)
{
    uv = uv + noise(uv*0.2)*0.005;
    float rot = curlStrain;
    float sinRot=sin(rot);
    float cosRot=cos(rot);
    mat2 rotMat = mat2(cosRot,-sinRot,sinRot,cosRot);
    return uv * rotMat;
}

float fbm (vec2 uv)
{
    float rot = 1.57;
    float sinRot=sin(rot);
    float cosRot=cos(rot);
    float f = 0.0;
    float total = 0.0;
    float mul = 0.5;
    mat2 rotMat = mat2(cosRot,-sinRot,sinRot,cosRot);
    
    for(int i = 0;i < 8;i++)
    {
        f += noise(uv+u_globalTime*0.00015*timeScale*(1.0-mul))*mul;
        total += mul;
        uv *= 3.0;
        uv=rotate(uv);
        mul *= 0.5;
    }
    return f/total;
}


void main() {
			vec2 screenUv = v_texCoord0.xy;
            vec2 uv = v_texCoord0.xy/(40000.0*cloudScale);

            float cover = 0.5;
            float bright = 0.85;

            float color1 =  fbm(uv-0.5+u_globalTime*0.004*timeScale);
            float color2 = fbm(uv-10.5+u_globalTime*0.002*timeScale);

            float clouds1 = smoothstep(1.0-cover,min((1.0-cover)+softness*2.0,1.0),color1);
            float clouds2 = smoothstep(1.0-cover,min((1.0-cover)+softness,1.0),color2);

            float cloudsFormComb = saturate(clouds1+clouds2);

            float cloudCol = saturate(saturate(1.0-pow(color1,1.0)*0.2)*bright);
            vec4 clouds1Color = vec4(cloudCol,cloudCol,cloudCol,1.0);
            vec4 clouds2Color = mix(clouds1Color,vec4(1.4, 1.4, 1.4, 0.6),0.4);
            vec4 cloudColComb = mix(clouds1Color,clouds2Color,saturate(clouds2-clouds1));
            gl_FragColor = mix(vec4(1.0, 1.0, 1.0, 0.0),cloudColComb,cloudsFormComb) * vec4(u_cloudColor, 1.0);
            
            float dist = gl_FragCoord.z / gl_FragCoord.w; 
   
    // 20 - fog starts; 80 - fog ends

            vec4 fogColor = vec4(u_cloudColor, 0.0);
            float fogFactor = (300.0 - dist)/(300.0 - 150.0);
            fogFactor = clamp(fogFactor, 0.0, 1.0 );
          
            
            gl_FragColor = mix(fogColor, gl_FragColor, fogFactor);
    
	
}