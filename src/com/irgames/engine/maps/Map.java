/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.maps;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.IRMat;
import com.irgames.engine.components.IRMatSaveLoad;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.controls.RigidBodyControl;
import com.irgames.engine.terrain.TerrainComponent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class Map {

    public static class EntityProperty {

        String name;
        Object val;

        public EntityProperty(String name, Object val) {
            this.name = name;
            this.val = val;
        }
    }
    public static String currentMapPath;
    public static boolean edit_mode = false;
    private static List<EntityProperty> currentEntityProperties = new ArrayList<>();

    private static Object getProperty(String name) {
        for (EntityProperty prop : currentEntityProperties) {
            if (prop.name.equals(name)) {
                return prop.val;
            }
        }
        return null;
    }

    private static boolean getProperty_Bool(String name) {
        Object obj = getProperty(name);
        if (obj != null) {
            if (obj instanceof Boolean) {
                return (Boolean) obj;
            }
        }
        return false;
    }

    public static void loadScene(String path, Camera cam, IRNode rootNode, List<ModelInfo> sceneFile) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        currentMapPath = path;

        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            boolean inBlock = false;
            String script = "";
            String finalPath = "";
            Vector3 loc = Vector3.Zero;
            Quaternion rot = new Quaternion();
            Vector3 scale = new Vector3(1, 1, 1);
            String matPath = "";
            while (line != null) {

                if (line.startsWith("#START")) {
                    inBlock = true;
                    matPath = "";
                    rot = new Quaternion();
                    currentEntityProperties.clear();
                    loc = Vector3.Zero;
                } else if (line.startsWith("#END")) {
                    inBlock = false;
                    IRNode s = MapModels.modelWithName(finalPath);

                    // s.setName(Integer.toString(sceneFile.size()));
                    s.setScale(scale);
                    rootNode.attachChild(s);
                    s.setLocalTranslation(loc);
                    if (!matPath.equals("")) {
                        File f = new File(path);
                        if (matPath.equals("default")) {
                            s.setMaterial(new IRMat());
                        } else {
                            IRMatSaveLoad.load(s, f.getParent() + "/" + matPath);
                        }
                    }
                    // s.setLocalRotation(rot);
                    sceneFile.add(new ModelInfo(finalPath, loc.toString(), scale.toString(), rot.toString()));
                    if (edit_mode /*|| getProperty_Bool("physics_enabled")*/) {
                        RigidBodyControl rbc = new RigidBodyControl(s, 0);
                        s.addControl(rbc);
                    }

                    /*if (script.contains("hostileai")) {
                     System.out.println("foundai");
                     HostileAI hostile;
                     hostile = new HostileAI((Node)s, new Random(), loc, terrain, "");
                     hostile.setHeadBone("head");
                     hostile.setNeckBone("neck");

                     hostile.attachToNode(rootNode, assetManager);
                     //aiNode.batch();
                     hostile.setID("");

                     ai.addAI(hostile);
                     } else if (script.equals("")) {
                     s.setLocalTranslation(loc);
                     rootNode.attachChild(s);
                     }*/
                } else if (line.startsWith("#GPS")) {

                    String camPosStr = line.substring(5);
                    camPosStr = camPosStr.replace("[", "");
                    camPosStr = camPosStr.replace("]", "");
                    String[] camPosSpl = camPosStr.split(",");
                    Vector3 camPos = new Vector3(Float.parseFloat(camPosSpl[0]), Float.parseFloat(camPosSpl[1]), Float.parseFloat(camPosSpl[2]));
                    if (cam != null) {
                        cam.position.set(camPos);
                        cam.update();
                    }
                } else if (line.startsWith("#LOOKAT")) {
                    String camRStr = line.substring(8);
                    camRStr = camRStr.replace("[", "");
                    camRStr = camRStr.replace("]", "");
                    String[] camRSpl = camRStr.split(",");
                    Vector3 dir = new Vector3(Float.parseFloat(camRSpl[0]), Float.parseFloat(camRSpl[1]), Float.parseFloat(camRSpl[2]));
                    if (cam != null) {
                        cam.direction.set(dir);
                        cam.update();
                    }
                }
                if (inBlock == true) {

                    sb.append(line);
                    sb.append(System.lineSeparator());

                    String[] sp = line.split("=");

                    if (sp[0].startsWith("path")) {
                        finalPath = sp[1].trim();
                    } else if (sp[0].startsWith("location")) {
                        String locat = sp[1];
                        locat = locat.replace(")", "");
                        locat = locat.replace("(", "");
                        locat = locat.replace(" ", "");
                        locat = locat.replace("]", "");
                        locat = locat.replace("[", "");
                        String[] xyz = locat.split(",");
                        loc = new Vector3(Float.parseFloat(xyz[0]), Float.parseFloat(xyz[1]), Float.parseFloat(xyz[2]));
                    } else if (sp[0].startsWith("scale")) {
                        String sc = sp[1].trim();
                        sc = sc.replace(")", "");
                        sc = sc.replace("(", "");
                        sc = sc.replace(" ", "");
                        sc = sc.replace("]", "");
                        sc = sc.replace("[", "");
                        String[] xyz = sc.split(",");
                        scale = new Vector3(Float.parseFloat(xyz[0]), Float.parseFloat(xyz[1]), Float.parseFloat(xyz[2]));
                    } else if (sp[0].startsWith("rotation")) {
                        String locat = sp[1];
                        locat = locat.replace(")", "");
                        locat = locat.replace("(", "");
                        locat = locat.replace("]", "");
                        locat = locat.replace("[", "");
                        locat = locat.replace(" ", "");
                        String[] xyzw = locat.split("|");

                        //rot = new Quaternion(Float.parseFloat(xyzw[0]), Float.parseFloat(xyzw[1]), Float.parseFloat(xyzw[2]), Float.parseFloat(xyzw[3]));
                        rot = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
                    } else if (sp[0].startsWith("script")) {
                        String type = sp[1].trim();
                        script = type.toLowerCase();
                    } else if (sp[0].startsWith("material")) {
                        String matpth = sp[1].trim();
                        matPath = matpth;
                    } else if (sp[0].startsWith("e_bool")) {

                        String[] sp1 = sp[0].split(" ");

                        String name = sp1[1];

                        Boolean b = Boolean.parseBoolean(sp[1].trim());
                        currentEntityProperties.add(new EntityProperty(name, b));
                    }

                }
                line = br.readLine();

            }
            String everything = sb.toString();
        } finally {
            br.close();
        }
    }

    public static String formatProperties(List<EntityProperty> properties) {
        String finalText = "";

        for (EntityProperty e : properties) {
            if (e.val instanceof Boolean) {
                finalText += "e_bool " + e.name + " = " + e.val.toString() + "\n";
            }
        }

        return finalText;
    }

    public static void saveScene(String path, Camera cam, List<ModelInfo> entities) throws FileNotFoundException, UnsupportedEncodingException {
        currentMapPath = path;
        PrintWriter writer = null;

        String allText = "";
        allText += "#GPS " + cam.position + "\n";
        Quaternion camRot = new Quaternion();
        cam.combined.getRotation(camRot);
        allText += "#LOOKAT " + cam.direction + "\n";
        for (int i = 0; i < entities.size(); i++) {
            String sc = "";
            if (entities.get(i).path.equals("Enemy")) {
                sc = "hostileai";
            }
            allText += "#START\n" + "path = " + entities.get(i).path
                    + "\n" + "location = " + entities.get(i).loc
                    + "\n" + "scale = " + entities.get(i).scale
                    + "\n" + "rotation = " + entities.get(i).rot
                    + "\n" + "script = " + sc
                    + "\n" + formatProperties(entities.get(i).properties)
                    + "\n" + "#END\n";
        }

        String name = path;
        if (!name.endsWith(".irmap")) {
            name += ".irmap";
        }
        writer = new PrintWriter(path, "UTF-8");
        writer.print(allText);

        writer.close();

    }

}
