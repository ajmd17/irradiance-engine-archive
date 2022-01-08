/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.shaders.post;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.deferredrendering.DeferredRenderManager;
import com.irgames.managers.DepthTextureManager;

/**
 *
 * @author Andrew
 */
public class SSAOFilter extends PostFilter {

    private float intensity = 1.5f;
    private float scale = 0.2f;
    private float bias = 0.1f;

    @Override
    public void onRender(Camera cam) {
        DepthTextureManager.depthTex.bind(8);
        shaderProgram.setUniformi("u_depthMap", 8);

        DeferredRenderManager.getNormalMap().bind(4);
        shaderProgram.setUniformi("u_normalMap", 4);
        shaderProgram.setUniformMatrix("u_invProjView", cam.invProjectionView);
        shaderProgram.setUniformf("u_intensity", intensity);
        shaderProgram.setUniformf("u_scale", scale);
        shaderProgram.setUniformf("u_bias", bias);
        shaderProgram.setUniformf("u_radius", 0.01f);
        shaderProgram.setUniformf("u_distanceThreshold", 0.15f);
    }

    public SSAOFilter() {
        this.createShader();

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
                + "uniform mat4 u_invProjView;\n"
                + "uniform vec2 frustCorner;\n"
                + "float shadowFactor;\n"
                + "uniform float u_bias;\n"
                + "uniform float u_intensity;\n"
                + "uniform float u_scale;\n"
                + "uniform float u_radius;\n"
                + "uniform float u_distanceThreshold;\n"
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
                + "vec3 getPosition(vec2 uv, float depth) {\n"
                + "     vec4 pos = vec4(uv.x * 2.0 - 1.0, (uv.y * 2.0 - 1.0), depth * 2.0 - 1.0, 1.0);\n"
                + "     pos = u_invProjView * pos;\n"
                + "     pos = pos/pos.w;\n"
                + "     return pos.xyz;\n"
                + "}\n"
                + "float doAmbientOcclusion(vec2 tc, float depth, vec3 pos, vec3 norm) {\n"
                + "    vec3 diff = getPosition(tc, depth)-pos;\n"
                + "    //vec3 diff = pos;\n"
                + "    vec3 v = normalize(diff);\n"
                + "    float d = length(diff) * u_scale;\n"
                + "    return step(0.00002,d)*max(0.0, dot(norm, v) - u_bias) * ( 1.0/(1.0 + d) ) * (u_intensity+shadowFactor) * smoothstep(0.00002,0.0027,d);\n"
                + "}\n"
                + "vec3 reflection(in vec3 v1,in vec3 v2){\n"
                + "    vec3 result= 2.0 * dot(v2, v1) * v2;\n"
                + "    result=v1-result;\n"
                + "    return result;\n"
                + "}\n"
                + "float unpack_depth(vec4 rgba_depth){\n"
                + "    const vec4 bit_shift =\n"
                + "        vec4(1.0/(256.0*256.0*256.0)\n"
                + "            , 1.0/(256.0*256.0)\n"
                + "            , 1.0/256.0\n"
                + "            , 1.0);\n"
                + "    float depth = dot(rgba_depth, bit_shift);\n"
                + "    return depth;\n"
                + "}\n"
                + ""
                + "void main()                                  \n"
                + "{                                            \n"
                + "  gl_FragColor = texture2D(u_sceneTex, v_texCoords);\n"
                + "  float depth = unpack_depth(texture2D(u_depthMap, v_texCoords));\n"
                + "  shadowFactor = 0.02;\n"
                + "  vec3 normals = normalize(texture2D(u_normalMap, v_texCoords).rgb * 2.0 - 1.0);\n"
                + "  vec4 position = vec4(getPosition(v_texCoords, depth), 1.0);\n"
                + ""
                + "  if (depth < 1.0) {\n"
                + "     float result = 0.0;\n"
                + "     float ao = doAmbientOcclusion(v_texCoords, depth, position.xyz, normals);\n"
                + "     int iterations = 16;\n"
                + "     for (int j = 0; j < iterations; ++j) {\n"
                + "            //vec3 coord1 = reflection(vec3(i*0.01), rand) * vec3(rad);\n"
                + "             vec2 sampleTexCoord = v_texCoords + (poisson16[j] * (u_radius));\n"
                + "             float sampleDepth = unpack_depth(texture2D(u_depthMap, sampleTexCoord));\n"
                + "             vec3 samplePos = getPosition(sampleTexCoord, sampleDepth);\n"
                + ""
                + "             vec3 sampleDir = normalize(samplePos - position);\n"
                + "         float NdotS = max(dot(normals, sampleDir), 0);\n"
                + "        // distance between SURFACE-POSITION and SAMPLE-POSITION\n"
                + "        float VPdistSP = distance(position, samplePos);\n"
                + " \n"
                + "        // a = distance function\n"
                + "        float a = 1.0 - VPdistSP;\n"
                + "        // b = dot-Product\n"
                + "        float b = NdotS;\n"
                + " \n"
                + "        result += a;\n"
                + ""
                + ""
                + ""
                + "            ao += doAmbientOcclusion(v_texCoords + (poisson16[j] * u_radius), depth, position.xyz, normals) - shadowFactor;\n"
                + "            // Fine Detail\n"
                + "        }\n"
                + "        //ao /= float(iterations) * (2.35-shadowFactor);\n"
                + "        //result = 1.0-ao;\n"
                + ""
                + ""
                + ""
                + ""
                + "     gl_FragColor = vec4(result, result, result, 1.0);\n"
                + "  }\n"
                + "}";

        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (shaderProgram.isCompiled() == false) {
            throw new IllegalStateException(shaderProgram.getLog());
        }
    }

}
