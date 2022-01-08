/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.irgames.engine.physics.Physics;
import com.irgames.engine.physics.Physics;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.components.IRSpatial;
import com.irgames.engine.maps.Map;
import com.irgames.utils.MathUtil;

/**
 *
 * @author Andrew
 */
public class RigidBodyControl extends GameControl {

    public btRigidBody rigidBody;
    private boolean isKinematic = false;

    enum BodyShape {

        box, mesh
    }

    public RigidBodyControl(ModelInstance modelIns, float mass) {
        btCollisionShape shape = Bullet.obtainStaticNodeShape(modelIns.model.nodes);
        //shape.setLocalScaling(new Vector3(scale, scaley, scale));
        shape.setLocalScaling(new Vector3(modelIns.transform.getScaleX(), modelIns.transform.getScaleY(), modelIns.transform.getScaleX()));
        rigidBody = Physics.constructRigidBody(shape, mass, 0, 0, 0);
        Matrix4 mat = new Matrix4();
        rigidBody.getMotionState().getWorldTransform(mat);
        Quaternion store = new Quaternion();
        modelIns.transform.getRotation(store);
        mat.rotate(store);
        rigidBody.setWorldTransform(mat);
        isKinematic = mass == 0.0f;

    }

    //spatial, node shape
   /* public RigidBodyControl(IRSpatial spat, float mass) {
     Node n = new Node();
     NodePart np = new NodePart();
     n.parts.add(np);
     np.meshPart.mesh = spat.mesh;
        
     btCollisionShape shape = Bullet.obtainStaticNodeShape(n, true);
     //shape.setLocalScaling(new Vector3(scale, scaley, scale));
     // shape.setLocalScaling(new Vector3(modelIns.transform.getScaleX(), modelIns.transform.getScaleY(), modelIns.transform.getScaleX()));
     rigidBody = Physics.constructRigidBody(shape, mass, 0, 0, 0);
     Matrix4 mat = new Matrix4();
     rigidBody.getMotionState().getWorldTransform(mat);
     Quaternion store = new Quaternion();
     // modelIns.transform.getRotation(store);
     mat.rotate(store);
     rigidBody.setWorldTransform(mat);
     isKinematic = mass == 0.0f;

     }*/
    public RigidBodyControl(IRNode node, float mass, Vector3 initalPos) {
        node.updateWorldMatrix();

        btCollisionShape shape = new btBoxShape(new Vector3(7f, 0.5f, 7f));
        if (node.getModelInstance() != null) {
            BoundingBox b = new BoundingBox();
            node.getModelInstance().calculateBoundingBox(b);
            Vector3 dimensions = new Vector3();
            b.getDimensions(dimensions);
            dimensions.x *= 0.5f;
            dimensions.y *= 0.5f;
            dimensions.z *= 0.5f;
            shape = new btBoxShape(dimensions);
            shape.setLocalScaling(node.getLocalScale());
        }

        //  btCollisionShape shape = Bullet.obtainStaticNodeShape(node.ins.model.nodes);
        rigidBody = Physics.constructRigidBody(shape, mass, initalPos.x, initalPos.y, initalPos.z);
        Matrix4 mat = new Matrix4();
        rigidBody.getMotionState().getWorldTransform(mat);
        mat.rotate(node.getLocalRotation());
        mat.setTranslation(node.getLocalTranslation());
        rigidBody.setWorldTransform(mat);
        isKinematic = mass == 0.0f;

    }

    public RigidBodyControl(IRNode node, float mass, BodyShape bodyShape) {
        this(node, mass, node.getLocalTranslation());
    }

    public RigidBodyControl(IRNode node, float mass) {
        this(node, mass, BodyShape.box);
    }

    public RigidBodyControl(ModelInstance modelIns, float mass, boolean kinematic) {
        this(modelIns, mass);
        this.isKinematic = kinematic;
    }

    public RigidBodyControl(IRNode nod, float mass, boolean kinematic) {
        this(nod, mass);
        this.isKinematic = kinematic;
    }
    public Vector3 worldTranslation = new Vector3();
    Quaternion worldRotation = new Quaternion();
    Matrix4 rigidMatrix = new Matrix4();
    float updateTime = 0f;
    float maxUpdateTime = 0.25f;
    Vector3 desiredLocation = null;

    public void move(Vector3 loc) {

        rigidBody.activate(true);
        rigidBody.translate(loc);

    }

    boolean Vector3Equals(Vector3 a, Vector3 b) {
        return (Math.round(a.x) == Math.round(b.x) && Math.round(a.y) == Math.round(b.y) && Math.round(a.z) == Math.round(b.z));
    }
    Vector3 oldWt = new Vector3();

    void setTransf(IRSpatial spat, Vector3 world, Quaternion worldr) {
        spat.worldTransform.set(world, worldr, spat.getLocalScale());
        if (spat instanceof IRNode) {
            IRNode irn = (IRNode) spat;
            for (IRSpatial child : irn.getChildren()) {
                setTransf(child, world.cpy().add(child.getLocalTranslation()), child.getLocalRotation().cpy().mul(worldr));

            }
        }

    }

    @Override
    public void update() {

        if (parent != null) {
            rigidBody.getWorldTransform(rigidMatrix);

            rigidMatrix.getTranslation(worldTranslation);
            rigidMatrix.getRotation(worldRotation);

            setTransf(parent, worldTranslation, worldRotation);

            /*if (Map.edit_mode == true) {
             if (isKinematic) {
             if (updateTime > maxUpdateTime) {
             rigidMatrix.scale(parent.getLocalScale().x, parent.getLocalScale().y, parent.getLocalScale().z);
             rigidBody.setWorldTransform(rigidMatrix);
             updateTime = 0f;
             } else {
             updateTime += Gdx.graphics.getDeltaTime();
             }
             }
             }
            
             if (Map.edit_mode == false) {*/
           /* if (isKinematic) {
                //if (updateTime > maxUpdateTime) {

                    rigidMatrix.rotate(parent.getLocalRotation());
                    Vector3 wt = parent.updateWorldTranslation();
                    rigidMatrix.setTranslation(wt.x, wt.y + (parent.getLocalScale().y / 2), wt.z);
                    rigidMatrix.scale(parent.getLocalScale().x, parent.getLocalScale().y, parent.getLocalScale().z);
                    rigidBody.setWorldTransform(rigidMatrix);
                   // updateTime = 0f;
               // } else {
                  //  updateTime += Gdx.graphics.getDeltaTime();
               // }
            }*/
            
        }
    }

    public void rotate(Vector3 axis, float degrees) {
        Matrix4 mat = new Matrix4();
        rigidBody.getMotionState().getWorldTransform(mat);
        mat.rotate(axis, degrees);
        rigidBody.setWorldTransform(mat);
    }

    @Override
    public void doEnable() {

        //    Physics.addCollisionObject(rigidBody);
        // } else {
        Physics.addRBC(this);
        // }
    }

    @Override
    public void resetTransform(Vector3 wTransform) {
        Matrix4 mat = new Matrix4();
        rigidBody.getMotionState().getWorldTransform(mat);
        mat.setTranslation(wTransform);
        //  rigidBody.setWorldTransform(mat);
    }

    @Override
    public void doDisable() {
        // if (isKinematic) {
        //   Physics.removeCollisionObject(rigidBody);
        // } else {
        Physics.removeRBC(this);

        // }
    }
}
