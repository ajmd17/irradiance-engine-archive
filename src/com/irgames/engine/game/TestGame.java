/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.game;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.IRMat;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.components.IRSpatial;
import com.irgames.engine.components.listeners.IRListener;
import com.irgames.engine.controls.RigidBodyControl;
import com.irgames.engine.deferredrendering.DeferredRenderManager;
import com.irgames.engine.game.tests.TestSkyBox;
import com.irgames.engine.maps.MapModels;
import com.irgames.engine.maps.ModelInfo;
import com.irgames.engine.maps.PlantingTool;
import com.irgames.engine.pagingengine.EntityPopulator;
import com.irgames.engine.pagingengine.GrassPopulator;
import com.irgames.engine.pagingengine.Patch;
import com.irgames.engine.pagingengine.TreePopulator;
import com.irgames.engine.pbr.Presets;
import com.irgames.engine.physics.Physics;
import com.irgames.engine.components.IRShader;
import static com.irgames.engine.game.Game.context;
import com.irgames.engine.shaders.MatCapShader;
import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.SimpleLit;
import com.irgames.engine.shaders.post.DOFFilter;
import com.irgames.engine.shaders.post.DepthTextureVisualizer;
import com.irgames.engine.shaders.post.FXAAFilter;
import com.irgames.engine.shaders.post.GammaCorrection;
import com.irgames.engine.shaders.post.GaussianBlur;
import com.irgames.engine.shaders.post.LightRaysFilter;
import com.irgames.engine.shaders.post.SSAOFilter;
import com.irgames.engine.shaders.post.ShadowPostBlur;
import com.irgames.engine.shaders.post.ShadowPostFilter;
import com.irgames.engine.shadows.ShadowMapperComponent;
import com.irgames.engine.sky.SkyBoxLoader;
import com.irgames.engine.sky.SkyDomeComponent;
import com.irgames.engine.terrain.HeightmapTerrainComponent;
import com.irgames.engine.terrain.ModelTerrainComponent;
import com.irgames.engine.terrain.TerrainComponent;
import com.irgames.engine.terrain.TerrainPatch;
import com.irgames.managers.DepthTextureManager;
import com.irgames.managers.EnvironmentMapper;
import com.irgames.managers.LightingManager;
import com.irgames.managers.OcclusionTextureManager;
import com.irgames.managers.PostProcessManager;
import com.irgames.managers.RenderManager;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.MyRandom;
import com.irgames.utils.NodeUtils;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class TestGame extends Game {

    public String itemPlacing = "Teapot";
    TerrainComponent terrain;
    SkyDomeComponent skyDome;
    boolean loaded = false;
    ModelInstance modelInstance;
    AnimationController controller;
    Environment environment;
    ModelBatch modelBatch;
    SimpleLit slit;
    IRNode irn2;
    ShadowPostFilter shadowFilter;
    ShadowMapperComponent shadowMapper;
    IRMat pbrMat;
    BRDFShader em;
    public TreePopulator treePop;
    public IRListener selectedObjectChangedListener;
    public IRSpatial selectedObject;

    public PlantingTool treeTool;
    public PlantingTool selectedTool;
    public PlantingTool modelTool;

    private boolean movingFoliage;
    private EntityPopulator movingPopulator;
    private Patch movingPatch;
    DeferredRenderManager drm;
    private IRNode irn;

    @Override
    public void loadScene() {
        super.loadScene();
        terrain.init();

    }

    @Override
    public void saveScene() throws FileNotFoundException, UnsupportedEncodingException {
        super.saveScene();
        //   System.out.println("Done. Saving terrain...");
        //terrain.saveAllPatches();
        System.out.println("Done. Saving trees...");
        treePop.saveAllPatches();
        System.out.println("Done. Saving rocks...");
        // gt.boulderControl.mapPath = filePath;
        // gt.boulderControl.saveAllPatches();
        System.out.println("Completed");
    }

    @Override
    public void init() {
        physics.renderDebug = true;
        Physics.setSpeed(1.4f);

        // IRNode teapot = NodeUtils.createTeapot(new Vector3(0, 5, 0), new Vector3(5, 5, 5));
        // teapot.setDrawsShadows(true);
        // rootNode.attachChild(teapot);
        pbrMat = new IRMat();

        pbrMat.setTexture("matcap", Assets.loadTexture(Gdx.files.internal("data/materials/matcaps/zbrush-mat4.png")));
        Texture dif = new Texture(Gdx.files.internal("data/textures/smoothmetal.jpg"), true);
        //dif.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pbrMat.setTexture("diffuse", dif);

        this.cam.far = 400f;
        terrain = new ModelTerrainComponent(rootNode, physics);
        // rootNode.setDrawsShadows(true);

        terrain.addPopulator(new GrassPopulator(rootNode));
        terrain.addPopulator(treePop = new TreePopulator(rootNode));
        this.addComponent(terrain);
        drm = new DeferredRenderManager(renderManager);
        treeTool = new PlantingTool() {
            @Override
            public void onPlace(Vector3 location) {
                Patch p = treePop.getClosestPatch(location);
                System.out.println("add tree");
                if (p != null) {
                    treePop.addNewEntity(p, location.cpy().sub(new Vector3(p.tile.x, 0f, p.tile.z)));

                }
            }
        };

        modelTool = new PlantingTool() {
            @Override
            public void onPlace(Vector3 location) {

                IRNode n = MapModels.modelWithName(itemName);
                if (n != null) {

                    n.setID(Integer.toString(sceneFile.size()));
                    n.setLocalTranslation(location);
                    /* pbrMat.setTexture("matcap", Assets.loadTexture(Gdx.files.internal("data/materials/matcaps/zbrush-mat4.png")));
                     n.setMaterial(pbrMat);
                     try {
                     n.setShader(ShaderManager.getShader(MatCapShader.class, new ShaderProperties().setProperty("ENV_MAP", true).setProperty("DIFFUSE_MAP", true)));
                     } catch (Exception ex) {
                     Logger.getLogger(TestGame.class.getName()).log(Level.SEVERE, null, ex);
                     }*/
                    mapNode.attachChild(n);
                    RigidBodyControl rbc = new RigidBodyControl(n, 0);
                    n.addControl(rbc);

                    sceneFile.add(new ModelInfo(itemName, location.toString(), new Vector3(1, 1, 1).toString(), new Quaternion().toString()));
                    if (sceneFileListener != null) {
                        sceneFileListener.action(n);
                    }
                    //selectedObject = n;
                    //selectedNode = n;
                }

            }

        };
        System.out.println(new Vector3(5,8,9).mul(new Matrix4().setToTranslation(4, 30, 1)));
        shadowFilter = new ShadowPostFilter();
        shadowMapper = new ShadowMapperComponent();
        //addComponent(shadowMapper);
        shadowMapper.setRenderSplits(false);
        shadowMapper.postFilterMode(shadowFilter);
        shadowMapper.setShadowIntensity(0.5f);
       // this.processor.add(shadowFilter);
      //  this.processor.add(new ShadowPostBlur());
        skyDome = new SkyDomeComponent() {
            float shadowUpdate = Float.MAX_VALUE;
            float maxShadowUpdate = 0.35f;

            @Override
            public void onTimeChanged() {
                super.onTimeChanged();
                //if (shadowUpdate >= maxShadowUpdate) {
                //    shadowMapper.setLightDirection(LightingManager.getSunDirection());
                //   shadowMapper.setShadowIntensity(MathUtils.clamp(-LightingManager.getSunDirection().y * 5.0f, 0.0f, 0.45f));
                //shadowUpdate = 0f;
                //} else {
                //shadowUpdate += Gdx.graphics.getDeltaTime();
                //}
            }
        };
        skyDome.setTime(8f);
        skyDome.setTimeScale(0.5f);

        this.addComponent(skyDome);

        modelBatch = new ModelBatch();

        Model zomb = Assets.loadG3dModel(Gdx.files.getFileHandle("data/models/cube/zomb.g3db", FileType.Internal));

        irn = new IRNode(zomb, true);
        irn.setDrawsShadows(true);
        irn.setLocalTranslation(new Vector3(0, -25, 0));
        // IRShader.enableWireframe();
        rootNode.attachChild(irn);
        // irn.setShader(slit);
        irn.playAnimation(zomb.animations.get(3).id, -1, 1);

        RigidBodyControl rbc = new RigidBodyControl(irn, 20f, false);
        rbc.rotate(Vector3.X, -90f);
        EnvironmentMapper.setEnvironmentMap(new Pixmap[]{SkyBoxLoader.getPixmap("checkered", "right.jpg"),
            SkyBoxLoader.getPixmap("checkered", "left.jpg"),
            SkyBoxLoader.getPixmap("checkered", "top.jpg"),
            SkyBoxLoader.getPixmap("checkered", "top.jpg"),
            SkyBoxLoader.getPixmap("checkered", "front.jpg"),
            SkyBoxLoader.getPixmap("checkered", "back.jpg")});
        /*this.data = new Pixmap[]{SkyBoxLoader.getPixmap("checkered", "right.jpg"),
         SkyBoxLoader.getPixmap("checkered", "left.jpg"),
         SkyBoxLoader.getPixmap("checkered", "top.jpg"),
         SkyBoxLoader.getPixmap("checkered", "top.jpg"),
         SkyBoxLoader.getPixmap("checkered", "front.jpg"),
         SkyBoxLoader.getPixmap("checkered", "back.jpg")};

         for (int i = 0; i < data.length; i++) {
         pbrMat.addPixmap(data[i]);
         }*/
        // processor.add(new DepthTextureVisualizer());
       // processor.add(new LightRaysFilter());
        processor.add(new FXAAFilter());
        processor.add(new GammaCorrection());
        loaded = true;
    }

    @Override

    public void preUpdate() {
//        drm.renderNormals(cam, context);
        DepthTextureManager.capture();
        renderManager.renderDepthTexture(cam, context);

        DepthTextureManager.release();
        OcclusionTextureManager.capture();
        RenderManager.renderOccTexture(cam, context);
        OcclusionTextureManager.release();
        // Vector3 loc = irn.getLocalTranslation().cpy();
        //irn.setLocalTranslation(new Vector3(loc.x + 0.1f * deltaTime, loc.y, loc.z));
        //irn.setUpdateNeeded();
    }
    Presets presets = new Presets();

    @Override
    public void onMouseDragLeft() {
        if (selectedObject != null) {
            if (selectedObjectChangedListener != null) {
                this.selectedObjectChangedListener.action();
            }
            Vector3 raycast = physics.getWorldIntersectionTerrain(cam, Gdx.input.getX(), Gdx.input.getY());
            if (!movingFoliage) {

                if (raycast != null) {
                    selectedObject.setLocalTranslation(raycast);

                    this.getSceneModel(Integer.parseInt(selectedObject.getID())).loc = raycast.toString();

                }

            } else if (movingFoliage) {
                if (raycast != null) {
                    if (selectedObject != null) {
                        if (!movingPopulator.useBatching) {
                            selectedObject.setLocalTranslation(raycast.cpy().sub(new Vector3(movingPatch.tile.x, 0, movingPatch.tile.z)));
                        } else {
                            selectedObject.setLocalTranslation(raycast);
                        }

                        Patch p = movingPopulator.getClosestPatch(raycast);
                        if (!p.tile.equals(movingPatch.tile)) {
                            //we need to check if we need to give the foilage a new home

                            if (movingPopulator.useBatching) {
                                movingPatch.mainNode.detachChild(selectedObject);
                                p.mainNode.attachChild(selectedObject);
                                movingPatch = p;

                            } else {
                                System.out.println(selectedObject.getLocalScale());
                                movingPatch.mainNode.detachChild(selectedObject);

                                p.mainNode.attachChild(selectedObject);

                                movingPatch = p;
                                System.out.println(selectedObject.getLocalScale());
                            }
                            System.out.println("change nodes");

                        }
                    }
                }
            }
        }
    }

    EntityPopulator popWithName(String name) {
        for (EntityPopulator pop : terrain.getPopulators()) {
            if (pop.getName().equals(name)) {
                return pop;
            }
        }
        return null;
    }

    @Override
    public void onMouseScroll(int amt) {
        float multiplier = 0.1f;
        if (selectedObject != null && !movingFoliage) {
            selectedObject.setScale(selectedObject.getLocalScale().cpy().add(new Vector3(amt, amt, amt).scl(multiplier)));
            if (sceneFile.size() - 1 >= (Integer.parseInt(selectedObject.getID()))) {
                sceneFile.get(Integer.parseInt(selectedObject.getID())).scale = selectedObject.getLocalScale().toString();
            }
        }
    }

    @Override
    public void onClickLeft(int screenX, int screenY) {

        selectedObject = physics.getWorldIntersectionSpatial(cam, screenX, screenY);
        if (selectedObject != null) {
            try {
                RigidBodyControl rbc = (RigidBodyControl) selectedObject.getControl(RigidBodyControl.class);
                if (rbc.rigidBody.userData != null) {
                    String spl1 = (String) rbc.rigidBody.userData;
                    String[] spl2 = spl1.split(":");

                    if (spl2[0].equals("foilage")) {
                        EntityPopulator p = popWithName(spl2[1]);
                        if (p != null) {
                            movingPatch = p.getClosestPatch(selectedObject.updateWorldTranslation());
                            movingPopulator = p;
                            movingFoliage = true;
                        } else {
                            movingPopulator = null;
                            movingFoliage = false;
                        }
                    } else {
                        movingPopulator = null;
                        movingFoliage = false;
                    }
                } else {
                    movingPopulator = null;
                    movingFoliage = false;
                }
            } catch (Exception ex) {

            }
        } else {

            Vector3 raycast = physics.getWorldIntersectionTerrain(cam, Gdx.input.getX(), Gdx.input.getY());

            if (raycast != null) {

                if (selectedTool != null) {

                    selectedTool.onPlace(raycast);
                }

            }

        }
    }

    @Override
    public void drawSpriteBatch() {
    //    this.spriteBatch.begin();

        // this.spriteBatch.draw(DepthTextureManager.depthTex, 8, 8, 64, 64);
        //     this.spriteBatch.draw(DepthTextureManager.shadowMaps[0], 128, 5, 64, 64);
        //     this.spriteBatch.draw(DepthTextureManager.shadowMaps[1], 200, 5, 64, 64);
        //     this.spriteBatch.draw(DepthTextureManager.shadowMaps[2], 272, 5, 64, 64);
        //    this.spriteBatch.draw(DepthTextureManager.shadowMaps[3], 344, 5, 64, 64);
        //    this.spriteBatch.end();
    }
    Pixmap[] data = new Pixmap[6];
    float updateTime = 0;
    float maxUpdateTime = 5;

    @Override
    public void update() {
        /* if (updateTime > maxUpdateTime) {
         //  data[0] = new Pixmap(Gdx.files.internal("data/textures/wood.jpg"));
         pbrMat.setTexture("diffuse", PostProcessManager.sceneTex);
         // this.em.updateData(data);
         updateTime = 0;
         } else {
         updateTime += Gdx.graphics.getDeltaTime();
         }*/

    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        //config.vSyncEnabled = true;
        // config.resizable = false;
        new LwjglApplication(new TestGame(), config);
    }
}
