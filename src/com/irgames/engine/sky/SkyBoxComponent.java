/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.sky;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.GameComponent;
import com.irgames.engine.components.IRNode;
import com.irgames.utils.NodeUtils;
import com.irgames.utils.RenderUtils;

/**
 *
 * @author Andrew
 */
public class SkyBoxComponent extends GameComponent {
    IRNode box;
    public SkyBoxShader skyShader;
    
    @Override
    public void init() {
        
        box = NodeUtils.createSkyBox(new Vector3(50, 50, 50), skyShader);
        
        box.setBucket(RenderUtils.Bucket.sky);
        box.setLocalTranslation(new Vector3(0, 0, 0));
        box.setDrawsShadows(false);
        rootNode.attachChild(box);
        
    }
    public void changeTextures(FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ, FileHandle negativeZ) {
        this.changeTextures(new Pixmap(positiveX), new Pixmap(negativeX), new Pixmap(positiveY), new Pixmap(negativeY), new Pixmap(positiveZ), new Pixmap(negativeZ));
    }
    public void changeTextures(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ) {
        skyShader.setTextures(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ);
    }
    public SkyBoxComponent(FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ, FileHandle negativeZ) {
        this(new Pixmap(positiveX), new Pixmap(negativeX), new Pixmap(positiveY), new Pixmap(negativeY), new Pixmap(positiveZ), new Pixmap(negativeZ));
    }
    public SkyBoxComponent(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ) {
        skyShader = new SkyBoxShader(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ);
        skyShader.init();
        
        
    }
    @Override 
    public void update() {
        //box.worldTransform.setToTranslation(cam.position);
    }
}
