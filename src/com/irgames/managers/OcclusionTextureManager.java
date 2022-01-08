/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import static com.irgames.managers.DepthTextureManager.fbo;

/**
 *
 * @author Andrew
 */
public class OcclusionTextureManager {

    static FrameBuffer fbo;
    public static Texture occTex;

    public static void capture() {
        if (fbo == null) {
            fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        }

        Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);

        fbo.begin();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void release() {

        fbo.end();
        occTex = fbo.getColorBufferTexture();
        Gdx.gl20.glDisable(GL20.GL_TEXTURE_2D);

    }
}
