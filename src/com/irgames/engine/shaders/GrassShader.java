/**
 * *****************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ****************************************************************************
 */
package com.irgames.engine.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 *
 * @author Xoppa
 */
public class GrassShader extends LightShader {

    Camera camera;
    RenderContext context;
    int u_projTrans;
    int u_worldTrans;
    Texture grassTex, noiseMap;
    float time, windSpeed, windAmount, maxViewDistance = 70.0f, fadeStart = 20.0f;

    public void setWindSpeed(float speed) {
        windSpeed = speed;
    }

    public void setWindAmount(float amt) {
        windAmount = amt;
    }

    public void setFade(float start, float end) {
        maxViewDistance = end;
        fadeStart = start;
    }
    public boolean yFlipped;

    public GrassShader() {
      //  this.grassTex = gTex;
        // this.noiseMap = noiseMap;
    }

    @Override
    public void init() {
        String vert = Gdx.files.internal("data/shaders/grass/grass.vertex.glsl").readString();
        String frag = Gdx.files.internal("data/shaders/grass/grass.fragment.glsl").readString();
        program = new ShaderProgram(vert, frag);
        if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
        }
        u_projTrans = program.getUniformLocation("u_projViewTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
        program.pedantic = false;

    }

    @Override
    public void dispose() {
        program.dispose();
    }

    public void setTime(float tm) {
        time = tm;
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        program.begin();
        program.setUniformMatrix(u_projTrans, camera.combined);
        program.setUniformf("u_cameraPosition", camera.position);
        // context.setDepthTest(GL20.GL_LEQUAL);
        //context.setCullFace(GL20.GL_BACK);
        //grassTex.bind(0);
        //program.setUniformi("u_texture", 0);
        //noiseMap.bind(1);
        program.setUniformi("u_noiseMap", 1);
        program.setUniformf("u_alphaDiscard", 0.35f);
        program.setUniformf("u_fogStart", fogStart);
        program.setUniformf("u_fogEnd", fogEnd);
        program.setUniformf("u_fogColor", fogColor);
        program.setUniformf("u_time", time);
        program.setUniformf("u_windSpeed", windSpeed);
        program.setUniformf("u_windAmount", windAmount * 0.1f);
        program.setUniformf("u_maxViewDistance", maxViewDistance);
        program.setUniformf("u_fadeStart", fadeStart);
        program.setUniformf("u_lightColor", lightColor);
    }

    @Override
    public void render(Renderable renderable) {

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_NICEST);
        Texture diffuseTex = this.irmat.getTexture("diffuse");
        if (diffuseTex != null) {
            diffuseTex.bind(0);
            program.setUniformi("u_texture", 0);
        }
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glDepthMask(true);
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        super.render(renderable);

        program.setUniformf("u_alphaDiscard", 0.0f);
        Gdx.gl.glDepthMask(false);
        // super.render(renderable);
        Gdx.gl.glDepthMask(true);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void end() {
        program.end();
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return true;
    }
}
