/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.game.tests;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
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
import com.irgames.engine.pagingengine.GrassPopulator;
import com.irgames.engine.pagingengine.TreePopulator;
import com.irgames.engine.pbr.Presets;
import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.SimpleLit;
import com.irgames.engine.sky.SkyBoxComponent;
import com.irgames.engine.sky.SkyBoxLoader;
import com.irgames.engine.sky.SkyDomeComponent;
import com.irgames.engine.terrain.TerrainComponent;
import com.irgames.managers.LightingManager;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.NodeUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class TestNormalMapping extends Game {

    boolean loaded = false;
    ModelInstance modelInstance;
    AnimationController controller;
    Environment environment;
    ModelBatch modelBatch;
    IRNode irn;
    
    @Override
    public void init() {
        physics.renderDebug = false;
        this.setTitle("NormalMapping Test");
        this.setBackgroundColor(Color.PINK);
        
        SkyBoxComponent skyBox = new SkyBoxComponent(SkyBoxLoader.getPixmap("siege", "right.jpg"), SkyBoxLoader.getPixmap("siege", "left.jpg"),
                SkyBoxLoader.getPixmap("siege", "top.jpg"), SkyBoxLoader.getPixmap("siege", "top.jpg"),
                SkyBoxLoader.getPixmap("siege", "front.jpg"), SkyBoxLoader.getPixmap("siege", "back.jpg")
        );
        addComponent(skyBox);
        Model zomb = Assets.loadObjModel(Gdx.files.getFileHandle("data/models/terrains.obj", FileType.Internal));
        
        irn = NodeUtils.createSphere(Vector3.Zero, new Vector3(2.5f,2.5f,2.5f));
        IRMat mat = new Presets().PBR_Silver;
        mat.setProperty("roughness", 0.2f);
        Texture dif = new Texture(Gdx.files.internal("data/textures/wood.jpg"), true);
        dif.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        mat.setTexture("diffuse", dif);
        mat.setTexture("normal", Assets.loadTexture(Gdx.files.internal("data/textures/wood_nrm.tga")));
        irn.setMaterial(mat);
        try {
            irn.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties().setProperty("DIFFUSE_MAP", true).setProperty("NORMAL_MAP", true)));
        } catch (Exception ex) {
            Logger.getLogger(TestNormalMapping.class.getName()).log(Level.SEVERE, null, ex);
        }
        cam.setChaseMode(irn.getLocalTranslation());
        rootNode.attachChild(irn);
        
        LightingManager.setSunColor(new Color(0.9f, 0.6f, 0.3f, 1.0f));
        LightingManager.setFog(Color.BLUE, 50, 100);
        loaded = true;
    }

    @Override
    public void update() {
        irn.rotate(Vector3.Y, deltaTime*25f);
      //  irn.rotate(Vector3.X, deltaTime*10f);
      //  irn.rotate(Vector3.Z, deltaTime*10f);
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 1000;
        new LwjglApplication(new TestNormalMapping(), config);
    }
}
