/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.shaders.components;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class ShaderProperties {

    public List<ShaderProperty> properties = new ArrayList<>();

    public int compare(ShaderProperties props) {
        for (ShaderProperty prop : props.properties) {
            ShaderProperty sp = getProperty(prop.name);
            if (sp != null) {
                if (sp.bool != prop.bool) {
                    return 0;
                }
            } else if (sp == null) {
                return 0;
            }
        }
        for (ShaderProperty prop : properties) {
            ShaderProperty sp = props.getProperty(prop.name);
            if (sp != null) {
                if (sp.bool != prop.bool) {
                    return 0;
                }
            } else if (sp == null) {
                return 0;
            }
        }
        return 1;
    }

    public boolean getPropertyValue(String name) {
        for (ShaderProperty ifst : properties) {
            if (ifst.name.equals(name)) {
                return ifst.bool;
            }
        }
        return false;
    }

    public ShaderProperty getProperty(String name) {
        for (ShaderProperty ifst : properties) {
            if (ifst.name.equals(name)) {
                return ifst;
            }
        }
        return null;
    }

    public ShaderProperties setProperty(String name, boolean bool) {
        for (ShaderProperty ifst : properties) {
            if (ifst.name.equals(name)) {
                ifst.bool = bool;
                return this;
            }
        }
        properties.add(new ShaderProperty(name, bool));
        return this;
    }
}
