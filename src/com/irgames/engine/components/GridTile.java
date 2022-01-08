/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;



/**
 *
 * @author Andrew
 */
public class GridTile {
    public float width, length, x, z;
    public Vector2 center;
    public float maxDistance;
    public GridTile(float width, float length, float x, float z, float maxDistance) {
        this.width = width;
        this.length = length;
        this.x = x;
        this.z = z;
        this.maxDistance = maxDistance;
    }

    @Override
    public String toString() {
        return "GridTile: { X:" + x + " Z:" + z + " Width:" + width + " Length:" + length + " }";
    }
    public float getDistance(Vector3 point) {
        return new Vector2(point.x, point.z).dst(new Vector2(x, z));
    }
    public float distFromCenter(Vector3 cam) {
        Vector2 c = new Vector2(cam.x, cam.z);
        return center.dst(c);
    }
     public void setMaxDistance(float distance) {
        this.maxDistance = distance;
    }
     public boolean collides(Vector2 point, Vector2 target, float size) {
        if (Math.round(point.x) >= target.x - size && Math.round(point.x) <= target.x + size) {
            if (Math.round(point.y) >= target.y - size && Math.round(point.y) <= target.y + size) {
                return true;
            }
        }
        return false;
    }
    
    public boolean inRange(Vector3 point) {
        float dist = center.cpy().dst(new Vector2(point.x, point.z));
     
        return  dist < maxDistance;
    }
    public int getDir() {
        int dir = 0;
        if (x < 0 && z < 0) {
            dir = 0;
        } else if (x > 0 && z > 0) {
            dir = 2;
        }
        return dir;
    }
}
