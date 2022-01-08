/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.sky;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 *
 * @author Andrew
 */
public class SkyBoxLoader {
    static String cubeMapPath = "data/textures/skybox_cubemaps/";
    public static FileHandle getPath(String skyboxName, String part) {
        return Gdx.files.getFileHandle(cubeMapPath + skyboxName + "/" + skyboxName + "_" + part, Files.FileType.Internal);
    }
    public static Pixmap getPixmap(String skyboxName, String part) {
        Texture tex = new Texture(Gdx.files.internal(cubeMapPath + skyboxName + "/" + skyboxName + "_" + part));
        tex.getTextureData().prepare();
        Pixmap p = tex.getTextureData().consumePixmap();
        tex.dispose();
        return p;
    }
}
