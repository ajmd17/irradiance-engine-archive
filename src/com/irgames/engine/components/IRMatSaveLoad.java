/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Quaternion;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.IRMat.val;
import static com.irgames.engine.maps.Map.currentMapPath;
import com.irgames.engine.maps.MapModels;
import com.irgames.engine.maps.ModelInfo;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.components.ShaderProperty;
import com.irgames.managers.ShaderManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class IRMatSaveLoad {

    private static class IRMatInfo {

        String name;
        IRMat data;
        Integer index;
        Boolean global = false;

        public IRMatInfo(String name, Integer index, Boolean global, IRMat data) {
            this.name = name;
            this.data = data;
            this.index = index;
            this.global = global;
        }
    }

    /*public static void loadFromStr(IRNode node, String text) {
       
        String[] lines = text.split("\n");
        boolean inBlock = false;
        String matName = "";
        IRMat mat = new IRMat();
        IRShader sh = null;
        boolean global = false;
        String shName = "";
        int idx = 0;
        List<IRMatInfo> info = new ArrayList<>();
        ShaderProperties shaderProps = new ShaderProperties();
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("#START")) {

                String[] spl = line.split(" ");
                mat = new IRMat();
                matName = spl[1];
                if (matName.equalsIgnoreCase("global")) {
                    global = true;
                }
                inBlock = true;
            } else if (line.startsWith("#END")) {

                inBlock = false;
                info.add(new IRMatInfo(matName, idx, global, mat));
                idx++;
            } else if (line.startsWith("texture") && inBlock) {

                String[] spl = line.split(":");
                String[] spl1 = spl[0].split(" ");
                String valName = spl1[1].trim();
                String texPath = spl[1].trim();
                File f = new File(path);

                Texture tex = Assets.loadTexture(Gdx.files.absolute(f.getParent() + "/" + texPath));
                mat.setTexture(valName, tex);

            } else if (line.startsWith("color") && inBlock) {
                String[] spl = line.split(":");

                String[] spl1 = spl[0].split(" ");
                String valName = spl1[1];
                String[] valData = spl[1].trim().split(",");
                mat.setProperty(valName, new Color(Float.parseFloat(valData[0]), Float.parseFloat(valData[1]), Float.parseFloat(valData[2]), Float.parseFloat(valData[3])));
            } else if (line.startsWith("float") && inBlock) {
                String[] spl = line.split(":");

                String[] spl1 = spl[0].split(" ");
                String valName = spl1[1];
                float valData = Float.parseFloat(spl[1].trim());
                mat.setProperty(valName, valData);
            } else if (line.startsWith("boolean") && inBlock) {
                String[] spl = line.split(":");
                String[] spl1 = spl[0].split(" ");
                String valName = spl1[1];
                boolean valData = Boolean.parseBoolean(spl[1].trim());
                mat.setProperty(valName, valData);
            } else if (line.startsWith("shader:") && inBlock) {
                String[] spl = line.split(":");
                String className = spl[1].trim();
                shName = className;

            } else if (line.startsWith("shaderProp") && inBlock) {
                String[] spl = line.split(":");
                String[] spl1 = spl[0].split(" ");
                String name = spl1[1];
                boolean data = Boolean.parseBoolean(spl[1].trim());
                shaderProps.setProperty(name, data);
            }
            if (!"".equals(shName)) {
                try {
                    sh = ShaderManager.getShader(Class.forName(shName), shaderProps);
                    mat.shader = sh;
                } catch (Exception ex) {
                    Logger.getLogger(IRMatSaveLoad.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }
*/
    public static void load(IRNode node, String path) throws IOException {
        List<IRMatInfo> info = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        //currentMapPath = path;
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            boolean inBlock = false;
            String matName = "";
            IRMat mat = new IRMat();
            IRShader sh = null;
            boolean global = false;
            String shName = "";
            int idx = 0;
            ShaderProperties shaderProps = new ShaderProperties();
            while (line != null) {
                line = line.trim();
                if (line.startsWith("#START")) {

                    String[] spl = line.split(" ");
                    mat = new IRMat();
                    matName = spl[1];
                    if (matName.equalsIgnoreCase("global")) {
                        global = true;
                    }
                    shaderProps = new ShaderProperties();
                    inBlock = true;
                } else if (line.startsWith("#END")) {

                    inBlock = false;
                    info.add(new IRMatInfo(matName, idx, global, mat));
                    idx++;
                } else if (line.startsWith("texture") && inBlock) {

                    String[] spl = line.split(":");
                    String[] spl1 = spl[0].split(" ");
                    String valName = spl1[1].trim();
                    String texPath = spl[1].trim();
                    File f = new File(path);

                    Texture tex = Assets.loadTexture(Gdx.files.absolute(f.getParent() + "/" + texPath));
                    mat.setTexture(valName, tex);

                } else if (line.startsWith("color") && inBlock) {
                    String[] spl = line.split(":");

                    String[] spl1 = spl[0].split(" ");
                    String valName = spl1[1];
                    String[] valData = spl[1].trim().split(",");
                    mat.setProperty(valName, new Color(Float.parseFloat(valData[0]), Float.parseFloat(valData[1]), Float.parseFloat(valData[2]), Float.parseFloat(valData[3])));
                } else if (line.startsWith("float") && inBlock) {
                    String[] spl = line.split(":");

                    String[] spl1 = spl[0].split(" ");
                    String valName = spl1[1];
                    float valData = Float.parseFloat(spl[1].trim());
                    mat.setProperty(valName, valData);
                } else if (line.startsWith("boolean") && inBlock) {
                    String[] spl = line.split(":");
                    String[] spl1 = spl[0].split(" ");
                    String valName = spl1[1];
                    boolean valData = Boolean.parseBoolean(spl[1].trim());
                    mat.setProperty(valName, valData);
                } else if (line.startsWith("shader:") && inBlock) {
                    String[] spl = line.split(":");
                    String className = spl[1].trim();
                    shName = className;

                } else if (line.startsWith("shaderProp") && inBlock) {
                    String[] spl = line.split(":");
                    String[] spl1 = spl[0].split(" ");
                    String name = spl1[1];
                    boolean data = Boolean.parseBoolean(spl[1].trim());
                    shaderProps.setProperty(name, data);
                }
                if (!"".equals(shName)) {
                    try {
                        sh = ShaderManager.getShader(Class.forName(shName), shaderProps);
                        mat.shader = sh;
                    } catch (Exception ex) {
                        Logger.getLogger(IRMatSaveLoad.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                line = br.readLine();

            }
            String everything = sb.toString();
        } finally {
            br.close();
        }
        for (int i = info.size() - 1; i > -1; i--) {
            IRMatInfo inf = info.get(i);
            //System.out.println(inf.name);
            if (!inf.global) {
                IRSpatial ch = node.getChild(i);
                if (ch != null) {
                    ch.setMaterial(inf.data);
                    if (ch.shader != null) {
                        ch.setShader(ch.shader);
                    }
                }
            } else if (inf.global) {
                node.setMaterial(inf.data);
            }
        }
        getAllChildren(node);
    }

    private static void getAllChildren(IRSpatial irs) {
        System.out.println(irs.getName() + " : " + irs.getClass().getSimpleName());
        if (irs instanceof IRNode) {
            IRNode irn = (IRNode) irs;
            for (IRSpatial child : irn.getChildren()) {
                getAllChildren(child);
            }
        }
    }

    private static String saveProperties(String finalText, List<IRProperty> properties) {
        for (IRProperty v : properties) {

            String type = "";
            String data = "";
            if (v.value instanceof Float) {
                type = "float";
                Float f = (Float) v.value;
                data = f.toString();
            } else if (v.value instanceof Color) {
                type = "color";
                Color c = (Color) v.value;
                data = c.r + "," + c.g + "," + c.b + "," + c.a;
            } else if (v.value instanceof Boolean) {
                type = "boolean";
                Boolean b = (Boolean) v.value;
                data = b.toString();
            }

            finalText += type + " " + v.name + ": " + data + "\n";
        }
        return finalText;
    }

    public static void save(IRNode node, String path) {
        PrintWriter writer = null;
        try {
            String finalText = "";
            if (node.getMaterial().shader != null && node.mesh != null) {
                finalText += "#START GLOBAL\n";
                IRMat irm = node.getMaterial();
                if (node.shader instanceof IRShader) {
                    IRShader irs = (IRShader) node.shader;
                    for (IRProperty sp : irs.getProperties()) {
                        finalText += "shaderProp " + sp.name + ": " + sp.value.toString() + "\n";
                    }
                }
                finalText += "shader: " + irm.shader.getClass().getName() + "\n";
                finalText = saveProperties(finalText, irm.getProperties());
                finalText += "#END\n";
            }
            for (int i = 0; i < node.getChildren().size(); i++) {
                IRSpatial ch = node.getChild(i);
                IRMat irm = ch.getMaterial();
                if (irm != null) {
                    finalText += "#START " + ch.getName() + "\n";
                    if (irm.shader instanceof IRShader) {
                        IRShader irs = (IRShader) irm.shader;
                        for (IRProperty sp : irs.getProperties()) {
                            finalText += "shaderProp " + sp.name + ": " + sp.value.toString() + "\n";
                        }
                    }
                    //finalText += "// shader properties MUST go before the shader is set!\n//Otherwise no properties will be sent to the shader"
                    finalText += "shader: " + irm.shader.getClass().getName() + "\n";
                    saveProperties(finalText, irm.getProperties());
                    finalText += "#END\n";
                }
            }
            writer = new PrintWriter(path, "UTF-8");
            writer.print(finalText);
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IRMatSaveLoad.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(IRMatSaveLoad.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            writer.close();
        }
    }
}
