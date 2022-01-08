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
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.irgames.engine.shaders.post.PostFilter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class PostProcessManager {

    List<PostFilter> postFilters = new ArrayList<PostFilter>();
    Mesh fsq;
    public static FrameBuffer fbo;
    static SpriteBatch fboBatch;
    public static Texture sceneTex;

    public PostProcessManager() {
        initializeFBO(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        fsq = this.createFullScreenQuad();
        //this.add(new TestPostFilter());

    }

    public void add(PostFilter pf) {
        if (!postFilters.contains(pf)) {

            postFilters.add(pf);
            pf.processor = this;
            pf.init();
        }
    }

    public void remove(PostFilter pf) {
        if (postFilters.contains(pf)) {
            postFilters.remove(pf);
        }
    }

    public void begin() {
        if (this.postFilters.size() > 0) {
            Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
            fbo.begin();
        }
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Color c = LightingManager.getBackgroundColor();
        Gdx.gl.glClearColor(c.r, c.g, c.b, c.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    public void resize() {
        for (PostFilter post : postFilters) {
            if (post != null) {
                post.resize();
            }
        }
    }

    public void end(Camera cam) {
        if (this.postFilters.size() > 0) {

            fbo.end();

            for (PostFilter post : postFilters) {
                Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                Color c = LightingManager.getBackgroundColor();
                Gdx.gl.glClearColor(c.r, c.g, c.b, c.a);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
                if (post.isEnabled()) {
                    post.preRender(cam);

               // fbo.begin();
                    //fbo.end();
                    post.update(cam);
                    int shadowMapWidth = 512;
                    int shadowMapHeight = 512;

                    post.shaderProgram.begin();

                    post.onRender(cam);
                    sceneTex = fbo.getColorBufferTexture();
                    sceneTex.bind(15);

                    sceneTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

                    post.shaderProgram.setUniformi("u_sceneTex", 15);

                    fsq.render(post.shaderProgram, GL20.GL_TRIANGLE_FAN);
                    post.shaderProgram.end();
                    if (post.bindSceneTex) {

                        Gdx.gl.glCopyTexSubImage2D(GL20.GL_TEXTURE_2D, 0, 0, 0, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

                        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
                    }
                    post.postRender();
                }
            }

            Gdx.gl20.glDisable(GL20.GL_TEXTURE_2D);
        }
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

    public static void rebuild(int w, int h) {
        initializeFBO(w, h);
    }

    private static void initializeFBO(int w, int h) {
        if (fbo != null) {
            fbo.dispose();
        }
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, true);
        fbo.getColorBufferTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

        if (fboBatch != null) {
            fboBatch.dispose();
        }
        fboBatch = new SpriteBatch();
    }
}
