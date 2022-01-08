/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.game;

import com.irgames.utils.MathUtil;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.IRGeom;
import com.irgames.engine.components.IRMat;
import com.irgames.utils.RenderUtils;
import com.irgames.utils.RenderUtils.Bucket;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.controls.RigidBodyControl;
import com.irgames.engine.shaders.BarkShader;
import com.irgames.engine.shaders.LeafShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.SimpleLit;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.RenderUtils.BackfaceCullMode;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class TreeModel extends IRNode {

    List<MyModelInstance> instances;
    ModelInstance myModel;
    float scl = 1.0f;
    float height;
    float amount = 1.0f;
    float speed = 1.0f;
    LeafShader leafShader;
    BarkShader barkShader;

    public TreeModel(Model model, LeafShader leafShader, BarkShader barkShader, Vector3 loc) {
        super("tree");
        //this.instances = instances;

        this.setBucket(Bucket.opaque);
        this.leafShader = leafShader;
        myModel = new ModelInstance(model);
        this.ins = myModel;
        ins.transform.setToTranslation(loc);
        //  myModel.transform.setToTranslation(new Vector3(0, 0f, 0));
        scl = 1.0f;//(float)MathUtils.randomInRange(0.8f, 1.3f);

        NodePart leafPart = model.nodes.get(1).parts.get(0); // leaves
        //shaderMan.addObject(leafshader, leafPart, Bucket.transparent);
        IRGeom leaves = new IRGeom();
        System.out.println(myModel.nodes.size);
        Texture leafTexture = Assets.loadTexture(Gdx.files.internal("data/trees/conifer/Leaf_Low.png"));
        leafTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
        IRMat leafMaterial = new IRMat();
        leafMaterial.setTexture("diffuse", leafTexture);
        leafMaterial.alphaDiscard = 0.4f;
        leafMaterial.cullMode = BackfaceCullMode.off;
        leafMaterial.setProperty("flip_y", false);
        // try {
        //    leafShader = (LeafShader) ShaderManager.getShader(LeafShader.class, new ShaderProperties().setProperty("DIFFUSE_MAP", true));
        // } catch (Exception ex) {
        //     Logger.getLogger(TreeModel.class.getName()).log(Level.SEVERE, null, ex);
        // }
        //
        try {
            this.leafShader = (LeafShader) ShaderManager.getShader(LeafShader.class, new ShaderProperties().setProperty("DIFFUSE_MAP", true).setProperty("FLIP_Y", true));
            leafMaterial.shader = this.leafShader;
        } catch (Exception ex) {
            Logger.getLogger(TreeModel.class.getName()).log(Level.SEVERE, null, ex);
        }

        

        NodePart trunkPart = model.nodes.get(0).parts.get(0); // trunk
        //shaderMan.addObject(barkshader, trunkPart, Bucket.opaque);
        IRGeom trunk = new IRGeom();
        IRMat barkMaterial = new IRMat();
        barkMaterial.cullMode = BackfaceCullMode.back;
        Texture diffuseTex = Assets.loadTexture(Gdx.files.internal("data/trees/conifer/BlueSpruceBark.png"));

        barkMaterial.setTexture("diffuse", diffuseTex);
        barkMaterial.setTexture("normal", Assets.loadTexture(Gdx.files.internal("data/trees/conifer/BlueSpruceBark_Normal.png")));
        try {
            this.barkShader = (BarkShader) ShaderManager.getShader(BarkShader.class, new ShaderProperties().setProperty("DIFFUSE_MAP", true));
            barkMaterial.shader = this.barkShader;
        } catch (Exception ex) {
            Logger.getLogger(TreeModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        trunk.setMaterial(barkMaterial);

        trunk.setBucket(Bucket.opaque);
        trunk.setDrawsShadows(true);
        trunkPart.setRenderable(trunk);
        this.attachChild(trunk);
        this.setLocalTranslation(loc);

        this.setDrawsShadows(true);
        
        
        
        
        leaves.setMaterial(leafMaterial);
        leaves.setBucket(Bucket.transparent);
        leafPart.setRenderable(leaves);

        this.attachChild(leaves);
        //trunk.ins  = myModel;

        //  this.scale(new Vector3(1, scl, 1));
        // this.speed = Math.abs((float)MathUtils.randomInRange(0.45f, 1.5f));
        // this.amount = Math.abs(MathUtils.clamp(1.0f-scl, 0.35f, 1.0f) * (float)MathUtils.randomInRange(0.85f,1.5f));
        BoundingBox bb = new BoundingBox();
        myModel.calculateBoundingBox(bb);
        Vector3 dimensions = new Vector3();
        bb.getDimensions(dimensions);
        dimensions = MathUtil.multVector3(dimensions, scl);
        height = dimensions.y;
    }

    @Override
    public void onDraw() {
        this.leafShader.setTreeHeight(height);
        this.barkShader.setTreeHeight(height);
        this.leafShader.setWindSpeed(speed);
        this.barkShader.setWindSpeed(speed);
        this.leafShader.setWindAmount(amount);
        this.barkShader.setWindAmount(amount);
        //this.leafShader.setDeltaTime(deltaTime);
        //this.barkShader.setDeltaTime(deltaTime);
    }

}
