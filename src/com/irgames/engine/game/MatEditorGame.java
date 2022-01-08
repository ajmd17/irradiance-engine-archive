/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.IRMat;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.components.IRProperty;
import com.irgames.engine.components.IRShader;
import com.irgames.engine.shaders.LeafShader;
import com.irgames.engine.components.listeners.IRListener;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Andrew
 */
public class MatEditorGame extends Game {

    boolean loaded = false;
    SkyBoxComponent skyBox;
    public IRNode sphereView;
    String shaderSetName = "";
    boolean shaderSet = false;
    public IRListener selectedShaderChanged;
    public IRListener matChanged;

    @Override
    public void init() {
        this.setTitle("Skybox Test");
        this.setBackgroundColor(Color.BLACK);
        loaded = true;
        cam.far = 300;
        //cam.disable();
        skyBox = new SkyBoxComponent(SkyBoxLoader.getPixmap("checkered", "right.jpg"), SkyBoxLoader.getPixmap("checkered", "left.jpg"),
                SkyBoxLoader.getPixmap("checkered", "top.jpg"), SkyBoxLoader.getPixmap("checkered", "top.jpg"),
                SkyBoxLoader.getPixmap("checkered", "front.jpg"), SkyBoxLoader.getPixmap("checkered", "back.jpg")
        );
        addComponent(skyBox);

        sphereView = NodeUtils.createRoundedBox(new Vector3(0, 0, -10), new Vector3(3, 3, 3));
        try {
            sphereView.setShader(ShaderManager.getShader(SimpleLit.class, new ShaderProperties()));
        } catch (Exception ex) {
            Logger.getLogger(MatEditorGame.class.getName()).log(Level.SEVERE, null, ex);
        }
        rootNode.attachChild(sphereView);

        LightingManager.setSunDirection(new Vector3(-1, -1, -1));

    }
    float updateTime = 0f;
    float maxUpdateTime = 5f;
    int skyboxIndex = 0;

    public IRMat getMaterial() {
        return sphereView.getMaterial();
    }

    public IRShader getSelectedShader() {
        return (IRShader) sphereView.shader;
    }

    @Override
    public void update() {
        if (this.getSelectedShader().updateNeeded) {

            Class clazz = getSelectedShader().getClass();
            ShaderProperties newProps = new ShaderProperties();

            for (IRProperty prop : getSelectedShader().getProperties()) {
                try {
                    Boolean b = Boolean.parseBoolean(prop.value.toString());
                    newProps.setProperty(prop.name, b);
                    System.out.println("set property: " + prop.name + "," + b);
                } catch (Exception ex) {System.out.println(ex);}
            }

            try {
                sphereView.setShader(ShaderManager.getShader(BRDFShader.class, newProps));
            } catch (Exception ex) {
                Logger.getLogger(MatEditorGame.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Switched shaders");
            getSelectedShader().updateNeeded = false;
        }
        if (shaderSet) {
            try {
                if (shaderSetName.equals("SimpleLit")) {
                    sphereView.setMaterial(new IRMat());
                    sphereView.setShader(ShaderManager.getShader(SimpleLit.class, new ShaderProperties().setProperty("DIFFUSE_MAP", false)));
                } else if (shaderSetName.equals("MatCap")) {

                    IRMat matCapMaterial = new IRMat();
                    matCapMaterial.setTexture("matcap", Assets.loadTexture(Gdx.files.internal("data/materials/matcaps/zbrush-mat1.png")));
                    sphereView.setMaterial(matCapMaterial);
                    sphereView.setShader(ShaderManager.getShader(MatCapShader.class, new ShaderProperties().setProperty("ENV_MAP", true).setProperty("DIFFUSE_MAP", false)));

                } else if (shaderSetName.equals("PBR")) {
                    IRMat irmat = new Presets().PBR_Silver;
                    //Texture dif = new Texture(Gdx.files.internal("data/textures/smoothmetal.jpg"), true);
                    //dif.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                    // irmat.setTexture("diffuse", dif);
                    //irmat.setTexture("normal", Assets.loadTexture(Gdx.files.internal("data/trees/conifer/Leaf_Low_Normal.png")));
                    irmat.addPixmap(SkyBoxLoader.getPixmap("checkered", "right.jpg"));
                    irmat.addPixmap(SkyBoxLoader.getPixmap("checkered", "left.jpg"));
                    irmat.addPixmap(SkyBoxLoader.getPixmap("checkered", "top.jpg"));
                    irmat.addPixmap(SkyBoxLoader.getPixmap("checkered", "top.jpg"));
                    irmat.addPixmap(SkyBoxLoader.getPixmap("checkered", "front.jpg"));
                    irmat.addPixmap(SkyBoxLoader.getPixmap("checkered", "back.jpg"));
                    sphereView.setMaterial(irmat);
                    sphereView.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties().setProperty("ENV_MAP", true).setProperty("DIFFUSE_MAP", false)));
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex);
            }
            if (selectedShaderChanged != null) {
                selectedShaderChanged.action();
            }
            if (matChanged != null) {
                matChanged.action();
            }
            shaderSet = false;
        }
    }

    public void setShader(String name) {
        shaderSet = true;
        shaderSetName = name;
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 30;
        config.vSyncEnabled = true;
        new LwjglApplication(new MatEditorGame(), config);
    }
}
