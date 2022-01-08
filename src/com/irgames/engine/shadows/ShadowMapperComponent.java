/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.shadows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.GameComponent;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.components.IRSpatial;
import com.irgames.engine.shaders.IRDepthShader;
import com.irgames.engine.components.IRShader;
import com.irgames.engine.shaders.NewDepthShader;
import com.irgames.engine.shaders.NewShadowShader2;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.post.ShadowPostFilter;
import com.irgames.engine.shadows.DirectionalShadowLight;
import com.irgames.managers.DepthTextureManager;
import com.irgames.managers.LightingManager;
import com.irgames.managers.RenderManager;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.NodeUtils;
import com.irgames.utils.RenderUtils;
import com.irgames.utils.RenderUtils.BackfaceCullMode;
import com.irgames.utils.RenderUtils.Bucket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class ShadowMapperComponent extends GameComponent {

    public DirectionalShadowLight[] shadowLight = new DirectionalShadowLight[4];
    private float[] ranges;
    public Vector3 lightPosition;
    private boolean isPostFilter = false;
    private ShadowPostFilter post;
    private int renderSplits = 0, softShadows = 0;
    private boolean initShader = false;
    private Vector3 tempLightDir = new Vector3();
    private Shader depthShader;

    public void setLightDirection(Vector3 lightDir) {
        //shadowShader3.lightDirection = lightDir;
        tempLightDir.x = lightDir.x;
        tempLightDir.z = lightDir.z;
        tempLightDir.y = lightDir.y;//MathUtils.clamp(lightDir.y, -1f, -.5f);
        tempLightDir.nor();
        for (int i = 0; i < shadowLight.length; i++) {
            shadowLight[i].lightDirection = tempLightDir;
        }
    }

    public ShadowMapperComponent() {
        this.setName("Shadow Mapper");
    }

    public void setShadowIntensity(float intensity) {
        if (this.isPostFilter) {
            this.post.shadowIntensity = intensity;
        }
    }

    public void setRenderSplits(boolean bool) {
        if (bool == true) {
            renderSplits = 1;
        } else {
            renderSplits = 0;
        }
    }

    public void updateFrustumSplits(float[] splits, float near, float far, float lambda) {
        splits[0] = 20;//((far / 2) / 2) / 2;
        splits[1] = 50;//(far / 2) / 2;
        splits[2] = 90;//far / 2;
        splits[3] = far;
    }

    public void setSoftShadows(boolean bool) {
        if (bool == true) {
            softShadows = 1;
        } else {
            softShadows = 0;
        }
    }
    ShadowShaderProvider ssp;

    @Override
    public void init() {

        ssp = new ShadowShaderProvider();
        float lambda = 0.55f;
        ranges = new float[4];//{20.0f, 50.0f, 120.0f, farFrustum};
        updateFrustumSplits(ranges, cam.near, cam.far, lambda);
        for (int i = 0; i < shadowLight.length; i++) {
            float near = 0f;
            if (i > 0) {
                near = ranges[i] - 1;
            }
            shadowLight[i] = new com.irgames.engine.shadows.DirectionalShadowLight(2048, 2048, -1, 1, near, ranges[i]);
            shadowLight[i].set(0.8f, 0.8f, 0.8f, -0.6f, -1f, 0f);
        }
        IRNode irn = NodeUtils.createRoundedBox(Vector3.Zero, Vector3.Zero);
        if (depthShader == null) {
            try {
                Renderable ren = new Renderable();
                irn.ins.getRenderable(ren);
                depthShader = ssp.getShader(ren);

            } catch (Exception ex) {
                Logger.getLogger(ShadowMapperComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void renderShadowMap() {

        for (int i = 0; i < shadowLight.length; i++) {
            Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

            shadowLight[i].begin(Vector3.Zero, cam.position, cam);
            for (IRSpatial irs : RenderManager.sceneObjects) {
                if (irs.getBucket() != Bucket.sky && irs.getBucket() != Bucket.translucent) {
                    //if (irs instanceof IRNode) {
                    //  IRNode irn = (IRNode) irs;

                    if (irs.mesh != null && irs.shader != null) {

                        if (irs.getMaterial().getBoolean("drawsShadows")) {
                            ShadowShader s = (ShadowShader) depthShader;
                            s.applyMaterial(irs.getMaterial());
                            
                            s.begin(shadowLight[i].getCamera(), context);
                            // if (irs.getMaterial() != null) {
                            ///     s.applyMaterial(irs.getMaterial());
                            //  }
                            s.render(irs);
                            s.end();
                        }

                    }
                    // }
                }

            }
            shadowLight[i].end();
            DepthTextureManager.shadowMaps[i] = (Texture) shadowLight[i].getDepthMap().texture;

        }

    }

    float updateTime = Float.MAX_VALUE;
    float maxUpdateTime = 0.05f;

    @Override
    public void preUpdate() {
        renderShadowMap();
        this.setLightDirection(LightingManager.getSunDirection());
        /*if (!isPostFilter) {
         if (!initShader) {
         shadowShader3 = new ShadowedShader();
         shadowShader3.init();
         initShader = true;
         }
         shadowShader3.u_shadowMapProjViewTrans0 = shadowLight[0].getCamera().combined;
         shadowShader3.u_shadowMapProjViewTrans1 = shadowLight[1].getCamera().combined;
         shadowShader3.u_shadowMapProjViewTrans2 = shadowLight[2].getCamera().combined;
         shadowShader3.u_shadowMapProjViewTrans3 = shadowLight[3].getCamera().combined;
         //shadowShader3.softShadows = this.softShadows;
         shadowShader3.renderSplits = this.renderSplits;
         shadowShader3.ranges = this.ranges;
         } else if (isPostFilter) {*/
        this.post.u_shadowMapProjViewTrans0 = shadowLight[0].getCamera().combined;
        this.post.u_shadowMapProjViewTrans1 = shadowLight[1].getCamera().combined;
        this.post.u_shadowMapProjViewTrans2 = shadowLight[2].getCamera().combined;
        this.post.u_shadowMapProjViewTrans3 = shadowLight[3].getCamera().combined;
        this.post.renderSplits = this.renderSplits;
        this.post.softShadows = this.softShadows;
        this.post.ranges = this.ranges;

        //}
    }

    public void postFilterMode(ShadowPostFilter postFilter) {
        this.isPostFilter = postFilter != null;
        this.post = postFilter;
    }

    private void renderShadowedObject(IRSpatial object) {
        if (object.drawsShadows() && object.mesh != null) {
            //shadowShader3.render(object);
        }
        if (object instanceof IRNode) {
            IRNode irn = (IRNode) object;
            for (IRSpatial child : irn.getChildren()) {
                renderShadowedObject(child);
            }
        }

    }

    @Override
    public void render(Camera cam, RenderContext context) {

    }

    @Override
    public void update() {
        if (!isPostFilter) {
            //shadowShader3.begin(cam, context);
            for (IRSpatial child : RenderManager.sceneObjects) {
                renderShadowedObject(child);
            }
            //shadowShader3.end();
        }
    }
}
