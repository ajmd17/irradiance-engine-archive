/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationListener;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.animation.AnimBatch;
import com.irgames.engine.animation.AnimShaderProvider;
import com.irgames.engine.controls.GameControl;
import com.irgames.utils.RenderUtils;
import com.irgames.utils.RenderUtils.Bucket;
import com.irgames.managers.RenderManager;
import com.irgames.utils.MathUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class IRNode extends IRSpatial {

    private final List<IRSpatial> children = new ArrayList<>();

    public ModelInstance ins;

    private boolean playingAnim = false;
    AnimationController controller;
    AnimBatch animBatch = new AnimBatch();
    private boolean useAnimations = false;
    AnimationListener listener;

    public List<IRSpatial> getChildren() {
        return children;
    }

    public IRSpatial getChild(int index) {
        if (children.size() > index) {
            return children.get(index);
        } else {
            return null;
        }
    }

    public IRSpatial getChild(String name) {
        for (IRSpatial child : children) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    public ModelInstance getModelInstance() {
        return ins;
    }

    public IRNode(String name) { // main
        this.nodeName = name;
    }

    public IRNode() {
        this("node");
    }

    public IRNode(String name, ModelInstance instance) {
        this(instance);
        this.nodeName = name;
    }

    public IRNode(String name, Model model) {

        this(new ModelInstance(model));
        this.nodeName = name;

    }

    public IRNode(String name, Model model, boolean useAnimations) {

        this(new ModelInstance(model), useAnimations);
        this.nodeName = name;
    }

    public IRNode(Model model) {
        this(new ModelInstance(model));
    }

    public IRNode(Model model, boolean useAnimations) {
        this(new ModelInstance(model), useAnimations);
        this.nodeName = "animated node";
    }

    public IRNode(ModelInstance instance) {
        this("node");
        for (Node n : instance.nodes) {

            for (NodePart np : n.parts) {

                IRGeom childN = new IRGeom();
                //childN.setName(n.id);

                np.setRenderable(childN);
                this.attachChild(childN);
            }
        }
        this.ins = instance;
    }

    public IRNode(String name, ModelInstance instance, boolean useAnimations) {
        this(instance, useAnimations);
        this.nodeName = name;
    }

    public IRNode(ModelInstance instance, boolean useAnimations) {
        this("node (animated)");
        this.ins = instance;
        this.useAnimations = useAnimations;
        if (useAnimations) {
            AnimShaderProvider animShaderProvider = new AnimShaderProvider();
            controller = new AnimationController(ins);
            for (int i = 0; i < ins.nodes.size; i++) {
                Node n = ins.nodes.get(i);
                for (int u = 0; u < n.parts.size; u++) {
                    NodePart np = n.parts.get(u);
                    IRGeom child = new IRGeom();
                    child.setName("child_" + i + "_" + u);
                    np.setRenderable(child);
                    child.shader = animShaderProvider.getShader(child);
                    this.attachChild(child);
                }
            }
            listener = new AnimationListener() {
                @Override
                public void onEnd(AnimationDesc animation) {
                    // this will be called when the current animation is done. 
                    // queue up another animation called "balloon". 
                    // Passing a negative to loop count loops forever.  1f for speed is normal speed.
                    onAnimEnd();
                }

                @Override
                public void onLoop(AnimationDesc animation) {
                    // TODO Auto-generated method stub
                    onAnimLoop();
                }
            };
        }
    }

    @Override
    public final void setBucket(Bucket bkt) {
        super.setBucket(bkt);
        for (IRSpatial ch : children) {
            if (!ch.bucketChanged) {
                ch.bucket = this.bucket;
            }
        }
    }

    @Override
    public void setUpdateNeeded() {
        super.setUpdateNeeded();
        if (children != null) {
            for (IRSpatial children1 : children) {
                children1.setUpdateNeeded();
            }
        }
    }

    @Override
    public void setShader(Shader s) {
        super.setShader(s);
        for (IRSpatial child : children) {
            child.setShader(s);
        }
    }

    @Override
    public void setMaterial(IRMat mat) {
        super.setMaterial(mat);
        for (IRSpatial child : children) {
            child.setMaterial(mat);
        }
    }

    @Override
    public void updateParents() {
        super.updateParents();
        for (GameControl gc : controls) {
            gc.resetTransform(worldTrans);
        }
        for (IRSpatial children1 : children) {
            children1.updateParents();
        }
    }

    @Override
    public void update() {
        super.update();
        if (ins != null) {
            ins.transform.set(worldTransform);

        }
    }

    public void onDraw() {
    }

    public void detachAllChildren() {
        for (int i = children.size() - 1; i > -1; i--) {
            this.detachChild(children.get(i));
        }
    }

    public void attachChild(IRSpatial child) {
        child.setParent(this);
        children.add(child);
        child.setRotation(localRotation);
        this.setUpdateNeeded();
        if (!child.bucketChanged) {
            child.bucket = this.bucket;
        }
    }

    @Override
    public void rotate(Vector3 axis, float degrees) {
        super.rotate(axis, degrees);
        /*if (ins != null) {
         this.ins.transform.set(worldTransform);
         }*/
        /*for (int i = 0; i < children.size(); i++) {
         children.get(i).setRotation(localRotation);
         }*/

    }

    @Override
    public void rotate(Quaternion rotToAdd) {
        super.rotate(rotToAdd);
        for (int i = 0; i < children.size(); i++) {
            children.get(i).rotate(rotToAdd);
        }
    }

    @Override
    public void setRotation(Quaternion rot) {
        super.setRotation(rot);
        for (int i = 0; i < children.size(); i++) {
            //children.get(i).rotate(rot);
        }
    }

    public int getSize() {
        return children.size();
    }

    @Override
    public void scale(Vector3 scl) {
        super.scale(scl);
        for (int i = 0; i < children.size(); i++) {
            children.get(i).scale(scl);
        }

    }

    @Override
    public void setScale(Vector3 scl) {
        super.setScale(scl);

        /*for (IRSpatial ir : children) {
         Vector3 diff = scl.cpy().sub(ir.localScale);
         ir.scale(diff);
         }*/
    }

    @Override
    public void onChangeWorldTranslation() {
        super.onChangeWorldTranslation();
        for (IRSpatial child : children) {
            child.onChangeWorldTranslation();
        }
    }

    @Override
    public void setLocalTranslation(Vector3 loc) {
        super.setLocalTranslation(loc);
    }

    public boolean hasChild(IRNode child) {
        if (children.contains(child)) {
            return true;
        }
        return false;
    }

    public void detachChild(IRSpatial child) {
        if (child != null) {
            child.setParent(null);
            children.remove(child);
            this.updateParents();
        }
    }
    Vector3 position = new Vector3();

    protected boolean isVisible(final Camera cam, final Renderable instance) {
        instance.worldTransform.getTranslation(position);
        return cam.frustum.pointInFrustum(position);
    }

    public final void playAnimation(String name, int loop, float speed) {
        controller.setAnimation(name, loop, speed, listener);
    }

    public final void stopAnimation() {
        controller.setAnimation(null);
    }

    public void onAnimEnd() {
    }

    public void onAnimLoop() {
    }

    @Override
    public void draw(Camera cam, RenderContext context) {
        if (useAnimations) {
            controller.update(Gdx.graphics.getDeltaTime());
        }
        
        super.draw(cam, context);
        for (IRSpatial child : children) {
            child.draw(cam, context);
        }
    }
}
