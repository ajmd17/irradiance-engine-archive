/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.shaders;

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
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.irgames.managers.LightingManager;
import com.irgames.utils.RenderUtils;
import com.irgames.utils.RenderUtils.BackfaceCullMode;
import com.irgames.utils.RenderUtils.DepthTestMode;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 *
 * @author Xoppa
 */
public class BarkShader extends LightShader {

    Camera camera;
    RenderContext context;
    int u_projTrans;
    int u_worldTrans;
    Texture leaf, normalMap;
    public boolean flipY = true;
    public DepthTestMode depthTestMode = DepthTestMode.on;

    String texPath, texPathNorm;
    float windSpeed = 1.0f;
    float windAmount = 1.0f;
    float treeHeight = 0.0f;
    float time = 0f;
    Matrix4 normalMatrix = new Matrix4();

    public BarkShader() {

    }

    public BarkShader(String texturePath, String normalMapPath, float treeHeight) {
        texPath = texturePath;
        texPathNorm = normalMapPath;
        this.treeHeight = treeHeight;
    }

    @Override
    public void init() {
        String vert = Gdx.files.internal("data/shaders/simplelit/simplelit_wind.vertex.glsl").readString();
        String frag = Gdx.files.internal("data/shaders/simplelit/simplelit.fragment.glsl").readString();
        vert = this.format(vert);
        frag = this.format(frag);
        program = new ShaderProgram(vert, frag);
        if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
        }
        u_projTrans = program.getUniformLocation("u_projViewTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
        program.pedantic = false;
        LightingManager.addShader(this);
    }

    public void setTreeHeight(float height) {
        this.treeHeight = height;

    }

    public void setWindSpeed(float speed) {
        this.windSpeed = speed;
    }

    public void setWindAmount(float amt) {
        this.windAmount = amt;
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
        context.setDepthTest(GL20.GL_LEQUAL);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);

        program.setUniformMatrix(u_projTrans, camera.combined);
        program.setUniformMatrix("u_viewTrans", camera.view);
        program.setUniformf("u_cameraPosition", camera.position);
        normalMatrix.set(camera.view);

    }

    public void setTime(float deltaTime) {
        this.time = deltaTime;
    }

    @Override
    public void render(Renderable renderable) {
       
       // program.setUniformMatrix("u_normalMatrix", normalMatrix);
        program.setUniformf("u_alphaDiscard", 0.0f);
        program.setUniformf("u_time", time);
        program.setUniformf("u_windAmount", windAmount);
        program.setUniformf("u_windSpeed", windSpeed);
        program.setUniformf("u_treeHeight", treeHeight);
        program.setUniformf("u_lightDirection", lightDirection);
        program.setUniformf("u_fogStart", fogStart);
        program.setUniformf("u_fogEnd", fogEnd);
        program.setUniformf("u_fogColor", fogColor);
        program.setUniformf("u_lightColor", lightColor);

        if (this.getBoolean("DIFFUSE_MAP")) {
            
            Texture diffuseTex = this.irmat.getTexture("diffuse");
            if (diffuseTex != null) {
                diffuseTex.bind(0);
                program.setUniformi("u_diffuseTexture", 0);
            }
        }
        if (this.getBoolean("NORMAL_MAP")) {
            Texture normalTex = this.irmat.getTexture("normal");
            if (normalTex != null) {
                normalTex.bind(1);
                program.setUniformi("u_normalMap", 1);
            }
        }
        
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
//        program.setUniformMatrix("u_viewInv", camera.view.inv());

       super.render(renderable);
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
