/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.terrain;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.GridTile;
import com.irgames.engine.components.IRNode;

/**
 *
 * @author Andrew
 */
public class TerrainPatch {

    public GridTile tile;
    float cacheTime = 0f, maxCacheTime = 2f;
    public PageState pageState = PageState.UNLOADED;

    public enum PageState {

        LOADED, UNLOADING, UNLOADED
    }

    public TerrainPatch(IRNode mod, GridTile tile) {
        this.model = mod;
        this.tile = tile;
    }
    public TerrainPatch(GridTile tile, Vector3 center) {
        this.tile = tile;
        this.tile.center = new Vector2(center.x, center.z);
    }
    public void update(float delta) {
        if (this.pageState == PageState.UNLOADING) {
            if (this.cacheTime < maxCacheTime) {
                cacheTime += delta * 1000;

            } else if (this.cacheTime >= maxCacheTime) {

                this.pageState = PageState.UNLOADED;
                cacheTime = 0f;
            }

        }
    }

    public void save(String mapPath, int chunkSize) {

        //entityNode.updateModelBound();
        //entityNode.updateGeometricState();
        /*BinaryExporter exporter = BinaryExporter.getInstance();
        File file = new File(mapPath);
        try {
            if (entityNode.getChildren().size() > 0) {

                Node n = (Node) entityNode.getChild(0);
                Geometry g = (Geometry) n.getChild(0);
                System.out.println(n.getChildren().size());
                exporter.save(g, file);
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }*/
    }
    public IRNode model;
}
