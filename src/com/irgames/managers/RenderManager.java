/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.irgames.utils.RenderUtils.Bucket;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.components.listeners.IRObjectAddedListener;
import com.irgames.engine.components.IRSpatial;
import com.irgames.engine.deferredrendering.NormalsShader;
import com.irgames.engine.shaders.IRShadowShader;
import com.irgames.engine.game.TestShader;
import com.irgames.engine.components.IRShader;
import com.irgames.engine.shaders.BlackShader;
import com.irgames.engine.shaders.NewDepthShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.stats.Stats;
import com.irgames.utils.NodeUtils;
import com.irgames.utils.RenderUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class RenderManager {

    static List<IRNode> opaqueBucket = new ArrayList<IRNode>();
    static List<IRNode> transparentBucket = new ArrayList<IRNode>();
    public static List<IRSpatial> shadowedObjects = new ArrayList<>();
    public static List<IRSpatial> sceneObjects = new ArrayList<>();
    public static List<ModelInstance> instances = new ArrayList<ModelInstance>();
    public static IRObjectAddedListener listener;
    private static BlackShader occShader;
    private static IRNode sunSphere;
    Environment environment;

    Renderable testCube = new Renderable();
    static TestShader testShader;
    ModelInstance ins;
    static NewDepthShader depthShader;
    IRShadowShader shadowShader;
    NormalsShader normShader;
    Camera mainCam;

    public static void setListener(IRObjectAddedListener lst) {
        listener = lst;
    }

    public static TestShader getTestShader() {
        return testShader;
    }

    public RenderManager(Camera mainCam) {

        this.mainCam = mainCam;
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder mpb = modelBuilder.part("parts", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorUnpacked,
                new Material(ColorAttribute.createDiffuse(Color.WHITE)));
        mpb.setColor(1f, 1f, 1f, 1f);
        mpb.box(0, -1.5f, 0, 10, 1, 10);
        mpb.setColor(1f, 0f, 1f, 1f);
        mpb.sphere(2f, 2f, 2f, 10, 10);
        Model cube = modelBuilder.end();

        ins = new ModelInstance(cube);
        ins.getRenderable(testCube);

        normShader = new NormalsShader();
        normShader.init();

        testShader = new TestShader();
        testShader.init();

        shadowShader = new IRShadowShader();
        shadowShader.init();

        initializeFBO();
        this.fsq = createFullScreenQuad();

        sunSphere = NodeUtils.createSphere(Vector3.Zero, new Vector3(8, 8, 8));

    }

    public void dispose() {
        this.depthShader.dispose();
        this.shadowShader.dispose();
        sceneObjects.clear();
        fboBatch.dispose();
    }

    private Mesh createFullScreenQuad() {
        float[] verts = new float[20];
        int i = 0;

        verts[i++] = -1; // x1
        verts[i++] = -1; // y1
        verts[i++] = 0;
        verts[i++] = 0f; // u1
        verts[i++] = 0f; // v1

        verts[i++] = 1f; // x2
        verts[i++] = -1; // y2
        verts[i++] = 0;
        verts[i++] = 1f; // u2
        verts[i++] = 0f; // v2

        verts[i++] = 1f; // x3
        verts[i++] = 1f; // y2
        verts[i++] = 0;
        verts[i++] = 1f; // u3
        verts[i++] = 1f; // v3

        verts[i++] = -1; // x4
        verts[i++] = 1f; // y4
        verts[i++] = 0;
        verts[i++] = 0f; // u4
        verts[i++] = 1f; // v4

        Mesh mesh = new Mesh(true, 4, 0, // static mesh with 4 vertices and no indices
                new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_Position"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoords"));

        mesh.setVertices(verts);
        return mesh;

    }

    public void render() {

    }

    private void initializeFBO() {
        if (fbo != null) {
            fbo.dispose();
        }
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        fbo.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);

        if (fboBatch != null) {
            fboBatch.dispose();
        }
        fboBatch = new SpriteBatch();
    }

    Texture depthTex;
    Mesh fsq;
    FrameBuffer fbo;
    SpriteBatch fboBatch;
    public Camera shadowCam = new OrthographicCamera(512, 512);

    private void renderUnderDepth(IRSpatial child) {
        if (child.mesh != null) {
            depthShader.render(child);
        }
        if (child instanceof IRNode) {
            IRNode irn = (IRNode) child;
            if (irn.getChildren().size() > 0) {
                for (IRSpatial ch : irn.getChildren()) {
                    renderUnderDepth(ch);
                }
            }
        }

    }

    private static void renderStandardDepth(Camera cam, RenderContext context, IRSpatial child) {
        depthShader.cullMode = RenderUtils.BackfaceCullMode.front;
        depthShader.applyMaterial(child.getMaterial());
        depthShader.render(child);

    }

    public static void renderDepthTexture(Camera cam, RenderContext context) {
        // beginDepth();
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        //depthShader.lightMatrix = this.shadowCam.combined;
        if (depthShader == null) {
            try {
                depthShader = (NewDepthShader) ShaderManager.getShader(NewDepthShader.class, new ShaderProperties().setProperty("SHADOW", false));
            } catch (Exception ex) {
                Logger.getLogger(RenderManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        depthShader.begin(cam, context);
        depthShader.near = cam.near;
        depthShader.far = cam.far;
        for (IRSpatial child : sceneObjects) {
            if (child.shader != null && child.mesh != null && child.getBucket() != Bucket.sky) {
                renderStandardDepth(cam, context, child);
            }
        }
        depthShader.end();
    }

    private static void renderStandardOcc(Camera cam, RenderContext context, IRSpatial child) {
        occShader.applyMaterial(child.getMaterial());
        occShader.render(child);

    }
    public static Vector3 lightPosWorld = new Vector3();

    public static void renderOccTexture(Camera cam, RenderContext context) {
        // beginDepth();
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        lightPosWorld.set(cam.position.cpy().add(LightingManager.getSunDirection().cpy().scl(-50)));

        sunSphere.worldTransform.setTranslation(lightPosWorld);

        if (occShader == null) {
            try {
                occShader = (BlackShader) ShaderManager.getShader(BlackShader.class, new ShaderProperties().setProperty("SHADOW", false));
            } catch (Exception ex) {
                Logger.getLogger(RenderManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        occShader.begin(cam, context);
        occShader.color = Color.WHITE;
        occShader.render(sunSphere);
        occShader.color = Color.BLACK;
        for (IRSpatial child : sceneObjects) {
            if (child.shader != null && child.mesh != null && child.getBucket() != Bucket.sky) {
                renderStandardOcc(cam, context, child);
            }
        }

        occShader.end();
    }

    private static void renderChild(IRSpatial child, Camera cam, RenderContext context) {
//        if (child instanceof IRGeom) {
        Shader shader = child.shader;
        if (shader != null && child.mesh != null) {

            shader.begin(cam, context);
            if (shader instanceof IRShader) {
                IRShader irs = (IRShader) shader;
                irs.applyMaterial(child.getMaterial());
            }
            shader.render(child);
            shader.end();
        }
        //}
    }

    public static void render(Camera cam, RenderContext context) {
        Stats.NUM_VERTICES = 0;
        for (IRSpatial child : sceneObjects) {

            if (child.getBucket().equals(Bucket.sky)) {
                renderChild(child, cam, context);
            }
        }
        //render all opaque
        for (IRSpatial child : sceneObjects) {

            if (child.getBucket().equals(Bucket.opaque)) {
                renderChild(child, cam, context);
            }
        }
        for (IRSpatial child : sceneObjects) {

            if (child.getBucket().equals(Bucket.transparent)) {
                renderChild(child, cam, context);
            }
        }

    }

    public void renderTranslucent(Camera cam, RenderContext context) {
        for (IRSpatial child : sceneObjects) {

            if (child.getBucket().equals(Bucket.translucent)) {
                renderChild(child, cam, context);
            }
        }
    }

    public static void remItem(IRSpatial node) {
        if (sceneObjects.contains(node)) {
            /*if (node instanceof IRNode) {
             IRNode irn = (IRNode) node;
             for (int i = 0; i < irn.getChildren().size(); i++) {

             sceneObjects.remove(irn.getChildren().get(i));
             if (irn.getChildren().get(i) instanceof IRNode) {
             IRNode irn2 = (IRNode) irn.getChildren().get(i);
             if (irn2.ins != null) {
             instances.remove(irn2.ins);
             }
             }
             }
             if (irn.ins != null) {
             instances.remove(irn.ins);
             }
             }*/

            sceneObjects.remove(node);

            if (node.drawsShadows()) {
                shadowedObjects.remove(node);
            }
        }

    }

    private static boolean hasElementWithName(String name) {
        for (IRSpatial sp : sceneObjects) {
            if (sp.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static IRSpatial elementWithName(String name) {
        for (IRSpatial sp : sceneObjects) {
            if (sp.getName().equals(name)) {
                return sp;
            }
        }
        return null;
    }

    public static void addItem(IRSpatial node) {
        if (!sceneObjects.contains(node)) {
            //System.out.println("added item:" + node.getName());

            String name = node.getName();
            int i = 0;
            while (hasElementWithName(name + "_" + i) == true) {
                i++;

            }
            name += "_" + i;
            node.setName(name);
            sceneObjects.add(node);
            if (listener != null) {
                listener.action(node);
            }
            if (node instanceof IRNode) {
                IRNode irn = (IRNode) node;
                if (irn.ins != null) {
                    instances.add(irn.ins);
                }
            }

        }
    }

}
