/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.shaders.post;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.irgames.managers.DepthTextureManager;

/**
 *
 * @author Andrew
 */
public class DOFFilter extends PostFilter {

    Vector2 scaleU;

    public DOFFilter() {
        this.createShader();
        float BLUR_COEF = 0.3f;
        scaleU = new Vector2(1.0f / (Gdx.graphics.getWidth() * BLUR_COEF), 1.0f / (Gdx.graphics.getHeight()));
    }
    Vector2 nearFar = new Vector2();
    @Override
    public void onRender(Camera cam) {
        this.shaderProgram.setUniformf("ScaleU", scaleU);
        if (DepthTextureManager.depthTex != null) {
            DepthTextureManager.depthTex.bind(2);
            shaderProgram.setUniformi("u_depthTex", 2);
        }
        nearFar.set(cam.near, cam.far);
        this.shaderProgram.setUniformf("u_nearFar", nearFar);
        this.shaderProgram.setUniformf("u_focusRange", 50f);
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
                + "uniform sampler2D u_texture;\n"
                + "uniform sampler2D u_sceneTex;\n"
                + "uniform sampler2D u_depthTex;\n"
                + "uniform float u_focusRange;\n"
                + "uniform vec2 u_nearFar;\n"
                + "uniform vec2 ScaleU;\n"
                + "float unpack_depth(const in vec4 rgba_depth){\n"
                + "    const vec4 bit_shift =\n"
                + "        vec4(1.0/(256.0*256.0*256.0)\n"
                + "            , 1.0/(256.0*256.0)\n"
                + "            , 1.0/256.0\n"
                + "            , 1.0);\n"
                + "    float depth = dot(rgba_depth, bit_shift);\n"
                + "    return depth;\n"
                + "}\n"
                + "void main()                                  \n"
                + "{                                            \n"
                + "     float depth = unpack_depth(texture2D(u_depthTex, v_texCoords));\n"
                + "     "
                + "     //gl_FragColor = texture2D( u_sceneTex, v_texCoords);\n"
                + "     vec4 finalBlurVal;"
                + "     finalBlurVal += texture2D( u_sceneTex, v_texCoords + vec2( -3.0*ScaleU.x, -3.0*ScaleU.y ) ) * 0.015625;\n"
                + "	finalBlurVal += texture2D( u_sceneTex, v_texCoords + vec2( -2.0*ScaleU.x, -2.0*ScaleU.y ) )*0.09375;\n"
                + "	finalBlurVal += texture2D( u_sceneTex, v_texCoords + vec2( -1.0*ScaleU.x, -1.0*ScaleU.y ) )*0.234375;\n"
                + "	finalBlurVal += texture2D( u_sceneTex, v_texCoords + vec2( 0.0 , 0.0) )*0.3125;\n"
                + "	finalBlurVal += texture2D( u_sceneTex, v_texCoords + vec2( 1.0*ScaleU.x,  1.0*ScaleU.y ) )*0.234375;\n"
                + "	finalBlurVal += texture2D( u_sceneTex, v_texCoords + vec2( 2.0*ScaleU.x,  2.0*ScaleU.y ) )*0.09375;\n"
                + "	finalBlurVal += texture2D( u_sceneTex, v_texCoords + vec2( 3.0*ScaleU.x, -3.0*ScaleU.y ) ) * 0.015625;\n"
                + "     float a = u_nearFar.y / (u_nearFar.y - u_nearFar.x);\n"
                + "\n"
                + "     float b = u_nearFar.y * u_nearFar.x / (u_nearFar.x - u_nearFar.y);\n"
                + "\n"
                + "     float z = b / (depth - a);\n"
                + "     float dynamicDepth = b / (depth - a);\n"
                + "\n"
                + "     float unfocus = min( 1.0, abs( z - dynamicDepth ) / u_focusRange );\n"
                + "     gl_FragColor = mix(texture2D(u_sceneTex, v_texCoords), finalBlurVal, unfocus);\n"
                + ""
                + "\n"
                + "\n"
                + "}";

        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (shaderProgram.isCompiled() == false) {
            throw new IllegalStateException(shaderProgram.getLog());
        }
    }
}
