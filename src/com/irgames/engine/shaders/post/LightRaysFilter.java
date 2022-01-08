/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.shaders.post;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.irgames.managers.DepthTextureManager;
import com.irgames.managers.LightingManager;
import com.irgames.managers.OcclusionTextureManager;

/**
 *
 * @author Andrew
 */
public class LightRaysFilter extends PostFilter {

    Vector2 scaleU;
    float intensity = 0.8f;

    public LightRaysFilter() {
        this.createShader();
        float BLUR_COEF = 0.8f;
        scaleU = new Vector2(1.0f / (Gdx.graphics.getWidth() * BLUR_COEF), 1.0f / (Gdx.graphics.getHeight()));
    }

    @Override
    public void onRender(Camera cam) {
        this.shaderProgram.setUniformf("ScaleU", scaleU);

        OcclusionTextureManager.occTex.bind(2);
        shaderProgram.setUniformi("u_occTex", 2);

        Vector3 lPosScreen = (LightingManager.getSunDirection());

        this.shaderProgram.setUniformf("u_lightDir", lPosScreen);
        this.shaderProgram.setUniformMatrix("u_VP", cam.combined);
        this.shaderProgram.setUniformf("Intensity", intensity);

    }

    @Override
    public void postRender() {

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
                + "uniform sampler2D u_occTex;\n"
                + "uniform vec2 ScaleU;\n"
                + "uniform mat4 u_VP;\n"
                + "uniform vec3 u_lightDir;\n"
                + "float Decay=0.96815;\n"
                + "float Exposure=0.2;\n"
                + "float Density=0.9;\n"
                + "float Weight=0.8;\n"
                + "uniform float Intensity = 0.5;\n"
                + ""
                + "const float NUM_SAMPLES = 100.0;\n"
      
                + "\nvoid main() {\n"
                + "   vec2 texCoord = v_texCoords;\n"
                + "   vec4 lPos = u_VP * vec4(-u_lightDir, 0.0);\n"
                + "   vec2 ScreenLightPos = 0.5*lPos.xy+0.5;\n"
                + "   // Calculate vector from pixel to light source in screen space.  \n"
                + "   vec2 deltaTexCoord = (texCoord - ScreenLightPos.xy);  \n"
                + "  // Divide by number of samples and scale by control factor.  \n"
                + "   deltaTexCoord *= 1.0 / NUM_SAMPLES * Density;  \n"
                + "  // Store initial sample.  \n"
                + "   vec3 color = texture2D(u_occTex, texCoord).rgb;  \n"

                + "  // Set up illumination decay factor.  \n"
                + "   float illuminationDecay = 1.0;  \n"
                + "  // Evaluate summation from Equation 3 NUM_SAMPLES iterations.  \n"
                + "   for (int i = 0; i < NUM_SAMPLES; i++)  \n"
                + "  {  \n"
                + "    // Step sample location along ray.  \n"
                + "    texCoord -= deltaTexCoord;  \n"
                + "    // Retrieve sample at new location.  \n"
                + "    vec3 sample = texture2D(u_occTex, texCoord).rgb;  \n"
        
                + "    // Apply sample attenuation scale/decay factors.  \n"
                + "    sample *= illuminationDecay * Weight;  \n"
                + "    // Accumulate combined color.  \n"
                + "    color += sample;  \n"
                + "    // Update exponential decay factor.  \n"
                + "    illuminationDecay *= Decay;  \n"
                + "  }  \n"
                + "  // Output final color with a further scale control factor.  \n"
                + "   gl_FragColor = texture2D(u_sceneTex, v_texCoords) + vec4( color * Exposure, 1.0);\n "
                + "   "
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
