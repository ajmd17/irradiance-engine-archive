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
public class GaussianBlur extends PostFilter {

    Vector2 scaleU;

    public GaussianBlur() {
        this.createShader();
        float BLUR_COEF = 0.5f;
        scaleU = new Vector2(1.0f / (Gdx.graphics.getWidth() * BLUR_COEF), 1.0f / (Gdx.graphics.getHeight()));
    }

    @Override
    public void onRender(Camera cam) {
        this.shaderProgram.setUniformf("ScaleU", scaleU);
        if (DepthTextureManager.depthTex != null) {
            DepthTextureManager.depthTex.bind(2);
            shaderProgram.setUniformi("u_depthTex", 2);
        }
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
                + "uniform vec2 ScaleU;\n"
                + "void main()                                  \n"
                + "{                                            \n"
                + "     "
                + "     //gl_FragColor = texture2D( u_sceneTex, v_texCoords);\n"
                + "     gl_FragColor += texture2D( u_sceneTex, v_texCoords + vec2( -3.0*ScaleU.x, -3.0*ScaleU.y ) ) * 0.015625;\n"
                + "	gl_FragColor += texture2D( u_sceneTex, v_texCoords + vec2( -2.0*ScaleU.x, -2.0*ScaleU.y ) )*0.09375;\n"
                + "	gl_FragColor += texture2D( u_sceneTex, v_texCoords + vec2( -1.0*ScaleU.x, -1.0*ScaleU.y ) )*0.234375;\n"
                + "	gl_FragColor += texture2D( u_sceneTex, v_texCoords + vec2( 0.0 , 0.0) )*0.3125;\n"
                + "	gl_FragColor += texture2D( u_sceneTex, v_texCoords + vec2( 1.0*ScaleU.x,  1.0*ScaleU.y ) )*0.234375;\n"
                + "	gl_FragColor += texture2D( u_sceneTex, v_texCoords + vec2( 2.0*ScaleU.x,  2.0*ScaleU.y ) )*0.09375;\n"
                + "	gl_FragColor += texture2D( u_sceneTex, v_texCoords + vec2( 3.0*ScaleU.x, -3.0*ScaleU.y ) ) * 0.015625;\n"
                + "     \n"
                + "  \n"
                + "}";

        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (shaderProgram.isCompiled() == false) {
            throw new IllegalStateException(shaderProgram.getLog());
        }
    }
}
