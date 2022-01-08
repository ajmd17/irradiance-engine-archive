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
import com.irgames.managers.LightingManager;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 *
 * @author Xoppa
 */
public class TerrainShader extends LightShader {

    Camera camera;
    RenderContext context;
    int u_projTrans;
    int u_worldTrans;
    Texture grass, rock, alphaMap;

    @Override
    public void init() {
        String vert = Gdx.files.internal("data/terrain.vertex.glsl").readString();
        String frag = Gdx.files.internal("data/terrain.fragment.glsl").readString();
        vert = format(vert);
        frag = format(frag);
        program = new ShaderProgram(vert, frag);
        if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
        }
        u_projTrans = program.getUniformLocation("u_projViewTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
        program.pedantic = false;
        grass = new Texture(Gdx.files.internal("data/textures/grass.jpg"), true);
        grass.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        grass.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        rock = new Texture(Gdx.files.internal("data/textures/rock.jpg"), true);
        rock.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        rock.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        alphaMap = new Texture(Gdx.files.internal("data/textures/alphaMap.png"));
        alphaMap.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        LightingManager.addShader(this);
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

        program.setUniformf("m_region1", new Vector3(0, 100, 16));
        program.setUniformf("m_slopeTileFactor", 8f);
        program.setUniformf("u_lightDirection", lightDirection);
        program.setUniformf("u_fogStart", fogStart);
        program.setUniformf("u_fogEnd", fogEnd);
        program.setUniformf("u_fogColor", fogColor);
        program.setUniformf("u_lightColor", lightColor);

    }

    @Override
    public void render(Renderable renderable) {
        //Gdx.gl.glEnable(GL20.GL_BLEND);
        //Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        /*if (this.properties.getPropertyValue("NORMAL_MAP")) {
         Texture normalTex = this.irmat.getTexture("normal");
         if (normalTex != null) {
         normalTex.bind(3);
         program.setUniformi("u_normalMap", 3);
         }
         }*/
        alphaMap.bind(5);
        program.setUniformf("u_alphaMap1", 5);

        grass.bind(0);
        program.setUniformi("m_region1ColorMap", 0);

        rock.bind(1);
        program.setUniformi("m_region2ColorMap", 0);
        program.setUniformi("m_slopeColorMap", 1);

        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);

        super.render(renderable);
        
        //Gdx.gl.glDisable(GL20.GL_CULL_FACE);
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
