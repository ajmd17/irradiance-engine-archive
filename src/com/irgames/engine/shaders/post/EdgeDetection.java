/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.shaders.post;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 *
 * @author Andrew
 */
public class EdgeDetection extends PostFilter {

    public EdgeDetection() {
        this.createShader();
    }
    @Override
    public void onRender(Camera cam) {
        this.shaderProgram.setUniformf("u_width", Gdx.graphics.getWidth());
        this.shaderProgram.setUniformf("u_height", Gdx.graphics.getHeight());
    }
    private void createShader() {
        String vertexShader = "attribute vec4 a_Position;    \n" + "attribute vec4 a_Color;\n" + "attribute vec2 a_texCoords;\n"
                + "varying vec4 v_Color;" + "varying vec2 v_texCoords; \n"
                + "void main()                  \n" + "{                            \n" + "   v_Color = a_Color;"
                + "   v_texCoords = a_texCoords;\n" + "   gl_Position =   a_Position;  \n" + "}                            \n";
        String fragmentShader = "#ifdef GL_ES\n"
                + "precision mediump float;\n"
                + "#endif\n"
                + "varying vec4 v_Color;\n"
                + "varying vec2 v_texCoords; \n"
                + "uniform sampler2D u_sceneTex;\n"
                + "uniform float u_width;\n"
                + "uniform float u_height;\n"
                + "\n"
                + "float threshold(in float thr1, in float thr2 , in float val) {\n"
                + " if (val < thr1) {return 0.0;}\n"
                + " if (val > thr2) {return 1.0;}\n"
                + " return val;\n"
                + "}\n"
                + "\n"
                + "// averaged pixel intensity from 3 color channels\n"
                + "float avg_intensity(in vec4 pix) {\n"
                + " return (pix.r + pix.g + pix.b)/3.;\n"
                + "}\n"
                + "\n"
                + "vec4 get_pixel(in vec2 coords, in float dx, in float dy) {\n"
                + " return texture2D(u_sceneTex,coords + vec2(dx, dy));\n"
                + "}\n"
                + "\n"
                + "// returns pixel color\n"
                + "float IsEdge(in vec2 coords){\n"
                + "  float dxtex = 1.0 / u_width /*image width*/;\n"
                + "  float dytex = 1.0 / u_height /*image height*/;\n"
                + "  float pix[9];\n"
                + "  int k = -1;\n"
                + "  float delta;\n"
                + "\n"
                + "  // read neighboring pixel intensities\n"
                + "  for (int i=-1; i<2; i++) {\n"
                + "   for(int j=-1; j<2; j++) {\n"
                + "    k++;\n"
                + "    pix[k] = avg_intensity(get_pixel(coords,float(i)*dxtex,\n"
                + "                                          float(j)*dytex));\n"
                + "   }\n"
                + "  }\n"
                + "\n"
                + "  // average color differences around neighboring pixels\n"
                + "  delta = (abs(pix[1]-pix[7])+\n"
                + "          abs(pix[5]-pix[3]) +\n"
                + "          abs(pix[0]-pix[8])+\n"
                + "          abs(pix[2]-pix[6])\n"
                + "           )/4.;\n"
                + "\n"
                + "  return threshold(0.6,1.0,clamp(50.0*delta,0.0,1.0));\n"
                + "}\n"
                + "\n"
                + "void main()\n"
                + "{\n"
                + "  vec4 color = vec4(0.0,0.0,0.0,1.0);\n"
                + "  color.g = IsEdge(v_texCoords.xy);\n"
                + "  gl_FragColor = color;\n"
                + "}";

        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (shaderProgram.isCompiled() == false) {
            throw new IllegalStateException(shaderProgram.getLog());
        }
    }
}
