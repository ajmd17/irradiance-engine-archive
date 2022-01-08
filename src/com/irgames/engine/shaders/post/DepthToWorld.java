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
import com.irgames.managers.DepthTextureManager;

/**
 *
 * @author Andrew
 */
public class DepthToWorld extends PostFilter {

    @Override
    public void onRender(Camera cam) {
        DepthTextureManager.depthTex.bind(8);
        shaderProgram.setUniformi("u_depthMap", 8);
        shaderProgram.setUniformMatrix("u_invProjView", cam.invProjectionView);
    }

    public DepthToWorld() {
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
                + "uniform mat4 u_invProjView;\n"
                + "uniform vec2 frustCorner;\n"
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
                + ""
                + "void main()                                  \n"
                + "{                                            \n"
                + "  float depth = unpack_depth(texture2D(u_depthMap, v_texCoords));\n"
                + "  if (depth < 1.0) {\n"
                + "     gl_FragColor = vec4(getPosition(v_texCoords, depth), 1.0);\n"
                + "  }\n"
                + "}";
        
        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (shaderProgram.isCompiled() == false) {
            throw new IllegalStateException(shaderProgram.getLog());
        }
    }

}
