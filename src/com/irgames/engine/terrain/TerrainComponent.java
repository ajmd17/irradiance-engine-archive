/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.terrain;

import com.irgames.engine.shaders.TerrainShader;
import com.irgames.managers.ShaderManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.physics.Physics;
import com.irgames.engine.components.GameComponent;
import com.irgames.engine.components.GridTile;
import com.irgames.engine.components.IRMat;
import com.irgames.engine.game.MyModelInstance;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.controls.RigidBodyControl;
import com.irgames.engine.maps.Map;
import com.irgames.engine.pagingengine.EntityPopulator;
import com.irgames.engine.shaders.components.ShaderProperties;

import com.irgames.managers.LightingManager;
import com.irgames.engine.terrain.TerrainPatch.PageState;
import com.irgames.utils.BufferUtils;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class TerrainComponent extends GameComponent {

    public enum TerrainMode {

        Model,
        Heightmap,
        Procedural
    }

    class LoadingModel {

        public Model model;
        public String asset;
    }
    TerrainMode terrainMode = TerrainMode.Model;
    ModelLoader loader = new ObjLoader();
    ModelBuilder modelBuilder = new ModelBuilder();
    AssetManager manager = new AssetManager();
    int chunkAmt = 8;
    int chunkSize = 256;
    int scale = 8;
    int scaley = 3;
    List<TerrainPatch> patches = new ArrayList<TerrainPatch>();
    private List<MyModelInstance> instances = new ArrayList<MyModelInstance>();
    ShaderManager shaderManager;
    TerrainShader tShader;
    public List<LoadingModel> toLoad = new ArrayList<LoadingModel>();

    public void updateToLoad() {
        for (LoadingModel toLoad1 : toLoad) {
            if (manager.isLoaded(toLoad1.asset)) {
                toLoad1.model = manager.get(toLoad1.asset, Model.class);
                toLoad.remove(toLoad1);
            }
        }
    }

    public Vector3 getScale() {
        return new Vector3(scale, scaley, scale);
    }

    private void raiseTerrain(int brushSize, int amount, int vertID, int width, Vector3[] vertexArray) {
        for (int x = 0; x < brushSize; x++) {
            for (int z = 0; z < brushSize; z++) {
                int idx = x * width + z;
                if (idx > 0 && idx < vertexArray.length) {
                    idx += vertID;
                    vertexArray[idx].y = 0;

                }
            }
        }
    }
    IRNode mainNod;
    Physics physics;
    List<EntityPopulator> populators = new ArrayList<>();

    public void addPopulator(EntityPopulator pop) {
        populators.add(pop);
        pop.terrain = this;
    }

    public List<EntityPopulator> getPopulators() {
        return populators;
    }

    public TerrainComponent(IRNode rootNode, Physics physics) {
        this.setName("Terrain");
        mainNod = new IRNode();
        rootNode.attachChild(mainNod);
        tShader = new TerrainShader();
        tShader.init();
        LightingManager.addShader(tShader);
        this.physics = physics;
        mainNod.rotate(Vector3.Y, 180);
        mainNod.setScale(new Vector3(scale, scaley, scale));

      //  chunkSize *= scale;
      //  chunkSize /= chunkAmt;
    }

    public Vector3 heightAtVector3(Vector3 loc) {
        if (loc != null) {
            return new Vector3(loc.x, heightAt(loc), loc.z);
        } else {
            return null;
        }

    }

    public void saveAllPatches() {

        for (int ii = 0; ii < patches.size(); ii++) {
            int x = (int) patches.get(ii).tile.x / (chunkSize);
            int z = (int) patches.get(ii).tile.z / (chunkSize);

            patches.get(ii).save(Map.currentMapPath + "/terrain/terrain_" + x + "_" + z + ".j3o", chunkSize);

        }

    }

    public float heightAt(Vector3 location) {
        Vector3 pos = new Vector3(location.x, 800, location.z);
        Vector3 dir = new Vector3(0.0001f, -1, 0f);
        dir = dir.nor();
        Ray ray = new Ray(pos, dir);
        Vector3 rayLoc = Physics.terrainRayTest(ray);
        if (rayLoc != null) {
            return rayLoc.y;
        }
        return Float.NaN;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    
    public IRNode loadTerrainPatch(GridTile tile ){
        return null;
    }
    

    @Override
    public void init() {

        patches.clear();
        this.mainNod.detachAllChildren();
        for (EntityPopulator pop : populators) {
            pop.rebuild();
        }
        int start = chunkAmt / 2;
        for (int x = -start; x < start; x++) {
            for (int z = -start; z < start; z++) {
                Vector3 chunkStart = Vector3.Zero.cpy();
                chunkStart.x = (x * chunkSize);
                chunkStart.z = (z * chunkSize);
                Vector2 chunkCenter = new Vector2(chunkStart.x - (chunkSize / 2f), chunkStart.z - (chunkSize / 2f));

                GridTile tile = new GridTile(chunkSize, chunkSize, chunkStart.x, chunkStart.z, 500);
                tile.center = chunkCenter;
                TerrainPatch patch = new TerrainPatch(null, tile);
                patches.add(patch);
            }
        }
    }
    float updateTime = 0f;
    float maxUpdateTime = 2f;
    float delta;

    @Override
    public void update() {
        delta = Gdx.graphics.getDeltaTime();
        try {
            for (EntityPopulator pop : populators) {
                pop.update(cam.position, delta);
            }
        } catch (Exception ex) {

        }
        if (this.updateTime < maxUpdateTime) {
            updateTime += delta;
        } else if (updateTime >= maxUpdateTime) {
            for (TerrainPatch patche : patches) {
                if (patche.tile.inRange(cam.position)) {
                    if (patche.pageState == PageState.UNLOADED) {
                        //if (this.terrainMode == TerrainMode.Model) {
                            //patche.model = this.loadTerrainModel(patche.tile);
                        //} else if (this.terrainMode == TerrainMode.Heightmap) {
                            //patche.model = this.loadHeightmapModel(patche.tile);
                        //}
                        patche.model = loadTerrainPatch(patche.tile);
                        if (!mainNod.hasChild(patche.model) && patche.model != null) {
                            mainNod.attachChild(patche.model);
                            for (EntityPopulator pop : populators) {
                                pop.addToPatch(patche);
                                System.out.println("POP");
                            }
                        }
                    }

                    patche.pageState = PageState.LOADED;
                } else {
                    if (patche.pageState == PageState.LOADED) {
                        patche.pageState = PageState.UNLOADING;
                    }

                }
                if (patche.pageState != PageState.UNLOADED) {
                    patche.update(delta);
                } else {
                    if (mainNod.hasChild(patche.model)) {
                        RigidBodyControl rbc = (RigidBodyControl) patche.model.getControl(RigidBodyControl.class);
                        if (rbc != null) {
                            rbc.disable();
                        }
                        mainNod.detachChild(patche.model);
                        for (EntityPopulator pop : populators) {
                            pop.remove(patche);
                        }
                        patche.model = null;
                        //System.gc();
                    }
                }
            }
            updateTime = 0f;
        }
    }
}
