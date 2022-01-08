/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.shaders;

import com.irgames.engine.components.IRShader;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.IRShader;

/**
 *
 * @author Andrew
 */
public class LightShader extends IRShader {
    
    public Vector3 lightDirection = Vector3.Zero;
    public float fogStart, fogEnd;
    public Vector3 fogColor = new Vector3();
    public Vector3 lightColor = new Vector3();
    
   
    
}
