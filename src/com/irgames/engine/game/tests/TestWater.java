/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.game.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.IRMat;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.game.Game;
import com.irgames.engine.pbr.Presets;

import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.SimpleLit;
import com.irgames.engine.shaders.WaterShader;
import com.irgames.engine.sky.SkyBoxComponent;
import com.irgames.engine.sky.SkyBoxLoader;
import com.irgames.engine.sky.SkyDomeComponent;
import com.irgames.managers.EnvironmentMapper;
import com.irgames.managers.LightingManager;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.NodeUtils;
import com.irgames.utils.RenderUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class TestWater extends Game {

    boolean loaded = false;
    SkyBoxComponent skyBox;

    Camera dummyCam0;

    @Override
    public void init() {
        this.setTitle("Water Test");
        this.setBackgroundColor(Color.BLACK);
        dummyCam0 = new PerspectiveCamera(45f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        loaded = true;
        cam.far = 300;
        skyBox = new SkyBoxComponent(SkyBoxLoader.getPixmap("checkered", "right.jpg"), SkyBoxLoader.getPixmap("checkered", "left.jpg"),
                SkyBoxLoader.getPixmap("checkered", "top.jpg"), SkyBoxLoader.getPixmap("checkered", "top.jpg"),
                SkyBoxLoader.getPixmap("checkered", "front.jpg"), SkyBoxLoader.getPixmap("checkered", "back.jpg")
        );
       // addComponent(skyBox);

        IRNode teapot = NodeUtils.createBox(new Vector3(0, 0, 0), new Vector3(15, 0, 15));
        teapot.setBucket(RenderUtils.Bucket.transparent);
        IRMat irmat = new Presets().PBR_Silver;
        Texture dif = new Texture(Gdx.files.internal("data/textures/water.jpg"), true);
        dif.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        irmat.setTexture("diffuse", dif);

        Texture nrm = new Texture(Gdx.files.internal("data/textures/water_nrm.jpg"), true);
        nrm.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        irmat.setTexture("normal", nrm);

        irmat.setProperty("albedo", new Color(0.7f, 0.8f, 0.9f, 1.0f));
        //irmat.setTexture("normal", Assets.loadTexture(Gdx.files.internal("data/trees/conifer/Leaf_Low_Normal.png")));
        irmat.addPixmap(SkyBoxLoader.getPixmap("checkered", "right.jpg"));
        irmat.addPixmap(SkyBoxLoader.getPixmap("checkered", "left.jpg"));
        irmat.addPixmap(SkyBoxLoader.getPixmap("checkered", "top.jpg"));
        irmat.addPixmap(SkyBoxLoader.getPixmap("checkered", "top.jpg"));
        irmat.addPixmap(SkyBoxLoader.getPixmap("checkered", "front.jpg"));
        irmat.addPixmap(SkyBoxLoader.getPixmap("checkered", "back.jpg"));
        teapot.setMaterial(irmat);
        try {
            teapot.shader = ShaderManager.getShader(WaterShader.class, new ShaderProperties().setProperty("ENV_MAP", false).setProperty("DIFFUSE_MAP", true).setProperty("NORMAL_MAP", true));
            WaterShader em = (WaterShader) teapot.shader;
            em.setupEnvMap(irmat);

        } catch (Exception ex) {
            Logger.getLogger(TestWater.class.getName()).log(Level.SEVERE, null, ex);
        }
        teapot.setDrawsShadows(true);
        rootNode.attachChild(teapot);
        LightingManager.setSunDirection(new Vector3(-1, -1, -1));

        IRNode teapot2 = NodeUtils.createTeapot(new Vector3(2, -5, 0), new Vector3(1, 1, 1));
        try {
            teapot2.shader = ShaderManager.getShader(SimpleLit.class, new ShaderProperties().setProperty("ENV_MAP", true));

        } catch (Exception ex) {
            Logger.getLogger(TestWater.class.getName()).log(Level.SEVERE, null, ex);
        }
        rootNode.attachChild(teapot2);

        SkyDomeComponent skyDome = new SkyDomeComponent();
        skyDome.setTime(8f);
        skyDome.setTimeScale(6f);

        this.addComponent(skyDome);

    }
    float updateTime = 0f;
    float maxUpdateTime = 5f;
    int skyboxIndex = 0;

    @Override
    public void update() {
        /* if (updateTime > maxUpdateTime) {
           
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
         }*/
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 1000;
        config.samples = 4;
        new LwjglApplication(new TestWater(), config);
    }
}
