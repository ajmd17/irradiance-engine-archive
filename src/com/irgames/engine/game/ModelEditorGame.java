/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.IRMat;
import com.irgames.engine.components.IRMatSaveLoad;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.components.IRSpatial;
import com.irgames.engine.components.listeners.IRListener;
import com.irgames.engine.controls.RigidBodyControl;
import com.irgames.engine.game.Game;
import com.irgames.engine.pbr.Presets;
import com.irgames.engine.shaders.MatCapShader;
import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.SimpleLit;
import com.irgames.engine.sky.SkyBoxComponent;
import com.irgames.engine.sky.SkyBoxLoader;
import com.irgames.managers.LightingManager;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.NodeUtils;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Andrew
 */
public class ModelEditorGame extends Game {

    public IRListener modelAddedListener;
    public IRNode selectedModel;
    boolean loaded = false;
    SkyBoxComponent skyBox;
    IRNode sphereView;
    FileHandle loadModelFH;
    boolean loadModel = false;
    public IRNode axisArrows;
    private boolean matLoaded;

    @Override
    public void init() {
        this.setTitle("Skybox Test");
        this.setBackgroundColor(Color.BLACK);
        physics.renderDebug = true;
        loaded = true;
        cam.far = 300;
        //cam.disable();
        skyBox = new SkyBoxComponent(SkyBoxLoader.getPixmap("checkered", "right.jpg"), SkyBoxLoader.getPixmap("checkered", "left.jpg"),
                SkyBoxLoader.getPixmap("checkered", "top.jpg"), SkyBoxLoader.getPixmap("checkered", "top.jpg"),
                SkyBoxLoader.getPixmap("checkered", "front.jpg"), SkyBoxLoader.getPixmap("checkered", "back.jpg")
        );
        addComponent(skyBox);

        LightingManager.setSunDirection(new Vector3(-1, -1, -1));
        axisArrows = new IRNode(Assets.loadObjModel(Gdx.files.internal("data/models/shapes/axis_arrows.obj")));
        axisArrows.getChild(0).setMaterial(new IRMat().setPropertyC("albedo", Color.GREEN));
        axisArrows.getChild(1).setMaterial(new IRMat().setPropertyC("albedo", Color.BLUE));
        axisArrows.getChild(2).setMaterial(new IRMat().setPropertyC("albedo", Color.RED));
        try {
            axisArrows.setShader(ShaderManager.getShader(SimpleLit.class, new ShaderProperties().setProperty("UNSHADED", true)));
        } catch (Exception ex) {
            Logger.getLogger(ModelEditorGame.class.getName()).log(Level.SEVERE, null, ex);
        }
        //axisArrows.scale(new Vector3(2, 2, 2));
        rootNode.attachChild(axisArrows);
        // IRMatSaveLoad.save(axisArrows, "data/materials/axis_arrows.irmat");

    }
    float updateTime = 0f;
    float maxUpdateTime = 5f;
    int skyboxIndex = 0;

    @Override
    public void update() {
        if (!matLoaded) {
            try {
                IRMatSaveLoad.load(axisArrows, "data/materials/axis_arrows.irmat");
            } catch (IOException ex) {
                Logger.getLogger(ModelEditorGame.class.getName()).log(Level.SEVERE, null, ex);
            }
            matLoaded = true;
        }
        if (loadModel) {
            if (selectedModel != null) {
                rootNode.detachChild(selectedModel);
            }
            try {
                Model model = null;
                if (loadModelFH.toString().endsWith("obj")) {
                    model = Assets.loadObjModel(loadModelFH);
                } else if (loadModelFH.toString().endsWith("g3db")) {
                    model = Assets.loadG3dModel(loadModelFH);
                }
              //  ModelInstance modelIns = new ModelInstance(model);
                selectedModel = new IRNode(model);
               // selectedModel.setMaterial(new IRMat());

               /* for (IRSpatial child : selectedModel.getChildren()) {

                    RigidBodyControl rbc = new RigidBodyControl(child, 0f);
                    selectedModel.addControl(rbc);
                }*/
                try {
                    selectedModel.setShader(ShaderManager.getShader(SimpleLit.class, new ShaderProperties()));
                } catch (Exception ex) {
                    Logger.getLogger(ModelEditorGame.class.getName()).log(Level.SEVERE, null, ex);
                }
                rootNode.attachChild(selectedModel);
                selectedModel.setLocalTranslation(new Vector3(0, 0, -5));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex);
            }
            if (modelAddedListener != null) {
                modelAddedListener.action();
            }
            loadModel = false;
        }
    }

    public void loadModel(FileHandle fh) {
        loadModel = true;
        this.loadModelFH = fh;
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 60;
        config.vSyncEnabled = true;
        new LwjglApplication(new ModelEditorGame(), config);
    }
}
