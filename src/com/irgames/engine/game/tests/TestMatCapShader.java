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
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.IRMat;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.game.Game;
import com.irgames.engine.pbr.Presets;
import com.irgames.engine.shaders.MatCapShader;
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
public class TestMatCapShader extends Game {

    SkyBoxComponent skyBox;

    @Override
    public void init() {
        this.setTitle("MatCap Test");
        this.setBackgroundColor(Color.BLACK);

        cam.far = 300;
        skyBox = new SkyBoxComponent(SkyBoxLoader.getPixmap("checkered", "right.jpg"), SkyBoxLoader.getPixmap("checkered", "left.jpg"),
                SkyBoxLoader.getPixmap("checkered", "top.jpg"), SkyBoxLoader.getPixmap("checkered", "top.jpg"),
                SkyBoxLoader.getPixmap("checkered", "front.jpg"), SkyBoxLoader.getPixmap("checkered", "back.jpg")
        );
        addComponent(skyBox);

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                int idx = x * 3 + y;
                try {
                  
                    IRNode teapot = NodeUtils.createTeapot(new Vector3((x - 1) * 15, 0, (y - 1) * 15), new Vector3(5, 5, 5));
                    IRMat irmat = new IRMat();
                    irmat.setTexture("matcap", Assets.loadTexture(Gdx.files.internal("data/materials/matcaps/zbrush-mat" + (idx + 1) + ".png")));
                    teapot.setMaterial(irmat);
                    try {
                        teapot.shader = ShaderManager.getShader(MatCapShader.class, new ShaderProperties().setProperty("ENV_MAP", true));
                    } catch (Exception ex) {
                        Logger.getLogger(TestSkyBox.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    teapot.setDrawsShadows(true);
                    rootNode.attachChild(teapot);
                    LightingManager.setSunDirection(new Vector3(-1, -1, -1));
                } catch (Exception ex) {

                }
            }
        }

    }
    float updateTime = 0f;
    float maxUpdateTime = 5f;
    int skyboxIndex = 0;

    @Override
    public void update() {

    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 1000;
        new LwjglApplication(new TestMatCapShader(), config);
    }
}
