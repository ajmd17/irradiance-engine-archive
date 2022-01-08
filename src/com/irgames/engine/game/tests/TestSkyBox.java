/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.game.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.IRMat;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.game.Game;
import com.irgames.engine.pbr.Presets;
import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.sky.SkyBoxComponent;
import com.irgames.engine.sky.SkyBoxLoader;
import com.irgames.managers.LightingManager;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.NodeUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class TestSkyBox extends Game {

    boolean loaded = false;
    SkyBoxComponent skyBox;

    @Override
    public void init() {
        this.setTitle("Skybox Test");
        this.setBackgroundColor(Color.BLACK);
        loaded = true;
        cam.far = 300;
        skyBox = new SkyBoxComponent(SkyBoxLoader.getPixmap("checkered", "right.jpg"), SkyBoxLoader.getPixmap("checkered", "left.jpg"),
                SkyBoxLoader.getPixmap("checkered", "top.jpg"), SkyBoxLoader.getPixmap("checkered", "top.jpg"),
                SkyBoxLoader.getPixmap("checkered", "front.jpg"), SkyBoxLoader.getPixmap("checkered", "back.jpg")
        );
        addComponent(skyBox);

        
        LightingManager.setSunDirection(new Vector3(-1, -1, -1));

    }
    float updateTime = 0f;
    float maxUpdateTime = 5f;
    int skyboxIndex = 0;

    @Override
    public void update() {
        if (updateTime > maxUpdateTime) {
            //update
            if (skyboxIndex == 1) {
                skyBox.changeTextures(SkyBoxLoader.getPixmap("checkered", "right.jpg"), SkyBoxLoader.getPixmap("checkered", "left.jpg"),
                        SkyBoxLoader.getPixmap("checkered", "top.jpg"), SkyBoxLoader.getPixmap("checkered", "top.jpg"),
                        SkyBoxLoader.getPixmap("checkered", "front.jpg"), SkyBoxLoader.getPixmap("checkered", "back.jpg")
                );
                skyboxIndex = 0;
            } else if (skyboxIndex == 0) {
                skyBox.changeTextures(SkyBoxLoader.getPixmap("siege", "right.jpg"), SkyBoxLoader.getPixmap("siege", "left.jpg"),
                        SkyBoxLoader.getPixmap("siege", "top.jpg"), SkyBoxLoader.getPixmap("siege", "top.jpg"),
                        SkyBoxLoader.getPixmap("siege", "front.jpg"), SkyBoxLoader.getPixmap("siege", "back.jpg")
                );
                skyboxIndex = 1;
            }
            updateTime = 0f;
        } else {
            updateTime += Gdx.graphics.getDeltaTime();
        }
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 1000;
        new LwjglApplication(new TestSkyBox(), config);
    }
}
