/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.components;

import com.irgames.managers.RenderManager;

/**
 *
 * @author Andrew
 */
public class IRRootNode extends IRNode {

    public IRRootNode() {
        super("root node");
    }
    public void addToRendertable(IRSpatial n) {
        RenderManager.addItem(n);
        //for (IRSpatial child : n.getChildren()) {
        //    addToRendertable(child);
        //}
    }
    @Override
    public void attachChild(IRSpatial ch) {
        super.attachChild(ch);
        //addToRendertable(ch);
    }
    
}
