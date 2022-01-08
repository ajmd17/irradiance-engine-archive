/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.game.tests;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.IRMatSaveLoad;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.game.Game;
import static com.irgames.engine.game.Game.rootNode;
import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.SimpleLit;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.managers.LightingManager;
import com.irgames.managers.ShaderManager;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class TestCornellBox extends Game {
    @Override
    public void init() {
        physics.renderDebug = false;
        this.setTitle("Cornell Box Test");
        this.setBackgroundColor(Color.PINK);
        
        LightingManager.setSunDirection(new Vector3(-1,-1,-1));
        
        IRNode irn = Assets.loadObjNode(Gdx.files.getFileHandle("data/models/tests/cornell/box.obj", Files.FileType.Internal));
        irn.scale(new Vector3(2,2,2));
        try {
            irn.setShader(ShaderManager.getShader(SimpleLit.class, new ShaderProperties()));
        } catch (Exception ex) {
            Logger.getLogger(TestCornellBox.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            IRMatSaveLoad.load(irn,Gdx.files.getFileHandle("data/models/tests/cornell/box.obj", Files.FileType.Internal).path());
        } catch (IOException ex) {
            Logger.getLogger(TestCornellBox.class.getName()).log(Level.SEVERE, null, ex);
        }
        rootNode.attachChild(irn);
 
    }

    @Override
    public void update() {
    }
    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        new LwjglApplication(new TestCornellBox(), config);
    }
}
