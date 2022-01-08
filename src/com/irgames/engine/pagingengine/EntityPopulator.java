/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.pagingengine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.irgames.engine.components.DynamicModel;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.IRNode;
import com.irgames.utils.NodeUtils;
import com.irgames.engine.components.GridTile;
import com.irgames.engine.components.IRGeom;
import com.irgames.engine.components.IRSpatial;
import com.irgames.engine.controls.RigidBodyControl;
import com.irgames.engine.maps.Map;
import com.irgames.utils.MathUtil;
import com.irgames.utils.MeshUtils;
import com.irgames.engine.game.MyModelInstance;
import com.irgames.engine.terrain.TerrainPatch;
import com.irgames.engine.terrain.TerrainComponent;
import com.irgames.engine.game.TestShader;
import com.irgames.utils.MyRandom;
import com.irgames.utils.RenderUtils;
import com.irgames.utils.RenderUtils.Bucket;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Andrew
 */
public class EntityPopulator {

    IRNode mainNode = new IRNode();
    List<Patch> patches = new ArrayList<Patch>();
    public TerrainComponent terrain;
    public boolean useBatching = true;
    protected int chunkAmount = 3;
    protected float tolerance = 0.15f;
    protected float maxDistance = 700f;
    protected int spread = 5;
    protected int entityPerPatch = 1;
    protected int patchCount = 1;
    protected int patchSpread = -1;
    protected String entityName;
    protected Bucket bucket = Bucket.opaque;
    boolean random = true;
    Shader shader = new TestShader();
    float chunkSize;

    public String getName() {
        return entityName;
    }

    public void rebuild() {
        this.patches.clear();
        this.mainNode.detachAllChildren();
        this.patchesToAddTo.clear();
    }

    protected void createPatches(TerrainPatch terrainPatch) {
        for (int i = 0; i < this.patches.size(); i++) {
            if (patches.get(i).terrainPatch.equals(terrainPatch)) {
                return;
            }
        }
        //scale = terrain.getScale();
        Vector3 chunkStart = Vector3.Zero.cpy();
        chunkStart.x = terrainPatch.tile.x;
        chunkStart.z = terrainPatch.tile.z;
        Vector2 centerLoc = new Vector2(terrainPatch.tile.x - terrainPatch.tile.width / 2, terrainPatch.tile.z - terrainPatch.tile.length / 2);
        //Vector2 startLoc = new Vector2(terrainPatch.tile.x - terrainPatch.tile.width / ((float) chunkAmount * (float) chunkAmount), terrainPatch.tile.z - terrainPatch.tile.length / ((float) chunkAmount * (float) chunkAmount));
        Vector2 startLoc = new Vector2(terrainPatch.tile.x, terrainPatch.tile.z);
        chunkSize = ((terrain.getChunkSize()) / (chunkAmount));
        for (int x = 0; x < chunkAmount; x++) {
            for (int z = 0; z < chunkAmount; z++) {
                Vector2 offsetLoc = new Vector2(x * chunkSize, z * chunkSize);
                Vector2 chunkLoc = startLoc.cpy().sub(offsetLoc);
                chunkStart.x = chunkLoc.x;//terrainPatch.tile.x  - (chunkSize / 2f);// + (x * chunkSize);
                chunkStart.z = chunkLoc.y;//terrainPatch.tile.z  - (chunkSize / 2f);// + (z * chunkSize);
                Vector2 chunkCenter = new Vector2(chunkStart.x, chunkStart.z);
                GridTile tile = new GridTile(chunkSize, chunkSize, chunkStart.x, chunkStart.z, this.tolerance * this.maxDistance);
                tile.center = chunkCenter;

                // mainNode.attachChild(NodeUtils.createBox(new Vector3(chunkStart.x,80, chunkStart.z), testShader));
                ArrayList<DynamicModel> Dynams = new ArrayList<DynamicModel>();
                Patch patch = new Patch(tile, null, terrainPatch);
                patch.chunkStart = chunkStart.cpy();
                patch.chunkSize = chunkSize;
                if (!this.useBatching) {
                    patch.dynamModels = Dynams;
                }
                patches.add(patch);
            }
        }
    }

