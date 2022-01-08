/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.shaders.post;

import com.irgames.managers.DepthTextureManager;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.irgames.managers.EnvironmentMapper;

/**
 *
 * @author Andrew
 */
public class DepthTextureVisualizer extends PostFilter {

    public DepthTextureVisualizer() {
        this.createShader();
    }

    @Override
    public void onRender(Camera cam) {
        
      /*  if (EnvironmentMapper.probes.size() > 0) {
            EnvironmentMapper.probes.get(0).tex[5].bind(0);
            this.shaderProgram.setUniformi("u_texture", 0);
        }*/
        
       
        if (DepthTextureManager.shadowMaps[1] != null) {
            DepthTextureManager.shadowMaps[1].bind(0);
            this.shaderProgram.setUniformi("u_texture", 0);
        }
    }

    private void createShader() {
        // u_texture will be the depth map in this case

        String vertexShader = "attribute vec4 a_Position;    \n" + "attribute vec4 a_Color;\n" + "attribute vec2 a_texCoords;\n"
                + "varying vec4 v_Color;" + "varying vec2 v_texCoords; \n"
                + "void main()                  \n" + "{                            \n" + "   v_Color = a_Color;"
                + "   v_texCoords = a_texCoords;\n" + "   gl_Position =   a_Position;  \n" + "}                            \n";
        String fragmentShader = "#ifdef GL_ES\n"
                + "precision mediump float;\n"
                + "#endif\n"
                + "varying vec4 v_Color;\n" + "varying vec2 v_texCoords; \n"
                + "uniform sampler2D u_texture;\n uniform sampler2D u_sceneTex;\n"
                + ""
                + "float LinearizeDepth(vec2 uv)\n"
                + "{\n"
                + "  float n = 1.0; // camera z near\n"
                + "  float f = 100.0; // camera z far\n"
                + "  float z = texture2D(u_texture, uv).x;\n"
                + "  return (2.0 * n) / (f + n - z * (f - n));	\n"
                + "}\n"
                + ""
                + ""
                + ""
                + "void main()                                  \n" + "{                                            \n"
                + ""
                + "vec2 uv = v_texCoords;\n"
                + "  //vec4 sceneTexel = texture2D(sceneSampler, uv);\n"
                + "  float d;\n"
                + "  "
                + "    d = LinearizeDepth(uv);\n" // linearize the depth to make it more visible
                + ""
                + ""
                + ""
                + ""
                + ""
                + "  gl_FragColor = vec4(texture2D(u_texture, uv).rgb, 1.0);\n" + "}";

        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (shaderProgram.isCompiled() == false) {
            throw new IllegalStateException(shaderProgram.getLog());
        }
    }
}
