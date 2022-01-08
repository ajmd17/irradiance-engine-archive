/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.game.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.controls.CharacterControl;
import com.irgames.engine.controls.GameControl;
import com.irgames.engine.controls.RigidBodyControl;
import com.irgames.engine.game.Game;
import com.irgames.engine.pbr.Presets;
import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.SimpleLit;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.post.BloomFilter;
import com.irgames.engine.shaders.post.DepthTextureVisualizer;
import com.irgames.engine.shaders.post.FXAAFilter;
import com.irgames.engine.shaders.post.GammaCorrection;
import com.irgames.engine.shaders.post.LightRaysFilter;
import com.irgames.engine.shaders.post.ShadowPostFilter;
import com.irgames.engine.shadows.ShadowMapperComponent;
import com.irgames.engine.sky.SkyBoxComponent;
import com.irgames.engine.sky.SkyBoxLoader;
import com.irgames.engine.sky.SkyDomeComponent;
import com.irgames.engine.terrain.HeightmapTerrain;
import com.irgames.engine.terrain.random.RandomTerrain;
import com.irgames.managers.DepthTextureManager;
import com.irgames.managers.LightingManager;
import com.irgames.managers.OcclusionTextureManager;
import com.irgames.managers.RenderManager;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.MyRandom;
import com.irgames.utils.NodeUtils;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class TestRandomTerrain extends Game {

    boolean loaded = false;
    ModelInstance modelInstance;
    AnimationController controller;
    Environment environment;
    ModelBatch modelBatch;
    IRNode myblock;
    Texture tex;

    @Override
    public void init() {
        physics.renderDebug = true;
        physics.renderMeshes = false;
        this.setTitle("Simplex Terrain Test");
        LightingManager.setSunColor(Color.WHITE);
        LightingManager.setFog(Color.WHITE, 50, 300);
        LightingManager.setSunDirection(new Vector3(-0.1f, -0.2f, -0.2f));
        this.setBackgroundColor(new Color(90, 136, 210, 1.0f));
        
        
        
        SkyDomeComponent skyDome = new SkyDomeComponent();
        skyDome.setTime(8f);
        skyDome.setTimeScale(.5f);

        this.addComponent(skyDome);
        
        cam.far = 300;
       
        cam.update();
        //cam.setSmoothing(0.0f);
        tex = new Texture(SkyBoxLoader.getPath("checkered", "top.jpg"));
        myblock = NodeUtils.createBox(Vector3.Zero, new Vector3(1, 1, 3));
        try {
            myblock.setShader(ShaderManager.getShader(SimpleLit.class, new ShaderProperties()));
        } catch (Exception ex) {
            Logger.getLogger(TestRandomTerrain.class.getName()).log(Level.SEVERE, null, ex);
        }
        rootNode.attachChild(myblock);
       
        try {
            //rootNode.attachChild(new HeightmapTerrainChunk());
            this.addComponent(new RandomTerrain());
        } catch (IOException ex) {
            Logger.getLogger(TestRandomTerrain.class.getName()).log(Level.SEVERE, null, ex);
        }
        ShadowMapperComponent shadowMapper = new ShadowMapperComponent();
        addComponent(shadowMapper);
        ShadowPostFilter filter;
        processor.add(filter = new ShadowPostFilter());
        shadowMapper.setRenderSplits(false);
        shadowMapper.postFilterMode(filter);
        
        loaded = true;
        processor.add(new BloomFilter());
        processor.add(new FXAAFilter());
        processor.add(new GammaCorrection());
       // processor.add(new DepthTextureVisualizer());
       // processor.add(new LightRaysFilter());
        // cam.addControl(new CharacterControl(cam, physics));
    }
    Vector3 tempVec;
     
    @Override
    public void onClickLeft(int screenX, int screenY) {
        tempVec = physics.getWorldIntersection(cam, screenX, screenY);
        if (tempVec != null) {
            tempVec.y += 2;
        }
        System.out.println(tempVec);
        addBox();
    }
    Presets presets = new Presets();
    Vector3 p = new Vector3();
    public void addBox() {

        if (tempVec != null) {
            Vector3 scale = new Vector3(1, 1, 1);
            IRNode box;

            box = NodeUtils.createMonkey(tempVec, scale);
            int i = MyRandom.random.nextInt(5);
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
                box.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties().setProperty("ENV_MAP", true)));
            } catch (Exception ex) {
                Logger.getLogger(TestPhysicsScene.class.getName()).log(Level.SEVERE, null, ex);
            }

            box.setDrawsShadows(true);

            GameControl rbc = new RigidBodyControl(box, MathUtils.random(5f) + 1f);
            // rbc.rotate(Vector3.X, MathUtils.random(360f));
            // rbc.rotate(Vector3.Y, MathUtils.random(360f));
            // rbc.rotate(Vector3.Z, MathUtils.random(360f));
            box.addControl(rbc);
            rootNode.attachChild(box);
        }

    }
    Vector3 worldTranslation;

    @Override
    public void update() {
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            cam.setFPSMode();
        }
        // myblock.setLocalTranslation(new Vector3(cam.position.x + 3, cam.position.y - 3, cam.position.z));
    }

    @Override
    public void drawSpriteBatch() {
        spriteBatch.begin();

        spriteBatch.draw(tex, 0, 0, 128, 128);
        spriteBatch.end();
    }

    @Override
    public void preUpdate() {
        DepthTextureManager.capture();
        RenderManager.renderDepthTexture(cam, context);
        DepthTextureManager.release();
     /*   OcclusionTextureManager.capture();
        RenderManager.renderOccTexture(cam, context);
        OcclusionTextureManager.release();*/
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 150;
        new LwjglApplication(new TestRandomTerrain(), config);
    }
}