    protected void loadPatches(TerrainPatch terrainPatch) throws IOException {
        for (int i = 0; i < this.patches.size(); i++) {
            if (patches.get(i).terrainPatch.equals(terrainPatch)) {
                return;
            }
        }
        //scale = terrain.getScale();
        Vector3 chunkStart = Vector3.Zero.cpy();
        chunkStart.x = terrainPatch.tile.x;
        chunkStart.z = terrainPatch.tile.z;
        Vector2 centerLoc = new Vector2(terrainPatch.tile.x - terrainPatch.tile.width / 2, terrainPatch.tile.z - terrainPatch.tile.length / 2);
        //Vector2 startLoc = new Vector2(terrainPatch.tile.x - terrainPatch.tile.width / ((float) chunkAmount * (float) chunkAmount), terrainPatch.tile.z - terrainPatch.tile.length / ((float) chunkAmount * (float) chunkAmount));
        Vector2 startLoc = new Vector2(terrainPatch.tile.x, terrainPatch.tile.z);
        chunkSize = ((terrain.getChunkSize()) / (chunkAmount));
        for (int x = 0; x < chunkAmount; x++) {
            for (int z = 0; z < chunkAmount; z++) {

                Vector2 offsetLoc = new Vector2(x * chunkSize, z * chunkSize);
                Vector2 chunkLoc = startLoc.cpy().sub(offsetLoc);
                chunkStart.x = chunkLoc.x;//terrainPatch.tile.x  - (chunkSize / 2f);// + (x * chunkSize);
                chunkStart.z = chunkLoc.y;//terrainPatch.tile.z  - (chunkSize / 2f);// + (z * chunkSize);
                Vector2 chunkCenter = new Vector2(chunkStart.x, chunkStart.z);
                GridTile tile = new GridTile(chunkSize, chunkSize, chunkStart.x, chunkStart.z, this.tolerance * this.maxDistance);
                tile.center = chunkCenter;
                // mainNode.attachChild(NodeUtils.createBox(new Vector3(chunkStart.x,80, chunkStart.z), testShader));
                ArrayList<DynamicModel> Dynams = new ArrayList<DynamicModel>();

                Patch patch = new Patch(tile, loadPatch(chunkStart.cpy(), Dynams), terrainPatch);
                if (!this.useBatching) {
                    patch.dynamModels = Dynams;
                }
                patches.add(patch);
            }
        }
    }

    public IRNode loadPatch(Vector3 loc, List<DynamicModel> outDynamMods) throws FileNotFoundException, IOException {
        IRNode patch = new IRNode();
        //  scale = terrain.getScale();
        String filePath = Map.currentMapPath.
                substring(0, Map.currentMapPath.lastIndexOf(File.separator));
        patch.setLocalTranslation(loc);
        try {
            if (!filePath.equals("")) {
                String path = filePath + File.separator + entityName + File.separator + entityName + "_" + loc.x + "_" + loc.z + ".patch";
                path = path.replace("\"", "/");
                //System.out.println(path);
                File f = new File(path);
                if (f.exists() && !f.isDirectory()) {
                    BufferedReader br = new BufferedReader(new FileReader(path));
                    try {
                        StringBuilder sb = new StringBuilder();
                        String line = br.readLine();

                        boolean inEntity = false;
                        Vector3 location = Vector3.Zero;
                        Quaternion rotation = new Quaternion();
                        float scale = 1f;
                        while (line != null) {
                            if (!line.startsWith("#")) {

                                if (line.startsWith("{")) {
                                    inEntity = true;
                                } else if (line.startsWith("}")) {
                                    inEntity = false;
                                    //end

                                    System.out.println("added " + entityName + " at " + location);
                                    float slope = 0f;
                                    //if (slopeAffected) {
                                    //slope = terrain.normalAt(location);
                                    //}
                                    //if (useBatching) {

                                    IRNode ent = createEntity(location, slope);
                                    // ent.setScale(new Vector3(scale, scale, scale));
                                    patch.attachChild(ent);
                                    /* } else {
                                     Node mainNode = createEntity(location, slope);
                                     mainNode.setLocalScale(scale);
                                     DynamicModel dm = new DynamicModel(assetManager, mainNode, assetManager.loadTexture(getBillboardPath()), this.glights, this.shaders);
                                     outDynamMods.add(dm);
                                     }*/

                                } else {

                                    String[] sp = line.split(":");
                                    String _0 = sp[0].trim();
                                    String _1 = sp[1].trim();

                                    /*if (_0.startsWith("patchloc")) {
                                     String locat = _1;
                                     locat = locat.replace(")", "");
                                     locat = locat.replace("(", "");
                                     locat = locat.replace(" ", "");
                                     String[] xyz = locat.split(",");
                                     Vector3f location = new Vector3f(Float.parseFloat(xyz[0]), Float.parseFloat(xyz[1]), Float.parseFloat(xyz[2]));
                                     patch.setLocalTranslation(location);
                                     }*/
                                    if (_0.startsWith("location")) {
                                        String locat = _1;
                                        locat = locat.replace(")", "");
                                        locat = locat.replace("(", "");
                                        locat = locat.replace(" ", "");
                                        String[] xyz = locat.split(",");
                                        location = new Vector3(Float.parseFloat(xyz[0]), Float.parseFloat(xyz[1]), Float.parseFloat(xyz[2]));

                                    } else if (_0.startsWith("rotation")) {
                                    } else if (_0.startsWith("scale")) {
                                        scale = Float.parseFloat(_1);

                                    }
                                }

                            }
                            line = br.readLine();

                        }
                        // String everything = sb.toString();
                    } finally {
                        br.close();
                    }

                }
            }
        } catch (Exception e) {

        }
        if (useBatching) {
            this.optimize(patch);
        }

        return patch;
    }

