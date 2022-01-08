/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.irgames.engine.components.IRSpatial;
import com.irgames.engine.controls.RigidBodyControl;
import com.irgames.utils.MathUtil;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class Physics {

    static List<RigidBodyControl> rbcs = new ArrayList<>();
    static btCollisionConfiguration collisionConfiguration = new btDefaultCollisionConfiguration();
    static btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfiguration);
    static btBroadphaseInterface broadphase = new btDbvtBroadphase();
    public static btDynamicsWorld collisionWorld;
    DebugDrawer debugDrawer = new DebugDrawer();
    public boolean renderDebug = false, renderMeshes = false;
    private static float speed = 1.5f;
    static btConstraintSolver constraintSolver = new btSequentialImpulseConstraintSolver();

    public static void purge() {

        collisionWorld.getCollisionObjectArray().clear();
        for (int i = rbcs.size() - 1; i > -1; i--) {
            rbcs.get(i).disable();
        }
    }

    public static void addRBC(RigidBodyControl rbc) {
        addRigidBody(rbc.rigidBody);
        rbcs.add(rbc);
    }

    public static void removeRBC(RigidBodyControl rbc) {
        removeRigidBody(rbc.rigidBody);
        rbcs.remove(rbc);
    }

    public static void addRigidBody(btRigidBody rigidBody) {
        collisionWorld.addRigidBody(rigidBody);
        rigidBody.setCollisionFlags(rigidBody.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
    }

    public static void removeRigidBody(btRigidBody rigidBody) {
        collisionWorld.removeRigidBody(rigidBody);
    }

    public static void setSpeed(float newSpeed) {
        speed = newSpeed;
    }

    public static float getSpeed() {
        return speed;
    }

    public static void addCollisionObject(btRigidBody rigidBody) {
        collisionWorld.addCollisionObject(rigidBody);
        rigidBody.setContactCallbackFilter(0);
    }

    public static void removeCollisionObject(btRigidBody rigidBody) {
        collisionWorld.removeCollisionObject(rigidBody);
    }
    private static Vector3 rayFrom = new Vector3();
    private static Vector3 rayTo = new Vector3();
    private static ClosestRayResultCallback callback = new ClosestRayResultCallback(rayFrom, rayTo);
    MyContactListener contactListener;

    public static IRSpatial rayTestSpatial(Ray ray) {
        rayFrom.set(ray.origin);
        // 1000 meters max from the origin
        rayTo.set(ray.direction).scl(1000f).add(rayFrom);

        callback.setCollisionObject(null);
        callback.setClosestHitFraction(1f);
        callback.setRayFromWorld(new Vector3(rayFrom.x, rayFrom.y, rayFrom.z));
        callback.setRayToWorld(new Vector3(rayTo.x, rayTo.y, rayTo.z));

        collisionWorld.rayTest(rayFrom, rayTo, callback);

        if (callback.hasHit()) {
            if (callback.getCollisionObject().userData != null) {
                if (!callback.getCollisionObject().userData.equals("terrain")) {
                    Vector3 hitp = new Vector3();
                    callback.getHitPointWorld(hitp);
                    Vector3 pos = new Vector3();
                    callback.getCollisionObject().getWorldTransform().getTranslation(pos);

                    return MathUtil.getClosestRBC(rbcs, pos).parent;
                } else if (callback.getCollisionObject().userData.equals("terrain")) {
                    return null;
                }
            } else {
                Vector3 hitp = new Vector3();
                callback.getHitPointWorld(hitp);
                Vector3 pos = new Vector3();
                callback.getCollisionObject().getWorldTransform().getTranslation(pos);

                return MathUtil.getClosestRBC(rbcs, pos).parent;
            }

        }
        return null;
    }

    public static Vector3 rayTest(Ray ray) {
        rayFrom.set(ray.origin);
        // 1000 meters max from the origin
        rayTo.set(ray.direction).scl(1000f).add(rayFrom);

        callback.setCollisionObject(null);
        callback.setClosestHitFraction(1f);
        callback.setRayFromWorld(new Vector3(rayFrom.x, rayFrom.y, rayFrom.z));
        callback.setRayToWorld(new Vector3(rayTo.x, rayTo.y, rayTo.z));

        collisionWorld.rayTest(rayFrom, rayTo, callback);

        if (callback.hasHit()) {

            Vector3 hitp = new Vector3();
            callback.getHitPointWorld(hitp);
            return hitp;

        }

        return null;
    }

    public static Vector3 terrainRayTest(Ray ray) {
        rayFrom.set(ray.origin);
        // 1000 meters max from the origin
        rayTo.set(ray.direction).scl(1000f).add(rayFrom);

        callback.setCollisionObject(null);
        callback.setClosestHitFraction(1f);
        callback.setRayFromWorld(new Vector3(rayFrom.x, rayFrom.y, rayFrom.z));
        callback.setRayToWorld(new Vector3(rayTo.x, rayTo.y, rayTo.z));

        collisionWorld.rayTest(rayFrom, rayTo, callback);

        if (callback.hasHit()) {
            if (callback.getCollisionObject().userData != null) {
                if (callback.getCollisionObject().userData.equals("terrain")) {
                    Vector3 hitp = new Vector3();
                    callback.getHitPointWorld(hitp);
                    return hitp;
                }
            }
        }

        return null;
    }
    public Vector3 getWorldIntersection(Vector3 v3) {
        Ray pickRay = new Ray(v3, v3);
        pickRay.origin.set(v3);
        pickRay.direction.set(new Vector3(0f, -1f, 0f).scl(1000f).add(v3));
        Vector3 body = rayTest(pickRay);
        if (body != null) {
            return body;
        }

        return null;
    }
    public Vector3 getWorldIntersectionTerrain(Camera cam, int screenX, int screenY) {
        Ray pickRay = cam.getPickRay(screenX, screenY);

        Vector3 body = terrainRayTest(pickRay);
        if (body != null) {
            return body;
        }

        return null;
    }

    public IRSpatial getWorldIntersectionSpatial(Camera cam, int screenX, int screenY) {
        Ray pickRay = cam.getPickRay(screenX, screenY);

        IRSpatial body = rayTestSpatial(pickRay);
        if (body != null) {
            return body;
        }

        return null;
    }

    public Vector3 getWorldIntersection(Camera cam, int screenX, int screenY) {
        Ray pickRay = cam.getPickRay(screenX, screenY);

        Vector3 body = rayTest(pickRay);
        if (body != null) {
            return body;
        }

        return null;
    }

    public static btRigidBody constructRigidBody(btCollisionShape shape, float mass, float x, float y, float z) {

        Matrix4 tempMatrix = new Matrix4();
        tempMatrix.idt().translate(new Vector3(x, y, z));

        Vector3 localInertia = new Vector3(0, 0, 0);
        if (mass > 0) {
            shape.calculateLocalInertia(mass, localInertia);
        }
        btDefaultMotionState motionState = new btDefaultMotionState(tempMatrix);

        btRigidBody.btRigidBodyConstructionInfo rigidBodyCI = new btRigidBody.btRigidBodyConstructionInfo(mass, motionState, shape, localInertia);

        btRigidBody rigidBody = new btRigidBody(rigidBodyCI);

        return rigidBody;
    }

    public Physics(boolean renderDebug) {

        collisionWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfiguration);
        debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);

        collisionWorld.setDebugDrawer(debugDrawer);
        this.renderDebug = renderDebug;
        contactListener = new MyContactListener();

    }

    public class MyContactListener extends ContactListener {

        @Override
        public void onContactStarted(btCollisionObject colObj0, btCollisionObject colObj1) {
            // implementation
            //System.out.println("Contact: " + colObj0.className + "  " + colObj1.className);
        }

        @Override
        public void onContactProcessed(int userValue0, int userValue1) {
            // implementation
        }
    }

    public void renderDebug(Camera cam) {
        final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

        collisionWorld.stepSimulation(delta * speed, 5, 1f / 60f);

        if (this.renderDebug) {
            debugDrawer.begin(cam);

            for (int i = 0; i < collisionWorld.getCollisionObjectArray().size(); i++) {
                if (!renderMeshes) {
                    if (collisionWorld.getCollisionObjectArray().at(i).getCollisionShape() instanceof btBoxShape) {
                        collisionWorld.debugDrawObject(collisionWorld.getCollisionObjectArray().at(i).getWorldTransform(), collisionWorld.getCollisionObjectArray().at(i).getCollisionShape(), new Vector3(1, 1, 1));
                    }
                } else if (renderMeshes) {
                    collisionWorld.debugDrawObject(collisionWorld.getCollisionObjectArray().at(i).getWorldTransform(), collisionWorld.getCollisionObjectArray().at(i).getCollisionShape(), new Vector3(1, 1, 1));
                }
            }

            debugDrawer.end();
        }
    }
}
