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
import com.badlogic.gdx.math.collision.BoundingBox;
import com.irgames.engine.animation.AnimBatch;
import com.irgames.engine.animation.AnimShaderProvider;
import com.irgames.engine.components.listeners.IRListener;
import com.irgames.engine.components.listeners.IRObjectAddedListener;
import com.irgames.engine.controls.GameControl;
import com.irgames.engine.controls.RigidBodyControl;
import com.irgames.engine.game.MyBoundingBox;
import com.irgames.utils.RenderUtils;
import com.irgames.utils.RenderUtils.Bucket;
import com.irgames.managers.RenderManager;
import com.irgames.utils.MathUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Andrew
 */
public class IRSpatial extends Renderable implements IRObject {

    protected String id = "0";
    protected IRNode parent = null;
    protected Bucket bucket = Bucket.opaque;
    public Vector3 worldTrans = new Vector3();
    protected Quaternion localRotation = new Quaternion();
    protected Vector3 localScale = new Vector3(1, 1, 1);
    protected Vector3 localTrans = new Vector3();
    protected Vector3 worldScale = new Vector3();
    protected Quaternion worldRotation = new Quaternion();
    protected boolean updateNeeded = false;
    protected String nodeName = "node";
    protected boolean drawShadows = false;
    public boolean attachedToRoot = false;
    protected List<GameControl> controls = new ArrayList<>();
    protected static IRObjectAddedListener parentUpdatedListener;
    public IRMat irmaterial = new IRMat();
    protected boolean bucketChanged = false;
    protected boolean updateRigidBody = true;
    public boolean fromRbc = false;
    protected BoundingBox boundingBox;
    /* IRObject methods */
    public List<IRProperty> properties = new ArrayList<>();

    @Override
    public List<IRProperty> getProperties() {
        return properties;
    }

    public BoundingBox getBoundingBox() {
        if (boundingBox == null) {
            if (this.mesh != null) {
                boundingBox = mesh.calculateBoundingBox().mul(worldTransform);
            }
        } else {
            return boundingBox;
        }
        return null;
    }

    @Override
    public void setProperty(String name, Object value) {
        setProperty(name, value, true);
    }

    @Override
    public void setProperty(String name, Object value, Boolean bool) {
        for (IRProperty property : properties) {
            if (property.name.equals(name)) {
                property.value = value;
                property.editable = bool;
                return;
            }
        }
        IRProperty prop = new IRProperty(name, value, bool);
        properties.add(prop);

    }

    @Override
    public Object getProperty(String name) {
        for (IRProperty property : properties) {
            if (property.name.equals(name)) {
                return property.value;
            }
        }
        return null;
    }

    @Override
    public Vector3 getVector3(String name) {
        Object obj = getProperty(name);
        if (obj instanceof Vector3) {
            return (Vector3) obj;
        }
        return null;
    }

    @Override
    public Boolean getBoolean(String name) {
        Object obj = getProperty(name);
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        return false;
    }

    @Override
    public Float getFloat(String name) {
        Object obj = getProperty(name);
        if (obj instanceof Float) {
            return (Float) obj;
        }
        return 0f;
    }

    @Override
    public String getString(String name) {
        Object obj = getProperty(name);
        if (obj instanceof String) {
            return (String) obj;
        }
        return "";
    }

    public IRSpatial() {
        this.setProperty("localTrans", Vector3.Zero);
        this.setProperty("worldTrans", Vector3.Zero, false);
        this.getMaterial().setProperty("drawsShadows", true);
    }

    public static void setParentUpdatedListener(IRObjectAddedListener listener) {
        parentUpdatedListener = listener;
    }

    public boolean drawsShadows() {
        return drawShadows;
    }

    public void setID(String newID) {
        this.id = newID;
    }

    public String getID() {
        return id;
    }

    public void setDrawsShadows(boolean draws) {
        this.drawShadows = draws;
    }

    public GameControl getControl(Class control) {
        for (GameControl ctrl : controls) {
            if (ctrl.getClass().equals(control)) {
                return ctrl;
            }
        }
        return null;
    }

    public void setMaterial(IRMat mat) {
        this.irmaterial = mat;
        if (irmaterial != null) {
            if (irmaterial.shader != null) {
                this.shader = irmaterial.shader;
            }
        }
    }

    public IRMat getMaterial() {
        return irmaterial;
    }

    public void setShader(Shader s) {
        this.shader = s;
        this.irmaterial.shader = s;
    }

    public GameControl getControl(int index) {
        return controls.get(index);
    }

    public void addControl(GameControl control) {
        if (getControl(control.getClass()) == null) {
            control.parent = this;
            control.enable();
            controls.add(control);
        }
    }

    public void removeControl(GameControl control) {
        control.disable();
        control.parent = null;
        controls.remove(control);
    }

    public void removeControl(Class control) {
        removeControl(getControl(control));
    }

    public Quaternion getLocalRotation() {
        return this.localRotation;
    }

    public Vector3 getLocalScale() {
        return this.localScale;
    }

    public String getName() {
        return this.nodeName;
    }

    public void setName(String name) {
        this.nodeName = name;
    }