    public void addNewEntity(Patch p, Vector3 location) {
        float slope = 0f;
        //if (this.) {
        //  slope = terrain.normalAt(location);
        // }
        IRNode obj = createEntity(location, slope);

        RigidBodyControl rbc = new RigidBodyControl(obj, 0);
        rbc.rigidBody.userData = "foilage:" + this.entityName;
        obj.addControl(rbc);

        //  if (!this.useBatching) {
        //   p.dynamModels.add(new DynamicModel(assetManager, mainNode, assetManager.loadTexture(getBillboardPath()), this.glights, this.shaders));
        // } else {
        p.mainNode.attachChild(obj);

        // }
    }

    public EntityPopulator(String name, IRNode rootNode) {
        shader.init();
        rootNode.attachChild(mainNode);
        this.entityName = name;
    }

    public Patch getClosestPatch(Vector3 loc) {
        float closestDistance = -1;
        Patch p = null;
        for (Patch object : this.patches) {
            if (object != null) {
                float thisDistance = new Vector2(object.tile.x, object.tile.z).dst(new Vector2(loc.x, loc.z));
                if (thisDistance < closestDistance || closestDistance == -1) {
                    closestDistance = thisDistance;
                    p = (Patch) object;
                }
            }
        }
        return p;
    }

    protected IRNode patch(Vector3 location, float size, List<DynamicModel> outDynamMods) {
        IRNode patch = new IRNode();
        //  scale = terrain.getScale();
        patch.setLocalTranslation(location);
        //Generate grass uniformly with random offset.
        Vector3 candidateGrassPatchLocation;
        Vector2 grassPatchRandomOffset = Vector2.Zero.cpy();
        float grassAmt = 35f;
        int halfSize = (int) (size / 2f);
        float finalGrassAmt = 0f;
        Vector3 patchLoc = Vector3.Zero;
        //patch.attachChild(createWireframe());
        for (float x = 0; x < patchCount; x++) {
            if (patchSpread == -1) {
                patchLoc = new Vector3(MathUtil.randInt((int) -halfSize, halfSize), 0, MathUtil.randInt(-halfSize, halfSize));
            }
            for (float z = 0; z < entityPerPatch; z++) {
                //   candidateGrassPatchLocation = new Vector3(patchLoc.x + (float) MathUtils.randInt(-spread, spread), 0f, patchLoc.z + (float) MathUtils.randInt(-spread, spread));
                //candidateGrassPatchLocation.set(x * 10 + (float) MathUtils.randInt(-spread, spread), 25, z * 10 + (float) MathUtils.randInt(-spread, spread));
                Vector3 loc = new Vector3(patchLoc.x + (float) MathUtil.randInt(-spread, spread), 0f, patchLoc.z + (float) MathUtil.randInt(-spread, spread));
                Vector3 worldLocation = (location.cpy().add(loc));
                loc.y = terrain.heightAt(worldLocation);
                //candidateGrassPatchLocation.setY(terrain.heightAt(worldLocation));
                //float slope = terrain.normalAt(worldLocation);
                // if (!Float.toString(candidateGrassPatchLocation.y).equals("NaN")) {
                IRNode dNode = createEntity(loc, 0);
                patch.attachChild(dNode);
                patch.setName(entityName);
                // if (!this.useBatching) {
                //DynamicModel dm = new DynamicModel(assetManager, dNode, assetManager.loadTexture(getBillboardPath()), this.glights, this.shaders);
                //outDynamMods.add(dm);
                // } else {
                //mainNode.attachChild(dNode);
                //dNode.setWorldTranslation(location);
                //  System.out.println(dNode.getLocalTranslation() + "  " + dNode.getWorldTranslation());
                // }
                // }
                //finalGrassAmt++;
            }
        }
        //if (patch.getMaterial() == null) {
        if (useBatching) {
            patch.setMaterial(patch.getChild(0).getMaterial());
        }
        //}
        if (useBatching) {
            // GeometryBatchFactory.optimize(patch);
            /* patch.setUpdateNeeded();
             ArrayList<Mesh> meshes = new ArrayList<Mesh>();
             MeshUtils.gatherMeshes(patch, meshes);
             ArrayList<Matrix4> transforms = new ArrayList<Matrix4>();
             MeshUtils.gatherTransforms(patch, transforms);
             IRNode finalNode = new IRNode("optimized");*/
            /*
             NodePart np = new NodePart();
             MeshPart mp = new MeshPart();
             mp.mesh = MeshUtils.mergeMeshes(meshes, transforms);
             np.meshPart = mp;
             Node n = new Node();
             n.parts.add(np);*/
            // finalNode.setLocalTranslation(location);
         /*   finalNode.mesh = MeshUtils.mergeMeshes(meshes, transforms);
             finalNode.meshPartOffset = patch.meshPartOffset;
             finalNode.meshPartSize = finalNode.mesh.getNumIndices();
             finalNode.primitiveType = GL20.GL_TRIANGLES;
             //  MeshUtils.optimize(patch, finalNode);
             finalNode.shader = this.testShader;
             //System.out.println(finalNode.mesh.getNumVertices());
             patch.detachAllChildren();
             //np.setRenderable(finalNode);
             patch.attachChild(finalNode);*/
            this.optimize(patch);
        }
        patch.setBucket(this.bucket);
        return patch;
    }

