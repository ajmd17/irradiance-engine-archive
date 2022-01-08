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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.IRMat;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.controls.RigidBodyControl;
import com.irgames.engine.game.Game;
import com.irgames.engine.pagingengine.GrassPopulator;
import com.irgames.engine.pagingengine.TreePopulator;
import com.irgames.engine.pbr.Presets;
import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.SimpleLit;
import com.irgames.engine.shaders.post.DepthTextureVisualizer;
import com.irgames.engine.shaders.post.FXAAFilter;
import com.irgames.engine.sky.SkyBoxComponent;
import com.irgames.engine.sky.SkyBoxLoader;
import com.irgames.engine.sky.SkyDomeComponent;
import com.irgames.engine.terrain.TerrainComponent;
import com.irgames.managers.EnvironmentMapper;
import com.irgames.managers.EnvironmentMapper.EnvironmentProbe;
import com.irgames.managers.LightingManager;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.MyRandom;
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
public class TestEnvironmentProbe extends Game {

    boolean loaded = false;
    ModelInstance modelInstance;
    AnimationController controller;
    Environment environment;
    ModelBatch modelBatch;
    EnvironmentProbe probe;

    static String intToString(int num, int digits) {
        assert digits > 0 : "Invalid number of digits";

        // create variable length array of zeros
        char[] zeros = new char[digits];
        Arrays.fill(zeros, '0');
        // format number as String
        DecimalFormat df = new DecimalFormat(String.valueOf(zeros));

        return df.format(num);
    }
    private boolean added = false;
    private IRNode monkey;
    private IRNode piece;
    Presets presets = new Presets();
    @Override
    public void init() {
        physics.renderDebug = false;
        this.setTitle("Cubemap Test");
        this.setBackgroundColor(Color.PINK);
        LightingManager.setSunColor(Color.WHITE);
        LightingManager.setFog(Color.TEAL, 300, 500);
        LightingManager.setSunDirection(new Vector3(-1f, -1f, -1));
        piece = NodeUtils.createRoundedBox(Vector3.Zero, new Vector3(2f, 2f, 2f));//new IRNode(Assets.loadObjModel(Gdx.files.internal("data/models/tests/tank.obj")));

        SkyBoxComponent skyBox = new SkyBoxComponent(SkyBoxLoader.getPixmap("siege", "right.jpg"), SkyBoxLoader.getPixmap("siege", "left.jpg"),
                SkyBoxLoader.getPixmap("siege", "top.jpg"), SkyBoxLoader.getPixmap("siege", "top.jpg"),
                SkyBoxLoader.getPixmap("siege", "front.jpg"), SkyBoxLoader.getPixmap("siege", "back.jpg")
        );
        addComponent(skyBox);

        IRMat bodymat2 = presets.PBR_Gold;
        bodymat2.setTexture("normal", Assets.loadTexture(Gdx.files.internal("data/textures/wood_nrm.tga")));
        piece.setMaterial(bodymat2);
        
        try {
            piece.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties().setProperty("ENV_MAP", true).setProperty("NORMAL_MAP", false)));
        } catch (Exception ex) {
            Logger.getLogger(TestEnvironmentProbe.class.getName()).log(Level.SEVERE, null, ex);
        }
        piece.scale(new Vector3(2, 2, 2));

        processor.add(new FXAAFilter());
        // processor.add(new DepthTextureVisualizer());
        probe = new EnvironmentProbe();
        for (int i = -1; i < 1; i++) {
            addMonkey(new Vector3(10, i * 2, 0), Color.RED); // right
        }
        for (int i = -1; i < 1; i++) {
            addSphere(new Vector3(10, i * 2, 0), Color.GREEN); // left
        }
        for (int i = -1; i < 1; i++) {
            addSphere(new Vector3(i * 2, 10, 0), Color.BLUE); // top
        }
        for (int i = -1; i < 1; i++) {
            addSphere(new Vector3(i * 2, -10, 0), Color.ORANGE); //bottom
        }
        for (int i = -1; i < 1; i++) {
            addSphere(new Vector3(i * 2, 0, 10), Color.PURPLE); // front
        }
        for (int i = -1; i < 1; i++) {
            addSphere(new Vector3(i * 2, 0, -10), Color.WHITE); // bottom
        }
        /* for (int i = 0; i < 15; i++) {
         addSphere();
         }*/
        cam.setChaseMode(piece.getLocalTranslation());
        loaded = true;
    }

    private void addMonkey(Vector3 pos, Color col) {
        float val = (float) (Math.random()+.5f);
        Vector3 scale = new Vector3(val,val,val);

        int i = MyRandom.random.nextInt(4);

        monkey = NodeUtils.createMonkey(pos, scale);
        try {
            monkey.setShader(ShaderManager.getShader(SimpleLit.class, new ShaderProperties()));
        } catch (Exception ex) {
            Logger.getLogger(TestEnvironmentProbe.class.getName()).log(Level.SEVERE, null, ex);
        }
        IRMat irm = new IRMat();
        // col.set((float)Math.random(),(float)Math.random(),(float)Math.random(),1.0f);
        irm.setProperty("albedo", col);
        monkey.setMaterial(irm);
        monkey.setDrawsShadows(true);
        rootNode.attachChild(monkey);

    }

    private void addSphere(Vector3 pos, Color col) {
        float val = (float) (Math.random()+.5f);
        Vector3 scale = new Vector3(val,val,val);
        IRNode box;
        int i = MyRandom.random.nextInt(4);

        box = NodeUtils.createSphere(pos, scale);
        try {
            box.setShader(ShaderManager.getShader(SimpleLit.class, new ShaderProperties()));
        } catch (Exception ex) {
            Logger.getLogger(TestEnvironmentProbe.class.getName()).log(Level.SEVERE, null, ex);
        }
        IRMat irm = new IRMat();
        // col.set((float)Math.random(),(float)Math.random(),(float)Math.random(),1.0f);
        irm.setProperty("albedo", col);
        box.setMaterial(irm);
        box.setDrawsShadows(true);
        rootNode.attachChild(box);

    }
    float updateTime;

    @Override
    public void update() {

        if (updateTime > 2);
        if (!added) {
            EnvironmentMapper.addEnvProbe(probe);
            rootNode.attachChild(piece);
            added = true;
        } else {
            updateTime += deltaTime;
        }
        if (Gdx.input.isKeyJustPressed(Keys.NUM_1)) {
            piece.setMaterial(presets.PBR_BluePlastic);
        } else if (Gdx.input.isKeyJustPressed(Keys.NUM_2)) {
            piece.setMaterial(presets.PBR_Copper);
        } else if (Gdx.input.isKeyJustPressed(Keys.NUM_3)) {
            piece.setMaterial(presets.PBR_Gold);
        } else if (Gdx.input.isKeyJustPressed(Keys.NUM_4)) {
            piece.setMaterial(presets.PBR_RedPaint);
        } else if (Gdx.input.isKeyJustPressed(Keys.NUM_5)) {
            piece.setMaterial(presets.PBR_Silver);
        }
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 150;
        new LwjglApplication(new TestEnvironmentProbe(), config);
    }
}
