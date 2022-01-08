/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.managers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.shaders.LightShader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class LightingManager {
    private static Color ambientColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);
    private static Color backgroundColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    private static Vector3 sunColor = new Vector3(1,1,1);
    private static Vector3 fogColor = new Vector3(0.8f, 0.7f, 0.9f);
    private static float fogStart = 50, fogEnd = 150;
    private static Vector3 sunDirection = new Vector3();
    private static List<LightShader> lightShaders = new ArrayList<LightShader>();
    
    public static void setSunColor(Color newCol) {
        sunColor = new Vector3(newCol.r,newCol.g, newCol.b);
        for (LightShader sh : lightShaders) {
            updateSunColor(sh);
        }
        //for (LightShader sh : lightShaders) {
        //   sh.program.setUniformf("u_lightColor", newCol);
        //}
    }
    public static float getFogEnd() { return fogEnd; }
    public static float getFogStart() { return fogStart; }
    public static void setAmbientColor(Color col) {
        ambientColor = col;
    }
    public static Color getAmbientColor() {
        return ambientColor;
    }
    public static Vector3 getSunDirection() {
        return sunDirection;
    }
    private static void updateSunColor(LightShader ls) {
        ls.lightColor = sunColor;

    }
    private static void updateSunDirection(LightShader ls) {
        ls.lightDirection = sunDirection;

    }

    private static void updateFog(LightShader ls) {
        ls.fogStart = fogStart;
        ls.fogEnd = fogEnd;
        ls.fogColor = fogColor;
    }

    public static void setFog(Color color, float start, float end) {
        fogStart = start;
        fogEnd = end;
        fogColor = new Vector3(color.r, color.g, color.b);
        for (LightShader sh : lightShaders) {
            updateFog(sh);
        }
    }

    public static void setSunDirection(Vector3 newDir) {
        sunDirection = newDir;
        //System.out.println(lightShaders.size());
        for (LightShader sh : lightShaders) {
            updateSunDirection(sh);
        }
    }

    public static Color getBackgroundColor() {
        return backgroundColor;
    }

    public static void setBackgroundColor(Color newCol) {
        backgroundColor = newCol;
    }

    public static void addShader(LightShader ls) {
        lightShaders.add(ls);
        updateSunDirection(ls);
        updateFog(ls);
        updateSunColor(ls);
    }
}
