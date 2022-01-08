/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.terrain;

import com.irgames.engine.terrain.random.RandomTerrainChunk;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.GameComponent;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.components.IRShader;
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
public class HeightmapTerrain extends GameComponent {

    public static class HeightInfo {

        public Vector2 pos = new Vector2();
        public HeightmapTerrainChunk hmtc;

        public HeightInfo(Vector2 pos, HeightmapTerrainChunk hmtc) {
            this.pos = pos;
            this.hmtc = hmtc;
        }
    }
    public float[] rHeights;
    public List<HeightInfo> heightmaps = new ArrayList<>();
    File file;
    BufferedImage img;
    Vector3 scale = new Vector3(1, 35, 1);
    int chunkWidth;
    int chunkHeight;
    int rows = 4;
    int cols = 4;
    int chunkSize = 128;
    IRNode box;
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

    public HeightmapTerrain() throws IOException {
        //  IRShader.enableWireframe();
        rHeights = new float[4];
        
        file = new File("data/textures/heightmap.png");
        img = ImageIO.read(file);
        //   chunkWidth = //(int) (img.getWidth() * scale.x / rows);
        // chunkHeight = //(int) (img.getHeight() * scale.z / cols);
        box = NodeUtils.createBox(Vector3.Zero, new Vector3(5, 5, 5));
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

    public HeightInfo hmWithCoords(int x, int z) {
        for (int i = 0; i < heightmaps.size(); i++) {
            if (heightmaps.get(i).pos.x == x && heightmaps.get(i).pos.y == z) {
                return heightmaps.get(i);
            }
        }

        return null;
    }

    @Override
    public void update() {

    }



    @Override
    public void init() {

        try {
            ImageSplitter.doHeightmap(file, rows, cols);
        } catch (IOException ex) {
            Logger.getLogger(HeightmapTerrain.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (int x = 0; x < rows; x++) {
            for (int z = 0; z < cols; z++) {
                Point cPoint = new Point(x, z);
                Point newPoint = ImageSplitter.getPoint(cPoint, rows, cols);
                HeightmapTerrainChunk hmtc = new HeightmapTerrainChunk(new File("/data/heightmaps/hm-" + newPoint.x + "-" + newPoint.y + ".png"), x, z, scale, chunkSize, null);
                heightmaps.add(new HeightInfo(new Vector2(x, z), hmtc));
            }
        }
        for (int x = 0; x < rows; x++) {
            for (int z = 0; z < cols; z++) {

                HeightInfo hmtc = this.hmWithCoords(x, z);
                hmtc.hmtc.create();
                rootNode.attachChild(hmtc.hmtc);
                hmtc.hmtc.setLocalTranslation(new Vector3((x - (rows / 2)) * (chunkSize - 1) * scale.x, 0, (z - (cols / 2)) * (chunkSize - 1) * scale.z));
            }
        }
        //this.rootNode.attachChild(rootNode);
    }

    public void getHeightmap(int x, int z) {

    }
}
