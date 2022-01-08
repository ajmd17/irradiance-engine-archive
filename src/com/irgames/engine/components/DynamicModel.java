/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.components;

import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.game.MyModelInstance;

/**
 *
 * @author Andrew
 */
public class DynamicModel {

    MyModelInstance mainInstance;
    MyModelInstance modelInstance;
    MyModelInstance imposterInstance;

    public Vector3 getLocation() {
        Vector3 loc = new Vector3();
        mainInstance.transform.getTranslation(loc);
        return loc;
    }
    private void switchModel(MyModelInstance newM) {
        
        Vector3 mloc = getLocation().cpy();
        mainInstance = newM;
        mainInstance.transform.setToTranslation(mloc);
        
        
    }


}
