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
package com.irgames.engine.sky;

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
import com.irgames.engine.components.IRShader;
import com.irgames.engine.shaders.LightShader;
import com.irgames.managers.LightingManager;
import com.irgames.utils.RenderUtils;
import com.irgames.utils.RenderUtils.BackfaceCullMode;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 *
 * @author Xoppa
 */
public class AtmosphereShader extends LightShader {

    
    Camera camera;
    RenderContext context;
    int u_projTrans;
    int u_worldTrans;
    Texture grass, rock;
    Texture starMap;

    
    public float globalTime;
    
    private Vector3 sunPosition = new Vector3();
    private Vector3 wavelengths = new Vector3();
    private Vector3 wavelengthsPow4 = new Vector3();
    private Vector3 invWavelengths = new Vector3();
    private Vector3 invPow4WavelengthsKrESun = new Vector3();
    private Vector3 scatteringConstants = new Vector3();
    private Vector3 kWavelengths4PI = new Vector3();
    private Vector3 invPow4Wavelengths = new Vector3();

    public final void setMieConstant(float f) {

        //  this.scatteringConstants.z = f;
        this.scatteringConstants.z = f * 4 * (float) Math.PI;

    }

    public final void setRayleighConstant(float f) {
        if (this.scatteringConstants.x == f) {
            return;
        }
        this.scatteringConstants.x = f;
        this.scatteringConstants.y = f * 4 * (float) Math.PI;

    }

    public final void setWavelengths(float r, float g, float b) {
        wavelengths.set(r, g, b);
        wavelengthsPow4.x = (float) Math.pow(wavelengths.x, 4);
        wavelengthsPow4.y = (float) Math.pow(wavelengths.y, 4);
        wavelengthsPow4.z = (float) Math.pow(wavelengths.z, 4);
        invWavelengths.x = 1 / wavelengthsPow4.x;
        invWavelengths.y = 1 / wavelengthsPow4.y;
        invWavelengths.z = 1 / wavelengthsPow4.z;

    }
    float lightIntensity = 1.0f;

    protected void updatePackedStructures() {
        //vec3 attenuation = exp(-scatter * (m_InvWavelengths * r4PI + m4PI));
        // K(wavelengths) * 4 * PI = m_InvWavelengths * r4PI + m4PI  
        this.scatteringConstants.y = 0.0025f * 4 * (float) Math.PI;
        this.scatteringConstants.z = 0.0010f * 4 * (float) Math.PI;

        float r4PI = scatteringConstants.y;
        float m4PI = scatteringConstants.z;
        kWavelengths4PI.x = invWavelengths.x * r4PI + m4PI;
        kWavelengths4PI.y = invWavelengths.y * r4PI + m4PI;
        kWavelengths4PI.z = invWavelengths.x * r4PI + m4PI;

        float rESun = scatteringConstants.x * lightIntensity;
        invPow4WavelengthsKrESun.x = invWavelengths.x * rESun;
        invPow4WavelengthsKrESun.y = invWavelengths.y * rESun;
        invPow4WavelengthsKrESun.z = invWavelengths.x * rESun;
    }

    @Override
    public void init() {
        String vert = Gdx.files.internal("data/shaders/atmosphere/sky.vertex.glsl").readString();
        String frag = Gdx.files.internal("data/shaders/atmosphere/sky.fragment.glsl").readString();
        //this.cullMode = BackfaceCullMode.front;
        starMap = new Texture(Gdx.files.internal("data/sky/textures/stars.png"), true);
        starMap.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        starMap.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        program = new ShaderProgram(vert, frag);
        if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
        }
        u_projTrans = program.getUniformLocation("u_projViewTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
        program.pedantic = false;
        LightingManager.addShader(this);

    }

    @Override
    public void dispose() {
        program.dispose();
    }

    public void setSunDirection(Vector3 dir) {
        this.sunPosition = dir;
    }
    float mpaFactor = -0.990f;
    float g = mpaFactor;
    float g2 = g * g;
    float phasePrefix1 = 1.5f * ((1.0f - g2) / (2.0f + g2));
    float phasePrefix2 = 1.0f + g2;
    float phasePrefix3 = 2.0f * g;

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        setRayleighConstant(0.0025f);
        setMieConstant(0.001f);
        setWavelengths(0.5f, 0.5f, 0.5f);
        updatePackedStructures();

        program.begin();
        program.setUniformMatrix(u_projTrans, camera.combined);
        starMap.bind(0);
        program.setUniformi("u_StarMap", 0);
        program.setUniformf("u_cameraPosition", camera.position);
        program.setUniformf("u_cameraHeight2", camera.viewportHeight * camera.viewportHeight);
        program.setUniformf("u_invWavelengths", invWavelengths);
        program.setUniformf("u_KmESun", scatteringConstants.x * 0.2f);
        program.setUniformf("u_Kr4PI", scatteringConstants.y);
        program.setUniformf("u_Km4PI", scatteringConstants.z);
        program.setUniformf("u_sunPosition", sunPosition);
        program.setUniformf("u_sunColor", this.lightColor);
        program.setUniformf("u_globalTime", globalTime);
        program.setUniformf("m_PhasePrefix1", phasePrefix1);
        program.setUniformf("m_PhasePrefix2", phasePrefix2);
        program.setUniformf("m_PhasePrefix3", phasePrefix3);
        //  context.setDepthTest(GL20.GL_LEQUAL);
        //context.setCullFace(GL20.GL_FRONT);
    }
    Matrix4 worldTransform = new Matrix4();
    @Override
    public void render(Renderable renderable) {
        this.preRender();
        worldTransform.set(renderable.worldTransform);
        worldTransform.setTranslation(camera.position);
        program.setUniformMatrix(u_worldTrans, worldTransform);
        Gdx.gl.glDepthMask(false);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
       // Gdx.gl.glCullFace(GL20.GL_FRONT);
        super.render(renderable);
        Gdx.gl.glDepthMask(true);
       // Gdx.gl.glCullFace(GL20.GL_BACK);
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
