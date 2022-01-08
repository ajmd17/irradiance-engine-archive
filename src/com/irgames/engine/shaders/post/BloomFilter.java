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
public class BloomFilter extends PostFilter {

    Vector2 scaleU;
    float intensity = 0.3f;
    public BloomFilter() {
        this.createShader();
        float BLUR_COEF = 0.8f;
        scaleU = new Vector2(1.0f / (Gdx.graphics.getWidth() * BLUR_COEF), 1.0f / (Gdx.graphics.getHeight()));
    }
    
    @Override
    public void onRender(Camera cam) {
        this.shaderProgram.setUniformf("ScaleU", scaleU);
        if (DepthTextureManager.depthTex != null) {
            DepthTextureManager.depthTex.bind(2);
            shaderProgram.setUniformi("u_depthTex", 2);
        }
        this.shaderProgram.setUniformf("Intensity", intensity);
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
                + "const float Threshold = 0.8;\n"
                + "uniform float Intensity = 0.5;\n"
                + "vec4 sum = vec4(0);\n"
                + "   vec2 texcoord = v_texCoords;\n"
                + "\nvoid main() {\n"
                + "   float depth = texture2D(u_depthTex, v_texCoords);\n"
                + "   for ( int i= -3 ;i < 3; i++)\n"
                + "   {\n"
                + "        for (int j = -3; j < 3; j++)\n"
                + "        {\n"
                + "            sum += texture2D(u_sceneTex, texcoord + (vec2(j, i)*0.001));\n"
                + "        }\n"
                + "   }\n"
                + "   //sum /= (6.*6.);\n"
                + "     if (depth < 0.9) {\n"
                + "       gl_FragColor = (sum*sum*0.001*Intensity) + texture2D(u_sceneTex, texcoord);\n"
                + "     } else {\n"
                + "       gl_FragColor = (sum*sum*0.001*Intensity*0.15) + texture2D(u_sceneTex, texcoord);\n"
                + "     }\n"
                + "}\n";

        /* + "vec4 blurColor() {\n"
         + "     vec4 col = vec4(0.0);\n"
         + "     col += texture2D( u_sceneTex, v_texCoords + vec2( -3.0*ScaleU.x, -3.0*ScaleU.y ) ) * 0.015625;\n"
         + "	col += texture2D( u_sceneTex, v_texCoords + vec2( -2.0*ScaleU.x, -2.0*ScaleU.y ) )*0.09375;\n"
         + "	col += texture2D( u_sceneTex, v_texCoords + vec2( -1.0*ScaleU.x, -1.0*ScaleU.y ) )*0.234375;\n"
         + "	col += texture2D( u_sceneTex, v_texCoords + vec2( 0.0 , 0.0) )*0.3125;\n"
         + "	col += texture2D( u_sceneTex, v_texCoords + vec2( 1.0*ScaleU.x,  1.0*ScaleU.y ) )*0.234375;\n"
         + "	col += texture2D( u_sceneTex, v_texCoords + vec2( 2.0*ScaleU.x,  2.0*ScaleU.y ) )*0.09375;\n"
         + "	col += texture2D( u_sceneTex, v_texCoords + vec2( 3.0*ScaleU.x, -3.0*ScaleU.y ) ) * 0.015625;\n"
         + "     return col;\n"
         + "}\n"
         + "void main()                                  \n"
         + "{                                            \n"
         + "     vec4 Highlight = clamp(blurColor()-Threshold,0.0,1.0)*1.0/(1.0-Threshold);\n"
         + "     vec4 Color = texture2D(u_sceneTex, v_texCoords);\n"

         + "     gl_FragColor =  1.0-(1.0-Color)*(1.0-Highlight*Intensity);\n"
         + "  \n"
         + "}";*/
        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (shaderProgram.isCompiled() == false) {
            throw new IllegalStateException(shaderProgram.getLog());
        }
    }
}
