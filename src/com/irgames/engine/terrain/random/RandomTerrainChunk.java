/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.terrain.random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.GridTile;
import com.irgames.engine.components.IRMat;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.controls.RigidBodyControl;
import com.irgames.engine.game.tests.TestRandomTerrain;
import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.SimpleLit;
import com.irgames.engine.shaders.TerrainShader;
import com.irgames.engine.shaders.TerrainShader2;
import com.irgames.engine.terrain.HeightmapTerrain;
import com.irgames.managers.ShaderManager;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class RandomTerrainChunk extends IRNode {

    public int x, z;
    public RandomHeightmap hm;
    public RandomTerrainChunk[] neighbors = new RandomTerrainChunk[4];
    private final Vector3 scale;
    private final int chunkSize;
    RandomTerrain parentT;

    public void create() {
        hm = new RandomHeightmap(parentT, x, z, scale, chunkSize);
        mesh = new Mesh(true, hm.vertices.length / 3, hm.indices.length,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));

        mesh.setVertices(hm.vertices);
        mesh.setIndices(hm.indices);

        hm = null;

        meshPartSize = mesh.getNumIndices();
        primitiveType = GL20.GL_TRIANGLES;

        IRMat mat = new IRMat().setPropertyC("albedo", new Color(0.3f, 0.8f, 0.3f, 1.0f));
        mat.setTexture("grass", Assets.loadTexture(Gdx.files.internal("data/textures/grass.jpg")));
        mat.setTexture("slope", Assets.loadTexture(Gdx.files.internal("data/textures/rock.jpg")));
        setMaterial(mat);
        try {
            setShader(ShaderManager.getShader(TerrainShader2.class, new ShaderProperties().setProperty("GRASS_TEX", true).setProperty("SLOPE_TEX", true)));
        } catch (Exception ex) {
            Logger.getLogger(RandomTerrainChunk.class.getName()).log(Level.SEVERE, null, ex);
        }

        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        mb.part("hi", mesh, primitiveType, this.meshPartOffset, this.meshPartSize, new Material());
        Model m = mb.end();
        mb = null;
        this.ins = new ModelInstance(m);
        addPhysics();
    }

    public void addPhysics() {
        this.addControl(new RigidBodyControl(ins, 0f, true));
    }

    public void removePhysics() {
        RigidBodyControl rbc = (RigidBodyControl) getControl(RigidBodyControl.class);
        rbc.disable();

        this.removeControl(rbc);

        rbc = null;

    }

    public RandomTerrainChunk(RandomTerrain parentT, int x, int z, Vector3 scale, int chunkSize, RandomTerrainChunk[] neighbors) {
        super("HeightmapChunk");
        this.x = x;
        this.z = z;
        this.neighbors = neighbors;
        this.scale = scale;
        this.chunkSize = chunkSize;
        this.parentT = parentT;
        // Heightmap hmt = new Heightmap(new File("data/heightmaps/hm-" + x + "-" + z + ".png"), scale);

    }
}
