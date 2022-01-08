/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.game.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.IRMat;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.game.Game;
import com.irgames.engine.pbr.Presets;
import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.post.FXAAFilter;
import com.irgames.engine.sky.SkyBoxComponent;
import com.irgames.engine.sky.SkyBoxLoader;
import com.irgames.managers.EnvironmentMapper;
import com.irgames.managers.EnvironmentMapper.EnvironmentProbe;
import com.irgames.managers.LightingManager;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.NodeUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class TestChaseCam extends Game {
    
    IRNode box;
    
    @Override
    public void init() {
        LightingManager.setSunDirection(new Vector3(-1, -1, 1));
        
        box = NodeUtils.createRoundedBox(Vector3.Zero, new Vector3(2, 2, 2));
        IRMat irm = new Presets().PBR_RedPaint;
        irm.setProperty("roughness", 0.5f);
        SkyBoxComponent skyBox = new SkyBoxComponent(SkyBoxLoader.getPixmap("checkered", "right.jpg"), SkyBoxLoader.getPixmap("checkered", "left.jpg"),
                SkyBoxLoader.getPixmap("checkered", "top.jpg"), SkyBoxLoader.getPixmap("checkered", "top.jpg"),
                SkyBoxLoader.getPixmap("checkered", "front.jpg"), SkyBoxLoader.getPixmap("checkered", "back.jpg")
        );
        addComponent(skyBox);
        irm.setTexture("normal", Assets.loadTexture(Gdx.files.internal("data/textures/bumpy.jpg")));
        box.setMaterial(irm);
        processor.add(new FXAAFilter());
        cam.setChaseMode(box.getLocalTranslation());
    }
    EnvironmentProbe probe;
    float globalTime = 0f;
    float updateTime = 0f;
    boolean added = false;
    
    @Override
    public void update() {
        cam.point.set(box.getLocalTranslation());
        if (updateTime > 3f);
        if (!added) {
            EnvironmentMapper.addEnvProbe(probe = new EnvironmentProbe());
            try {
                box.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties().setProperty("ENV_MAP", false).setProperty("NORMAL_MAP", false)));
            } catch (Exception ex) {
                Logger.getLogger(TestChaseCam.class.getName()).log(Level.SEVERE, null, ex);
            }
            rootNode.attachChild(box);
            this.resize(width, height);
            added = true;
        } else {
            updateTime += deltaTime;
        }
    }
    
    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 150;
        new LwjglApplication(new TestChaseCam(), config);
    }
}
