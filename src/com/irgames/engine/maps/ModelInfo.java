/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.maps;

import com.irgames.engine.components.IRMat;
import com.irgames.engine.maps.Map.EntityProperty;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class ModelInfo {

    /**
     *
     * @author Andrew
     */
    public String path, loc, scale, rot;
    public IRMat material;
    public List<Map.EntityProperty> properties = new ArrayList<>();

    public ModelInfo(String assetPath, String loc, String scale, String rot) {
        this.path = assetPath;
        this.loc = loc;
        this.scale = scale;
        this.rot = rot;
    }

    public void setProperty(String name, Object val) {
        EntityProperty p = getProperty(name);
        if (p != null) {
            p.val = val;
        } else if (p == null) {
            properties.add(new EntityProperty(name, val));
        }
    }

    public EntityProperty getProperty(String name) {
        for (EntityProperty p : properties) {
            if (p.name.equals(name)) {
                return p;
            }
        }
        return null;
    }

}
