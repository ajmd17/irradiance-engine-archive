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
import com.irgames.engine.components.IRSpatial;
import com.irgames.engine.controls.RigidBodyControl;
import com.irgames.engine.game.Game;
import static com.irgames.engine.game.Game.context;
import static com.irgames.engine.game.Game.rootNode;
import com.irgames.engine.pagingengine.GrassPopulator;
import com.irgames.engine.pagingengine.TreePopulator;
import com.irgames.engine.pbr.Presets;
import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.SimpleLit;
import com.irgames.engine.shaders.post.DepthTextureVisualizer;
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
public class TestMovingRoot extends Game {

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
        physics.renderDebug = true;
        this.setTitle("FXAA Test");
        this.setBackgroundColor(Color.PINK);
        LightingManager.setSunColor(Color.WHITE);
        LightingManager.setFog(Color.TEAL, 300, 500);
        LightingManager.setSunDirection(new Vector3(-1f, -1f, -1));

        IRNode floor = NodeUtils.createBox(Vector3.Zero, new Vector3(75, 1, 75));
        floor.setDrawsShadows(true);
        floor.setMaterial(presets.PBR_RedPaint);

        try {
            floor.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties().setProperty("ENV_MAP", false)));
        } catch (Exception ex) {
            Logger.getLogger(TestPhysicsScene.class.getName()).log(Level.SEVERE, null, ex);
        }
        RigidBodyControl rbc2 = new RigidBodyControl(floor, 0f);
        floor.addControl(rbc2);
        rootNode.attachChild(floor);

        IRNode piece = new IRNode(Assets.loadObjModel(Gdx.files.internal("data/models/shapes/weird_cube.obj")));//new IRNode(Assets.loadObjModel(Gdx.files.internal("data/models/tests/tank.obj")));
        piece.setName("hello");
        piece.setScale(new Vector3(3, 3, 3));
        cam.setFPSMode();
        cam.setSmoothing(0.35f);
        cam.setSensitivity(0.6f);
        cam.position.set(11f, 0f, 0f);
        IRMat bodymat2 = presets.PBR_Gold;
        bodymat2.setProperty("roughness", 0.5f);
        piece.setMaterial(bodymat2);

        piece.setDrawsShadows(true);

        try {
            piece.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties().setProperty("ENV_MAP", true)));

        } catch (Exception ex) {
            Logger.getLogger(TestMovingRoot.class.getName()).log(Level.SEVERE, null, ex);
        }
        piece.setLocalTranslation(new Vector3(0, 50, 0));
        RigidBodyControl rbc3 = new RigidBodyControl(piece, 0.5f);
        //rbc3.move(new Vector3(0, 25, 0));
        piece.addControl(rbc3);
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
    /* when the camera is too far away from the center of the world, shift it back to 0,0,0 and move the world */

    public void moveWorld() {
        float maxDist = 25;
        if (cam.position.x > maxDist) {

            rootNode.setLocalTranslation(rootNode.getLocalTranslation().cpy().add(new Vector3(-maxDist, 0, 0)));
            cam.position.set(0, cam.position.y, cam.position.z);
            System.out.println("move");
            cam.update();
        }
        if (cam.position.z > maxDist) {

            rootNode.setLocalTranslation(rootNode.getLocalTranslation().cpy().add(new Vector3(0, 0, -maxDist)));
            cam.position.set(cam.position.x, cam.position.y, 0);
            System.out.println("move");
            cam.update();
        }
        if (cam.position.x < -maxDist) {

            rootNode.setLocalTranslation(rootNode.getLocalTranslation().cpy().add(new Vector3(maxDist, 0, 0)));
            cam.position.set(0, cam.position.y, cam.position.z);
            System.out.println("move");
            cam.update();
        }
        if (cam.position.z < -maxDist) {

            rootNode.setLocalTranslation(rootNode.getLocalTranslation().cpy().add(new Vector3(0, 0, maxDist)));
            cam.position.set(cam.position.x, cam.position.y, 0);
            System.out.println("move");
            cam.update();
        }
    }

    @Override
    public void update() {

        moveWorld();

        if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            for (IRSpatial spat : rootNode.getChildren()) {
                spat.setPhysicsLocation(new Vector3(0, 10, 0));
            }

        }
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 150;
        new LwjglApplication(new TestMovingRoot(), config);
    }
}
