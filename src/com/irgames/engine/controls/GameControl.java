/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.controls;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.components.IRSpatial;

/**
 *
 * @author Andrew
 */
public class GameControl {

    public boolean added = false;
    private boolean enabled = false;
    public IRSpatial parent = null;
    public void update() {

    }
    public void resetTransform(Vector3 wTrans) {
        
    }
    public final void disable() {
        if (enabled) {
            doDisable();
            enabled = false;
        }
    }

    public final void enable() {
        if (!enabled) {
            doEnable();
            enabled = true;
        }
    }

    public void doEnable() {

    }

    public void doDisable() {

    }
}
