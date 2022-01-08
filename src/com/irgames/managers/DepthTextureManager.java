/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.irgames.engine.shaders.post.PostFilter;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class DepthTextureManager {

    List<PostFilter> postFilters = new ArrayList<PostFilter>();
    Mesh fsq;
    static FrameBuffer fbo, fboShadow;
    static SpriteBatch fboBatch;
    Texture tex;
    static int quality = 4;
    static boolean setUpFBO = false;
    static boolean setUpFBOs = false;

    public DepthTextureManager() {

        // fsq = this.createFullScreenQuad();
        //this.add(new TestPostFilter());
        //  this.add(new TestPostFilter2());
    }

    public static void captureShadow() {

        if (!setUpFBOs) {
            initializeFBOShadow();
            setUpFBOs = true;
        }
        Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
        fboShadow.begin();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    public static void capture() {
        if (!setUpFBO) {
            initializeFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            setUpFBO = true;
        }

        Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
       
        fbo.begin();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    public static Texture depthTex, shadowMap;
    public static Texture[] shadowMaps = new Texture[4];
    public static void release() {

        fbo.end();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Color c = LightingManager.getBackgroundColor();
        Gdx.gl.glClearColor(c.r, c.g, c.b, c.a);
        depthTex = fbo.getColorBufferTexture();
       
        //depthTex.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

        Gdx.gl20.glDisable(GL20.GL_TEXTURE_2D);

    }

    public static void releaseShadow() {

        fboShadow.end();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Color c = LightingManager.getBackgroundColor();
        Gdx.gl.glClearColor(c.r, c.g, c.b, c.a);
        shadowMap = fboShadow.getColorBufferTexture();

        shadowMap.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        Gdx.gl20.glDisable(GL20.GL_TEXTURE_2D);

    }

    public void dispose() {
        fbo.dispose();
        fboBatch.dispose();
    }

    private Mesh createFullScreenQuad() {
        float[] verts = new float[20];
        int i = 0;

        verts[i++] = -1; // x1
        verts[i++] = -1; // y1
        verts[i++] = 0;
        verts[i++] = 0f; // u1
        verts[i++] = 0f; // v1

        verts[i++] = 1f; // x2
        verts[i++] = -1; // y2
        verts[i++] = 0;
        verts[i++] = 1f; // u2
        verts[i++] = 0f; // v2

        verts[i++] = 1f; // x3
        verts[i++] = 1f; // y2
        verts[i++] = 0;
        verts[i++] = 1f; // u3
        verts[i++] = 1f; // v3

        verts[i++] = -1; // x4
        verts[i++] = 1f; // y4
        verts[i++] = 0;
        verts[i++] = 0f; // u4
        verts[i++] = 1f; // v4

        Mesh mesh = new Mesh(true, 4, 0, // static mesh with 4 vertices and no indices
                new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_Position"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoords"));

        mesh.setVertices(verts);
        return mesh;

    }

    public void render() {

    }

    private static void initializeFBOShadow() {
        if (fboShadow != null) {
            fboShadow.dispose();
        }
        fboShadow = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        fboShadow.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

    }
    public static void rebuild(int w, int h) {
        initializeFBO(w, h);
    }
    private static void initializeFBO(int w, int h ) {
        if (fbo != null) {
            fbo.dispose();
        }
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, true);
        
       // fbo.getColorBufferTexture().
        fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        if (fboBatch != null) {
            fboBatch.dispose();
        }
        fboBatch = new SpriteBatch();
    }
}
