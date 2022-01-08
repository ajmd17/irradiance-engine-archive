/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.pbr;

import com.badlogic.gdx.graphics.Color;
import com.irgames.engine.components.IRMat;

/**
 *
 * @author Andrew
 */
public class Presets {

    public final IRMat PBR_Gold = new IRMat();
    public final IRMat PBR_Silver = new IRMat();
    public final IRMat PBR_Copper = new IRMat();
    public final IRMat PBR_BluePlastic = new IRMat();
    public final IRMat PBR_RedPaint = new IRMat();

    public Presets() {
        PBR_Gold.setProperty("specular", 20.0f);
        PBR_Gold.setProperty("roughness", 0.3f);
        PBR_Gold.setProperty("metallic", 0.6f);
        PBR_Gold.setProperty("albedo", new Color(1.000f, 0.766f, 0.336f, 1f));
        PBR_Gold.setProperty("F0", 0.2f);
        PBR_Gold.setProperty("grime", 0.4f);
        
        
        
        PBR_Silver.setProperty("specular", 30.0f);
        PBR_Silver.setProperty("roughness", 0.2f);
        PBR_Silver.setProperty("metallic", 0.6f);
        PBR_Silver.setProperty("albedo", new Color(0.972f, 0.960f, 0.915f, 1f));
        PBR_Silver.setProperty("F0", 0.3f);
        PBR_Silver.setProperty("grime", 0.6f);
        
        
        PBR_Copper.setProperty("specular", 10.0f);
        PBR_Copper.setProperty("roughness", 0.3f);
        PBR_Copper.setProperty("metallic", 0.6f);
        PBR_Copper.setProperty("albedo", new Color(0.955f, 0.637f, 0.538f, 1f));
        PBR_Copper.setProperty("F0", 0.2f);
        PBR_Copper.setProperty("grime", 0.6f);
        
        

        PBR_BluePlastic.setProperty("specular", 16.0f);
        PBR_BluePlastic.setProperty("roughness", 0.2f);
        PBR_BluePlastic.setProperty("metallic", 0.2f);
        PBR_BluePlastic.setProperty("albedo", new Color(0.2f, 0.3f, 0.8f, 1f));
        PBR_BluePlastic.setProperty("F0", 0.1f);
        PBR_BluePlastic.setProperty("grime", 0.2f);

        PBR_RedPaint.setProperty("specular", 10.0f);
        PBR_RedPaint.setProperty("roughness", 0.4f);
        PBR_RedPaint.setProperty("metallic", 0.2f);
        PBR_RedPaint.setProperty("albedo", new Color(255f / 255f, 65f / 255f, 33f / 255f, 1f));
        PBR_RedPaint.setProperty("F0", 0.3f);
        PBR_RedPaint.setProperty("grime", 0.2f);
    }
}
