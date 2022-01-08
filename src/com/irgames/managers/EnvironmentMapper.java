/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.CubemapData;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FacedCubemapData;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.irgames.engine.assets.PngtoJpg;
import com.irgames.engine.game.Game;
import com.irgames.engine.sky.SkyBoxLoader;
import static com.irgames.managers.EnvironmentMapper.cubeMaps;
import static com.irgames.managers.EnvironmentMapper.idx;
import static com.irgames.managers.EnvironmentMapper.probes;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class EnvironmentMapper {

    public static Pixmap tex0;
    public static List<Cubemap> cubeMaps = new ArrayList<>();
    public static List<EnvironmentProbe> probes = new ArrayList<>();
    static FrameBuffer fbo;

    public static class EnvironmentProbe {

        public Cubemap cubemap;
        public FrameBuffer[] fbos = new FrameBuffer[6];
        public Camera[] cams = new Camera[6];
        public Vector3 position = new Vector3(0, 0, 0);
        public Pixmap[] textures = new Pixmap[6];
        public Texture[] tex = new Texture[6];
        public boolean rendered = false;
        public EnvironmentProbe() {
            for (int i = 0; i < fbos.length; i++) {
                fbos[i] = new FrameBuffer(Pixmap.Format.RGBA8888, 512, 512, true);
                fbos[i].getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                cams[i] = new PerspectiveCamera();
               
            }
            cams[0].rotate(Vector3.Y, -90); // right
            cams[1].rotate(Vector3.Y, 90); // left
            cams[2].rotate(Vector3.X, 90);
            cams[2].rotate(Vector3.Y, 180);
            cams[3].rotate(Vector3.X, -90);
            cams[3].rotate(Vector3.Y, 180);
            cams[4].rotate(Vector3.Y, 180);
            // cams[5].rotate(Vector3.X, -180);
            for (int i = 0; i < cams.length; i++) {
                cams[i].position.set(position);
                cams[i].viewportHeight = 512;
                cams[i].viewportWidth = 512;
               
                cams[i].update();
            }

        }

        public void renderCubemap() {

            for (int i = 0; i < fbos.length; i++) {
                fbos[i].begin();
                Gdx.gl.glViewport(0, 0, fbos[i].getWidth(), fbos[i].getHeight());
                Gdx.gl.glClearColor(LightingManager.getBackgroundColor().r,
                        LightingManager.getBackgroundColor().g,
                        LightingManager.getBackgroundColor().b,
                        1.0f);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
                RenderManager.render(cams[i], Game.context);
                fbos[i].end();
                fbos[i].bind();

                textures[i] = ScreenUtils.getFrameBufferPixmap(0, 0, fbos[i].getWidth(), fbos[i].getHeight());//
                PixmapIO.writePNG(Gdx.files.absolute("C:\\Users\\Andrew\\screenshot_" + i + ".png"), textures[i]);
                tex[i] = new Texture(textures[i]);
                PngtoJpg.convert("C:\\Users\\Andrew\\screenshot_" + i + ".png", "C:\\Users\\Andrew\\screenshot_" + i + ".jpg");
            }

            //cubemap = new Cubemap(tex[0].getTextureData(), tex[1].getTextureData(), tex[2].getTextureData(), tex[3].getTextureData(), 
            //         tex[4].getTextureData(), tex[5].getTextureData());
            //  cubemap = new Cubemap(SkyBoxLoader.getPath("siege", "right.jpg"), SkyBoxLoader.getPath("siege", "left.jpg"), SkyBoxLoader.getPath("siege", "top.jpg"),
            //        SkyBoxLoader.getPath("siege", "top.jpg"), SkyBoxLoader.getPath("siege", "front.jpg"), SkyBoxLoader.getPath("siege", "back.jpg"));
            cubemap = new Cubemap(Gdx.files.absolute("C:\\Users\\Andrew\\screenshot_" + 0 + ".jpg"), Gdx.files.absolute("C:\\Users\\Andrew\\screenshot_" + 1 + ".jpg"),
                    Gdx.files.absolute("C:\\Users\\Andrew\\screenshot_" + 2 + ".jpg"),
                    Gdx.files.absolute("C:\\Users\\Andrew\\screenshot_" + 3 + ".jpg"), Gdx.files.absolute("C:\\Users\\Andrew\\screenshot_" + 4 + ".jpg"),
                    Gdx.files.absolute("C:\\Users\\Andrew\\screenshot_" + 5 + ".jpg"));

        }
    }

    public static void addEnvProbe(EnvironmentProbe probe) {

        if (!cubeMaps.contains(probe.cubemap)) {

            
            probes.add(probe);
        }

    }
    static float time = 0, max = 2f;
    static int idx = 0;

    public static void update() {
        if (probes.size() > 0) {
            if (time > max) {

                if (probes.get(idx).rendered == false) {
                    probes.get(idx).renderCubemap();
                    cubeMaps.add(probes.get(idx).cubemap);
                    probes.get(idx).rendered = true;
                }
                if (idx == probes.size() - 1) {
                    idx = 0;
                } else {
                    idx++;
                }

                time = 0;
            } else {
                time += Gdx.graphics.getDeltaTime();
            }
        }
    }

    public static void setEnvironmentMap(Pixmap[] data) {

        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_CUBE_MAP, 0);
        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL20.GL_RGB, data[0].getWidth(), data[0].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[0].getPixels());
        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL20.GL_RGB, data[1].getWidth(), data[1].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[1].getPixels());

        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL20.GL_RGB, data[2].getWidth(), data[2].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[2].getPixels());
        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL20.GL_RGB, data[3].getWidth(), data[3].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[3].getPixels());

        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL20.GL_RGB, data[4].getWidth(), data[4].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[4].getPixels());
        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL20.GL_RGB, data[5].getWidth(), data[5].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[5].getPixels());

        Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR_MIPMAP_LINEAR);
        Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR_MIPMAP_LINEAR);
        Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
        Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
        Gdx.gl.glGenerateMipmap(GL20.GL_TEXTURE_CUBE_MAP);
    }
}
