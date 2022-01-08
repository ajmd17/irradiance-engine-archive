/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.utils;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.IRSpatial;
import com.irgames.engine.controls.RigidBodyControl;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Andrew
 */
public class MathUtil {

    public static IRSpatial getClosestObject(List<IRSpatial> list, Vector3 location) {
        float closestDistance = -1;
        IRSpatial p = null;
        for (IRSpatial object : list) {
            float thisDistance = object.updateWorldTranslation().dst(location);
            if (thisDistance < closestDistance || closestDistance == -1) {
                closestDistance = thisDistance;
                p = (IRSpatial) object;
            }
        }
        return p;
    }

    public static RigidBodyControl getClosestRBC(List<RigidBodyControl> list, Vector3 location) {
        float closestDistance = -1;
        RigidBodyControl p = null;
        for (RigidBodyControl object : list) {
            float thisDistance = object.parent.updateWorldTranslation().dst(location);
            if (thisDistance < closestDistance || closestDistance == -1) {
                closestDistance = thisDistance;
                p = (RigidBodyControl) object;
            }
        }
        return p;
    }

    public static Vector3 round(Vector3 vec) {
        return new Vector3(Math.round(vec.x), Math.round(vec.y), Math.round(vec.z));
    }

    public static float modulo(float fValue, float modulus) {

        float result = (fValue % modulus + modulus) % modulus;

        assert result >= 0f : result;
        assert result < modulus : result;
        return result;
    }

    /**
     * Compute the least non-negative value congruent with a double-precision
     * value with respect to the specified modulus.
     *
     * @param dValue input value
     * @param modulus (&gt;0)
     * @return x MOD modulus (&lt;modulus, &ge;0)
     */
    public static double modulo(double dValue, double modulus) {

        double result = (dValue % modulus + modulus) % modulus;

        assert result >= 0.0 : result;
        assert result < modulus : result;
        return result;
    }

    public static int modulo(int iValue, int modulus) {

        int result = (iValue % modulus + modulus) % modulus;

        assert result >= 0f : result;
        assert result < modulus : result;
        return result;
    }

    public static double distance(Vector3 point1, Vector3 point2) {
        return Math.sqrt(((point1.x - point2.x) * (point1.x - point2.x))
                + ((point1.y - point2.y) * (point1.y - point2.y))
                + ((point1.z - point2.z) * (point1.z - point2.z)));
    }

    public static double distance(Vector2 point1, Vector2 point2) {
        return Math.sqrt(((point1.x - point2.x) * (point1.x - point2.x))
                + ((point1.y - point2.y) * (point1.y - point2.y)));
    }

    public static Vector3 minVector3(Vector3 value1, Vector3 value2) {
        return new Vector3(
                Math.min(value1.x, value2.x),
                Math.min(value1.y, value2.y),
                Math.min(value1.z, value2.z));
    }

    public static Vector3 maxVector3(Vector3 value1, Vector3 value2) {
        return new Vector3(
                Math.max(value1.x, value2.x),
                Math.max(value1.y, value2.y),
                Math.max(value1.z, value2.z));
    }

    public static Vector3 divVector3(Vector3 src, float multiplier) {
        return new Vector3(src.x / multiplier, src.y / multiplier, src.z / multiplier);
    }

    public static Vector3 multVector3(Vector3 src, float multiplier) {
        return new Vector3(src.x * multiplier, src.y * multiplier, src.z * multiplier);
    }

    public static Vector3 multVector3(Vector3 src, Vector3 multiplier) {
        return new Vector3(src.x * multiplier.x, src.y * multiplier.y, src.z * multiplier.z);
    }

    public static Quaternion subQuaternion(Quaternion src, Quaternion other) {
        Quaternion newQuat = new Quaternion(src.x - other.x, src.y - other.y, src.z - other.z, src.w - other.w);
        return newQuat;
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
    static Random rand = new Random(666);

    public static int randInt(int min, int max) {
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public static Vector3 lerpVector3(Vector3 a, Vector3 b, float v) {
        return new Vector3(lerp(a.x, b.x, v), lerp(a.y, b.y, v), lerp(a.z, b.z, v));
    }

    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public static double randomInRange(double min, double max) {
        double range = max - min;
        double scaled = rand.nextDouble() * range;
        double shifted = scaled + min;
        return shifted; // == (rand.nextDouble() * (max-min)) + min;
    }
}
