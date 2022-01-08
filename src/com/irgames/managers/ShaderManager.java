/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.managers;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.irgames.utils.RenderUtils.Bucket;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.components.IRProperty;
import com.irgames.engine.shaders.BarkShader;
import com.irgames.engine.shaders.LeafShader;
import com.irgames.engine.shaders.GrassShader;
import com.irgames.engine.shaders.IRDepthShader;
import com.irgames.engine.components.IRShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.components.ShaderProperty;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class ShaderManager {

    public static List<IRShader> shaders = new ArrayList<>();
    public List<ModelInstance> models = new ArrayList<>();
    public List<IRNode> irnodes = new ArrayList<>();
    static List<LeafShader> leafShaders = new ArrayList<>();
    static List<BarkShader> barkShaders = new ArrayList<>();
    static List<GrassShader> grassShaders = new ArrayList<>();
    public static IRNode rootNode;

    public static void addBarkShader(BarkShader bs) {
        barkShaders.add(bs);
    }

    public static void addLeafShader(LeafShader ls) {
        leafShaders.add(ls);
    }

    public static void addGrassShader(GrassShader gs) {
        grassShaders.add(gs);

    }

    public ShaderManager() {

    }

    public static IRShader getShader(Class irs, ShaderProperties properties) throws Exception {
        if (IRShader.class.isAssignableFrom(irs)) {
            
            List<IRProperty> props = new ArrayList<>();
            for (ShaderProperty p : properties.properties) {
                props.add(new IRProperty(p.name, p.bool));
            }
            
            IRShader sh = fetchShader(irs, props);
            if (sh == null) {
                IRShader shader = (IRShader) irs.newInstance();
                
                shader.properties = props;
                shaders.add(shader);
                shader.init();
                String propsList = "{";
                for (ShaderProperty prop : properties.properties) {
                    propsList += prop.name + ":" + prop.bool + ",";
                }
                propsList += "}";
                System.out.println("Added shader: " + irs.getName() + " with properties " + propsList);
                return shader;
            } else {
                return sh;
            }
        } else {
            throw new Exception("Must be instance of IRShader!");
        }
    }

    private static IRShader fetchShader(Class cls, List<IRProperty> sp) {
        for (IRShader sh : shaders) {
            if (sh.compareProperties(sp) == 1 && sh.getClass().getName().equals(cls.getName())) {
                /*String propsLista = "{";
                for (IRProperty prop : sh.getProperties()) {
                    propsLista += prop.name + ":" + prop.value + ",";
                }
                propsLista += "}";
                
                String propsListb = "{";
                for (IRProperty prop : sp) {
                    propsListb += prop.name + ":" + prop.value + ",";
                }
                propsListb += "}";
                
                System.out.println(propsLista + "   =   " + propsListb);*/
                return sh;
            }

        }
        return null;
    }
    private static float time = 0f;

    public static void updateTime(float deltaTime) {
        time += deltaTime;

        for (IRShader sh : shaders) {
            if (sh instanceof BarkShader) {
                BarkShader bs = (BarkShader) sh;
                bs.setTime(time);
            } else if (sh instanceof LeafShader) {
                LeafShader ls = (LeafShader) sh;
                ls.setTime(time);
            }
        }

        for (LeafShader ls : leafShaders) {
            ls.setTime(time);
        }
        // for (BarkShader bs : barkShaders) {
        //    bs.setTime(time);
        // }
        for (GrassShader gs : grassShaders) {
            gs.setTime(time);
        }
    }

}
