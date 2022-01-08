/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.terrain;

import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.GridTile;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.physics.Physics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Andrew
 */
public class HeightmapTerrainComponent extends TerrainComponent {

    File file;
    BufferedImage img;
    int rows = 4;
    int cols = 4;

    public HeightmapTerrainComponent(IRNode rootNode, Physics physics) {
        super(rootNode, physics);

        file = new File("data/textures/heightmap.png");
        try {
            img = ImageIO.read(file);
        } catch (IOException ex) {
            Logger.getLogger(HeightmapTerrainComponent.class.getName()).log(Level.SEVERE, null, ex);
        }

        int chunkWidth = img.getWidth() / rows;
        int chunkHeight = img.getHeight() / cols;
        try {
            ImageSplitter.doHeightmap(file, rows, cols);
        } catch (IOException ex) {
            Logger.getLogger(HeightmapTerrain.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int x = 0; x < rows; x++) {
            for (int z = 0; z < cols; z++) {
                Point cPoint = new Point(x, z);
                Point newPoint = ImageSplitter.getPoint(cPoint, rows, cols);
             //   HeightmapTerrainChunk hmtc = new HeightmapTerrainChunk(newPoint.x, newPoint.y, new Vector3(1,1,1));
              //  rootNode.attachChild(hmtc);
              //  hmtc.setLocalTranslation(new Vector3((x - (rows / 2)) * chunkWidth, 0, (z - (cols / 2)) * chunkHeight));
            }
        }

    }

    @Override
    public IRNode loadTerrainPatch(GridTile tile) {
        int x = (int) tile.x / ((int) this.chunkSize);
        int z = (int) tile.z / ((int) this.chunkSize);
        Point cPoint = new Point(x, z);
        Point newPoint = ImageSplitter.getPoint(cPoint, rows, cols);
        try {
           // IRNode node = new HeightmapTerrainChunk(newPoint.x, newPoint.y, new Vector3(1,1,1));

            //   RigidBodyControl rbc = new RigidBodyControl(node.ins, 0f, true);
            //    rbc.rigidBody.userData = ("terrain");
            //   rbc.rotate(Vector3.Y, 180f);
            //  node.addControl(rbc);
           // return node;
        } catch (Exception e) {

        }
        return null;
    }
}
