/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.assets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class MTLtoIRMAT {

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static void convert(String MTL, String outPath) throws FileNotFoundException, UnsupportedEncodingException {
        String[] splMtl = MTL.split("\n");
        String outIRMat = "";
        int count = 0;
        for (int i = 0; i < splMtl.length; i++) {
            String[] tokens = splMtl[i].split(" ");
            if (tokens[0].equals("newmtl")) {
                if (count > 0) {
                    outIRMat += "#END\n";
                }

                String name = tokens[1];
                outIRMat += "#START " + name + "\n";
                outIRMat += "shader: com.irgames.engine.shaders.BRDFShader\n";
                count++;
            } else if (tokens[0].equals("Ka")) { // ambient color
            } else if (tokens[0].equals("Kd")) { // diffuse color
                outIRMat += "color albedo: " + tokens[1] + "," + tokens[2] + "," + tokens[3] + "," + "1.0\n";
            } else if (tokens[0].equals("map_Bump")) {
                File f = new File(splMtl[i].replace("map_Bump", "").trim());

                if (f.exists()) {
                    outIRMat += "shaderProp NORMAL_MAP: true\n";
                    Path p = Paths.get(outPath);
                    Path folder = p.getParent();
                    String newstr = folder.toString();
                    File finalDir = new File(newstr + "\\" + f.getName());
                    try {
                        copyFile(f, finalDir);
                    } catch (IOException ex) {
                        Logger.getLogger(MTLtoIRMAT.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    outIRMat += "texture normal: " + f.getName() + "\n";
                }

            } else if (tokens[0].equals("Ns")) {
                float specval = Float.parseFloat(tokens[1]);
                float div = specval / 150f;
                outIRMat += "float roughness: " + div + "\n";
            } else if (tokens[0].equals("map_Kd")) { // diffuse map
                File f = new File(splMtl[i].replace("map_Kd", "").trim());

                if (f.exists()) {
                    outIRMat += "shaderProp DIFFUSE_MAP: true\n";
                    Path p = Paths.get(outPath);
                    Path folder = p.getParent();
                    String newstr = folder.toString();
                    File finalDir = new File(newstr + "\\" + f.getName());
                    try {
                        copyFile(f, finalDir);
                    } catch (IOException ex) {
                        Logger.getLogger(MTLtoIRMAT.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    outIRMat += "texture diffuse: " + f.getName() + "\n";
                }
            }

        }
        if (count > 0) {
            outIRMat += "#END\n";
        }
        PrintWriter writer = new PrintWriter(outPath, "UTF-8");
        writer.print(outIRMat);
        writer.close();
    }
}
