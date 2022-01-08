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
import com.irgames.engine.game.Game;
import static com.irgames.engine.game.Game.context;
import static com.irgames.engine.game.Game.rootNode;
import com.irgames.engine.pagingengine.GrassPopulator;
import com.irgames.engine.pagingengine.TreePopulator;
import com.irgames.engine.pbr.Presets;
import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.SimpleLit;
import com.irgames.engine.shaders.post.FXAAFilter;
import com.irgames.engine.shaders.post.ShadowPostFilter;
import com.irgames.engine.shadows.ShadowMapperComponent;
import com.irgames.engine.sky.SkyBoxLoader;
import com.irgames.engine.sky.SkyDomeComponent;
import com.irgames.engine.terrain.TerrainComponent;
import com.irgames.managers.DepthTextureManager;
import com.irgames.managers.LightingManager;
import com.irgames.managers.RenderManager;
import com.irgames.managers.ShaderManager;
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
public class TestFXAA extends Game {

    boolean loaded = false;
    ModelInstance modelInstance;
    AnimationController controller;
    Environment environment;
    ModelBatch modelBatch;
    boolean fxaaEnabled = true;

    private FXAAFilter fxaaFilter;
    Presets presets = new Presets();

    @Override
    public void init() {
        physics.renderDebug = false;
        this.setTitle("FXAA Test");
        this.setBackgroundColor(Color.PINK);
        LightingManager.setSunColor(Color.WHITE);
        LightingManager.setFog(Color.TEAL, 300, 500);
        LightingManager.setSunDirection(new Vector3(-1f, -1f, -1));

        IRNode floor = NodeUtils.createBox(Vector3.Zero, new Vector3(75, 1, 75));
        floor.setMaterial(presets.PBR_Gold);

        try {
            floor.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties().setProperty("ENV_MAP", false)));
        } catch (Exception ex) {
            Logger.getLogger(TestPhysicsScene.class.getName()).log(Level.SEVERE, null, ex);
        }

        rootNode.attachChild(floor);

        IRNode piece = new IRNode(Assets.loadObjModel(Gdx.files.internal("data/models/tests/tank.obj")));
        piece.setScale(new Vector3(3, 3, 3));
        cam.setFPSMode();
        cam.setSmoothing(0.35f);
        cam.setSensitivity(0.6f);
        IRMat bodymat2 = presets.PBR_RedPaint;
        bodymat2.setProperty("roughness", 0.5f);
        piece.setMaterial(bodymat2);

        try {
            piece.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties().setProperty("ENV_MAP", true)));

        } catch (Exception ex) {
            Logger.getLogger(TestMovingRoot.class.getName()).log(Level.SEVERE, null, ex);
        }
        piece.setLocalTranslation(new Vector3(0, 2, 0));
        rootNode.attachChild(piece);

        ShadowMapperComponent shadowMapper = new ShadowMapperComponent();
        addComponent(shadowMapper);
        ShadowPostFilter posConstruct;

        processor.add(posConstruct = new ShadowPostFilter());

        shadowMapper.setRenderSplits(false);
        shadowMapper.postFilterMode(posConstruct);

        processor.add(fxaaFilter = new FXAAFilter());
        loaded = true;
    }

    @Override
    public void preUpdate() {
        DepthTextureManager.capture();
        RenderManager.renderDepthTexture(cam, context);
        DepthTextureManager.release();

    }

    @Override
    public void update() {
        if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            fxaaEnabled = !fxaaEnabled;
            if (fxaaEnabled) {
                processor.add(fxaaFilter);
            } else {
                processor.remove(fxaaFilter);
            }
        }
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 150;
        new LwjglApplication(new TestFXAA(), config);
    }
}
