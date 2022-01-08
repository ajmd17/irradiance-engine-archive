/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.utils;

/**
 *
 * @author Andrew
 */
public class RenderUtils {
    public enum BackfaceCullMode {
        back,
        front,
        both, 
        off
    }
    public enum DepthTestMode {
        off,
        on
    }
    public enum Bucket {
        opaque,
        transparent,
        sky,
        translucent
    }
}
