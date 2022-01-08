#ifdef GL_ES 
precision mediump float;
#endif
uniform samplerCube u_cubeMap;
varying vec3 v_cubeMapUV;
varying vec2 v_texCoord0;


void main() {

    gl_FragColor = vec4(0.5, 0.0, 0.5, 1.0);
	
}