/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.shaders.post;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 *
 * @author Andrew
 */
public class GammaCorrection extends PostFilter {

    public GammaCorrection() {
        this.createShader();
    }

    private void createShader() {
        String vertexShader = "attribute vec4 a_Position;    \n" + "attribute vec4 a_Color;\n" + "attribute vec2 a_texCoords;\n"
                + "varying vec4 v_Color;" + "varying vec2 v_texCoords; \n"
                + "void main()                  \n" + "{                            \n" + "   v_Color = a_Color;"
                + "   v_texCoords = a_texCoords;\n" + "   gl_Position =   a_Position;  \n" + "}                            \n";
        String fragmentShader = "#ifdef GL_ES\n"
                + "precision mediump float;\n"
                + "#endif\n"
                + "varying vec4 v_Color;\n" + "varying vec2 v_texCoords; \n"
                + "uniform sampler2D u_sceneTex;\n"
                + "void main()                                  \n" 
                + "{                                            \n"
                + "  gl_FragColor = texture2D(u_sceneTex, v_texCoords);\n"
                + "  float gamma = 1.3;\n"
                + "  gl_FragColor.rgb = pow( gl_FragColor.rgb, vec3( 1.0 / gamma ) );\n"
                + "}";

        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (shaderProgram.isCompiled() == false) {
            throw new IllegalStateException(shaderProgram.getLog());
        }
    }
}