    public IRNode getParent() {
        return parent;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public boolean isAttachedToRoot() {
        if (this.nodeName.equals("root node")) {
            return true;
        } else {
            IRNode par = this.parent;
            if (par != null) {
                while ((!par.getName().equals("root node"))) {
                    if (par.hasParent()) {
                        par = par.getParent();
                    } else {
                        return false;
                    }
                }
            } else if (par == null) {
                return false;
            }
        }
        return true;
    }

    public Bucket getBucket() {
        return this.bucket;
    }

    public void setBucket(Bucket bkt) {
        this.bucket = bkt;
        this.bucketChanged = true;

    }

    public boolean hasParent(String name) {
        IRNode par = this.parent;
        if (hasParent()) {
            while (!par.getName().equals(name)) {
                if (par.hasParent()) {
                    par = par.getParent();
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void setUpdateNeeded() {
        updateNeeded = true;

    }

    public void updateParents() {
        attachedToRoot = isAttachedToRoot();
        if (attachedToRoot) {
            RenderManager.addItem(this);
        } else if (!attachedToRoot) {
            RenderManager.remItem(this);
        }
        if (parentUpdatedListener != null) {
            parentUpdatedListener.action(this);
        }

    }
    Quaternion temp = new Quaternion();

    public void update() {

        if (updateNeeded) {
            updateWorldMatrix();
            updateNeeded = false;
        }
    }

    public void setParent(IRNode newParent) {

        this.parent = newParent;
        updateParents();

    }

    public void onDraw() {
    }

    public Matrix4 getWorldMatrix() {
        return worldTransform;
    }

    boolean Vector3Equals(Vector3 a, Vector3 b) {
        return (Math.round(a.x) == Math.round(b.x) && Math.round(a.y) == Math.round(b.y) && Math.round(a.z) == Math.round(b.z));
    }

    public void setPhysicsLocation(Vector3 loc) {
        RigidBodyControl rbc = (RigidBodyControl) this.getControl(RigidBodyControl.class);
        if (rbc != null) {
            Vector3 rwt = new Vector3();
            rbc.rigidBody.getWorldTransform().getTranslation(rwt);
            Vector3 diff = loc.cpy().sub(rwt);
            rbc.move(diff);

        } else {

        }
    }

    Vector3 oldWt = new Vector3();

    public void updateWorldMatrix() {
        //setProperty("worldTrans",this.getWorldTranslation(), false);

        // 
        RigidBodyControl rbc = (RigidBodyControl) this.getControl(RigidBodyControl.class);
        worldRotation = this.getWorldRotation();
        worldScale = this.getWorldScale();
        if (rbc == null) {
            worldTrans = updateWorldTranslation();
            this.worldTransform.set(worldTrans, localRotation, worldScale);
        }

        //if (!fromRbc) {
        // setPhysicsLocation(worldTrans);
        //}
    }

    public void rotate(Vector3 axis, float degrees) {
        this.worldTransform.rotate(axis, degrees);
        worldTransform.getRotation(localRotation);
        // setUpdateNeeded();
    }

    public void scale(Vector3 scl) {
        localScale = MathUtil.multVector3(localScale, scl);

        this.setUpdateNeeded();
    }

    public void setScale(Vector3 scl) {
        localScale = scl;
        this.setUpdateNeeded();
    }

    public void setRotation(Quaternion rot) {
        this.localRotation = rot;
        this.setUpdateNeeded();
    }

    public void rotate(Quaternion rotToAdd) {
        this.localRotation.mul(rotToAdd);
        this.setUpdateNeeded();
    }

    public void setWorldTranslation(Vector3 loc, boolean updateRigidBody) {
        this.updateRigidBody = updateRigidBody;
        Vector3 want = loc.cpy();
        Vector3 local = want.sub(this.getWorldTranslation());
        setLocalTranslation(loc);
    }

    public void setLocalTranslation(Vector3 loc) {
        this.localTrans = loc;

        this.setProperty("localTrans", loc);
        setUpdateNeeded();
      //  updateWorldMatrix();
        this.onChangeWorldTranslation();
    }

    public Vector3 getLocalTranslation() {

        Vector3 loc = (Vector3) getProperty("localTrans");

        return loc;//this.localTrans.cpy();

    }

    public void getWorldRotation(Quaternion outr) {

        if (this.parent != null) {
            outr.mul(this.localRotation);
            parent.getWorldRotation(outr);
        }
    }

    public Quaternion getWorldRotation() {
        Quaternion wr = new Quaternion();
        getWorldRotation(wr);
        return wr;
    }

    public void getWorldScale(Vector3 outw) {

        if (this.parent != null) {
            outw.scl(parent.localScale);
            parent.getWorldScale(outw);
        }
    }

    public void onChangeWorldTranslation() {
        this.setPhysicsLocation(this.updateWorldTranslation().cpy());
    }

    public Vector3 getWorldScale() {
        Vector3 wl = localScale.cpy();//new Vector3();
        getWorldScale(wl);
        return wl;
    }

    public void updateWorldTranslation(Vector3 outw) {

        Vector3 loc = this.getLocalTranslation();
        outw.add(loc);

        if (this.parent != null) {

            parent.updateWorldTranslation(outw);
        }
    }

    public Vector3 updateWorldTranslation() {
        Vector3 wl = new Vector3();
        updateWorldTranslation(wl);
        return wl;
    }

    public Vector3 getWorldTranslation() {
        worldTransform.getTranslation(worldTrans);
        return worldTrans;
    }
    Vector3 position = new Vector3();

    protected boolean isVisible(final Camera cam, final Renderable instance) {
        instance.worldTransform.getTranslation(position);
        return cam.frustum.pointInFrustum(position);
    }

    public void draw(Camera cam, RenderContext context) {
        /*if (irmaterial == null) {
         if (shader != null) {
         IRMat irm = new IRMat();
         setMaterial(irm);
         irm.shader = this.shader;
         }
         }*/
        for (GameControl ctrl : controls) {
            ctrl.update();
        }
        this.onDraw();
        update();

    }
}
