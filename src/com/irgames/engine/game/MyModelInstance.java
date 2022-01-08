/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.game;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Shader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class MyModelInstance extends ModelInstance {
    public Shader shader;
    List<MyModelInstance> children = new ArrayList<MyModelInstance>();
    MyModelInstance parent;
    
    public void addChild(MyModelInstance child) {
        children.add(child);
        child.parent = this;
    }
    public void removeChild(int index) {
        children.get(index).parent = null;
        children.remove(index);
    }
    public void removeChild(MyModelInstance child) {
        children.remove(child);
        child.parent = null;
    }
    public boolean hasParent() {
        return this.parent != null;
    }
    public MyModelInstance getParent() {
        return parent;
    }
    
    
    public MyModelInstance(Model model) {
        super(model);
    }
    
}
