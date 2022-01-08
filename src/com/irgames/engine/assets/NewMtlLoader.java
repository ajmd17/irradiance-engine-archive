/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.assets;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelTexture;
import com.badlogic.gdx.utils.Array;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Andrew
 */
public class NewMtlLoader {

    public static Array<ModelMaterial> materials = new Array<ModelMaterial>();

    /**
     * loads .mtl file
     */
    public static void load(FileHandle file) {
        String line;
        String[] tokens;
        String curMatName = "default";
        Color difcolor = Color.WHITE;
        Color speccolor = Color.WHITE;
        float opacity = 1.f;
        float shininess = 0.f;
        String texFilename = null;

        if (file == null || file.exists() == false) {
            return;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(file.read()), 4096);
        try {
            while ((line = reader.readLine()) != null) {

                if (line.length() > 0 && line.charAt(0) == '\t') {
                    line = line.substring(1).trim();
                }

                tokens = line.split("\\s+");

                if (tokens[0].length() == 0) {
                    continue;
                } else if (tokens[0].charAt(0) == '#') {
                    continue;
                } else {
                    final String key = tokens[0].toLowerCase();
                    if (key.equals("newmtl")) {
                        ModelMaterial mat = new ModelMaterial();
                        mat.id = curMatName;
                        mat.diffuse = new Color(difcolor);
                        mat.specular = new Color(speccolor);
                        mat.opacity = opacity;
                        mat.shininess = shininess;
                        if (texFilename != null) {
                            ModelTexture tex = new ModelTexture();
                            tex.usage = ModelTexture.USAGE_DIFFUSE;
                            tex.fileName = new String(texFilename);
                            if (mat.textures == null) {
                                mat.textures = new Array<ModelTexture>(1);
                            }
                            mat.textures.add(tex);
                        }
                        materials.add(mat);

                        if (tokens.length > 1) {
                            curMatName = tokens[1];
                            curMatName = curMatName.replace('.', '_');
                        } else {
                            curMatName = "default";
                        }

                        difcolor = Color.WHITE;
                        speccolor = Color.WHITE;
                        opacity = 1.f;
                        shininess = 0.f;
                    } else if (key.equals("kd") || key.equals("ks")) // diffuse or specular
                    {
                        float r = Float.parseFloat(tokens[1]);
                        float g = Float.parseFloat(tokens[2]);
                        float b = Float.parseFloat(tokens[3]);
                        float a = 1;
                        if (tokens.length > 4) {
                            a = Float.parseFloat(tokens[4]);
                        }

                        if (tokens[0].toLowerCase().equals("kd")) {
                            difcolor = new Color();
                            difcolor.set(r, g, b, a);
                        } else {
                            speccolor = new Color();
                            speccolor.set(r, g, b, a);
                        }
                    } else if (key.equals("tr") || key.equals("d")) {
                        opacity = Float.parseFloat(tokens[1]);
                    } else if (key.equals("ns")) {
                        shininess = Float.parseFloat(tokens[1]);
                    } else if (key.equals("map_kd")) {
                        texFilename = file.parent().child(tokens[1]).path();
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            return;
        }

        // last material
        ModelMaterial mat = new ModelMaterial();
        mat.id = curMatName;
        mat.diffuse = new Color(difcolor);
        mat.specular = new Color(speccolor);
        mat.opacity = opacity;
        mat.shininess = shininess;
        if (texFilename != null) {
            ModelTexture tex = new ModelTexture();
            tex.usage = ModelTexture.USAGE_DIFFUSE;
            tex.fileName = new String(texFilename);
            if (mat.textures == null) {
                mat.textures = new Array<ModelTexture>(1);
            }
            mat.textures.add(tex);
        }
        materials.add(mat);

        return;
    }

    public static ModelMaterial getMaterial(final String name) {
        for (final ModelMaterial m : materials) {
            if (m.id.equals(name)) {
                return m;
            }
        }
        ModelMaterial mat = new ModelMaterial();
        mat.id = name;
        mat.diffuse = new Color(Color.WHITE);
        materials.add(mat);
        return mat;
    }
}
