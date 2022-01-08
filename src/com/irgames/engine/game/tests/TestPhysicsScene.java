/**
 * *****************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ****************************************************************************
 */
package com.irgames.engine.game.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.controls.RigidBodyControl;
import com.irgames.engine.deferredrendering.DeferredRenderManager;

import com.irgames.engine.game.Game;
import com.irgames.engine.pbr.Presets;
import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.post.BloomFilter;
import com.irgames.engine.shaders.post.DepthTextureVisualizer;
import com.irgames.utils.NodeUtils;
import com.irgames.managers.DepthTextureManager;
import com.irgames.engine.shaders.post.FXAAFilter;
import com.irgames.engine.shaders.post.GammaCorrection;
import com.irgames.engine.shaders.post.ShadowPostFilter;
import com.irgames.engine.shadows.ShadowMapperComponent;
import com.irgames.engine.sky.SkyBoxComponent;
import com.irgames.engine.sky.SkyBoxLoader;
import com.irgames.engine.stats.Stats;
import com.irgames.managers.LightingManager;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.MyRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestPhysicsScene extends Game {

    ShadowMapperComponent shadowMapper;
    IRNode teapot;
    ShapeRenderer sr;
    ShadowPostFilter posConstruct;
    Presets presets = new Presets();
    private boolean addBox;
    DeferredRenderManager drm;
    @Override
    public void init() {

        this.setTitle("Physics Test");
        drm = new DeferredRenderManager(renderManager);
        physics.renderDebug = false;
        IRNode floor = NodeUtils.createBox(Vector3.Zero, new Vector3(75, 1, 75));
        floor.setMaterial(presets.PBR_RedPaint);
        LightingManager.setSunDirection(new Vector3(-1,-1,-1));
        try {
            floor.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties().setProperty("ENV_MAP", false)));
        } catch (Exception ex) {
            Logger.getLogger(TestPhysicsScene.class.getName()).log(Level.SEVERE, null, ex);
        }
        floor.ins.transform.setToScaling(10, 1, 10);
        RigidBodyControl rbc2 = new RigidBodyControl(floor, 0f);
        floor.addControl(rbc2);
        rootNode.attachChild(floor);

        floor.setDrawsShadows(true);
        cam.setFPSMode();

        addComponent(new SkyBoxComponent(SkyBoxLoader.getPath("checkered", "right.jpg"), SkyBoxLoader.getPath("checkered", "left.jpg"),
                SkyBoxLoader.getPath("checkered", "top.jpg"), SkyBoxLoader.getPath("checkered", "top.jpg"),
                SkyBoxLoader.getPath("checkered", "front.jpg"), SkyBoxLoader.getPath("checkered", "back.jpg")
        ));
        shadowMapper = new ShadowMapperComponent();
        addComponent(shadowMapper);
        processor.add(posConstruct = new ShadowPostFilter());
        teapot = NodeUtils.createRoundedBox(new Vector3(0, 5, 0), new Vector3(5, 5, 5));
        teapot.setMaterial(presets.PBR_RedPaint);
        try {
            teapot.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties()));
        } catch (Exception ex) {
            Logger.getLogger(TestPhysicsScene.class.getName()).log(Level.SEVERE, null, ex);
        }
        teapot.addControl(new RigidBodyControl(teapot, 1f));
        teapot.setDrawsShadows(true);
        rootNode.attachChild(teapot);

        shadowMapper.setRenderSplits(false);
        shadowMapper.postFilterMode(posConstruct);
        for (int i = 0; i < 15; i++) {
            dropBox();
        }
        processor.add(new BloomFilter());
        processor.add(new FXAAFilter());
        processor.add(new GammaCorrection());
        //EnvironmentMapper.addEnvProbe(new EnvironmentProbe());
        //processor.add(new DepthTextureVisualizer());
    }

    private void dropBox() {
        Vector3 scale = new Vector3(1, 1, 1);
        IRNode box;
        int i = MyRandom.random.nextInt(4);
        if (i == 0) {
            box = NodeUtils.createBox(new Vector3((float) com.irgames.utils.MathUtil.randomInRange(90, -90), 5, (float) com.irgames.utils.MathUtil.randomInRange(90, -90)), scale);
        } else if (i == 1) {

            box = NodeUtils.createSphere(new Vector3((float) com.irgames.utils.MathUtil.randomInRange(90, -90), 5, (float) com.irgames.utils.MathUtil.randomInRange(90, -90)), scale);
        } else {

            box = NodeUtils.createTeapot(new Vector3((float) com.irgames.utils.MathUtil.randomInRange(90, -90), 5, (float) com.irgames.utils.MathUtil.randomInRange(90, -90)), scale);
        }

        box.setMaterial(presets.PBR_BluePlastic);
        try {
            box.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties()));
        } catch (Exception ex) {
            Logger.getLogger(TestPhysicsScene.class.getName()).log(Level.SEVERE, null, ex);
        }

        box.setDrawsShadows(true);
        rootNode.attachChild(box);
        RigidBodyControl rbc = new RigidBodyControl(box, MathUtils.random(5f) + 1f);
        rbc.rotate(Vector3.X, MathUtils.random(360f));
        rbc.rotate(Vector3.Y, MathUtils.random(360f));
        rbc.rotate(Vector3.Z, MathUtils.random(360f));
        box.addControl(rbc);

    }
    Vector3 tempVec;

    @Override
    public void onClickLeft(int screenX, int screenY) {
        tempVec = physics.getWorldIntersection(cam, screenX, screenY);
        addBox = true;

    }
    
    @Override
    public void preUpdate() {
        drm.renderDiffuse(cam, context);
        drm.renderNormals(cam, context);
        DepthTextureManager.capture();
        renderManager.renderDepthTexture(cam, context);
        DepthTextureManager.release();

    }

    @Override
    public void update() {
       
        if (Gdx.input.isKeyJustPressed(Keys.V)) {
            System.out.println("Vertices in scene: " + Stats.NUM_VERTICES);
        } else if (Gdx.input.isKeyJustPressed(Keys.P)) {
            physics.renderDebug = !physics.renderDebug;
        }
        
        if (addBox) {
            if (tempVec != null) {
                Vector3 scale = new Vector3(2, 2, 2);
                IRNode box;

                box = NodeUtils.createRoundedBox(tempVec, scale);
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
                    box.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties().setProperty("ENV_MAP", true).setProperty("NOISE_MAP", true)));
                } catch (Exception ex) {
                    Logger.getLogger(TestPhysicsScene.class.getName()).log(Level.SEVERE, null, ex);
                }
                box.getMaterial().setTexture("noise", Assets.loadTexture(Gdx.files.internal("data/textures/noise/roughness.png")));
                box.getMaterial().setProperty("grime", 0.9f);
                box.setDrawsShadows(true);

                RigidBodyControl rbc = new RigidBodyControl(box, MathUtils.random(5f) + 1f);
                // rbc.rotate(Vector3.X, MathUtils.random(360f));
                // rbc.rotate(Vector3.Y, MathUtils.random(360f));
                // rbc.rotate(Vector3.Z, MathUtils.random(360f));
                box.addControl(rbc);
                rootNode.attachChild(box);
            }
            addBox = false;
        }
    }

    public static void main(String args[]) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 150;
        new LwjglApplication(new TestPhysicsScene(), config);
    }
}
