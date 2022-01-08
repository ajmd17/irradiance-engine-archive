/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.terrain.random;

import com.badlogic.gdx.Gdx;
import com.irgames.engine.terrain.*;
import com.irgames.engine.terrain.random.RandomTerrainChunk;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.GameComponent;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.components.IRShader;
import com.irgames.engine.game.Game;
import com.irgames.engine.shaders.SimpleLit;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.terrain.TerrainPatch.PageState;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.MathUtil;
import com.irgames.utils.NodeUtils;
import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Andrew
 */
public class RandomTerrain extends GameComponent {

    public static class HeightInfo {

        public Vector2 midpoint = new Vector2();
        public Vector2 pos = new Vector2();
        Vector3 v3tr = new Vector3();
        public RandomTerrainChunk hmtc;
        public PageState pageState = PageState.UNLOADED;
        public List<Vector2> neighbors = new ArrayList<>();
        boolean hasPhysics = false;

        public HeightInfo(Vector2 pos, RandomTerrainChunk hmtc) {
            this.pos = pos;
            this.hmtc = hmtc;
        }
        float unloadTime = 0f;

        public void updateChunk() {
            //Vector3 lt = hmtc.getLocalTranslation();
            //pos.set(lt.x, lt.z);
           // hmtc.worldTransform.getTranslation(v3tr);
           // pos.set(v3tr.x, v3tr.z);
           
            if (pageState == PageState.LOADED) {
                if (!hasPhysics) {
                    hmtc.addPhysics();
                    hasPhysics = true;
                }
                unloadTime = 0f;
            } else if (pageState == PageState.UNLOADING) {

                if (unloadTime < 0.5f) {

                    unloadTime += Gdx.graphics.getDeltaTime() * 10;
                } else {
                    if (hasPhysics) {
                        hmtc.removePhysics();
                        hasPhysics = false;
                    }
                    pageState = PageState.UNLOADED;
                }
            }
        }

    }

    public float[] rHeights;
    public List<HeightInfo> heightmaps;
    File file;
    BufferedImage img;
    Vector3 scale = new Vector3(1, 5, 1);
    int chunkWidth;
    int chunkHeight;
    int rows = 4;
    int cols = 4;
    int chunkSize = 64;
    IRNode box;

    public RandomTerrain() throws IOException {
        //  IRShader.enableWireframe();
        rHeights = new float[4];
        int numberOfOctaves = 8;
        octaves = new OpenSimplexNoise[numberOfOctaves];
        frequencys = new double[numberOfOctaves];
        amplitudes = new double[numberOfOctaves];

        for (int i = 0; i < numberOfOctaves; i++) {
            octaves[i] = new OpenSimplexNoise(666);

            frequencys[i] = Math.pow(2, i);
            amplitudes[i] = Math.pow(0.5f, octaves.length - i);

        }
    }
    OpenSimplexNoise[] octaves;
    double[] frequencys;
    double[] amplitudes;

    public double getNoise(int x, int y) {

        double result = 0;

        for (int i = 0; i < octaves.length; i++) {
            result = result + octaves[i].eval(x / frequencys[i], y / frequencys[i]) * amplitudes[i];
        }

        return result;

    }

    public HeightInfo hmWithCoords(int x, int z) {
        for (int i = 0; i < heightmaps.size(); i++) {
            if (heightmaps.get(i).pos.x == x && heightmaps.get(i).pos.y == z) {
                return heightmaps.get(i);
            }
        }

        return null;
    }

    public HeightInfo hmWithCoords(float x, float z) {

        return hmWithCoords((int) x, (int) z);
    }

    private List<Vector2> getNeighbors(HeightInfo origin) {

        List<Vector2> hinf = new ArrayList<>();
        hinf.add(new Vector2(origin.pos.x + 1, origin.pos.y));
        hinf.add(new Vector2(origin.pos.x - 1, origin.pos.y));
        hinf.add(new Vector2(origin.pos.x, origin.pos.y + 1));
        hinf.add(new Vector2(origin.pos.x, origin.pos.y - 1));
        hinf.add(new Vector2(origin.pos.x + 1, origin.pos.y - 1));
        hinf.add(new Vector2(origin.pos.x - 1, origin.pos.y - 1));
        hinf.add(new Vector2(origin.pos.x + 1, origin.pos.y + 1));
        hinf.add(new Vector2(origin.pos.x - 1, origin.pos.y - 1));
        return hinf;
    }
    Vector2 tmpCenter = new Vector2();

