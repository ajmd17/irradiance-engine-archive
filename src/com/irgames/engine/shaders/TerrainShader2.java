/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.irgames.managers.LightingManager;
import com.irgames.utils.RenderUtils;
import com.irgames.utils.RenderUtils.BackfaceCullMode;
import com.irgames.utils.RenderUtils.DepthTestMode;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 *
 * @author Xoppa
 */
public class TerrainShader2 extends LightShader {

    Camera camera;
    RenderContext context;
    int u_projTrans;
    int u_worldTrans;
    Texture leaf, normalMap;
    public boolean flipY = true;
    public DepthTestMode depthTestMode = DepthTestMode.on;
    public BackfaceCullMode faceCullMode = BackfaceCullMode.back;
    String texPath, texPathNorm;
    public int[] regions = new int[]{0, 20, 40, 60};
    public boolean debugView = false;

    public TerrainShader2() {

    }

    @Override
    public void init() {
        String vert = Gdx.files.internal("data/shaders/terrain/terrain.vertex.glsl").readString();
        String frag = Gdx.files.internal("data/shaders/terrain/terrain.fragment.glsl").readString();
        //this.setProperty("TURN_RED", false);
        vert = this.format(vert);
        frag = this.format(frag);
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

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        program.begin();
        context.setDepthTest(GL20.GL_LEQUAL);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);

        program.setUniformMatrix(u_projTrans, camera.combined);
        program.setUniformf("u_cameraPosition", camera.position);
    }

    @Override
    public void render(Renderable renderable) {
        super.preRender();
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        program.setUniformf("u_lightDirection", lightDirection);
        program.setUniformf("u_fogStart", LightingManager.getFogStart());
        program.setUniformf("u_fogEnd", LightingManager.getFogEnd());
        program.setUniformf("u_fogColor", fogColor);
        program.setUniformf("u_lightColor", lightColor);
        program.setUniformf("u_ambient", LightingManager.getAmbientColor());
        Color alb = irmat.getColor("albedo");
        if (alb == null) {
            alb = Color.WHITE;
        }
        program.setUniformf("u_albedo", alb);

        if (this.getBoolean("GRASS_TEX")) {
            Texture gt = this.irmat.getTexture("grass");
            if (gt != null) {
                gt.bind(0);
                program.setUniformi("u_grassTex", 0);
            }
        }
        if (this.getBoolean("SLOPE_TEX")) {
            Texture st = this.irmat.getTexture("slope");
            if (st != null) {
                st.bind(1);
                program.setUniformi("u_slopeTex", 1);
            }
        }
        if (this.getBoolean("REGIONS")) {
            for (int i = 0; i < 3; i++) {
                program.setUniformi("u_region" + i, regions[i]);
            }
            Texture r1 = this.irmat.getTexture("region1");
            if (r1 != null) {
                r1.bind(2);
                program.setUniformi("u_region1Tex", 2);
            }
            Texture r2 = this.irmat.getTexture("region2");
            if (r2 != null) {
                r2.bind(3);
                program.setUniformi("u_region2Tex", 3);
            }
            Texture r3 = this.irmat.getTexture("region3");
            if (r3 != null) {
                r3.bind(4);
                program.setUniformi("u_region2Tex", 4);
            }
        }
        
        super.render(renderable);
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
