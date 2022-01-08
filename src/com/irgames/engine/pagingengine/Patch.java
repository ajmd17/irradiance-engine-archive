/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.pagingengine;

import com.irgames.engine.components.DynamicModel;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.components.GridTile;
import com.irgames.engine.game.MyModelInstance;
import com.irgames.engine.terrain.TerrainPatch;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class Patch {
    List<DynamicModel> dynamModels = new ArrayList<DynamicModel>();
    Vector3 chunkStart;
    boolean created;
    float chunkSize;
    
    public enum PageState {

        LOADED, UNLOADING, UNLOADED
    }
    
    public Patch(GridTile tile, IRNode entityNode, TerrainPatch terrainPatch) {
        this.mainNode = entityNode;
        this.tile = tile;
        this.terrainPatch = terrainPatch;
    }
    float cacheTime = 0f;
    float maxCacheTime = 1.5f;
    public void update(float delta, Vector3 camLoc) {
        if (this.dynamModels.size() > 0) {
            for (int i = 0; i < this.dynamModels.size(); i++) {
               // float dist = dynamModels.get(i).getLocation().dst(camLoc);
                /*if (!dynamModels.get(i).belongsToParent(entityNode)) {
                    dynamModels.get(i).attachToNode(entityNode);
                }*/

                //dynamModels.get(i).updateLOD(camLoc);

            }
        }
        if (this.pageState == PageState.UNLOADING) {
            if (this.cacheTime < maxCacheTime) {
                cacheTime += delta*1000f;
            } else if (this.cacheTime >= maxCacheTime) {
                this.pageState = PageState.UNLOADED;
                cacheTime = 0f;
            }
        }
    }
    public IRNode mainNode;
    public PageState pageState = PageState.UNLOADED;
    public TerrainPatch terrainPatch;
    public GridTile tile;
}
