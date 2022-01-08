/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.game.tests;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.game.Game;
import com.irgames.engine.sky.SkyBoxComponent;
import com.irgames.engine.sky.SkyBoxLoader;
import com.irgames.engine.terrain.HeightmapTerrain;
import com.irgames.engine.terrain.random.RandomTerrain;
import com.irgames.managers.LightingManager;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class TestHeightmapTerrain extends Game {

    boolean loaded = false;
    ModelInstance modelInstance;
    AnimationController controller;
    Environment environment;
    ModelBatch modelBatch;
    
    @Override
    public void preUpdate() {
        //DepthTextureManager.capture();
        //renderManager.renderDepthTexture(cam, context);
        //DepthTextureManager.release();

    }

    @Override
    public void init() {
        physics.renderDebug = false;
        this.setTitle("Heightmap Terrain Test");
        this.setBackgroundColor(Color.PINK);
        cam.far = 700;
        SkyBoxComponent skyBox = new SkyBoxComponent(SkyBoxLoader.getPixmap("checkered", "right.jpg"), SkyBoxLoader.getPixmap("checkered", "left.jpg"),
                SkyBoxLoader.getPixmap("checkered", "top.jpg"), SkyBoxLoader.getPixmap("checkered", "top.jpg"),
                SkyBoxLoader.getPixmap("checkered", "front.jpg"), SkyBoxLoader.getPixmap("checkered", "back.jpg")
        );
        addComponent(skyBox);
        
        try {
            //rootNode.attachChild(new HeightmapTerrainChunk());
            this.addComponent(new HeightmapTerrain());
        } catch (IOException ex) {
            Logger.getLogger(TestHeightmapTerrain.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        LightingManager.setSunColor(Color.WHITE);
        LightingManager.setFog(Color.TEAL, 300, 500);
        LightingManager.setSunDirection(new Vector3(-1f, 1f, -1));
        loaded = true;
    }

    @Override
    public void update() {
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 1000;
        new LwjglApplication(new TestHeightmapTerrain(), config);
    }
}
