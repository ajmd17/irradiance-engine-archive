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
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.controls.RigidBodyControl;

import com.irgames.engine.game.Game;
import com.irgames.engine.game.depth.DepthShaderProvider;
import com.irgames.utils.NodeUtils;
import com.irgames.managers.DepthTextureManager;
import com.irgames.engine.shaders.post.ShadowPostFilter;
import com.irgames.engine.shadows.ShadowMapperComponent;
import com.irgames.utils.MyRandom;

public class TestSimpleShadows extends Game {

    ShadowMapperComponent shadowMapper;
    IRNode teapot;
    ShapeRenderer sr;
    

    
    @Override
    public void init() {
        this.setTitle("Simple Shadows (deprecated, use TestPostShadows.java)");
        physics.renderDebug = false;
        IRNode floor = NodeUtils.createBox(Vector3.Zero, new Vector3(75, 1, 75));
        rootNode.attachChild(floor);
        floor.ins.transform.setToScaling(10, 1, 10);

        RigidBodyControl rbc2 = new RigidBodyControl(floor, 0f);
        floor.addControl(rbc2);
        floor.setDrawsShadows(true);
        shadowMapper = new ShadowMapperComponent();
        
        teapot = NodeUtils.createTeapot(new Vector3(0, 5, 0), new Vector3(5, 5, 5));
        teapot.setDrawsShadows(true);
        rootNode.attachChild(teapot);
        addComponent(shadowMapper);
    }

    private void dropBox() {
        Vector3 scale = new Vector3(1, 1, 1);
        IRNode box;
        int i = MyRandom.random.nextInt(4);
        if (i == 0) {
            box = NodeUtils.createBox(new Vector3((float) com.irgames.utils.MathUtil.randomInRange(90, -90), 30, (float) com.irgames.utils.MathUtil.randomInRange(90, -90)), scale);
        } else if (i == 1) {

            box = NodeUtils.createSphere(new Vector3((float) com.irgames.utils.MathUtil.randomInRange(90, -90), 30, (float) com.irgames.utils.MathUtil.randomInRange(90, -90)), scale);
        } else {

            box = NodeUtils.createTeapot(new Vector3((float) com.irgames.utils.MathUtil.randomInRange(90, -90), 30, (float) com.irgames.utils.MathUtil.randomInRange(90, -90)), scale);
        }
        box.setDrawsShadows(true);
        rootNode.attachChild(box);
        RigidBodyControl rbc = new RigidBodyControl(box, MathUtils.random(5f) + 1f);
        rbc.rotate(Vector3.X, MathUtils.random(360f));
        rbc.rotate(Vector3.Y, MathUtils.random(360f));
        rbc.rotate(Vector3.Z, MathUtils.random(360f));
        box.addControl(rbc);
    }

    float updateTime = 0f;
    float maxUpdateTime = 0.7f;
    @Override
    public void preUpdate() {
        DepthTextureManager.capture();
        renderManager.renderDepthTexture(cam, context);
        DepthTextureManager.release();
        
    }
    @Override
    public void update() {
        this.setBackgroundColor(Color.MAROON);
        if (updateTime > maxUpdateTime) {
            dropBox();
            updateTime = 0f;
        }
        updateTime += Gdx.graphics.getDeltaTime();
    }

    public static void main(String args[]) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 1000;
        new LwjglApplication(new TestSimpleShadows(), config);
    }
}
