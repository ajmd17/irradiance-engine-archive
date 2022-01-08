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

import com.badlogic.gdx.Files;
import com.irgames.managers.RenderManager;
import com.badlogic.gdx.Gdx;
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
import com.irgames.engine.assets.MTLtoIRMAT;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.controls.RigidBodyControl;
import com.irgames.engine.deferredrendering.DeferredRenderManager;
import com.irgames.engine.components.IRMat;
import com.irgames.engine.components.IRMatSaveLoad;

import com.irgames.engine.game.Game;
import com.irgames.engine.game.depth.DepthShaderProvider;
import com.irgames.engine.shaders.SimpleLit;
import com.irgames.engine.deferredrendering.DeferredRendering;
import static com.irgames.engine.game.Game.rootNode;
import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.utils.NodeUtils;
import com.irgames.managers.DepthTextureManager;
import com.irgames.engine.shaders.post.DepthTextureVisualizer;
import com.irgames.engine.shaders.post.DepthToWorld;
import com.irgames.engine.shaders.post.PostFilter;
import com.irgames.engine.shaders.post.ShadowPostFilter;
import com.irgames.engine.shadows.ShadowMapperComponent;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.MyRandom;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestDeferred extends Game {

    ShadowMapperComponent shadowMapper;
    IRNode teapot;
    ShapeRenderer sr;
    ShadowPostFilter posConstruct;
    DeferredRenderManager drm;
    SimpleLit slit;

    @Override
    public void init() {
        slit = new SimpleLit();
        slit.init();
        physics.renderDebug = true;
        this.setTitle("Deferred Rendering");
        this.setBackgroundColor(Color.WHITE);
        physics.renderDebug = false;
        IRNode floor = NodeUtils.createBox(Vector3.Zero, new Vector3(64, 1, 64));
        RigidBodyControl rbc2 = new RigidBodyControl(floor, 0f);
        // floor.addControl(rbc2);
        floor.setDrawsShadows(true);
        rootNode.attachChild(floor);

        drm = new DeferredRenderManager(renderManager);

        processor.add(new DeferredRendering());
        teapot = NodeUtils.createTeapot(new Vector3(0, 5, 0), new Vector3(5, 5, 5));
        //teapot.shader = testShader;
        teapot.setDrawsShadows(true);
        rootNode.attachChild(teapot);
        shadowMapper = new ShadowMapperComponent();
       // addComponent(shadowMapper);
        // processor.add(posConstruct = new ShadowPostFilter());
        // shadowMapper.postFilterMode(posConstruct);
        shadowMapper.setRenderSplits(false);
        IRNode irn = Assets.loadObjNode(Gdx.files.getFileHandle("data/models/tests/level/level.obj", Files.FileType.Internal));

        try {
            irn.setShader(ShaderManager.getShader(BRDFShader.class, new ShaderProperties()));
        } catch (Exception ex) {
            Logger.getLogger(TestFPSCam.class.getName()).log(Level.SEVERE, null, ex);
        }
        // irn.setMaterial(new Presets().PBR_RedPaint);
        System.out.println(irn.getChildren().size());
        try {
            MTLtoIRMAT.convert(Gdx.files.getFileHandle("data/models/tests/level/level.mtl", Files.FileType.Internal).readString(), "data/models/tests/level/level.irmat");
            IRMatSaveLoad.load(irn, "data/models/tests/level/level.irmat");
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            Logger.getLogger(TestFPSCam.class.getName()).log(Level.SEVERE, null, ex);
        }
        irn.scale(new Vector3(2, 2, 2));
        rootNode.attachChild(irn);
        for (int i = 0; i < 15; i++) {
            dropBox();
        }
        //cam.rotate(Vector3.Y, 20);
        //cam.update();
    }
        static final Vector3 l_vez = new Vector3();
	static final Vector3 l_vex = new Vector3();
	static final Vector3 l_vey = new Vector3();
        public Matrix4 setToLookAt (Vector3 direction, Vector3 up) {
            Matrix4 res = new Matrix4();
		l_vez.set(direction).nor();
		l_vex.set(direction).nor();
                
		l_vex.crs(up).nor();
               
		l_vey.set(l_vex).crs(l_vez).nor();
		res.idt();
		res.val[Matrix4.M00] = l_vex.x;
		res.val[Matrix4.M01] = l_vex.y;
		res.val[Matrix4.M02] = l_vex.z;
		res.val[Matrix4.M10] = l_vey.x;
		res.val[Matrix4.M11] = l_vey.y;
		res.val[Matrix4.M12] = l_vey.z;
		res.val[Matrix4.M20] = -l_vez.x;
		res.val[Matrix4.M21] = -l_vez.y;
		res.val[Matrix4.M22] = -l_vez.z;

		return res;
	}
    private void dropBox() {
        Vector3 scale = new Vector3(3, 3, 3);
        IRNode object;

        object = NodeUtils.createTeapot(new Vector3((float) com.irgames.utils.MathUtil.randomInRange(45, -45), 30, (float) com.irgames.utils.MathUtil.randomInRange(45, -45)), scale);

        object.setDrawsShadows(true);
        rootNode.attachChild(object);
      //  RigidBodyControl rbc = new RigidBodyControl(object, MathUtils.random(5f) + 1f);
        //  rbc.rotate(Vector3.X, MathUtils.random(360f));
        //  rbc.rotate(Vector3.Y, MathUtils.random(360f));
        // rbc.rotate(Vector3.Z, MathUtils.random(360f));
        //  object.addControl(rbc);
    }

    float updateTime = 0f;
    float maxUpdateTime = 0.7f;

    @Override
    public void preUpdate() {
        drm.renderNormals(cam, context);
        drm.renderDiffuse(cam, context);

        DepthTextureManager.capture();
        renderManager.renderDepthTexture(cam, context);
        DepthTextureManager.release();

    }

    @Override
    public void update() {
        this.setBackgroundColor(Color.MAROON);
        /*if (updateTime > maxUpdateTime) {
         dropBox();
         updateTime = 0f;
         }
         updateTime += Gdx.graphics.getDeltaTime();
         */
        Matrix4 tmpMat = new Matrix4();
        tmpMat.setToRotation(Vector3.Y, -20);
        Quaternion q = new Quaternion();
        cam.direction.y = 2f;
        cam.update();
        cam.view.getRotation(q);
        
        //System.out.println(q);
        
        
        
		//Quaternion q2 = new Quaternion().setFromMatrix(setToLookAt(new Vector3(-1,-1,-1), Vector3.Y));
		//System.out.println(q2);
                //System.out.println(new Vector3(-1,-1,-1).crs(new Vector3(0, 0, 2)));
                Matrix4 mat = setToLookAt(new Vector3(-1,-1,-1), new Vector3(0,1,0));
                Quaternion q2 = new Quaternion().setFromMatrix(mat);
                System.out.println(q2);
                
                
                
        teapot.rotate(Vector3.X, 15f * deltaTime);
        teapot.rotate(Vector3.Y, 15f * deltaTime);
        teapot.rotate(Vector3.Z, 15f * deltaTime);
    }

    public static void main(String args[]) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 1000;
        //config.samples = 8;
        new LwjglApplication(new TestDeferred(), config);
    }
}
