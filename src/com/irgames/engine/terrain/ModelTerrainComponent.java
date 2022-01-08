/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.GridTile;
import com.irgames.engine.components.IRMat;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.controls.RigidBodyControl;
import com.irgames.engine.game.MyModelInstance;
import com.irgames.engine.physics.Physics;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.TerrainShader;
import com.irgames.managers.ShaderManager;

/**
 *
 * @author Andrew
 */
public class ModelTerrainComponent extends TerrainComponent {

    public ModelTerrainComponent(IRNode rootNode, Physics physics) {
        super(rootNode, physics);
      //  mainNod.rotate(Vector3.Y, 180);
        // mainNod.setScale(new Vector3(scale, scaley, scale));

        chunkSize *= scale;
        chunkSize /= chunkAmt;
    }

    @Override
    public IRNode loadTerrainPatch(GridTile tile) {
        int x = (int) tile.x / ((int) this.chunkSize);
        int z = (int) tile.z / ((int) this.chunkSize);
        try {
            Model model = Assets.loadObjModel(Gdx.files.internal("data/terrain/terrain_" + x + "_" + z + ".obj"));
            MyModelInstance modelIns = new MyModelInstance(model);
            NodePart nPart = modelIns.model.nodes.get(0).parts.get(0);
            modelIns.transform.rotate(Vector3.Y, 180);
            modelIns.transform.scale(scale, scaley, scale);
            IRNode ren = new IRNode(modelIns);
            //ren.setDrawsShadows(true);
            // ren.drawShadows = false;
            nPart.setRenderable(ren);
            ren.shader = ShaderManager.getShader(TerrainShader.class, new ShaderProperties().setProperty("NORMAL_MAP", true));

            IRMat tmat = new IRMat();
            tmat.setTexture("normal", Assets.loadTexture(Gdx.files.internal("data/textures/grass_nrm.jpg")));
            ren.setMaterial(tmat);

            ren.ins = modelIns;
            RigidBodyControl rbc = new RigidBodyControl(modelIns, 0f, true);
            rbc.rigidBody.userData = "terrain";
            rbc.rotate(Vector3.Y, 180f);
            ren.addControl(rbc);

            return ren;
        } catch (Exception e) {

        }
        return null;
    }
}
