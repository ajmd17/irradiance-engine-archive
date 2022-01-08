/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.IRNode;
import com.irgames.managers.RenderManager;
import com.irgames.engine.game.TestShader;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class NodeUtils {

    static ModelBuilder modelBuilder = new ModelBuilder();
   
    public static IRNode nodeFromMesh(Mesh mesh) {
        IRNode irn = new IRNode();
        
        irn.mesh = mesh;
        //System.out.println("Num vertices: " + mesh.vertices.length);
        irn.meshPartSize = mesh.getNumIndices();
        irn.primitiveType = GL20.GL_TRIANGLES;
        return irn;
    }

    public static IRNode createMonkey(Vector3 location, Vector3 scale, Shader shader) {
        Model model = Assets.loadObjModel(Gdx.files.internal("data/models/shapes/monkey.obj"));
        if (shader == null) {
            shader = RenderManager.getTestShader();
        }
        ModelInstance instance = new ModelInstance(model);
        instance.transform.setToTranslation(location);
        NodePart np = instance.nodes.get(0).parts.get(0);
        IRNode nod = new IRNode();
        nod.shader = shader;
        instance.transform.setToScaling(scale);//scale.x, scale.y, scale.z);
        nod.ins = instance;
        nod.setName("monkey");
        // nod.shader.init();
        np.setRenderable(nod);
        nod.setLocalTranslation(location);
        nod.scale(scale);

        return nod;
    }

    public static IRNode createMonkey(Vector3 location, Vector3 scale) {
        return createMonkey(location, scale, null);
    }

    public static IRNode createMonkey(Vector3 location, Shader shader) {
        return createMonkey(location, new Vector3(1, 1, 1), shader);
    }

    public static IRNode createTeapot(Vector3 location, Vector3 scale, Shader shader) {
        Model model = Assets.loadObjModel(Gdx.files.internal("data/models/shapes/teapot.obj"));
        if (shader == null) {
            shader = RenderManager.getTestShader();
        }
        ModelInstance instance = new ModelInstance(model);
        instance.transform.setToTranslation(location);
        NodePart np = instance.nodes.get(0).parts.get(0);
        IRNode nod = new IRNode();
        nod.shader = shader;
        instance.transform.setToScaling(scale);//scale.x, scale.y, scale.z);
        nod.ins = instance;
        nod.setName("teapot");
        // nod.shader.init();
        np.setRenderable(nod);
        nod.setLocalTranslation(location);
        nod.scale(scale);

        return nod;
    }

    public static IRNode createTeapot(Vector3 location, Vector3 scale) {
        return createTeapot(location, scale, null);
    }

    public static IRNode createTeapot(Vector3 location, Shader shader) {
        return createTeapot(location, new Vector3(1, 1, 1), shader);
    }

    public static IRNode createCameraShape(Vector3 location, Vector3 scale, Shader shader) {
        Model model = Assets.loadObjModel(Gdx.files.internal("data/models/shapes/camera.obj"));
        if (shader == null) {
            shader = RenderManager.getTestShader();
        }
        ModelInstance instance = new ModelInstance(model);
        instance.transform.setToTranslation(location);
        NodePart np = instance.nodes.get(0).parts.get(0);
        IRNode nod = new IRNode();
        nod.shader = shader;
        instance.transform.setToScaling(scale);//scale.x, scale.y, scale.z);
        nod.ins = instance;
        nod.setName("camera");
        // nod.shader.init();
        np.setRenderable(nod);
        nod.setLocalTranslation(location);
        nod.scale(scale);

        return nod;
    }

    public static IRNode createCameraShape(Vector3 location, Vector3 scale) {
        return createCameraShape(location, scale, null);
    }

    public static IRNode createCameraShape(Vector3 location, Shader shader) {
        return createCameraShape(location, new Vector3(1, 1, 1), shader);
    }

    public static IRNode createSphere(Vector3 location, Vector3 scale, Shader shader) {
        Model model = Assets.loadObjModel(Gdx.files.internal("data/models/shapes/sphere.obj"));
        if (shader == null) {
            shader = RenderManager.getTestShader();
        }
        ModelInstance instance = new ModelInstance(model);
        instance.transform.setToTranslation(location);
        NodePart np = instance.nodes.get(0).parts.get(0);
        IRNode nod = new IRNode();
        nod.shader = shader;
        instance.transform.setToScaling(scale);//scale.x, scale.y, scale.z);
        nod.ins = instance;
        nod.setName("sphere");
        // nod.shader.init();
        np.setRenderable(nod);
        nod.setLocalTranslation(location);
        nod.scale(scale);

        return nod;
    }

    public static IRNode createSphere(Vector3 location, Vector3 scale) {
        return createSphere(location, scale, null);
    }

    public static IRNode createSphere(Vector3 location, Shader shader) {
        return createSphere(location, new Vector3(1, 1, 1), shader);
    }

    public static IRNode createSkyBox(Vector3 location, Vector3 scale, Shader shader) {
        Model model = Assets.loadObjModel(Gdx.files.internal("data/models/shapes/skybox.obj"));
        if (shader == null) {
            shader = RenderManager.getTestShader();
        }
        ModelInstance instance = new ModelInstance(model);
        instance.transform.setToTranslation(location);
        NodePart np = instance.nodes.get(0).parts.get(0);
        IRNode nod = new IRNode();
        nod.setDrawsShadows(true);
        nod.shader = shader;
        instance.transform.setToScaling(scale);//scale.x, scale.y, scale.z);
        nod.ins = instance;
        nod.setName("box");
        // nod.shader.init();
        np.setRenderable(nod);
        nod.setLocalTranslation(location);
        nod.scale(scale.scl(25f));

        return nod;
    }

    public static IRNode createSkyBox(Vector3 location, Vector3 scale) {
        return createSkyBox(location, scale, null);
    }

    public static IRNode createSkyBox(Vector3 location, Shader shader) {
        return createSkyBox(location, new Vector3(1, 1, 1), shader);
    }

    public static IRNode createBox(Vector3 location, Vector3 scale, Shader shader) {
        Model model = Assets.loadObjModel(Gdx.files.internal("data/models/shapes/cube.obj"));
        if (shader == null) {
            shader = RenderManager.getTestShader();
        }
        ModelInstance instance = new ModelInstance(model);
        instance.transform.setToTranslation(location);
        NodePart np = instance.nodes.get(0).parts.get(0);
        IRNode nod = new IRNode();
        nod.setDrawsShadows(true);
        nod.shader = shader;
        instance.transform.setToScaling(scale);//scale.x, scale.y, scale.z);
        nod.ins = instance;
        nod.setName("box");
        // nod.shader.init();
        np.setRenderable(nod);
        nod.setLocalTranslation(location);
        nod.scale(scale);

        return nod;
    }

    public static IRNode createBox(Vector3 location, Vector3 scale) {
        return createBox(location, scale, null);
    }

    public static IRNode createBox(Vector3 location, Shader shader) {
        return createBox(location, new Vector3(1, 1, 1), shader);
    }

    public static IRNode createRoundedBox(Vector3 location, Vector3 scale, Shader shader) {
        Model model = Assets.loadObjModel(Gdx.files.internal("data/models/shapes/roundedcube.obj"));
        if (shader == null) {
            shader = RenderManager.getTestShader();
        }
        ModelInstance instance = new ModelInstance(model);
        instance.transform.setToTranslation(location);
        NodePart np = instance.nodes.get(0).parts.get(0);
        IRNode nod = new IRNode();
        nod.setDrawsShadows(true);
        nod.shader = shader;
        instance.transform.setToScaling(scale);//scale.x, scale.y, scale.z);
        nod.ins = instance;
        nod.setName("box");
        // nod.shader.init();
        np.setRenderable(nod);
        nod.setLocalTranslation(location);
        nod.scale(scale);

        return nod;
    }

    public static IRNode createRoundedBox(Vector3 location, Vector3 scale) {
        return createRoundedBox(location, scale, null);
    }

    public static IRNode createRoundedBox(Vector3 location, Shader shader) {
        return createRoundedBox(location, new Vector3(1, 1, 1), shader);
    }

}
