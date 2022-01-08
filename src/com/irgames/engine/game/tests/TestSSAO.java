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
import com.irgames.engine.components.IRMat;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.deferredrendering.DeferredRenderManager;
import com.irgames.engine.game.Game;
import static com.irgames.engine.game.Game.context;
import static com.irgames.engine.game.Game.rootNode;
import com.irgames.engine.pagingengine.GrassPopulator;
import com.irgames.engine.pagingengine.TreePopulator;
import com.irgames.engine.pbr.Presets;
import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.SimpleLit;
import com.irgames.engine.shaders.post.BloomFilter;
import com.irgames.engine.shaders.post.DepthTextureVisualizer;
import com.irgames.engine.shaders.post.FXAAFilter;
import com.irgames.engine.shaders.post.GammaCorrection;
import com.irgames.engine.shaders.post.SSAOFilter;
import com.irgames.engine.shaders.post.SSAOFilter2;
import com.irgames.engine.shaders.post.ShadowPostFilter;
import com.irgames.engine.shadows.ShadowMapperComponent;
import com.irgames.engine.sky.SkyBoxComponent;
import com.irgames.engine.sky.SkyBoxLoader;
import com.irgames.engine.sky.SkyDomeComponent;
import com.irgames.engine.terrain.TerrainComponent;
import com.irgames.managers.DepthTextureManager;
import com.irgames.managers.LightingManager;
import com.irgames.managers.RenderManager;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.MyRandom;
import com.irgames.utils.NodeUtils;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class TestSSAO extends Game {

    boolean loaded = false;
    ModelInstance modelInstance;
    AnimationController controller;
    Environment environment;
    ModelBatch modelBatch;
    boolean fxaaEnabled = true;
    DeferredRenderManager drm;
    private FXAAFilter fxaaFilter;
    Presets presets = new Presets();
    SSAOFilter2 ssao;

    @Override
    public void init() {

        drm = new DeferredRenderManager(renderManager);

        physics.renderDebug = false;
        this.setTitle("FXAA Test");
        this.setBackgroundColor(Color.PINK);
        LightingManager.setSunColor(Color.WHITE);
        LightingManager.setFog(Color.TEAL, 300, 500);
        LightingManager.setSunDirection(new Vector3(-0.4f, -0.4f, -1f));
        SkyBoxComponent skyBox = new SkyBoxComponent(SkyBoxLoader.getPixmap("checkered", "right.jpg"), SkyBoxLoader.getPixmap("checkered", "left.jpg"),
                SkyBoxLoader.getPixmap("checkered", "top.jpg"), SkyBoxLoader.getPixmap("checkered", "top.jpg"),
                SkyBoxLoader.getPixmap("checkered", "front.jpg"), SkyBoxLoader.getPixmap("checkered", "back.jpg")
        );
        addComponent(skyBox);
        IRNode floor = NodeUtils.createBox(Vector3.Zero, new Vector3(75, 1, 75));
        floor.setMaterial(presets.PBR_Gold);
        floor.getMaterial().setTexture("noise", Assets.loadTexture(Gdx.files.internal("data/textures/noise/roughness.png")));
        try {
            floor.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties()));
        } catch (Exception ex) {
            Logger.getLogger(TestPhysicsScene.class.getName()).log(Level.SEVERE, null, ex);
        }

        rootNode.attachChild(floor);

        IRNode piece = new IRNode(Assets.loadObjModel(Gdx.files.internal("data/models/tests/sponza/sponza.obj")));
        piece.setScale(new Vector3(.1f, .1f, .1f));

        IRMat bodymat2 = presets.PBR_Silver;
        piece.setMaterial(bodymat2);

        try {
            piece.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties().setProperty("ENV_MAP", false)));

        } catch (Exception ex) {
            Logger.getLogger(TestMovingRoot.class.getName()).log(Level.SEVERE, null, ex);
        }
        piece.setLocalTranslation(new Vector3(0, 2, 0));
        rootNode.attachChild(piece);

        processor.add(ssao = new SSAOFilter2());
        ShadowMapperComponent shadowMapper = new ShadowMapperComponent();
       // addComponent(shadowMapper);
        ShadowPostFilter posConstruct;
        //processor.add(posConstruct = new ShadowPostFilter());
        shadowMapper.setRenderSplits(false);
       // shadowMapper.postFilterMode(posConstruct);
      //  processor.add(fxaaFilter = new FXAAFilter());
        processor.add(new GammaCorrection());
       // processor.add(new DepthTextureVisualizer());
        loaded = true;

        for (int x = -3; x < 4; x++) {
            for (int z = -3; z < 4; z++) {

                IRNode box = NodeUtils.createMonkey(new Vector3(x * 5, 10, z * 5), new Vector3(2, 2, 2));
                //NodeUtils.createRoundedBox(new Vector3(x * 17, 3, z * 17), new Vector3(2, 2, 2));
                int i = MyRandom.random.nextInt(4);

                if (i == 0) {
                    box.setMaterial(presets.PBR_BluePlastic);
                } else if (i == 1) {
                    box.setMaterial(presets.PBR_Copper);
                } else if (i == 2) {
                    box.setMaterial(presets.PBR_Gold);
                } else if (i == 3) {
                    box.setMaterial(presets.PBR_RedPaint);
                } else if (i == 4) {
                    box.setMaterial(presets.PBR_Silver);
                }
                try {
                    box.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties()));

                } catch (Exception ex) {
                    Logger.getLogger(TestMovingRoot.class.getName()).log(Level.SEVERE, null, ex);
                }
                rootNode.attachChild(box);
            }
        }

    }

    @Override
    public void preUpdate() {
        drm.renderDiffuse(cam, context);
        drm.renderNormals(cam, context);

        DepthTextureManager.capture();
        RenderManager.renderDepthTexture(cam, context);
        DepthTextureManager.release();

    }

    @Override
    public void update() {
        if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            //ssao.showDebug = !ssao.showDebug;
            ssao.setEnabled(!ssao.isEnabled());
        }
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 150;
        new LwjglApplication(new TestSSAO(), config);
    }
}