    public void optimize(IRNode node) {
        node.setUpdateNeeded();
        ArrayList<Mesh> meshes = new ArrayList<>();
        MeshUtils.gatherMeshes(node, meshes);
        ArrayList<Matrix4> transforms = new ArrayList<>();
        MeshUtils.gatherTransforms(node, transforms);
        IRGeom finalNode = new IRGeom();
        node.setName(this.getName() + " (optimized)");
        finalNode.setName("Optimized geometry");
        finalNode.mesh = MeshUtils.mergeMeshes(meshes, transforms);
        finalNode.meshPartOffset = node.meshPartOffset;
        finalNode.meshPartSize = finalNode.mesh.getNumIndices();
        finalNode.primitiveType = GL20.GL_TRIANGLES;
        //  MeshUtils.optimize(patch, finalNode);
        finalNode.shader = this.shader;
        node.detachAllChildren();
        //for (IRSpatial ch : node.getChildren()) {
        //ch = null;
        // }
        node.attachChild(finalNode);
        finalNode.setMaterial(node.getMaterial());
    }

    protected IRNode setupEntityNode(Vector3 location) {
        return null;
    }

    public IRNode createEntity(Vector3 location, float slope) {
        IRNode entity = setupEntityNode(location);
        if (this.random) {
            entity.rotate(Vector3.Y, MyRandom.random.nextFloat() * 360f);
            entity.updateWorldMatrix();
            //entity.worldTransform.rotate(new Quaternion().setFromAxisRad(new Vector3(0, 1, 0), ((rand.nextFloat() * 349f) + 1f) * (float)Math.PI / 180f));
        }
        //if (this.slopeAffected) {
        //entity.rotate(this.slope, 0, 0);
        //}
        //entity.setShadowMode(this.shadowMode);
        return entity;
    }

    public void addToPatch(TerrainPatch terrainPatch) {
        if (terrainPatch.model != null) {
            if (!patchesToAddTo.contains(terrainPatch)) {
                this.patchesToAddTo.add(terrainPatch);
            }
        }
    }
    float queueUpdateTimer = 0f;
    float queueUpdateTimerMax = 1f;
    List<TerrainPatch> patchesToAddTo = new ArrayList<TerrainPatch>();

