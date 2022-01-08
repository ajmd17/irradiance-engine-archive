/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.shaders.post;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.deferredrendering.DeferredRenderManager;
import com.irgames.managers.DepthTextureManager;

/**
 *
 * @author Andrew
 */
public class SSAOFilter2 extends PostFilter {

    private float intensity = 1.5f;
    private float scale = 0.1f;
    private float bias = 0.1f;
    Texture randomTex;
    public boolean showDebug = false;

    @Override
    public void onRender(Camera cam) {
        DepthTextureManager.depthTex.bind(8);
        shaderProgram.setUniformi("u_depthMap", 8);

        DeferredRenderManager.getNormalMap().bind(4);
        shaderProgram.setUniformi("u_normalMap", 4);
//        DeferredRenderManager.getDiffuseMap().bind(5);
        //shaderProgram.setUniformi("gdiffuse", 5);
        shaderProgram.setUniformMatrix("u_proj", cam.projection);
        shaderProgram.setUniformMatrix("u_invProjView", cam.invProjectionView);
        shaderProgram.setUniformf("u_intensity", intensity);
        shaderProgram.setUniformf("u_scale", scale);
        shaderProgram.setUniformf("u_bias", bias);
        shaderProgram.setUniformf("u_radius", 0.3f);
        shaderProgram.setUniformf("u_distanceThreshold", 0.15f);
        if (showDebug) {
            shaderProgram.setUniformi("u_renderDebug", 1);
        } else {
            shaderProgram.setUniformi("u_renderDebug", 0);
        }
        randomTex.bind(9);
        shaderProgram.setUniformi("u_random", 9);
        this.shaderProgram.setUniformf("u_width", Gdx.graphics.getWidth());
        this.shaderProgram.setUniformf("u_height", Gdx.graphics.getHeight());
    }

    public SSAOFilter2() {
        this.createShader();
        randomTex = new Texture(Gdx.files.internal("data/textures/noise/normal.jpg"));
        randomTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
    }

