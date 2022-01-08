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
package com.irgames.engine.shadows;

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
import com.irgames.engine.components.IRSpatial;
import com.irgames.engine.game.MyBoundingBox;
import com.irgames.engine.game.ShadowUtil;
import com.irgames.managers.RenderManager;
import com.irgames.utils.MathUtil;

/**
 * @deprecated Experimental, likely to change, do not use!
 * @author Xoppa
 */
public class DirectionalShadowLight extends DirectionalLight implements ShadowMap, Disposable {

    protected FrameBuffer fbo;
    protected Camera cam;
    protected float halfDepth;
    protected float halfHeight;
    protected final Vector3 tmpV = new Vector3();
    protected final TextureDescriptor textureDesc;
    public float viewBound;
    public Vector3 lightDirection;

    /**
     * @deprecated Experimental, likely to change, do not use!
     */
    public DirectionalShadowLight(int shadowMapWidth, int shadowMapHeight, float shadowViewportWidth, float shadowViewportHeight,
            float shadowNear, float shadowFar) {
        lightDirection = new Vector3(-0.6f, -1f, 0f);
        fbo = new FrameBuffer(Format.RGBA8888, shadowMapWidth, shadowMapHeight, true);
        cam = new OrthographicCamera(shadowViewportWidth, shadowViewportHeight);
        viewBound = 20f;
        cam.near = shadowNear;
        cam.far = shadowFar;
        halfHeight = shadowViewportHeight * 0.5f;
        halfDepth = shadowNear + 0.5f * (shadowFar - shadowNear);
        textureDesc = new TextureDescriptor();
        textureDesc.minFilter = textureDesc.magFilter = Texture.TextureFilter.MipMap;
        textureDesc.uWrap = textureDesc.vWrap = Texture.TextureWrap.ClampToEdge;
    }
    protected Vector3[] points = new Vector3[8];

    public void update(final Camera camera) {
        update(tmpV.set(camera.direction).scl(halfHeight), camera.direction, camera);
    }
    Vector3 camPos = new Vector3();
    public BoundingBox b = new BoundingBox();
    Vector3 center = new Vector3();
    public Vector3 fcenter = new Vector3();
    float gTime;
    Vector3 centerPos = new Vector3();

    public void transform(Vector3[] in, Vector3[] out, Matrix4 mat) {
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i].cpy().mul(mat);
        }
    }
    private float oldFar, oldNear;
    Vector3[] fPoints = new Vector3[]{new Vector3(),
        new Vector3(),
        new Vector3(),
        new Vector3(),
        new Vector3(),
        new Vector3(),
        new Vector3(),
        new Vector3()};
    MyBoundingBox bb = new MyBoundingBox();

    private boolean collides(Vector3 a, Vector3 b, float range) {
        if ((a.x - b.x) < (range / 2) && (a.x - b.x) > -(range / 2)) {
            if ((a.z - b.z) < (range / 2) && (a.z - b.z) > -(range / 2)) {
                return true;
            }
        }
        return false;
    }

    static boolean Vector3Equals(Vector3 a, Vector3 b) {
        return (Math.round(a.x) == Math.round(b.x) && Math.round(a.y) == Math.round(b.y) && Math.round(a.z) == Math.round(b.z));
    }

    private void updateFrustumPoints2(Camera viewCam, Vector3[] pts) {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        oldFar = viewCam.far;
        // oldNear = viewCam.near;
        viewCam.far = cam.far;
        // viewCam.near = cam.near;
        viewCam.update();
        // for (int i = 0; i < pts.length; i++) {
        // pts[i] = viewCam.frustum.planePoints[i];
        //  }
        bb.clr();
        /* for (IRSpatial spat : RenderManager.sceneObjects) { //calculate minimal aa bounding box for all objects
         if (spat.mesh != null) {
         //if (collides(spat.getWorldTranslation(), viewCam.position, cam.far)) {//
         if (spat.getWorldTranslation().dst(viewCam.position) < cam.far) {
         //if (spat.getBoundingBox() != null) {
         bb.ext(spat.getWorldTranslation());
         
         //}
         }
         }
         }*/
        bb.ext(viewCam.position.cpy().add(new Vector3(cam.far + 10, cam.far + 10, cam.far + 10)));
        bb.ext(viewCam.position.cpy().add(new Vector3(-cam.far - 10, -cam.far - 10, -cam.far - 10)));

        //System.out.println(bb.getCenterY());
        for (int i = 0; i < bb.getCorners().length; i++) {
            pts[i].set(bb.getCorners()[i]);
        }
        // for (int i = 0; i < pts.length; i++) {
        //  pts[i] = viewCam.frustum.planePoints[i];
        //  }
        viewCam.far = oldFar;
        //viewCam.near = oldNear;
    }
    Vector3[] frustumCornersLS = new Vector3[8];
    Vector3[] frustumCornersWS = new Vector3[8];
    Matrix4 newView = new Matrix4();
    Vector3 maxes = new Vector3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
    Vector3 mins = new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

    public void update(final Vector3 center, final Vector3 forward, Camera mainCam) {
        oldFar = mainCam.far;
        oldNear = mainCam.near;
        updateFrustumPoints2(mainCam, fPoints);
        centerPos.set(0, 0, 0);
        frustumCornersWS = fPoints;
        for (int i = 0; i < 8; i++) {
            centerPos.add(frustumCornersWS[i]);
        }
        centerPos.scl(1 / 8);
        newView.setToLookAt(centerPos.cpy().sub(lightDirection), centerPos, Vector3.Y);

        transform(frustumCornersWS, frustumCornersLS, newView);
        maxes.set(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        mins.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        for (int i = 0; i < frustumCornersLS.length; i++) {
            if (frustumCornersLS[i].x > maxes.x) {
                maxes.x = frustumCornersLS[i].x;
            } else if (frustumCornersLS[i].x < mins.x) {
                mins.x = frustumCornersLS[i].x;
            }
            if (frustumCornersLS[i].y > maxes.y) {
                maxes.y = frustumCornersLS[i].y;
            } else if (frustumCornersLS[i].y < mins.y) {
                mins.y = frustumCornersLS[i].y;
            }
            if (frustumCornersLS[i].z > maxes.z) {
                maxes.z = frustumCornersLS[i].z;
            } else if (frustumCornersLS[i].z < mins.z) {
                mins.z = frustumCornersLS[i].z;
            }
        }

        cam.view.set(newView);
        cam.projection.setToOrtho(mins.x, maxes.x, mins.y, maxes.y, -maxes.z, -mins.z);
        cam.combined.set(cam.projection).mul(cam.view);
    }
    Vector3 min = new Vector3(), max = new Vector3();

    public void begin(final Camera camera) {
        update(camera);
        begin();
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
        Gdx.gl.glClearColor(1, 1, 1, 1);
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
