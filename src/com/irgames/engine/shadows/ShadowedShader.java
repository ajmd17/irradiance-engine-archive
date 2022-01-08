/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.shadows;

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
import com.irgames.engine.shaders.LightShader;
import com.irgames.managers.DepthTextureManager;
import com.irgames.utils.RenderUtils;
import com.irgames.utils.RenderUtils.BackfaceCullMode;
import com.irgames.utils.RenderUtils.DepthTestMode;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 *
 * @author Xoppa
 */
public class ShadowedShader extends LightShader {

    ShaderProgram program;
    Camera camera;
    RenderContext context;
    int u_projTrans;
    int u_worldTrans;
    Texture leaf, normalMap;
    public boolean flipY = true;
    public DepthTestMode depthTestMode = DepthTestMode.on;
    public BackfaceCullMode faceCullMode = BackfaceCullMode.back;
    String texPath, texPathNorm;
    public Matrix4 u_shadowMapProjViewTrans0, u_shadowMapProjViewTrans1, u_shadowMapProjViewTrans2, u_shadowMapProjViewTrans3;
    public Texture texture;
    public Vector3 lightPosition;
    public float[] ranges = new float[4];
    public int renderSplits = 0;
    public ShadowedShader() {

    }

    @Override
    public void init() {
        String vert = Gdx.files.internal("data/shaders/simplelit/simpleshadow.vertex.glsl").readString();
        String frag = Gdx.files.internal("data/shaders/simplelit/simpleshadow.fragment.glsl").readString();
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

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        program.begin();
        context.setDepthTest(GL20.GL_LEQUAL);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);
        program.setUniformMatrix(u_projTrans, camera.combined);

    }
    Vector3 rangev3 = new Vector3();

    @Override
    public void render(Renderable renderable) {
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        for (int i = 0; i < DepthTextureManager.shadowMaps.length; i++) {
            DepthTextureManager.shadowMaps[i].bind(i);
            program.setUniformi("u_shadowMap" + i, i);
        }
        program.setUniformf("u_lightDirection", lightDirection);
        program.setUniformf("u_fogStart", fogStart);
        program.setUniformf("u_fogEnd", fogEnd);
        program.setUniformf("u_fogColor", fogColor);
        program.setUniformf("u_lightColor", lightColor);
        program.setUniformi("u_renderSplits", renderSplits);
        rangev3.set(ranges[0], ranges[1], ranges[2]);
        program.setUniformf("u_ranges", rangev3);
        program.setUniformMatrix("u_shadowMapProjViewTrans0", u_shadowMapProjViewTrans0);
        program.setUniformMatrix("u_shadowMapProjViewTrans1", u_shadowMapProjViewTrans1);
        program.setUniformMatrix("u_shadowMapProjViewTrans2", u_shadowMapProjViewTrans2);
        program.setUniformMatrix("u_shadowMapProjViewTrans3", u_shadowMapProjViewTrans3);
        renderable.mesh.render(program,
                renderable.primitiveType,
                renderable.meshPartOffset,
                renderable.meshPartSize);
      //  Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void end() {
        program.end();
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
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
