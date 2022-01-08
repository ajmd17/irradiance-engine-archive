/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.irgames.utils.MathUtil;

/**
 *
 * @author Andrew
 */
public class ShadowUtil {

    public static void updateFrustumSplits(float[] splits, float near, float far, float lambda) {
        for (int i = 0; i < splits.length; i++) {
            float IDM = i / (float) splits.length;
            float log = near * (float) Math.pow((far / near), IDM);
            float uniform = near + (far - near) * IDM;
            splits[i] = log * lambda + uniform * (1.0f - lambda);
        }

        // This is used to improve the correctness of the calculations. Our main near- and farplane
        // of the camera always stay the same, no matter what happens.
        splits[0] = near;
        splits[splits.length - 1] = far;
    }

    public static void updateFrustumPoints(Camera viewCam,
            float nearOverride,
            float farOverride,
            float scale,
            Vector3[] points) {

        Vector3 pos = viewCam.position;
        Vector3 dir = viewCam.direction;
        Vector3 up = viewCam.up;

        float depthHeightRatio = viewCam.viewportHeight / viewCam.near;
        float near = nearOverride;
        float far = farOverride;
        float ftop = viewCam.viewportHeight;
        float fright = viewCam.viewportWidth;
        float ratio = fright / ftop;

        float near_height;
        float near_width;
        float far_height;
        float far_width;

        if (viewCam instanceof OrthographicCamera) {
            near_height = ftop;
            near_width = near_height * ratio;
            far_height = ftop;
            far_width = far_height * ratio;
        } else {
            near_height = depthHeightRatio * near;
            near_width = near_height * ratio;
            far_height = depthHeightRatio * far;
            far_width = far_height * ratio;
        }

        Vector3 right = dir.crs(up).nor();

        Vector3 temp = new Vector3();
        temp = MathUtil.multVector3(dir, far).add(pos);//.set(dir).multLocal(far).addLocal(pos);
        Vector3 farCenter = temp.cpy();
        temp = MathUtil.multVector3(dir, near).add(pos);// .set(dir).multLocal(near).addLocal(pos);
        Vector3 nearCenter = temp.cpy();

        Vector3 nearUp = temp.set(MathUtil.multVector3(up, near_height)).cpy();//.mul(near_height).clone();
        Vector3 farUp = temp.set(MathUtil.multVector3(up, far_height)).cpy();
        Vector3 nearRight = temp.set(MathUtil.multVector3(up, near_width)).cpy();
        Vector3 farRight = temp.set(MathUtil.multVector3(up, far_width)).cpy();

        points[0] = (nearCenter).sub(nearUp).sub(nearRight);
        points[1] = (nearCenter).add(nearUp).sub(nearRight);
        points[2] = (nearCenter).add(nearUp).add(nearRight);
        points[3] = (nearCenter).sub(nearUp).add(nearRight);

        points[4] = (farCenter).sub(farUp).sub(farRight);
        points[5] = (farCenter).add(farUp).sub(farRight);
        points[6] = (farCenter).add(farUp).add(farRight);
        points[7] = (farCenter).sub(farUp).add(farRight);

        if (scale != 1.0f) {
            // find center of frustum
            Vector3 center = new Vector3();
            for (int i = 0; i < 8; i++) {
                center.add(points[i]);
            }
            center = MathUtil.divVector3(center, 8f);//center.divideLocal(8f);

            Vector3 cDir = new Vector3();
            for (int i = 0; i < 8; i++) {
                cDir.set(points[i]).sub(center);
                cDir = MathUtil.multVector3(cDir, scale - 1.0f);
                points[i].add(cDir);
            }
        }
    }
    public static MyBoundingBox bound;

