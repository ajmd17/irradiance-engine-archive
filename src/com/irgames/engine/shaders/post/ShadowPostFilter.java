/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.shaders.post;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.assets.Assets;
import com.irgames.managers.DepthTextureManager;
import com.irgames.managers.LightingManager;
import com.irgames.managers.PostProcessManager;
import static com.irgames.managers.PostProcessManager.fbo;

/**
 *
 * @author Andrew
 */
public class ShadowPostFilter extends PostFilter {

    // Deferred shadows
    public Matrix4 u_shadowMapProjViewTrans0, u_shadowMapProjViewTrans1, u_shadowMapProjViewTrans2, u_shadowMapProjViewTrans3;
    public float[] ranges = new float[4];
    private Vector3 rangev3 = new Vector3();
    public static FrameBuffer postShadowFbo;
    public int softShadows = 0, renderSplits = 0;
    public float shadowIntensity = 0.4f;
    ShadowPostBlur spb;
    Texture noiseMap;
    @Override
    public void onRender(Camera cam) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        postShadowFbo.begin();
        DepthTextureManager.depthTex.bind(8);
        shaderProgram.setUniformi("u_depthMap", 8);
        shaderProgram.setUniformMatrix("u_invProjView", cam.invProjectionView);
        for (int i = 0; i < DepthTextureManager.shadowMaps.length; i++) {
            DepthTextureManager.shadowMaps[i].bind(i);
            shaderProgram.setUniformi("u_shadowMap" + i, i);
        }
        shaderProgram.setUniformf("u_ambientColor", LightingManager.getAmbientColor());
        shaderProgram.setUniformf("u_shadowIntensity", shadowIntensity);
        shaderProgram.setUniformi("u_softShadows", softShadows);
        shaderProgram.setUniformi("u_renderSplits", renderSplits);
        shaderProgram.setUniformf("u_shadowSplit0", ranges[0]);
        shaderProgram.setUniformf("u_shadowSplit1", ranges[1]);
        shaderProgram.setUniformf("u_shadowSplit2", ranges[2]);
        shaderProgram.setUniformf("u_shadowSplit3", ranges[3]);
        shaderProgram.setUniformf("u_camPos", cam.position);
        shaderProgram.setUniformMatrix("u_shadowMapProjViewTrans0", u_shadowMapProjViewTrans0);
        shaderProgram.setUniformMatrix("u_shadowMapProjViewTrans1", u_shadowMapProjViewTrans1);
        shaderProgram.setUniformMatrix("u_shadowMapProjViewTrans2", u_shadowMapProjViewTrans2);
        shaderProgram.setUniformMatrix("u_shadowMapProjViewTrans3", u_shadowMapProjViewTrans3);
        noiseMap.bind(9);
        shaderProgram.setUniformi("u_noiseMap", 9);
        spb.shadowIntensity = this.shadowIntensity;
    }

    @Override
    public void postRender() {
        postShadowFbo.end();
    }

    @Override
    public void init() {

        this.processor.add(spb = new ShadowPostBlur());

        postShadowFbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        postShadowFbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        noiseMap = Assets.loadTexture(Gdx.files.internal("data/textures/noise/tex16.png"));
    }

    @Override
    public void resize() {
        postShadowFbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        postShadowFbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    public ShadowPostFilter() {
        this.createShader();

        bindSceneTex = false;

    }

    private void createShader() {
        String vertexShader = "attribute vec4 a_Position;    \n"
                + "attribute vec4 a_Color;\n"
                + "attribute vec2 a_texCoords;\n"
                + "uniform vec3 u_ranges;\n"
                + "varying vec3 ranges;\n"
                + "varying vec4 v_Color;"
                + "varying vec2 v_texCoords; \n"
                + "void main()                  \n"
                + "{                            \n"
                + "   ranges = u_ranges;\n"
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
                + "uniform mat4 u_invProjView;\n"
                + "uniform vec3 u_camPos;\n"
                + "uniform sampler2D u_sceneTex;\n"
                + "uniform vec4 u_ambientColor;\n"
                + ""
                + "//Shadow uniforms"
                + "varying vec3 ranges;\n"
                + "uniform int u_softShadows;\n"
                + "uniform float u_shadowIntensity;\n"
                + "uniform int u_renderSplits;\n"
                + ""
                + "uniform mat4 u_shadowMapProjViewTrans0;\n"
                + "uniform mat4 u_shadowMapProjViewTrans1;\n"
                + "uniform mat4 u_shadowMapProjViewTrans2;\n"
                + "uniform mat4 u_shadowMapProjViewTrans3;\n"
                + ""
                + "uniform sampler2D u_shadowMap0;\n"
                + "uniform sampler2D u_shadowMap1;\n"
                + "uniform sampler2D u_shadowMap2;\n"
                + "uniform sampler2D u_shadowMap3;\n"
                + ""
                + "uniform float u_shadowSplit0;\n"
                + "uniform float u_shadowSplit1;\n"
                + "uniform float u_shadowSplit2;\n"
                + "uniform float u_shadowSplit3;\n"
                + ""
                + "uniform sampler2D u_noiseMap;\n"
                + ""
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
                + "vec3 getShadowCoord(int index, vec3 worldPos) {\n"
                + "     vec4 shadowPos = vec4(0.0);\n"
                + ""
                + "     if (index == 0) {\n"
                + "         shadowPos = u_shadowMapProjViewTrans0 * vec4(worldPos, 1.0);\n"
                + "     } else if (index == 1) {\n"
                + "         shadowPos = u_shadowMapProjViewTrans1 * vec4(worldPos, 1.0);\n"
                + "     } else if (index == 2) {\n"
                + "         shadowPos = u_shadowMapProjViewTrans2 * vec4(worldPos, 1.0);\n"
                + "     } else {\n"
                + "         shadowPos = u_shadowMapProjViewTrans3 * vec4(worldPos, 1.0);\n"
                + "     }\n"
                + "     shadowPos *= 0.5;\n"
                + "     shadowPos += 0.5;\n"
                + "     return shadowPos.xyz;\n"
                + "}\n"
                + ""
                + "float getShadowness(int idx, vec3 coord)\n"
                + "{\n"
                + "	float result = 1.0;\n"
                + "	vec4 depth;\n"
                + "     const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 160581375.0);\n"
                + "	if (idx == 0) {\n"
                + "		depth = texture2D(u_shadowMap0, coord.xy);\n"
                + "	} else if (idx == 1) {\n"
                + "		depth = texture2D(u_shadowMap1, coord.xy);\n"
                + "	} else if (idx == 2) {\n"
                + "		depth = texture2D(u_shadowMap2, coord.xy);\n"
                + "	} else {\n"
                + "		depth = texture2D(u_shadowMap3, coord.xy);\n"
                + "	}\n"
                + "     result = max(step(coord.z + 0.0004, dot(depth, bitShifts)), 0.0);\n"
                + "     \n"
                + "     \n"
                + "	return result;\n"
                + "}\n"
                + ""
                + "float getShadow(int idx, vec3 coord) \n"
                + "{\n"
                + "	float shadowVal = getShadowness(idx, coord);\n"
                + "	return shadowVal;\n"
                + "}\n"
                + ""
                + ""
                + "float unpack_depth(const in vec4 rgba_depth){\n"
                + "    const vec4 bit_shift =\n"
                + "        vec4(1.0/(256.0*256.0*256.0)\n"
                + "            , 1.0/(256.0*256.0)\n"
                + "            , 1.0/256.0\n"
                + "            , 1.0);\n"
                + "    float depth = dot(rgba_depth, bit_shift);\n"
                + "    return depth;\n"
                + "}\n"
                + "vec3 getPosition(vec2 uv, float depth) {\n"
                + "     vec4 pos = vec4(uv.x * 2.0 - 1.0, (uv.y * 2.0 - 1.0), depth * 2.0 - 1.0, 1.0);\n"
                + "     pos = u_invProjView * pos;\n"
                + "     pos = pos/pos.w;\n"
                + "     return pos.xyz;\n"
                + "}\n"
                + "void main()                                  \n"
                + "{                                            \n"
                + "  vec4 color = texture2D(u_sceneTex, v_texCoords);\n"
                + "  gl_FragColor = color;\n"
                + "  float depth = unpack_depth(texture2D(u_depthMap, v_texCoords));\n"
                + "  if (depth < 1.0) {\n"
                + "  vec3 worldPos = getPosition(v_texCoords, depth);\n"
                + "  vec2 camNoY = vec2(u_camPos.x, u_camPos.z);\n"
                + "  vec2 worldNoY = vec2(worldPos.x, worldPos.z);\n"
                + "  vec4 vpPos = (inverse(u_invProjView) * vec4(worldPos, 1.0));\n"
                + "  float dist = vpPos.z;//length(u_camPos - worldPos);\n"
                + "  vec3 splitColor = vec3(1.0);\n"
                + "  int index = 0;"
                + "  if (dist <= u_shadowSplit0) {\n"
                + "	index = 0;\n"
                + "     splitColor = vec3(0.0, 1.0, 0.0);\n"
                + "  } else if (dist > u_shadowSplit0 && dist <= u_shadowSplit1){\n"
                + "	index = 1;\n"
                + "     splitColor = vec3(1.0, 0.0, 0.0);\n"
                + "  } else if (dist > u_shadowSplit1 && dist <= u_shadowSplit2){\n"
                + "	index = 2;\n"
                + "     splitColor = vec3(0.0, 0.0, 1.0);\n"
                + "  } else if (dist > u_shadowSplit2) {\n"
                + "	index = 3;\n"
                + "     splitColor = vec3(1.0, 1.0, 0.0);\n"
                + "  }\n"
                + "  float shadow;\n"
                + "  float radius = 0.1;\n"
                + "  for (int x = 0; x < 4; x++) {\n"
                + " for (int y = 0; y < 4; y++) {\n"
                + "     vec2 offset = (poisson16[x * 4 + y]*radius);\n"
                + "     vec3 random = texture2D(u_noiseMap, v_texCoords*(depth)*15.0).rgb;\n"
                
    
                + "     vec3 shadowCoord = getShadowCoord(index, worldPos+(vec3(offset,0.0)));\n"
                + "     float currentShadow = getShadow(index, shadowCoord);\n"
                + "  //if (currentShadow < 1.0) { // this means we are in shadow\n"
                + "     //currentShadow *= random.r;\n"
                + "  //}\n"
                + "     shadow += clamp(currentShadow, 0.0, 1.0);\n"
     
                + "  }\n"
                + "  }\n"
                + ""
                + "  shadow /= 16.0;\n"
                + "  float fogEnd = u_shadowSplit3;\n"
                + "  float fogStart = u_shadowSplit2;\n"
                + "  float fogFactor = (fogEnd - dist)/(fogEnd - fogStart);\n"
                + "  fogFactor = clamp( fogFactor, 0.0, 1.0 );\n"
                + "  shadow = mix(1.0, shadow, fogFactor);\n"
                + "  //if (dist < u_shadowSplit3) {\n"
                + "     if (u_renderSplits == 1) {\n"
                + "         gl_FragColor = vec4(splitColor, 1.0) * vec4(shadow, shadow, shadow, shadow);\n"
                + "     } else if (u_renderSplits == 0) {\n"
                + "         gl_FragColor = vec4(shadow, shadow, shadow, shadow);\n"
                + "     }\n"
                + "  //} else {\n"
                + "     //gl_FragColor = vec4(1.0);\n"
                + "  //}\n"
                + " //} else {\n"
                + "     //gl_FragColor = vec4(1.0);\n"
                + " //}\n"
                + "}\n"
                + "}";

        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (shaderProgram.isCompiled() == false) {
            throw new IllegalStateException(shaderProgram.getLog());
        }
    }

}
