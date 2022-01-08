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
import com.irgames.managers.DepthTextureManager;

/**
 *
 * @author Andrew
 */
public class DepthOfField extends PostFilter {

    public DepthOfField() {
        this.createShader();

    }

    @Override
    public void onRender(Camera cam) {
        if (DepthTextureManager.depthTex != null) {
            DepthTextureManager.depthTex.bind(2);
            shaderProgram.setUniformi("u_depthTex", 2);
        }
        shaderProgram.setUniformi("u_width", Gdx.graphics.getWidth());
        shaderProgram.setUniformi("u_height", Gdx.graphics.getHeight());
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
                + "uniform sampler2D u_sceneTex;\n"
                + "uniform sampler2D u_depthTex;\n"
                + "uniform float u_width;\n"
                + "uniform float u_height;\n"
                + "const float blurclamp = 3.0;  // max blur amount\n"
                + "const float bias = 0.6;	//aperture - bigger values for shallower depth of field\n"
                + "uniform float focus;  // this value comes from ReadDepth script.\n"
                + " \n"
                + "void main() \n"
                + "{\n"
                + "\n"
                + "	float aspectratio = 800.0/600.0;\n"
                + "	vec2 aspectcorrect = vec2(1.0,1.0);\n"
                + "	\n"
                + "	vec4 depth1   = texture2D(u_depthTex,v_texCoords.xy );\n"
                + "\n"
                + "	float factor = ( depth1.x - focus );\n"
                + "	 \n"
                + "	vec2 dofblur = vec2 (clamp( factor * bias, -blurclamp, blurclamp ));\n"
                + "\n"
                + "\n"
                + "	vec4 col = vec4(0.0);\n"
                + "	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy);\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.0,0.4 )*aspectcorrect) * dofblur);\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.15,0.37 )*aspectcorrect) * dofblur);\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.29,0.29 )*aspectcorrect) * dofblur);\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( -0.37,0.15 )*aspectcorrect) * dofblur);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.4,0.0 )*aspectcorrect) * dofblur);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.37,-0.15 )*aspectcorrect) * dofblur);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.29,-0.29 )*aspectcorrect) * dofblur);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( -0.15,-0.37 )*aspectcorrect) * dofblur);\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.0,-0.4 )*aspectcorrect) * dofblur);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( -0.15,0.37 )*aspectcorrect) * dofblur);\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( -0.29,0.29 )*aspectcorrect) * dofblur);\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.37,0.15 )*aspectcorrect) * dofblur);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( -0.4,0.0 )*aspectcorrect) * dofblur);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( -0.37,-0.15 )*aspectcorrect) * dofblur);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( -0.29,-0.29 )*aspectcorrect) * dofblur);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.15,-0.37 )*aspectcorrect) * dofblur);\n"
                + "	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.15,0.37 )*aspectcorrect) * dofblur*0.9);\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( -0.37,0.15 )*aspectcorrect) * dofblur*0.9);		\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.37,-0.15 )*aspectcorrect) * dofblur*0.9);		\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( -0.15,-0.37 )*aspectcorrect) * dofblur*0.9);\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( -0.15,0.37 )*aspectcorrect) * dofblur*0.9);\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.37,0.15 )*aspectcorrect) * dofblur*0.9);		\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( -0.37,-0.15 )*aspectcorrect) * dofblur*0.9);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.15,-0.37 )*aspectcorrect) * dofblur*0.9);	\n"
                + "	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.29,0.29 )*aspectcorrect) * dofblur*0.7);\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.4,0.0 )*aspectcorrect) * dofblur*0.7);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.29,-0.29 )*aspectcorrect) * dofblur*0.7);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.0,-0.4 )*aspectcorrect) * dofblur*0.7);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( -0.29,0.29 )*aspectcorrect) * dofblur*0.7);\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( -0.4,0.0 )*aspectcorrect) * dofblur*0.7);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( -0.29,-0.29 )*aspectcorrect) * dofblur*0.7);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.0,0.4 )*aspectcorrect) * dofblur*0.7);\n"
                + "			 \n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.29,0.29 )*aspectcorrect) * dofblur*0.4);\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.4,0.0 )*aspectcorrect) * dofblur*0.4);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.29,-0.29 )*aspectcorrect) * dofblur*0.4);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.0,-0.4 )*aspectcorrect) * dofblur*0.4);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( -0.29,0.29 )*aspectcorrect) * dofblur*0.4);\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( -0.4,0.0 )*aspectcorrect) * dofblur*0.4);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( -0.29,-0.29 )*aspectcorrect) * dofblur*0.4);	\n"
                + "	col += texture2D(u_sceneTex, v_texCoords.xy + (vec2( 0.0,0.4 )*aspectcorrect) * dofblur*0.4);	\n"
                + "			\n"
                + "	gl_FragColor = col/41.0;\n"
                + "	gl_FragColor.a = 1.0;\n"
                + "}\n";

        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (shaderProgram.isCompiled() == false) {
            throw new IllegalStateException(shaderProgram.getLog());
        }
    }
}
