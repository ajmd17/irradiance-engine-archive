/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.utils;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 *
 * @author Andrew
 */
public class BufferUtils {
    public static Vector3[] getVector3Array(FloatBuffer buff) {
        buff.clear();
        Vector3[] verts = new Vector3[buff.limit() / 3];
        System.out.println(verts.length);
        for (int x = 0; x < verts.length; x++) {
            Vector3 v = new Vector3(buff.get(), buff.get(), buff.get());
            verts[x] = v;
        }
        return verts;
    }
    public static FloatBuffer createFloatBuffer(Vector3... data) {
        if (data == null) {
            return null;
        }
        FloatBuffer buff = com.badlogic.gdx.utils.BufferUtils.newFloatBuffer(3 * data.length);
        for (int x = 0; x < data.length; x++) {
            if (data[x] != null) {
                buff.put(data[x].x).put(data[x].y).put(data[x].z);
            } else {
                buff.put(0).put(0).put(0);
            }
        }
        buff.flip();
        return buff;
    }
    public static float[] getFloatArray(FloatBuffer buff) {
        if (buff == null) {
            return null;
        }
        buff.clear();
        float[] inds = new float[buff.limit()];
        for (int x = 0; x < inds.length; x++) {
            inds[x] = buff.get();
        }
        return inds;
    }
    public static FloatBuffer createFloatBuffer(Quaternion... data) {
        if (data == null) {
            return null;
        }
        FloatBuffer buff = createFloatBuffer(4 * data.length);
        for (int x = 0; x < data.length; x++) {
            if (data[x] != null) {
                buff.put(data[x].x).put(data[x].y).put(data[x].z).put(data[x].w);
            } else {
                buff.put(0).put(0).put(0);
            }
        }
        buff.flip();
        return buff;
    }
    public static FloatBuffer createFloatBuffer(int size) {
        FloatBuffer buf = ByteBuffer.allocateDirect(4 * size).order(ByteOrder.nativeOrder()).asFloatBuffer();
        buf.clear();
        onBufferAllocated(buf);
        return buf;
    }
    /**
     * Generate a new FloatBuffer using the given array of float primitives.
     * @param data array of float primitives to place into a new FloatBuffer
     */
    public static FloatBuffer createFloatBuffer(float... data) {
        if (data == null) {
            return null;
        }
        FloatBuffer buff = createFloatBuffer(data.length);
        buff.clear();
        buff.put(data);
        buff.flip();
        return buff;
    }
    private static void onBufferAllocated(Buffer buffer){
        /*
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        int initialIndex = 0;
        
        for (int i = 0; i < stackTrace.length; i++){
            if (!stackTrace[i].getClassName().equals(BufferUtils.class.getName())){
                initialIndex = i;
                break;
            }
        }
        
        int allocated = buffer.capacity();
        int size = 0;
    
        if (buffer instanceof FloatBuffer){
            size = 4;
        }else if (buffer instanceof ShortBuffer){
            size = 2;
        }else if (buffer instanceof ByteBuffer){
            size = 1;
        }else if (buffer instanceof IntBuffer){
            size = 4;
        }else if (buffer instanceof DoubleBuffer){
            size = 8;
        }
        
        allocated *= size;
        
        for (int i = initialIndex; i < stackTrace.length; i++){
            StackTraceElement element = stackTrace[i];
            if (element.getClassName().startsWith("java")){
                break;
            }
            
            try {
                Class clazz = Class.forName(element.getClassName());
                if (i == initialIndex){
                    System.out.println(clazz.getSimpleName()+"."+element.getMethodName()+"():" + element.getLineNumber() + " allocated " + allocated);
                }else{
                    System.out.println(" at " + clazz.getSimpleName()+"."+element.getMethodName()+"()");
                }
            } catch (ClassNotFoundException ex) {
            }
        }*/
        
        
    }
}
