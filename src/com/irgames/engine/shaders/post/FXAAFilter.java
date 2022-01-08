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
public class FXAAFilter extends PostFilter {

   

    public FXAAFilter() {
        this.createShader();
       
    }

    @Override
    public void onRender(Camera cam) {
       
        shaderProgram.setUniformf("frameBufSize", new Vector2(Gdx.graphics.getWidth(),Gdx.graphics.getHeight()));
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
        String fragmentShader = "uniform sampler2D u_sceneTex;\n"
                + "uniform vec2 frameBufSize;\n"
                + "varying vec2 v_texCoords;\n"
                + "\n"
                + "void main() {\n"
                + "    //gl_FragColor.xyz = texture2D(u_sceneTex,v_texCoords).xyz;\n"
                + "    //return;\n"
                + "\n"
                + "    float FXAA_SPAN_MAX = 8.0;\n"
                + "    float FXAA_REDUCE_MUL = 1.0/8.0;\n"
                + "    float FXAA_REDUCE_MIN = 1.0/128.0;\n"
                + "\n"
                + "    vec3 rgbNW=texture2D(u_sceneTex,v_texCoords+(vec2(-1.0,-1.0)/frameBufSize)).xyz;\n"
                + "    vec3 rgbNE=texture2D(u_sceneTex,v_texCoords+(vec2(1.0,-1.0)/frameBufSize)).xyz;\n"
                + "    vec3 rgbSW=texture2D(u_sceneTex,v_texCoords+(vec2(-1.0,1.0)/frameBufSize)).xyz;\n"
                + "    vec3 rgbSE=texture2D(u_sceneTex,v_texCoords+(vec2(1.0,1.0)/frameBufSize)).xyz;\n"
                + "    vec3 rgbM=texture2D(u_sceneTex,v_texCoords).xyz;\n"
                + "\n"
                + "    vec3 luma=vec3(0.299, 0.587, 0.114);\n"
                + "    float lumaNW = dot(rgbNW, luma);\n"
                + "    float lumaNE = dot(rgbNE, luma);\n"
                + "    float lumaSW = dot(rgbSW, luma);\n"
                + "    float lumaSE = dot(rgbSE, luma);\n"
                + "    float lumaM  = dot(rgbM,  luma);\n"
                + "\n"
                + "    float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));\n"
                + "    float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));\n"
                + "\n"
                + "    vec2 dir;\n"
                + "    dir.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));\n"
                + "    dir.y =  ((lumaNW + lumaSW) - (lumaNE + lumaSE));\n"
                + "\n"
                + "    float dirReduce = max(\n"
                + "        (lumaNW + lumaNE + lumaSW + lumaSE) * (0.25 * FXAA_REDUCE_MUL),\n"
                + "        FXAA_REDUCE_MIN);\n"
                + "\n"
                + "    float rcpDirMin = 1.0/(min(abs(dir.x), abs(dir.y)) + dirReduce);\n"
                + "\n"
                + "    dir = min(vec2( FXAA_SPAN_MAX,  FXAA_SPAN_MAX),\n"
                + "          max(vec2(-FXAA_SPAN_MAX, -FXAA_SPAN_MAX),\n"
                + "          dir * rcpDirMin)) / frameBufSize;\n"
                + "\n"
                + "    vec3 rgbA = (1.0/2.0) * (\n"
                + "        texture2D(u_sceneTex, v_texCoords.xy + dir * (1.0/3.0 - 0.5)).xyz +\n"
                + "        texture2D(u_sceneTex, v_texCoords.xy + dir * (2.0/3.0 - 0.5)).xyz);\n"
                + "    vec3 rgbB = rgbA * (1.0/2.0) + (1.0/4.0) * (\n"
                + "        texture2D(u_sceneTex, v_texCoords.xy + dir * (0.0/3.0 - 0.5)).xyz +\n"
                + "        texture2D(u_sceneTex, v_texCoords.xy + dir * (3.0/3.0 - 0.5)).xyz);\n"
                + "    float lumaB = dot(rgbB, luma);\n"
                + "\n"
                + "    if((lumaB < lumaMin) || (lumaB > lumaMax)){\n"
                + "        gl_FragColor.xyz=rgbA;\n"
                + "    }else{\n"
                + "        gl_FragColor.xyz=rgbB;\n"
                + "    }\n"
                + "}";

        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (shaderProgram.isCompiled() == false) {
            throw new IllegalStateException(shaderProgram.getLog());
        }
    }
}