    private void addChunk(int x, int z) {
        HeightInfo hi = hmWithCoords(x, z);

        if (hi == null) {

            RandomTerrainChunk hmtc = new RandomTerrainChunk(this, x, z, scale, chunkSize, null);
            heightmaps.add(hi = new HeightInfo(new Vector2(x, z), hmtc));
            hi.hmtc.create();
            rootNode.attachChild(hi.hmtc);
            hi.hmtc.setLocalTranslation(new Vector3(x * (chunkSize - 1) * scale.x, 0, z * ((chunkSize - 1) * scale.z)));
            hi.midpoint = new Vector2(x * (chunkSize - 1) * scale.x - ((chunkSize - 1) * scale.x / 2), z * ((chunkSize - 1) * scale.z) - (chunkSize - 1) * scale.z / 2);

            List<Vector2> neighbors = getNeighbors(hi);
            hi.neighbors = neighbors;

            /*IRNode irn = NodeUtils.createBox(new Vector3(hi.midpoint.x, 5, hi.midpoint.y), new Vector3(5,5,5));
             try {
             irn.setShader(ShaderManager.getShader(SimpleLit.class, new ShaderProperties()));
             } catch (Exception ex) {
             Logger.getLogger(RandomTerrain.class.getName()).log(Level.SEVERE, null, ex);
             }
             this.rootNode.attachChild(irn);*/
            for (Vector2 hf : hi.neighbors) {
                
                //Vector2 sc = sclDiv(new Vector2(rootNode.getLocalTranslation().x, rootNode.getLocalTranslation().z));
                
                int _x = (int) hf.x;
                int _y = (int) hf.y;
                
                tmpCenter.set(_x, _y);
                //Vector2 v2 = new Vector2(_x, _y);
                float dst = tmpCenter.dst(v2cp);
                if (dst < maxDist) {
                    addChunk(_x, _y);
                }
            }
        } else {
            /*if (hi.pageState == PageState.UNLOADING) {
             hi.hmtc.addPhysics();
             }*/
        }
        hi.pageState = PageState.LOADED;

    }
    public Vector2 sclDiv(Vector2 a) {
        return new Vector2(a.cpy().scl(1f / ((int) chunkSize - 1)).scl(new Vector2(scale.x, scale.z)));
    }
    int maxDist = 5;
    float updateTime = 0, maxUpdateTime = 0.5f;
    Vector2 v2cp = new Vector2();
    Vector3 cp = new Vector3();

    @Override
    public void update() {

        if (updateTime > maxUpdateTime) {
            cp.set(cam.position.cpy().sub(rootNode.getWorldTranslation()).scl(1f / ((int) chunkSize - 1)).scl(scale));
            v2cp.set(cp.x, cp.z);
           // System.out.println(Game.rootNode.getLocalTranslation());
            //System.out.println(sclDiv(new Vector2(rootNode.getLocalTranslation().x, rootNode.getLocalTranslation().z)));
            System.out.println(heightmaps.size());
            for (int i = heightmaps.size() - 1; i > -1; i--) {
                HeightInfo hinf = heightmaps.get(i);

                if (hinf.pageState == PageState.LOADED) {
                    if (hinf.pos.dst(v2cp) > maxDist) {
                        hinf.pageState = PageState.UNLOADING;
                    } else {
                        for (Vector2 v2 : hinf.neighbors) {

                            addChunk((int) v2.x, (int) v2.y);

                        }
                    }

                } else if (hinf.pageState == PageState.UNLOADING) {
                    // if (hinf.pos.dst(v2cp) < maxDist) { 
                    //    hinf.pageState = PageState.LOADED;
                    // }
                } else if (hinf.pageState == PageState.UNLOADED) {
                    rootNode.detachChild(hinf.hmtc);
                    heightmaps.remove(hinf);
                    /* else {
                     hinf.hmtc.create();
                     rootNode.attachChild(hinf.hmtc);
                     hinf.pageState = PageState.LOADED;
                     }*/
                    //hinf.hmtc = null;
                    // hinf.neighbors.clear();
                    //
                    // hinf = null;
                }
                hinf.updateChunk();
            }

            addChunk((int) cp.x, (int) cp.z);
            updateTime = 0f;
        } else {
            updateTime += Gdx.graphics.getDeltaTime();

        }

    }

    @Override
    public void init() {
        heightmaps = new ArrayList<>();
        /*int amt = 6;
         for (int x = 0; x < amt; x++) {
         for (int z = 0; z < amt; z++) {
         Point cPoint = new Point(x, z);
         //Point newPoint = ImageSplitter.getPoint(cPoint, rows, cols);
         RandomTerrainChunk hmtc = new RandomTerrainChunk(this, x, z, scale, chunkSize, null);
         heightmaps.add(new HeightInfo(new Vector2(x, z), hmtc));
         }
         }
         for (int x = 0; x < amt; x++) {
         for (int z = 0; z < amt; z++) {

         HeightInfo hmtc = this.hmWithCoords(x, z);
         hmtc.hmtc.create();
         rootNode.attachChild(hmtc.hmtc);
         hmtc.hmtc.setLocalTranslation(new Vector3(x * (chunkSize - 1) * scale.x - ((chunkSize - 1) * scale.x), 0, z * (chunkSize - 1) * scale.z - ((chunkSize - 1) * scale.z)));
         }
         }*/
        //this.rootNode.attachChild(rootNode);
    }

    public void getHeightmap(int x, int z) {

    }
}
