/**
 * *****************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ****************************************************************************
 */
package com.irgames.engine.sky;

import com.irgames.engine.game.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.irgames.engine.components.IRShader;
import com.irgames.managers.EnvironmentMapper;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 *
 * @author Xoppa
 */
public class SkyBoxShader extends IRShader {
    public Cubemap cubemap;
    ShaderProgram program;
    Camera camera;
    RenderContext context;
    int u_projTrans;
    int u_worldTrans;
    Texture grass, rock;
    public Pixmap[] data = new Pixmap[6];

    @Override
    public void init() {
        String vert = " attribute vec3 a_position; \n"
                + " attribute vec3 a_normal; \n"
                + " attribute vec2 a_texCoord0; \n"
                + " uniform mat4 u_worldTrans; \n"
                + " varying vec2 v_texCoord0; \n"
                + " varying vec3 v_cubeMapUV; \n"
                + " uniform mat4 u_projViewTrans;\n"
                + " uniform vec3 u_fresnel;\n"
                + " uniform vec3 u_cameraPosition;\n"
               
                + ""
                + ""
                + " void main() { \n"
                + "     v_texCoord0 = a_texCoord0;     \n"
                + "     vec4 g_position = u_worldTrans * vec4(a_position, 1.0); \n"
                + "     v_cubeMapUV = normalize(a_position); \n"
                
                
                + "     gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0); \n"
                + " } \n";

        String frag = "#ifdef GL_ES \n"
                + " precision mediump float; \n"
                + " #endif \n"
                + " uniform samplerCube u_environmentCubemap; \n"
                + " varying vec2 v_texCoord0; \n"
                + " varying vec3 v_cubeMapUV; \n"
                + " varying vec4 refVec;\n"
                + " void main() {      \n"
                + "   gl_FragColor = vec4(textureCube(u_environmentCubemap, v_cubeMapUV).rgb, 1.0);   \n"
                + " } \n";
        program = new ShaderProgram(vert, frag);
        if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
        }
        u_projTrans = program.getUniformLocation("u_projViewTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
        program.pedantic = false;

    }

    public SkyBoxShader(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ) {
        this.setTextures(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ);
    }

    public void setTextures(FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ, FileHandle negativeZ) {
        cubemap = new Cubemap(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, true);
        cubemap.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
    }
    int skyBoxLoc = GL20.GL_TEXTURE_CUBE_MAP;
    public void setTextures(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ) {
        /*data[0] = (positiveX);
        data[1] = (negativeX);
        data[2] = (positiveY);
        data[3] = (negativeY);
        data[4] = (positiveZ);
        data[5] = (negativeZ);
        Gdx.gl.glBindTexture(skyBoxLoc, 0);
        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL20.GL_RGB, data[0].getWidth(), data[0].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[0].getPixels());
        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL20.GL_RGB, data[1].getWidth(), data[1].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[1].getPixels());

        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL20.GL_RGB, data[2].getWidth(), data[2].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[2].getPixels());
        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL20.GL_RGB, data[3].getWidth(), data[3].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[3].getPixels());

        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL20.GL_RGB, data[4].getWidth(), data[4].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[4].getPixels());
        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL20.GL_RGB, data[5].getWidth(), data[5].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[5].getPixels());

        Gdx.gl.glTexParameteri(skyBoxLoc, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR_MIPMAP_LINEAR);
        Gdx.gl.glTexParameteri(skyBoxLoc, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR_MIPMAP_LINEAR);
        Gdx.gl.glTexParameteri(skyBoxLoc, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
        Gdx.gl.glTexParameteri(skyBoxLoc, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
        Gdx.gl.glGenerateMipmap(skyBoxLoc);*/
        
        
        cubemap = new Cubemap(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, true);
        cubemap.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    @Override
    public void dispose() {
        program.dispose();
    }

    public SkyBoxShader(FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ, FileHandle negativeZ) {
        this(new Pixmap(positiveX), new Pixmap(negativeX), new Pixmap(positiveY), new Pixmap(negativeY), new Pixmap(positiveZ), new Pixmap(negativeZ));
    }
    Vector3 fresnel = new Vector3();
    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        program.begin();
        program.setUniformMatrix(u_projTrans, camera.combined);

        program.setUniformMatrix("u_projViewTrans", camera.combined);
        program.setUniformf("u_cameraPosition", camera.position);
        fresnel.set(0.5f, 0.8f, 0.11f);
        program.setUniformf("u_fresnel", fresnel);
    }
    Matrix4 wTrans = new Matrix4();
    @Override
    public void render(Renderable renderable) {
        wTrans = renderable.worldTransform;
        wTrans.setTranslation(camera.position);
        program.setUniformMatrix(u_worldTrans, wTrans);
        Gdx.gl.glDepthMask(false);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);
        //bind cubemap
        cubemap.bind(0);
        program.setUniformi("u_environmentCubemap", 0);
        renderable.mesh.render(program,
                renderable.primitiveType,
                renderable.meshPartOffset,
                renderable.meshPartSize);
        Gdx.gl.glDepthMask(true);
    }

    @Override
    public void end() {
        program.end();
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return true;
    }
}
