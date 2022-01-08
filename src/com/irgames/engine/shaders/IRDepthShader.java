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

import com.irgames.engine.components.IRShader;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
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
public class IRDepthShader extends IRShader {

   
    Camera camera;
    RenderContext context;
    int u_projTrans;
    int u_worldTrans;
    Texture grass, rock;

    @Override
    public void init() {
        String vert = Gdx.files.internal("data/shaders/depth/irdepth.vertex.glsl").readString();
        String frag = Gdx.files.internal("data/shaders/depth/irdepth.fragment.glsl").readString();
        frag = format(frag);
        vert = format(vert);
        program = new ShaderProgram(vert, frag);
        System.out.println(frag);
        if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
        }
        u_projTrans = program.getUniformLocation("u_projViewTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
        program.pedantic = false;

    }
    public Matrix4 lightMatrix;

    @Override
    public void dispose() {
        program.dispose();
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        program.begin();
        //program.setUniformMatrix(u_projTrans, camera.combined);
        
        program.setUniformMatrix("u_projViewTrans", camera.combined);
        //program.setUniformMatrix("u_lightMatrix", camera.combined);
        context.setDepthTest(GL20.GL_LEQUAL);

    }

    @Override
    public void render(Renderable renderable) {
        super.render(renderable);
        program.setUniformMatrix("u_worldTrans", renderable.worldTransform);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);

        program.setUniformf("u_alphaDiscard", this.alphaDiscard);
        /*if (textures.size() > 0) {
         if (textures.get(0) != null) {
         textures.get(0).bind(0);
         program.setUniformi("u_diffuseTexture", 0);
         }
         }*/

        // if (this.properties.getPropertyValue("DIFFUSE_MAP")) {
        Texture diffuseTex = this.irmat.getTexture("diffuse");
        if (diffuseTex != null) {
            diffuseTex.bind(0);
            program.setUniformi("u_diffuseTexture", 0);
        }

        Boolean FlipY = this.irmat.getBoolean("flip_y");
        if (FlipY != null) {
            if (FlipY == false) {
                program.setUniformi("u_flipY", 0);
            } else {
                program.setUniformi("u_flipY", 1);
            }
        }
        Boolean Discard = this.irmat.getBoolean("discard");
        if (Discard != null) {
            if (Discard == false) {
                program.setUniformi("u_discard", 0);
            } else if (Discard == true) {
                program.setUniformi("u_discard", 1);
            }
        } else {
            program.setUniformi("u_discard", 0);
        }
        //  }
        renderable.mesh.render(program,
                renderable.primitiveType,
                renderable.meshPartOffset,
                renderable.meshPartSize);
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
