/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.game.tests;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.game.Game;
import com.irgames.engine.game.ProjectProperties;
import com.irgames.engine.maps.Map;
import com.irgames.engine.pagingengine.GrassPopulator;
import com.irgames.engine.pagingengine.TreePopulator;
import com.irgames.engine.sky.SkyDomeComponent;
import com.irgames.engine.terrain.TerrainComponent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class TestStandalone extends Game {

   

    @Override
    public void init() {
        ProjectProperties.projectPath = "C:\\Users\\Andrew\\Documents\\NetBeansProjects\\IrradianceEngine3\\IrradianceEngine2\\IrradianceEngine\\NewProject\\";
        try {
            Map.loadScene(ProjectProperties.projectPath + "/assets/scenes/sponza/sponza", cam, rootNode, sceneFile);
        } catch (IOException ex) {
            Logger.getLogger(TestStandalone.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void update() {
    }

    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        new LwjglApplication(new TestStandalone(), config);
    }
}