    public void remove(TerrainPatch patch) {
        for (int i = 0; i < patches.size(); i++) {
            if (patches.get(i).terrainPatch.tile.equals(patch.tile)) {
                this.mainNode.detachChild(patches.get(i).mainNode);
                patches.remove(i);
            }
        }
        for (int i = 0; i < patchesToAddTo.size(); i++) {
            if (patchesToAddTo.get(i) == patch) {
                patchesToAddTo.remove(i);
            }
        }
    }

    static String toString(Vector3 v3) {
        return v3.x + "," + v3.y + "," + v3.z;
    }

    static String toString(Quaternion q) {
        return q.x + "," + q.y + "," + q.z + "," + q.w;
    }

    public void saveAllPatches() throws FileNotFoundException, UnsupportedEncodingException {
        String filePath = Map.currentMapPath.
                substring(0, Map.currentMapPath.lastIndexOf(File.separator));
        new File(filePath + File.separator + entityName).mkdirs();
        for (Patch patche : patches) {
            PrintWriter writer = null;
            String allText = "";//patchloc: " + patches.get(ii).tile..toString() + "\n";
            /*if (!this.useBatching) {
             for (int i = 0; i < patches.get(ii).dynamModels.size(); i++) {
             allText += "{\n"
             + "   location: " + patches.get(ii).dynamModels.get(i).getLocalLocation().toString() + "\n"
             + "   rotation: " + patches.get(ii).dynamModels.get(i).getLocalRotation().toString() + "\n"
             + "   scale: " + patches.get(ii).dynamModels.get(i).getScale() + "\n}\n";
             }
             } else {*/
            for (int i = 0; i < patche.mainNode.getChildren().size(); i++) {
                //allText += "obj: " + patches.get(ii).entityNode.getChild(i).getLocalTranslation().toString() + "\n";
                allText += "{\n"
                        + "   location: " + toString(patche.mainNode.getChild(i).getLocalTranslation())
                        + "\n" + "   rotation: " + toString(patche.mainNode.getChild(i).getLocalRotation())
                        + "\n" + "   scale: " + patche.mainNode.getChild(i).getLocalScale().x + "\n}\n";
            }
            // }
            if (!allText.equals("")) {
                String x = Float.toString(patche.tile.x);
                String z = Float.toString(patche.tile.z);
                writer = new PrintWriter(filePath + File.separator + entityName + File.separator + entityName + "_" + x + "_" + z + ".patch", "UTF-8");
                writer.print(allText);
                writer.close();
            }
        }
    }

    public void updatePatchesQueue(float tpf) throws IOException {
        if (patchesToAddTo.size() > 0) {
            if (queueUpdateTimer >= queueUpdateTimerMax) {
                //Planting ...
                if (random) {
                    this.createPatches(patchesToAddTo.get(0)); // randomly populate the map

                } else {
                    this.loadPatches(patchesToAddTo.get(0)); //  load the positions from files

                }
                patchesToAddTo.remove(0);
                queueUpdateTimer = 0f;
            } else {
                queueUpdateTimer += tpf;
            }
        }
    }
    float updateTime = 0f;
    final float maxUpdateTime = 1f;

    public void update(Vector3 camLoc, float delta) throws IOException {
        //manager.update();
        updatePatchesQueue(delta);
        if (this.updateTime < maxUpdateTime) {
            updateTime += delta*10;
        } else if (updateTime >= maxUpdateTime) {

            for (Patch patche : patches) {
                if (patche.tile.inRange(camLoc)) {
                    if (patche.pageState == Patch.PageState.UNLOADED) {
                        if (!patche.created && random) {
                            if (this.random) {
                                patche.mainNode = patch(patche.chunkStart, patche.chunkSize, patche.dynamModels);
                            }
                            patche.created = true;
                            if (!mainNode.hasChild(patche.mainNode)) {
                                mainNode.attachChild(patche.mainNode);
                            }
                        }

                        if (!mainNode.hasChild(patche.mainNode)) {
                            mainNode.attachChild(patche.mainNode);
                        }

                    }
                    patche.pageState = Patch.PageState.LOADED;
                } else {
                    if (patche.pageState == Patch.PageState.LOADED) {
                        patche.pageState = Patch.PageState.UNLOADING;
                    }

                }
                if (patche.pageState != Patch.PageState.UNLOADED) {
                    patche.update(delta, camLoc);
                } else {
                    if (mainNode.hasChild(patche.mainNode)) {
                        mainNode.detachChild(patche.mainNode);
                    }
                }
            }
            updateTime = 0f;
        }
    }
}
