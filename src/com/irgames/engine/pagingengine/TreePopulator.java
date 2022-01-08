/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.pagingengine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.controls.RigidBodyControl;
import com.irgames.engine.shaders.BarkShader;
import com.irgames.engine.shaders.LeafShader;
import com.irgames.managers.LightingManager;
import com.irgames.managers.ShaderManager;
import com.irgames.engine.terrain.TerrainComponent;
import com.irgames.engine.game.TestShader;
import com.irgames.engine.game.TreeModel;
import com.irgames.utils.MathUtil;

/**
 *
 * @author Andrew
 */
public class TreePopulator extends EntityPopulator {

    ModelBuilder modelBuilder = new ModelBuilder();
    ObjLoader objLoader;
    LeafShader leafshader;
    BarkShader barkshader;
    Shader s;
    Model model;

    public TreePopulator(IRNode rootNode) {
        super("trees", rootNode);
       // s = new TestShader();
       // s.init();
        objLoader = new ObjLoader();
        model = Assets.loadObjModel(Gdx.files.internal("data/trees/conifer/conifer.obj"));
        this.tolerance = 0.3f;
        /*leafshader = new LeafShader("data/trees/conifer/Leaf_Low.png", 1);
        leafshader.init();
        barkshader = new BarkShader("data/trees/conifer/BlueSpruceBark.png", "data/trees/conifer/BlueSpruceBark_Normal.png", 1);
        barkshader.init();*/

        this.chunkAmount = 1;
        this.entityPerPatch = 1;
        this.patchCount = 1;
        this.spread = 35;
        //ShaderManager.addLeafShader(leafshader);
        //ShaderManager.addBarkShader(barkshader);

        //LightingManager.addShader(leafshader);
        //LightingManager.addShader(barkshader);
        this.useBatching = false;
        this.random = true;
    }

    @Override
    public IRNode setupEntityNode(Vector3 loc) {

        TreeModel tm = new TreeModel(model, leafshader, barkshader, loc);
        float randScale = (float) MathUtil.randomInRange(1.0f, 1.3f);
        tm.setScale(new Vector3(1.5f, 1.5f * randScale, 1.5f));
    //    tm.setDrawsShadows(true);
        //tm.addControl(new RigidBodyControl(tm, 0));
        return tm;
    }
}
