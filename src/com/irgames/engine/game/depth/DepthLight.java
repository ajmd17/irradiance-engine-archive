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
package com.irgames.engine.game.depth;

import com.irgames.engine.shadows.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.ShadowMap;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;
import com.irgames.engine.game.ShadowUtil;
import com.irgames.utils.MathUtil;


public class DepthLight extends DirectionalLight implements ShadowMap, Disposable {

    protected FrameBuffer fbo;
    protected Camera cam;
    protected float halfDepth;
    protected float halfHeight;
    protected final Vector3 tmpV = new Vector3();
    protected final TextureDescriptor textureDesc;
    public float viewBound;
    public Vector3 lightDirection;


    public DepthLight(Camera camera) {
        lightDirection = new Vector3(-0.6f, -1f, 0f);
        fbo = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.cam = camera;
        textureDesc = new TextureDescriptor();
        textureDesc.minFilter = textureDesc.magFilter = Texture.TextureFilter.Nearest;
        textureDesc.uWrap = textureDesc.vWrap = Texture.TextureWrap.ClampToEdge;
    }
  

    public void transform(Vector3[] in, Vector3[] out, Matrix4 mat) {
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i].cpy().mul(mat);
        }
    }
    
    public void update(final Vector3 center, final Vector3 forward, Camera mainCam) {
        
    }
  


    public void begin(final Vector3 center, final Vector3 forward, Camera mainCam) {
        update(center, forward, mainCam);
        begin();
    }

    public void begin() {
        final int w = fbo.getWidth();
        final int h = fbo.getHeight();
        fbo.begin();
        Gdx.gl.glViewport(0, 0, w, h);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(1, 1, w - 2, h - 2);
    }

    public void end() {
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        fbo.end();
    }

    public FrameBuffer getFrameBuffer() {
        return fbo;
    }

    public Camera getCamera() {
        return cam;
    }

    @Override
    public Matrix4 getProjViewTrans() {
        return cam.combined;
    }

    @Override
    public TextureDescriptor getDepthMap() {
        textureDesc.texture = fbo.getColorBufferTexture();
        return textureDesc;
    }

    @Override
    public void dispose() {
        if (fbo != null) {
            fbo.dispose();
        }
        fbo = null;
    }
}
