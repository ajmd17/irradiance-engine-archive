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

import com.irgames.managers.RenderManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.controls.RigidBodyControl;

import com.irgames.engine.game.Game;
import com.irgames.engine.game.depth.DepthShaderProvider;
import com.irgames.engine.pbr.Presets;
import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.post.BloomFilter;
import com.irgames.utils.NodeUtils;
import com.irgames.managers.DepthTextureManager;
import com.irgames.engine.shaders.post.DepthTextureVisualizer;
import com.irgames.engine.shaders.post.DepthToWorld;
import com.irgames.engine.shaders.post.FXAAFilter;
import com.irgames.engine.shaders.post.GammaCorrection;
import com.irgames.engine.shaders.post.PostFilter;
import com.irgames.engine.shaders.post.ShadowPostFilter;
import com.irgames.engine.shadows.ShadowMapperComponent;
import com.irgames.engine.sky.SkyBoxComponent;
import com.irgames.engine.sky.SkyboxComponent_old;
import com.irgames.engine.sky.SkyBoxLoader;
import com.irgames.engine.stats.Stats;
import com.irgames.managers.EnvironmentMapper;
import com.irgames.managers.EnvironmentMapper.EnvironmentProbe;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.MyRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestBloom extends Game {

    ShadowMapperComponent shadowMapper;
    IRNode teapot;
    ShapeRenderer sr;
    ShadowPostFilter posConstruct;
    Presets presets = new Presets();
    private boolean addBox;

    @Override
    public void init() {

        this.setTitle("Physics Test");
        physics.renderDebug = false;
        IRNode floor = NodeUtils.createBox(Vector3.Zero, new Vector3(30, 1, 30));
        floor.setMaterial(presets.PBR_RedPaint);

        try {
            floor.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties().setProperty("ENV_MAP", false)));
        } catch (Exception ex) {
            Logger.getLogger(TestBloom.class.getName()).log(Level.SEVERE, null, ex);
        }
        RigidBodyControl rbc2 = new RigidBodyControl(floor, 0f);
        floor.addControl(rbc2);
        rootNode.attachChild(floor);

        floor.setDrawsShadows(true);
        
        cam.setSmoothing(0.7f);

        addComponent(new SkyBoxComponent(SkyBoxLoader.getPath("checkered", "right.jpg"), SkyBoxLoader.getPath("checkered", "left.jpg"),
                SkyBoxLoader.getPath("checkered", "top.jpg"), SkyBoxLoader.getPath("checkered", "top.jpg"),
                SkyBoxLoader.getPath("checkered", "front.jpg"), SkyBoxLoader.getPath("checkered", "back.jpg")
        ));
        shadowMapper = new ShadowMapperComponent();
        addComponent(shadowMapper);

        processor.add(posConstruct = new ShadowPostFilter());
        

        shadowMapper.setRenderSplits(false);
        shadowMapper.postFilterMode(posConstruct);
        for (int x = -5; x < 5; x++) {
            for (int y = -5; y < 5; y++) {
                dropBox(new Vector3(x*4, 10, y*4));
            }
            
        }
        processor.add(new BloomFilter());
        processor.add(new FXAAFilter());
        processor.add(new GammaCorrection());
    }

    private void dropBox(Vector3 pos) {
        Vector3 scale = new Vector3(3, 3, 3);
        IRNode box;
        int i = MyRandom.random.nextInt(4);

        box = NodeUtils.createMonkey(pos, scale);
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
            box.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties()));
        } catch (Exception ex) {
            Logger.getLogger(TestBloom.class.getName()).log(Level.SEVERE, null, ex);
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
        DepthTextureManager.capture();
        renderManager.renderDepthTexture(cam, context);
        DepthTextureManager.release();

    }

    @Override
    public void update() {
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            cam.setFPSMode();
        }
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            cam.setDragMode();
        }
        if (addBox) {
            if (tempVec != null) {
                Vector3 scale = new Vector3(3, 3, 3);
                IRNode box;

                box = NodeUtils.createTeapot(tempVec, scale);
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
                    box.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties().setProperty("ENV_MAP", true)));
                } catch (Exception ex) {
                    Logger.getLogger(TestBloom.class.getName()).log(Level.SEVERE, null, ex);
                }

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
        config.foregroundFPS = 100;
        new LwjglApplication(new TestBloom(), config);
    }
}