    private void createShader() {
        String vertexShader = "attribute vec4 a_Position;    \n"
                + "attribute vec4 a_Color;\n"
                + "attribute vec2 a_texCoords;\n"
                + "varying vec4 v_Color;"
                + "varying vec2 v_texCoords; \n"
                + "void main()                  \n"
                + "{                            \n"
                + "   v_Color = a_Color;"
                + "   v_texCoords = a_texCoords;\n"
                + "   gl_Position =   a_Position;  \n"
                + "}                            \n";
        String fragmentShader = "#ifdef GL_ES\n"
                + "precision mediump float;\n"
                + "#endif\n"
                + "varying vec4 v_Color;\n"
                + "varying vec2 v_texCoords; \n"
                + "uniform sampler2D u_depthMap;\n"
                + "uniform sampler2D u_normalMap;\n"
                + "uniform sampler2D u_sceneTex;\n"
                + "uniform sampler2D u_random;\n"
                + "uniform sampler2D gdiffuse;\n"
                + "uniform mat4 u_invProjView;\n"
                + "uniform mat4 u_proj;\n"
                + "uniform vec2 frustCorner;\n"
                + "uniform int u_renderDebug;\n"
                + "float shadowFactor;\n"
                + "uniform float u_width;\n"
                + "uniform float u_height;\n"
                + "uniform float u_bias;\n"
                + "uniform float u_intensity;\n"
                + "uniform float u_scale;\n"
                + "uniform float u_radius;\n"
                + "uniform float u_distanceThreshold;\n"
                + "#define CAP_MIN_DISTANCE 0.0001\n"
                + "#define CAP_MAX_DISTANCE 0.005\n"
                + "const vec2 poisson16[] = vec2[](    // These are the Poisson Disk Samples\n"
                + "                                vec2( -0.94201624,  -0.39906216 ),\n"
                + "                                vec2(  0.94558609,  -0.76890725 ),\n"
                + "                                vec2( -0.094184101, -0.92938870 ),\n"
                + "                                vec2(  0.34495938,   0.29387760 ),\n"
                + "                                vec2( -0.91588581,   0.45771432 ),\n"
                + "                                vec2( -0.81544232,  -0.87912464 ),\n"
                + "                                vec2( -0.38277543,   0.27676845 ),\n"
                + "                                vec2(  0.97484398,   0.75648379 ),\n"
                + "                                vec2(  0.44323325,  -0.97511554 ),\n"
                + "                                vec2(  0.53742981,  -0.47373420 ),\n"
                + "                                vec2( -0.26496911,  -0.41893023 ),\n"
                + "                                vec2(  0.79197514,   0.19090188 ),\n"
                + "                                vec2( -0.24188840,   0.99706507 ),\n"
                + "                                vec2( -0.81409955,   0.91437590 ),\n"
                + "                                vec2(  0.19984126,   0.78641367 ),\n"
                + "                                vec2(  0.14383161,  -0.14100790 )\n"
                + "                               );\n"
                + "float unpack_depth(vec4 rgba_depth){\n"
                + "    const vec4 bit_shift =\n"
                + "        vec4(1.0/(256.0*256.0*256.0)\n"
                + "            , 1.0/(256.0*256.0)\n"
                + "            , 1.0/256.0\n"
                + "            , 1.0);\n"
                + "    float depth = dot(rgba_depth, bit_shift);\n"
                + "    return depth;\n"
                + "}\n"
                + "vec3 getPosition(vec2 uv, float depth) {\n"
                + "     "
                + "     vec3 ray = inverse(u_proj) * vec4((uv.x - 0.5) *2., ((uv.y - 0.5) * 2.), 1.0, 1.0);\n"
                + "     ray *= depth;\n"
                + "     return ray.xyz;\n"
                + "}\n"
                + " vec2 getRandom(in vec2 uv)\n"
                + "{\n"
                + "return normalize(texture2D(u_random, uv / 0.1).xy * 2.0 - 1.0);\n"
                + "}\n"
                + "vec3 readNormal(in vec2 coord)  \n"
                + "{  \n"
                + "     return normalize(texture2D(u_normalMap, coord).xyz*2.0  - 1.0);  \n"
                + "}\n"
                + "//Ambient Occlusion form factor:\n"
                + "    float aoFF(in vec3 ddiff,in vec3 cnorm, in float c1, in float c2){\n"
                + "          vec3 vv = normalize(ddiff);\n"
                + "          float rd = length(ddiff);\n"
                + "          return (1.0-clamp(dot(readNormal(v_texCoords+vec2(c1,c2)),-vv),0.0,1.0)) *\n"
                + "           clamp(dot( cnorm,vv ),0.0,1.0)* \n"
                + "                 (1.0 - 1.0/sqrt(1.0/(rd*rd) + 1.0));\n"
                + "    }\n"
                + ""
                + "//GI form factor:\n"
                + "    float giFF(in vec3 ddiff,in vec3 cnorm, in float c1, in float c2){\n"
                + "          vec3 vv = normalize(ddiff);\n"
                + "          float rd = length(ddiff);\n"
                + "          return 1.0*clamp(dot(readNormal(v_texCoords+vec2(c1,c2)),vv),0.0,1.0)*\n"
                + "                     clamp(dot( cnorm,vv ),0.0,1.0)/\n"
                + "                     (rd*rd+1.0);  \n"
                + "    }\n"
                + ""
                + ""
                + "float threshold(in float thr1, in float thr2 , in float val) {\n"
                + " if (val < thr1) {return 0.0;}\n"
                + " if (val > thr2) {return 1.0;}\n"
                + " return val;\n"
                + "}\n"
                + "\n"
                + "// averaged pixel intensity from 3 color channels\n"
                + "float avg_intensity(in vec4 pix) {\n"
                + " return (pix.r + pix.g + pix.b)/3.;\n"
                + "}\n"
                + "\n"
                + "vec4 get_pixel(in vec2 coords, in float dx, in float dy) {\n"
                + " return texture2D(u_sceneTex,coords + vec2(dx, dy));\n"
                + "}\n"
                + "\n"
                + "// returns pixel color\n"
                + "float IsEdge(in vec2 coords){\n"
                + "  float dxtex = 1.0 / u_width /*image width*/;\n"
                + "  float dytex = 1.0 / u_height /*image height*/;\n"
                + "  float pix[9];\n"
                + "  int k = -1;\n"
                + "  float delta;\n"
                + "\n"
                + "  // read neighboring pixel intensities\n"
                + "  for (int i=-1; i<2; i++) {\n"
                + "   for(int j=-1; j<2; j++) {\n"
                + "    k++;\n"
                + "    pix[k] = avg_intensity(get_pixel(coords,float(i)*dxtex,\n"
                + "                                          float(j)*dytex));\n"
                + "   }\n"
                + "  }\n"
                + "\n"
                + "  // average color differences around neighboring pixels\n"
                + "  delta = (abs(pix[1]-pix[7])+\n"
                + "          abs(pix[5]-pix[3]) +\n"
                + "          abs(pix[0]-pix[8])+\n"
                + "          abs(pix[2]-pix[6])\n"
                + "           )/4.;\n"
                + "\n"
                + "  return threshold(0.2,0.7,clamp(2.0*delta,0.0,1.0));\n"
                + "}\n"
                + "\n"
                + "void main()                                  \n"
                + "{                                            \n"
                + "  vec3 gi = vec3(0.0,0.0,0.0);\n"
                + "  gl_FragColor = texture2D(u_sceneTex, v_texCoords);\n"
                + "  float depth = unpack_depth(texture2D(u_depthMap, v_texCoords));\n"
                + "  vec3 p = getPosition(v_texCoords, depth);\n"
                + "  //float edge = 1.0-IsEdge(v_texCoords);\n"
                + "  shadowFactor = 0.02;\n"
                + "  vec3 normals = normalize(texture2D(u_normalMap, v_texCoords).rgb * 2.0 - 1.0);\n"
                + "  vec2 fres = vec2(1.0/u_width,1.0/u_height);\n"
                + "  vec4 vpPos = vec4(p, 1.0);\n"
                + "  vec3 random = normalize(texture(u_random, v_texCoords*50.0).rgb) * 2.0 - 1.0;\n"
                + "     float incx = 1.0/40.0;\n"
                + "     float incy = 1.0/40.0;\n"
                + "     float pw = incx;\n"
                + "     float ph = incy;\n"
                + ""
                + "  float occlusion;\n"
                + "  if (depth < 1.0) {\n"
                + "     float result = 0.0;\n"
                + "     float ao;\n"
                + "     int iterations = 16;\n"
                + "for (int x = 0; x < 4; x++) {\n"
                + " for (int y = 0; y < 4; y++) {\n"
                + "     float npw = (x + poisson16[x])*(1.0-depth);\n"
                + "     float nph = (y + poisson16[y])*(1.0-depth);\n"
                + "     //float sampleDepth = unpack_depth(texture2D(u_depthMap, v_texCoords+vec2(npw,nph)));\n"
                + "     vec3 ddiff = getPosition(v_texCoords+vec2(npw,nph), depth);\n"
                + "     ao += aoFF(ddiff,normals,npw,nph);\n"
                + "  }\n"
                + "}\n"
      
                + "     ao/=(16.);\n"
                + "     //gi/=(16.);\n"
                + "     "
                + "    // ao *= edge;\n"
                + "     float strength = 1.0;\n"
                + "     result = 1.0-strength * ao;\n"
                + "     result = mix(0.1, 1.0, result*result);\n"
                + "     if (u_renderDebug == 1) {\n"
                + "         gl_FragColor = (vec4(result, result, result, 1.0) );\n"
                + "     } else {\n"
                + "         gl_FragColor = (texture2D(u_sceneTex, v_texCoords)*(vec4(result, result, result, 1.0) ) ) ;//+ vec4(gi, 1.0);\n"
                + "     }\n"
                + "  }\n"
                + "}";

        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (shaderProgram.isCompiled() == false) {
            throw new IllegalStateException(shaderProgram.getLog());
        }
    }

}
