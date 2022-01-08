/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.deferredrendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.components.IRSpatial;
import static com.irgames.managers.DepthTextureManager.depthTex;
import com.irgames.managers.LightingManager;
import com.irgames.managers.RenderManager;
import com.irgames.utils.RenderUtils;
import com.irgames.utils.RenderUtils.Bucket;

/**
 *
 * @author Andrew
 */
public class DeferredRenderManager {

    NormalsShader normShader;
    DiffuseShader diffuseShader;
    FrameBuffer fboNormal, fboDiffuse;
    static Texture normalMap, diffuseMap;
    boolean loaded = false;
    RenderManager ren;

    public static Texture getNormalMap() {
        return normalMap;
    }

    public static Texture getDiffuseMap() {
        return diffuseMap;
    }

    public DeferredRenderManager(RenderManager renderManager) {
        normShader = new NormalsShader();
        normShader.init();
        diffuseShader = new DiffuseShader();
        diffuseShader.init();
        initializeFBONormal();
        initializeFBODiffuse();
        ren = renderManager;
    }

    private void initializeFBODiffuse() {
        if (fboDiffuse != null) {
            fboDiffuse.dispose();
        }
        fboDiffuse = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        fboDiffuse.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    private void initializeFBONormal() {
        if (fboNormal != null) {
            fboNormal.dispose();
        }
        fboNormal = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        fboNormal.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    private void capture(FrameBuffer fbo) {

        Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);

        fbo.begin();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

    }

    private void release(FrameBuffer fbo) {
        if (fbo != null) {
            fbo.end();

            Gdx.gl20.glDisable(GL20.GL_TEXTURE_2D);
        }
    }

    public void renderDiffuse(Camera cam, RenderContext ctx) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        capture(fboDiffuse);

        for (IRSpatial n : RenderManager.sceneObjects) {
            if (n.mesh != null && n.shader != null && n.getBucket() != Bucket.sky) {
                diffuseShader.begin(cam, ctx);
                diffuseShader.applyMaterial(n.getMaterial());
                diffuseShader.render(n);
                diffuseShader.end();
            }
        }

        release(fboDiffuse);
        diffuseMap = fboDiffuse.getColorBufferTexture();
        diffuseMap.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        diffuseMap.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
    }

    public void renderNormals(Camera cam, RenderContext ctx) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        capture(fboNormal);
        normShader.begin(cam, ctx);
        for (IRSpatial n : RenderManager.sceneObjects) {
            if (n.mesh != null && n.shader != null && n.getBucket() != Bucket.sky) {
                normShader.render(n);
            }
        }
        normShader.end();
        release(fboNormal);
        normalMap = fboNormal.getColorBufferTexture();

    }

}