    public static BoundingBox computeBoundForPoints(Vector3[] pts, Matrix4 mat) {
        Vector3 min = new Vector3();

        Vector3 max = new Vector3();
        Vector3 temp = new Vector3();
        for (int i = 0; i < pts.length; i++) {

            float w = multProj(mat, pts[i], temp);

            temp.x /= w;
            temp.y /= w;
            // Why was this commented out?
            temp.z /= w;

            min = MathUtil.minVector3(min, temp);// min.minLocal(temp);
            max = MathUtil.maxVector3(max, temp);
        }

        Vector3 center = MathUtil.multVector3(min.cpy().add(max), 0.5f);
        Vector3 extent = MathUtil.multVector3(max.cpy().sub(min), 0.5f);

        //Nehon 08/18/2010 : Added an offset to the extend to avoid banding artifacts when the frustum are aligned
        BoundingBox b = new BoundingBox();
        b.set(pts);
     //   b.ext(extent.x + 2.0f, extent.y + 2.0f, extent.z + 2.5f);
        //b.setCenter(center);
       // System.out.println(max);
        //bound = b;
        return b;
       // b.
        //return new BoundingBox(center, extent.x + 2.0f, extent.y + 2.0f, extent.z + 2.5f);

        //return new BoundingBox().set(pts);
    }

    public static float multProj(Matrix4 mat, Vector3 vec, Vector3 store) {
        float vx = vec.x, vy = vec.y, vz = vec.z;
        float[] val = mat.val;
        store = vec.cpy().mul(mat);
        String str = "";
        for (int i = 0; i< val.length; i++) {
            str += val[i] + ",";
        }
        System.out.println(store);
        // return val[12] * vx + val[13] * vy + val[14] * vz + val[15];
        store.x = val[0] * vx + val[1] * vy + val[2] * vz + val[3];
        store.y = val[4] * vx + val[5] * vy + val[6] * vz + val[7];
        store.z = val[8] * vx + val[9] * vy + val[10] * vz + val[11];
        return val[12] * vx + val[13] * vy + val[14] * vz + val[15];
    }

    Vector3 var1 = new Vector3();
    Vector3 var2 = new Vector3();

    public static void updateShadowCamera(Camera shadowCam, Vector3[] points) {
        boolean ortho = shadowCam instanceof OrthographicCamera;
        shadowCam.projection.setToOrtho(-1, 1, -1, 1, 1, -1);

        /*if (ortho) {
         shadowCam.(-1, 1, -1, 1, 1, -1);
         } else {
         shadowCam.setFrustumPerspective(45, 1, 1, 150);
         }*/
        shadowCam.update();
        Matrix4 viewProjMatrix = shadowCam.combined;
        Matrix4 projMatrix = shadowCam.projection;

        BoundingBox splitBB = computeBoundForPoints(points, viewProjMatrix);

        Vector3 splitMin = splitBB.min;
        Vector3 splitMax = splitBB.max;
        // System.out.println(splitBB.);
//        splitMin.z = 0;
        // Create the crop matrix.
        float scaleX, scaleY, scaleZ;
        float offsetX, offsetY, offsetZ;

        scaleX = 2.0f / (splitMax.x - splitMin.x);
        scaleY = 2.0f / (splitMax.y - splitMin.y);
        offsetX = -0.5f * (splitMax.x + splitMin.x) * scaleX;
        offsetY = -0.5f * (splitMax.y + splitMin.y) * scaleY;
        scaleZ = 1.0f / (splitMax.z - splitMin.z);
        offsetZ = -splitMin.z * scaleZ;

        Matrix4 cropMatrix = new Matrix4();
        float[] vals = new float[]{scaleX, 0f, 0f, offsetX,
            0f, scaleY, 0f, offsetY,
            0f, 0f, scaleZ, offsetZ,
            0f, 0f, 0f, 1f};
        cropMatrix.set(vals);

        Matrix4 result = new Matrix4();
        result.set(cropMatrix);
        result.mul(projMatrix);

        //
        shadowCam.projection.set(result);
        shadowCam.combined.set(shadowCam.projection.cpy().mul(shadowCam.view));
        //shadowCam.combined.set(shadowCam.view.cpy().mul(shadowCam.projection));
        // shadowCam.update();
        //  shadowCam.view.setToLookAt(shadowCam.position, shadowCam.position.cpy().add(shadowCam.direction), shadowCam.up);
        // Matrix4.mul(shadowCam.combined.val, shadowCam.view.val);
    }
}
