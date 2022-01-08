/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.materials;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.irgames.engine.assets.Assets;

/**
 *
 * @author Andrew
 */
public class MatCapTextures {

    public static Texture MatCap_Texture(int i) {
        return Assets.loadTexture(Gdx.files.internal("data/materials/matcaps/zbrush-mat" + i + ".png"));
    }
}
