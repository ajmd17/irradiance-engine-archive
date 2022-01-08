/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.components;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Vector3;

/**
 *
 * @author Andrew
 */
public class GameComponent {
    public RenderContext context;
    public String name = "";
    public Camera cam;
    public IRNode rootNode;
    private boolean isEnabled = true;
    public boolean added;

    public void enable() {
        isEnabled = true;
    }

    public void disable() {
        isEnabled = false;
    }

    public void setEnabled(boolean enable) {
        isEnabled = enable;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void init() {};

    public void update(){};

    public void preUpdate() {

    }
    
    public void render(Camera cam, RenderContext context) {

    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
}
