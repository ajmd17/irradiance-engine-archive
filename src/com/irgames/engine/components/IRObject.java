/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.components;

import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public abstract interface IRObject {

    public List<IRProperty> getProperties();

    public void setProperty(String name, Object value, Boolean bool);

    public void setProperty(String name, Object value);

    public Object getProperty(String name);

    public Vector3 getVector3(String name);

    public Boolean getBoolean(String name);

    public Float getFloat(String name);

    public String getString(String name);
    
    public void setUpdateNeeded();
}
