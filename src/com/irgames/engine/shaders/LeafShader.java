/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.shaders;

import com.irgames.engine.shaders.LightShader;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.irgames.engine.shaders.NewDepthShader_Leaves;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.managers.LightingManager;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.RenderUtils;
import com.irgames.utils.RenderUtils.BackfaceCullMode;
import com.irgames.utils.RenderUtils.DepthTestMode;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 *
 * @author Xoppa
 */
public class LeafShader extends LightShader {
    
    Camera camera;
    RenderContext context;
    int u_projTrans;
    int u_worldTrans;
    Texture leaf;
    public boolean flipY = true;
    public DepthTestMode depthTestMode = DepthTestMode.on;
    
    String texPath;
    
    float windSpeed = 1.0f;
    float windAmount = 1.0f;
    float treeHeight = 0.0f;
    float time = 0f;
    
    public LeafShader() {
        
    }
    
    public LeafShader(String str, float flt) {
        
    }
    
    @Override
    public void init() {
        try {
            depthShader = ShaderManager.getShader(NewDepthShader_Leaves.class, new ShaderProperties());
        } catch (Exception ex) {
            Logger.getLogger(LeafShader.class.getName()).log(Level.SEVERE, null, ex);
        }
        String vert = Gdx.files.internal("data/shaders/simplelit/simplelit_wind.vertex.glsl").readString();
        String frag = Gdx.files.internal("data/shaders/simplelit/simplelit_leaves.fragment.glsl").readString();
        program = new ShaderProgram(vert, frag);
        if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
        }
        u_projTrans = program.getUniformLocation("u_projViewTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
        program.pedantic = false;
        
        this.cullMode = BackfaceCullMode.off;
        LightingManager.addShader(this);
    }
    
    @Override
    public void dispose() {
        program.dispose();
    }
    
    public void setWindSpeed(float speed) {
        this.windSpeed = speed;
    }
    
    public void setTreeHeight(float height) {
        this.treeHeight = height;
    }
    
    public void setWindAmount(float amt) {
        this.windAmount = amt;
    }
    
    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        
        program.begin();
        context.setDepthTest(GL20.GL_LEQUAL);
        
        program.setUniformMatrix(u_projTrans, camera.combined);
        //if (flipY) {
        //program.setUniformi("u_flipY", 1);
        //}
        //program.setUniformf("u_alphaDiscard", 0.5f);
        //leaf.bind(0);

        program.setUniformf("u_lightDirection", lightDirection);
    }
    
    public void setTime(float deltaTime) {
        this.time = deltaTime;
    }
    
    @Override
    public void render(Renderable renderable) {
        
       // super.render(renderable);
        super.preRender();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glDepthMask(true);
        Gdx.gl.glDepthFunc(GL20.GL_NICEST);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // render twice
        if (this.getBoolean("DIFFUSE_MAP")) {
            Texture diffuseTex = this.irmat.getTexture("diffuse");
            if (diffuseTex != null) {
                diffuseTex.bind(0);
                program.setUniformi("u_leafTexture", 0);
            }
        }
        
        depthShader.program.setUniformf("u_time", time);
        depthShader.program.setUniformf("u_windAmount", windAmount);
        depthShader.program.setUniformf("u_windSpeed", windSpeed);
        depthShader.program.setUniformf("u_treeHeight", treeHeight);
        
        program.setUniformf("u_alphaDiscard", 0.4f);
        program.setUniformf("u_time", time);
        program.setUniformf("u_windAmount", windAmount);
        program.setUniformf("u_windSpeed", windSpeed);
        program.setUniformf("u_treeHeight", treeHeight);
        program.setUniformf("u_fogStart", fogStart);
        program.setUniformf("u_fogEnd", fogEnd);
        program.setUniformf("u_fogColor", fogColor);
        program.setUniformf("u_lightColor", lightColor);
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        
        super.render(renderable);
        
        program.setUniformf("u_alphaDiscard", 0.0f);
        Gdx.gl.glDepthMask(false);
        super.render(renderable);

        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glDepthMask(true);
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
