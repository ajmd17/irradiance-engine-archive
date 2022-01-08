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
 *****************************************************************************
 */
package com.irgames.engine.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 *
 * @author Xoppa
 */
public class CloudShader implements Shader {

    ShaderProgram program;
    Camera camera;
    RenderContext context;
    int u_projTrans;
    int u_worldTrans;
    Texture cloudMap;
    public float globalTime;
    public Vector3 cloudColor = Vector3.Zero;
    public CloudShader(Texture clouds) {
        cloudMap = clouds;
    }
    @Override
    public void init() {
        String vert = Gdx.files.internal("data/shaders/atmosphere/clouds.vertex.glsl").readString();
        String frag = Gdx.files.internal("data/shaders/atmosphere/clouds.fragment.glsl").readString();
        program = new ShaderProgram(vert, frag);
        if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
        }
        u_projTrans = program.getUniformLocation("u_projViewTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
        program.pedantic = false;
        
        
    }

    @Override
    public void dispose() {
        program.dispose();
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        program.begin();
        program.setUniformMatrix(u_projTrans, camera.combined);
       
        context.setDepthTest(GL20.GL_LEQUAL);
       //context.setCullFace(GL20.GL_BACK);
    }

    @Override
    public void render(Renderable renderable) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glDepthFunc(GL20.GL_NICEST);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        cloudMap.bind(0);
        program.setUniformi("u_cloudMap", 0);
        program.setUniformf("u_globalTime", globalTime*3f);
        program.setUniformf("u_cloudColor", cloudColor);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(false);
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        renderable.mesh.render(program,
                renderable.primitiveType,
                renderable.meshPartOffset,
                renderable.meshPartSize);
        Gdx.gl.glDepthMask(true);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glDisable(GL20.GL_BLEND);
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
