/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.utils.UBJsonReader;
import com.irgames.engine.components.IRNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class Assets {
    static class LoadedNode {

        public LoadedNode(IRNode mod, FileHandle fh) {
            model = mod;
            fha = fh;
        }

        public IRNode getModel() {
            return model;
        }

        public FileHandle getFileHandle() {
            return fha;
        }
        private final IRNode model;
        private final FileHandle fha;
    }
    static class LoadedModel {

        public LoadedModel(Model mod, FileHandle fh) {
            model = mod;
            fha = fh;
        }

        public Model getModel() {
            return model;
        }

        public FileHandle getFileHandle() {
            return fha;
        }
        private final Model model;
        private final FileHandle fha;
    }

    static class LoadedTexture {

        public LoadedTexture(Texture tex, FileHandle fh) {
            texture = tex;
            fha = fh;
        }

        public Texture getTexture() {
            return texture;
        }

        public FileHandle getFileHandle() {
            return fha;
        }
        private final Texture texture;
        private final FileHandle fha;
    }

    static class LoadedShader {

        public LoadedShader(Shader sh, FileHandle fh) {
            shader = sh;
            fha = fh;
        }

        public Shader getShader() {
            return shader;
        }

        public FileHandle getFileHandle() {
            return fha;
        }
        private final Shader shader;
        private final FileHandle fha;
    }

    private static LoadedTexture newLoadedTexture(Texture t, FileHandle fh) {
        LoadedTexture l = new LoadedTexture(t, fh);
        return l;
    }

    private static LoadedTexture getLoadedTexture(List<LoadedTexture> list, FileHandle path) {
        for (LoadedTexture lt : list) {
            if (lt.fha.equals(path)) {
                return lt;
            }
        }
        return null;
    }

    private static LoadedShader getLoadedShader(List<LoadedShader> list, FileHandle path) {
        for (LoadedShader ls : list) {
            if (ls.fha.equals(path)) {
                return ls;
            }
        }
        return null;
    }

    private static LoadedModel newLoadedModel(Model m, FileHandle fh) {
        LoadedModel l = new LoadedModel(m, fh);
        return l;
    }

    private static LoadedModel getLoadedModel(List<LoadedModel> list, FileHandle path) {
        for (LoadedModel lm : list) {
            if (lm.fha.equals(path)) {
                return lm;
            }
        }
        return null;
    }
    private static LoadedNode newLoadedNode(IRNode m, FileHandle fh) {
        LoadedNode l = new LoadedNode(m, fh);
        return l;
    }
    private static LoadedNode getLoadedNode(List<LoadedNode> list, FileHandle path) {
        for (LoadedNode lm : list) {
            if (lm.fha.equals(path)) {
                return lm;
            }
        }
        return null;
    }
    static ObjLoader3 objLoader = new ObjLoader3();
    static final UBJsonReader jsonReader = new UBJsonReader();
    static G3dModelLoader g3dLoader = new G3dModelLoader(jsonReader);
    static List<LoadedNode> loadedNodes = new ArrayList<>();
    static List<LoadedModel> loadedModels = new ArrayList<>();
    static List<LoadedTexture> loadedTextures = new ArrayList<>();
    static List<LoadedShader> loadedShaders = new ArrayList<>();

    public static Texture loadTexture(FileHandle path) {
        return loadTexture(path, Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
    }

    public static Texture loadTexture(FileHandle path, TextureFilter min, TextureFilter mag) {
        LoadedTexture lt = getLoadedTexture(loadedTextures, path);
        if (lt != null) {
            return lt.texture;
        } else {
            try {
                Texture t = new Texture(path, true);
                t.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
                t.setFilter(min, mag);
                loadedTextures.add(new LoadedTexture(t, path));
                return t;
            } catch (Exception ex) {
                System.out.println(ex);
                return null;
            }
        }
    }

    public static IRNode loadObjNode(FileHandle path) {
        LoadedNode lm = getLoadedNode(loadedNodes, path);
        if (lm != null) {
            return lm.model;
        } else {
            try {
                IRNode m = objLoader.getModel(path);
                loadedNodes.add(newLoadedNode(m, path));
                return m;
            } catch (Exception ex) {
                System.out.println(ex);
                return null;
            }
        }
    }

    public static Model loadObjModel(FileHandle path) {
        LoadedModel lm = getLoadedModel(loadedModels, path);
        if (lm != null) {
            return lm.model;
        } else {
            try {
                Model m = objLoader.loadModel(path);
                loadedModels.add(newLoadedModel(m, path));
                return m;
            } catch (Exception ex) {
                System.out.println(ex);
                return null;
            }
        }
    }

    public static Model loadG3dModel(FileHandle path) {
        LoadedModel lm = getLoadedModel(loadedModels, path);
        if (lm != null) {
            return lm.model;
        } else {
            try {
                Model m = g3dLoader.loadModel(path);
                loadedModels.add(newLoadedModel(m, path));
                return m;
            } catch (Exception ex) {
                System.out.println(ex);
                return null;
            }
        }
    }
    static String fbxConvPath = "/tools/fbx-conv/fbx-conv-win32.exe";

}
