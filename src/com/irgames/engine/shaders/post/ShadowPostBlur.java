/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.shaders.post;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.irgames.managers.DepthTextureManager;
import com.irgames.managers.LightingManager;
import com.irgames.managers.PostProcessManager;

/**
 *
 * @author Andrew
 */
public class ShadowPostBlur extends PostFilter {
    public float shadowIntensity = 0.4f;
    float blurAmount = 0.015f;
    int blur_samples = 6;
    Vector2 scaleU = new Vector2();
    Texture shadowTex;
    float BLUR_COEF = 0.35f;

    public ShadowPostBlur() {
        this.createShader();

    }

    @Override
    public void onRender(Camera cam) {

        this.shaderProgram.setUniformf("u_blurAmount", blurAmount);
        ShadowPostFilter.postShadowFbo.getColorBufferTexture().bind(0);
        this.shaderProgram.setUniformi("u_shadowTex", 0);
        this.shaderProgram.setUniformi("blur_samples", blur_samples);
        this.shaderProgram.setUniformf("u_ambientColor", LightingManager.getAmbientColor());
        this.shaderProgram.setUniformf("u_width", Gdx.graphics.getWidth());
        this.shaderProgram.setUniformf("u_height", Gdx.graphics.getHeight());
        this.shaderProgram.setUniformf("u_shadowIntensity", shadowIntensity);
        if (DepthTextureManager.depthTex != null) {
            DepthTextureManager.depthTex.bind(3);
            this.shaderProgram.setUniformi("u_depthTex", 3);
        }
    }

    private void createShader() {
        String vertexShader = "attribute vec4 a_Position;    \n"
                + "attribute vec4 a_Color;\n"
                + "attribute vec2 a_texCoords;\n"
                + ""
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
                + "uniform sampler2D u_depthTex;\n"
                + "uniform sampler2D u_sceneTex;\n"
                + "uniform sampler2D u_shadowTex;\n"
                + "uniform float u_shadowIntensity;\n"
                + "uniform vec4 u_ambientColor;\n"
                + "uniform vec2 ScaleU;\n"
                + "uniform float u_blurAmount;\n"
                + "uniform int blur_samples;\n"
                + "uniform float u_width;\n"
                + "uniform float u_height;\n"
                + "int xamt = blur_samples;\n"
                + "int yamt = blur_samples;\n"
                + "const float shadow_Bias = 7.0;\n"
                + "float unpack_depth(const in vec4 rgba_depth){\n"
                + "    const vec4 bit_shift =\n"
                + "        vec4(1.0/(256.0*256.0*256.0)\n"
                + "            , 1.0/(256.0*256.0)\n"
                + "            , 1.0/256.0\n"
                + "            , 1.0);\n"
                + "    float depth = dot(rgba_depth, bit_shift);\n"
                + "    return depth;\n"
                + "}\n"
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
                + "  return threshold(0.0,1.0,clamp(10.0*delta,0.0,1.0));\n"
                + "}\n"
                + "\n"
                + "void main()                                  \n"
                + "{                                            \n"
                + "     vec4 col = texture2D(u_shadowTex, v_texCoords);\n"
                + "     vec4 blurred;\n"
                + "     float depth = unpack_depth(texture2D(u_depthTex, v_texCoords));\n"
                + "     for (int x = -(xamt/2); x < (xamt/2); x++) {\n"
                + "         for (int y = -(yamt/2); y < (yamt/2); y++) {\n"
                + "             float distanceOrigin = distance(vec2(x, y), vec2(0.0));\n"
                + "             vec4 currentColor;\n"
                + "             float exp = (col.r);\n"
                + "             currentColor = texture2D(u_shadowTex, v_texCoords + vec2(x*(1.0-depth)*u_blurAmount, y*(1.0-depth)*u_blurAmount));\n"
                + "             //if (currentColor.r > 0.6) {\n"
                + "                 //currentColor = col.r;\n"
                + "             //}\n"
               
                /* + "             if (currentColor.r < 1.0) {\n"
                 + "                 currentColor += (1.0 - distanceOrigin) * 0.1;\n"
                 + "             }\n"*/
                + "             blurred += currentColor;\n"
                + "         }\n"
                + "     }\n"
                + "     blurred /= (xamt*yamt);\n"
         
    
                + "     blurred = clamp(blurred+vec4((1.0-u_shadowIntensity)), 0.0, 1.0);\n"
                + "     "
                /* + "     gl_FragColor += texture2D( u_shadowTex, v_texCoords + vec2( -3.0*ScaleU.x, -3.0*ScaleU.y ) ) * 0.015625;\n"
                 + "	gl_FragColor += texture2D( u_shadowTex, v_texCoords + vec2( -2.0*ScaleU.x, -2.0*ScaleU.y ) )*0.09375;\n"
                 + "	gl_FragColor += texture2D( u_shadowTex, v_texCoords + vec2( -1.0*ScaleU.x, -1.0*ScaleU.y ) )*0.234375;\n"
                 + "	gl_FragColor += texture2D( u_shadowTex, v_texCoords + vec2( 0.0 , 0.0) )*0.3125;\n"
                 + "	gl_FragColor += texture2D( u_shadowTex, v_texCoords + vec2( 1.0*ScaleU.x,  1.0*ScaleU.y ) )*0.234375;\n"
                 + "	gl_FragColor += texture2D( u_shadowTex, v_texCoords + vec2( 2.0*ScaleU.x,  2.0*ScaleU.y ) )*0.09375;\n"
                 + "	gl_FragColor += texture2D( u_shadowTex, v_texCoords + vec2( 3.0*ScaleU.x, -3.0*ScaleU.y ) ) * 0.015625;\n"*/
                + "     gl_FragColor = texture2D( u_sceneTex, v_texCoords) * blurred;// - ((vec4(1.0)-blurred)*u_shadowIntensity);\n"
                + "  \n"
                + "}";

        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (shaderProgram.isCompiled() == false) {
            throw new IllegalStateException(shaderProgram.getLog());
        }
    }
}
