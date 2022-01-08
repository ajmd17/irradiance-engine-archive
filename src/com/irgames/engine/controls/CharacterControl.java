/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.physics.Physics;
import com.irgames.utils.MathUtil;

/**
 *
 * @author Andrew
 */
public class CharacterControl extends CameraControl {

    Physics physics;
    private Vector3 worldTranslation;
    private Vector3 oldTranslation;
    private float offset = 1;
    public CharacterControl(Camera cam, Physics phys) {
        super(cam);
        this.physics = phys;
    }

    @Override
    public void update() {
        if (oldTranslation == null) {
            oldTranslation = cam.position;
            worldTranslation = oldTranslation;
        }
        
        Vector3 ray = physics.getWorldIntersection(worldTranslation);
        if (ray != null) {
            
            worldTranslation.y = ray.y+offset;
            cam.position.set(worldTranslation);
            cam.update();
        }
        
        oldTranslation = cam.position;
    }

}
