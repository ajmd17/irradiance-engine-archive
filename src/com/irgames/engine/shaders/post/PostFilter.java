/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.shaders.post;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.irgames.managers.PostProcessManager;

/**
 *
 * @author Andrew
 */
public class PostFilter {
    public ShaderProgram shaderProgram;
    public boolean bindSceneTex = true;
    public PostProcessManager processor;
    public boolean enabled = true;
    public void setEnabled(boolean bool) {
        enabled = bool;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public PostFilter() {
        
    }
    public void resize() {
        
    }
    public void init() {
        
    }
    public void postRender() {
        
    }
            
    public void onRender(Camera cam) {
        
    }
    public void update(Camera cam) {
        
    }

    public void preRender(Camera cam) {
       
    }
}
