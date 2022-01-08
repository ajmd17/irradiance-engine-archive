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
import com.irgames.managers.DepthTextureManager;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 *
 * @author Xoppa
 */
public class IRShadowShader implements Shader {

    ShaderProgram program;
    Camera camera;
    RenderContext context;
    int u_projTrans;
    int u_worldTrans;
    Texture grass, rock;
    public Vector3 lightPos;

    @Override
    public void init() {
        String vert = Gdx.files.internal("data/shaders/depth/irshadow.vertex.glsl").readString();
        String frag = Gdx.files.internal("data/shaders/depth/irshadow.fragment.glsl").readString();
        program = new ShaderProgram(vert, frag);
        if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
        }
        u_projTrans = program.getUniformLocation("u_projViewTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
        program.pedantic = false;

        /* float[] ar = {-10, 10, -10, 10, -10, 20};
         Vector3 lightInvDir = new Vector3(-0.5f, -1.0f, 0);
         Matrix4 depthProjectionMatrix = new Matrix4();
         depthProjectionMatrix.setToOrtho2D(-10, 10, -10, 10, -10, 20);
         Matrix4 depthViewMatrix = new Matrix4();
         depthViewMatrix.setToLookAt(lightInvDir, new Vector3(0,0,0), new Vector3(0,1,0));
         Matrix4 modelMatrix = new Matrix4();
         modelMatrix.setToTranslation(new Vector3(1.0f,1.0f,1.0f));
         this.lightMatrix = depthProjectionMatrix.mul(depthViewMatrix);
         this.lightPos = new Vector3(0, -1, 0);*/
    }

    @Override
    public void dispose() {
        program.dispose();
    }
    public Matrix4 lightMatrix;

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;

        program.begin();

        context.setDepthTest(GL20.GL_LEQUAL);
        //  context.setCullFace(GL20.GL_FRONT);
        Gdx.gl.glDepthMask(true);
        context.setCullFace(GL20.GL_BACK);
    }

    @Override
    public void render(Renderable renderable) {
        program.setUniformMatrix("u_worldTrans", renderable.worldTransform);

        //Gdx.gl.glCullFace(GL20.GL_FRONT);
        DepthTextureManager.shadowMap.bind(1);
        program.setUniformi("u_depthMap", 1);
        //program.setUniformi("u_shadowMap", 0);
        // program.setUniformf("u_lightPosition", lightPos);
        program.setUniformMatrix("u_projViewTrans", camera.combined);
        program.setUniformMatrix("u_proj", camera.projection);
        program.setUniformMatrix("u_lightMatrix", lightMatrix);
        program.setUniformMatrix("u_vmInverse", camera.view.cpy().inv());

        renderable.mesh.render(program,
                renderable.primitiveType,
                renderable.meshPartOffset,
                renderable.meshPartSize);
        //Gdx.gl.glCullFace(GL20.GL_BACK);
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
