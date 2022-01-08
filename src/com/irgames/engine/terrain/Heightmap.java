/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.terrain;

import com.badlogic.gdx.math.Vector3;
import com.irgames.utils.MathUtil;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Andrew
 */
public class Heightmap {

    public float[] heights;
    public float[] vertices;
    public short[] indices;
    public int height, width;
    int vertexSize = (3 + 3 + 1 + 2);
    private final int positionSize = 3;
    private Vector3 scale;
    private float minHeight = 0f;
    private float maxHeight = 256f;

    public Heightmap(File file, Vector3 scale) {
        try {
            this.scale = scale;
            height = 32 - 1;
            width = 32 - 1;
            heights = new float[(height + 1) * (width + 1)];
            vertices = new float[heights.length * vertexSize];
            indices = new short[width * height * 6];
            this.getHeights(file);
            this.buildVertices();
            this.buildIndices();
            this.calcNormals(indices, vertices);
        } catch (Exception ex) {
            Logger.getLogger(Heightmap.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    // Gets the index of the first float of a normal for a specific vertex
    private int getNormalStart(int vertIndex) {
        return vertIndex * vertexSize + positionSize;
    }

    // Gets the index of the first float of a specific vertex
    private int getPositionStart(int vertIndex) {
        return vertIndex * vertexSize;
    }

    // Adds the provided value to the normal
    private void addNormal(int vertIndex, float[] verts, float x, float y, float z) {

        int i = getNormalStart(vertIndex);

        verts[i] += -x;
        verts[i + 1] += -y;
        verts[i + 2] += -z;
    }

    /*
     * Normalizes normals
     */
    private void normalizeNormal(int vertIndex, float[] verts) {

        int i = getNormalStart(vertIndex);

        float x = verts[i];
        float y = verts[i + 1];
        float z = verts[i + 2];

        float num2 = ((x * x) + (y * y)) + (z * z);
        float num = 1f / (float) Math.sqrt(num2);
        x *= num;
        y *= num;
        z *= num;

        verts[i] = x;
        verts[i + 1] = y;
        verts[i + 2] = z;
    }

    /*
     * Calculates the normals
     */
    private void calcNormals(short[] indices, float[] verts) {

        for (int i = 0; i < indices.length; i += 3) {
            int i1 = getPositionStart(indices[i]);
            int i2 = getPositionStart(indices[i + 1]);
            int i3 = getPositionStart(indices[i + 2]);

            // p1
            float x1 = verts[i1];
            float y1 = verts[i1 + 1];
            float z1 = verts[i1 + 2];

            // p2
            float x2 = verts[i2];
            float y2 = verts[i2 + 1];
            float z2 = verts[i2 + 2];

            // p3
            float x3 = verts[i3];
            float y3 = verts[i3 + 1];
            float z3 = verts[i3 + 2];

            // u = p3 - p1
            float ux = x3 - x1;
            float uy = y3 - y1;
            float uz = z3 - z1;

            // v = p2 - p1
            float vx = x2 - x1;
            float vy = y2 - y1;
            float vz = z2 - z1;

            // n = cross(v, u)
            float nx = (vy * uz) - (vz * uy);
            float ny = (vz * ux) - (vx * uz);
            float nz = (vx * uy) - (vy * ux);

            // normalize(n)
            float num2 = ((nx * nx) + (ny * ny)) + (nz * nz);
            float num = 1f / (float) Math.sqrt(num2);
            nx *= num;
            ny *= num;
            nz *= num;

            addNormal(indices[i], verts, nx, ny, nz);
            addNormal(indices[i + 1], verts, nx, ny, nz);
            addNormal(indices[i + 2], verts, nx, ny, nz);
        }

        for (int i = 0; i < (verts.length / vertexSize); i++) {
            normalizeNormal(i, verts);
        }
    }

    public float[] getHeights(File file) throws IOException {
        if (file.exists()) {
            BufferedImage tex = ImageIO.read(file);

            float[][] data = new float[tex.getWidth()][tex.getHeight()];
            height = tex.getHeight() - 1;
            width = tex.getWidth() - 1;
            heights = new float[(height + 1) * (width + 1)];
            vertices = new float[heights.length * vertexSize];
            indices = new short[width * height * 6];
            int idh = 0;
            Color color;
            for (int z = 0; z < width + 1; z++) {
                for (int x = 0; x < height + 1; x++) {
                    //Color.rgba8888ToColor(color, tex.getRGB(z, x));
                    int i = tex.getRGB(z, x);
                    color = new Color(i);
                    heights[idh++] = color.getRed() / 255f;
                    //   System.out.println(color.getRed()/255f);
                }
            }
            return heights;
        } else {
            height = 32 - 1;
            width = 32 - 1;
            heights = new float[(height + 1) * (width + 1)];
            vertices = new float[heights.length * vertexSize];
            indices = new short[width * height * 6];
        }
        System.out.println("File does not exist: " + file.toPath());
        return null;
    }

    
    

    public void buildVertices() {
        int heightPitch = height + 1;
        int widthPitch = width + 1;

        int idx = 0;
        int hIdx = 0;
        int strength = 10; // multiplier for height map

        int inc = vertexSize - 3;
        for (int z = 0; z < heightPitch; z++) {
            for (int x = 0; x < widthPitch; x++) {

                // POSITION
                vertices[idx++] = scale.x * x;
                vertices[idx++] = heights[hIdx++] * strength;//
                vertices[idx++] = scale.z * z;
                idx += inc;

            }

        }
    }

    private void buildIndices() {
        int idx = 0;
        short pitch = (short) (width + 1);
        short i1 = 0;
        short i2 = 1;
        short i3 = (short) (1 + pitch);
        short i4 = pitch;

        short row = 0;

        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                indices[idx++] = i1;
                indices[idx++] = i3;
                indices[idx++] = i2;

                indices[idx++] = i3;
                indices[idx++] = i1;
                indices[idx++] = i4;

                i1++;
                i2++;
                i3++;
                i4++;
            }

            row += pitch;
            i1 = row;
            i2 = (short) (row + 1);
            i3 = (short) (i2 + pitch);
            i4 = (short) (row + pitch);
        }
    }

   
}
