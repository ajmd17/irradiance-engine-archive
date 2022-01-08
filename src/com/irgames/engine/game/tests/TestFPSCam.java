/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.game.tests;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.assets.MTLtoIRMAT;
import com.irgames.engine.components.IRMatSaveLoad;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.deferredrendering.DeferredRenderManager;
import com.irgames.engine.game.Game;
import static com.irgames.engine.game.Game.context;
import com.irgames.engine.pagingengine.GrassPopulator;
import com.irgames.engine.pagingengine.TreePopulator;
import com.irgames.engine.pbr.Presets;
import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.post.BloomFilter;
import com.irgames.engine.shaders.post.FXAAFilter;
import com.irgames.engine.shaders.post.GammaCorrection;
import com.irgames.engine.shaders.post.PostFilter;
import com.irgames.engine.shaders.post.SSAOFilter2;
import com.irgames.engine.shaders.post.ShadowPostFilter;
import com.irgames.engine.shadows.ShadowMapperComponent;
import com.irgames.engine.sky.SkyBoxComponent;
import com.irgames.engine.sky.SkyBoxLoader;
import com.irgames.engine.sky.SkyDomeComponent;
import com.irgames.engine.terrain.TerrainComponent;
import com.irgames.managers.DepthTextureManager;
import com.irgames.managers.RenderManager;
import com.irgames.managers.ShaderManager;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class TestFPSCam extends Game {

    boolean loaded = false;
    ModelInstance modelInstance;
    AnimationController controller;
    Environment environment;
    ModelBatch modelBatch;
    DeferredRenderManager drm;

    @Override
    public void init() {
        physics.renderDebug = false;
        this.setTitle("FPSCam - Press space to enable, Esc to disable");
        this.setBackgroundColor(Color.PINK);

        SkyBoxComponent skyBox = new SkyBoxComponent(SkyBoxLoader.getPath("siege", "right.jpg"), SkyBoxLoader.getPath("siege", "left.jpg"),
                SkyBoxLoader.getPath("siege", "top.jpg"), SkyBoxLoader.getPath("siege", "top.jpg"),
                SkyBoxLoader.getPath("siege", "front.jpg"), SkyBoxLoader.getPath("siege", "back.jpg")
        );
        addComponent(skyBox);
        drm = new DeferredRenderManager(renderManager);
        IRNode irn = Assets.loadObjNode(Gdx.files.getFileHandle("data/models/tests/level/level.obj", FileType.Internal));

        try {
            irn.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties()));
        } catch (Exception ex) {
            Logger.getLogger(TestFPSCam.class.getName()).log(Level.SEVERE, null, ex);
        }
        // irn.setMaterial(new Presets().PBR_RedPaint);
        System.out.println(irn.getChildren().size());
        try {
            MTLtoIRMAT.convert(Gdx.files.getFileHandle("data/models/tests/level/level.mtl", FileType.Internal).readString(), "data/models/tests/level/level.irmat");
            IRMatSaveLoad.load(irn, "data/models/tests/level/level.irmat");
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            Logger.getLogger(TestFPSCam.class.getName()).log(Level.SEVERE, null, ex);
        }
        irn.scale(new Vector3(2, 2, 2));
        rootNode.attachChild(irn);

         ShadowMapperComponent shadowMapper = new ShadowMapperComponent();
         addComponent(shadowMapper);
         ShadowPostFilter filter;
         processor.add(filter = new ShadowPostFilter());
         shadowMapper.setRenderSplits(false);
         shadowMapper.postFilterMode(filter);
        processor.add(new BloomFilter());
        processor.add(new GammaCorrection());
        processor.add(new FXAAFilter());

        cam.setFPSMode();

        loaded = true;
    }

    @Override
    public void update() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            cam.setDragMode();
        } else if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            cam.setFPSMode();
        }

    }

    @Override
    public void preUpdate() {
        drm.renderNormals(cam, context);
        DepthTextureManager.capture();
        RenderManager.renderDepthTexture(cam, context);
        DepthTextureManager.release();
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 150;
        new LwjglApplication(new TestFPSCam(), config);
    }
}
