/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.maps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.assets.ObjLoader3;
import com.irgames.engine.assets.loaders.NewObjLoader;
import com.irgames.engine.components.IRMat;
import com.irgames.engine.components.IRMatSaveLoad;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.game.ModelEditorGame;
import com.irgames.engine.game.ProjectProperties;
import com.irgames.engine.materials.MatCapTextures;
import com.irgames.engine.shaders.MatCapShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.SimpleLit;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.MyRandom;
import com.irgames.utils.NodeUtils;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class MapModels {

    private static String remExtension(String name) {
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex >= 0) { // to prevent exception if there is no dot
            return name.substring(0, dotIndex);
        }
        return name;
    }

    public static IRNode modelWithName(String name) {
        if (name.equals("House")) {
            //return new MedievalHouse(assetManager, glights);
        } else if (name.equals("Enemy")) {
            //return (Node) assetManager.loadModel("Models/Enemy/armydude.j3o");
        } else if (name.equals("Rock")) {
            //Node bould = (Node) assetManager.loadModel("Models/Boulder/boulder.j3o");
            //bould.scale(8f);
            //bould.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        } else if (name.equals("Log")) {
            IRNode bNode = new IRNode(Assets.loadObjModel(Gdx.files.internal("data/models/log/log.obj")));
            bNode.setName("Log");
            IRMat woodMat = new IRMat();
            woodMat.setTexture("diffuse", Assets.loadTexture(Gdx.files.internal("data/models/log/bark.tga")));
            woodMat.setTexture("normal", Assets.loadTexture(Gdx.files.internal("data/models/log/bark_nrm.tga")));

            IRMat capMat = new IRMat();
            capMat.setTexture("diffuse", Assets.loadTexture(Gdx.files.internal("data/models/log/cap.tga")));
            capMat.setTexture("normal", Assets.loadTexture(Gdx.files.internal("data/models/log/cap_nrm.tga")));
            bNode.getChild(1).setMaterial(woodMat);
            bNode.getChild(0).setMaterial(capMat);
            try {
                bNode.setShader(ShaderManager.getShader(SimpleLit.class, new ShaderProperties().setProperty("DIFFUSE_MAP", true).setProperty("NORMAL_MAP", true).setProperty("SHININESS", true)));
            } catch (Exception ex) {
                Logger.getLogger(MapModels.class.getName()).log(Level.SEVERE, null, ex);
            }

            return bNode;
        } else if (name.equals("Teapot")) {
            IRNode teapot = NodeUtils.createTeapot(Vector3.Zero, new Vector3(2f, 2f, 2f));
            teapot.setName("Teapot");
            IRMat teapotMat = new IRMat();

            teapotMat.setTexture("matcap", MatCapTextures.MatCap_Texture(MyRandom.random.nextInt(9) + 1));
            teapot.setMaterial(teapotMat);
            try {
                teapot.setShader(ShaderManager.getShader(MatCapShader.class, new ShaderProperties().setProperty("ENV_MAP", true)));
            } catch (Exception ex) {

            }
            return teapot;
        } else {
            Model m = null;
            IRNode irn = null;
            if (name.endsWith(".obj")) {
                //m = Assets.loadObjModel(Gdx.files.absolute(ProjectProperties.projectPath + "\\assets\\models\\" + name));
                irn = NewObjLoader.loadModelData(Gdx.files.absolute(ProjectProperties.projectPath + "\\assets\\models\\" + name), false);

            } else if (name.endsWith(".g3db")) {
                m = Assets.loadG3dModel(Gdx.files.absolute(ProjectProperties.projectPath + "\\assets\\models\\" + name));
                irn = new IRNode(m);

            }

            File f = new File(ProjectProperties.projectPath + "\\assets\\models\\" + remExtension(name) + ".irmat");
            if (f.exists()) {
                //JOptionPane.showMessageDialog(null, "Material exists!");
                try {
                    IRMatSaveLoad.load(irn, ProjectProperties.projectPath + "\\assets\\models\\" + File.separator + remExtension(name) + ".irmat");
                } catch (IOException ex) {
                    Logger.getLogger(ObjLoader3.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                if (irn != null) {
                    try {
                        irn.setShader(ShaderManager.getShader(SimpleLit.class, new ShaderProperties().setProperty("TURN_RED", true)));
                    } catch (Exception ex) {
                        Logger.getLogger(MapModels.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            return irn;

        }
        return null;
    }
}
