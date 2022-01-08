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
public class LogBlur extends PostFilter {

    Vector2 scaleU;

    public LogBlur() {
        this.createShader();
        float BLUR_COEF = 0.5f;
        scaleU = new Vector2(1.0f / (Gdx.graphics.getWidth() * BLUR_COEF), 1.0f / (Gdx.graphics.getHeight()));
    }

    @Override
    public void onRender(Camera cam) {
        this.shaderProgram.setUniformf("vPixelSize", scaleU);
        ShadowPostFilter.postShadowFbo.getColorBufferTexture().bind(0);
        this.shaderProgram.setUniformi("u_shadowTex", 0);
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
                + "uniform sampler2D u_shadowTex;\n"
                + "uniform vec2 ScaleU;\n"
      
                + "uniform vec2 vPixelSize;\n"
                + "\n"
                + "float vGaussianBlur[10] ;\n"
                + "\n"
                + "//= { 0.0882357, 0.0957407, 0.101786, 0.106026, 0.108212, 0.108212, 0.106026, 0.101786, 0.0957407, 0.0882357 };\n"
                + "\n"
                + "float log_conv ( float x0, float X, float y0, float Y )\n"
                + "{\n"
                + "    return ( X + log( x0 + (y0 * exp(Y - X) ) ) );\n"
                + "}\n"
                + "\n"
                + "vec4 LogGaussianFilter()\n"
                + "{\n"
                + "	float depth = texture2D(u_depthTex, v_texCoords);\n\n"
                + "	vGaussianBlur[0] = 0.0882357;\n"
                + "	vGaussianBlur[1] = 0.0957407;\n"
                + "	vGaussianBlur[2] = 0.101786;\n"
                + "	vGaussianBlur[3] = 0.106026;\n"
                + "	vGaussianBlur[4] = 0.108212;\n"
                + "	vGaussianBlur[5] = 0.108212;\n"
                + "	vGaussianBlur[6] = 0.106026;\n"
                + "	vGaussianBlur[7] = 0.101786;\n"
                + "	vGaussianBlur[8] = 0.0957407;\n"
                + "	vGaussianBlur[9] = 0.0882357;\n"
                + "	\n"
                + "	float vSample[ 10 ];\n"
                + "\n"
                + "    for (int i = 0; i < 10; i++)\n"
                + "    {\n"
                + "		float fOffSet = i - 4.5;		\n"
                + "		vec2 vTexCoord = vec2( v_texCoords.x + fOffSet * vPixelSize.x, v_texCoords.y + fOffSet * vPixelSize.y );\n"
                + "		vSample[i] = texture2D( u_shadowTex, vTexCoord ).r;\n"
                + "	}\n"
                + " \n"
                + "    float fAccum;\n"
                + "    fAccum = log_conv( vGaussianBlur[0], vSample[0], vGaussianBlur[1], vSample[1] );\n"
                + "    for (int i = 2; i < 10; i++)\n"
                + "    {\n"
                + "        fAccum = log_conv( 1.0, fAccum, vGaussianBlur[i], vSample[i] );\n"
                + "    }        \n"
                + "    \n"
                + "    return vec4( fAccum, fAccum, fAccum, 1.0);\n"
                + "}\n"
                + "\n"
                + "void main()\n"
                + "{\n"
                + "     "
                + "	gl_FragColor = texture2D(u_sceneTex, v_texCoords) * LogGaussianFilter();\n"
                + "}\n";
                

        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (shaderProgram.isCompiled() == false) {
            throw new IllegalStateException(shaderProgram.getLog());
        }
    }
}
