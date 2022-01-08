/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.utils;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.components.IRSpatial;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class MeshUtils {
     public static Mesh meshFromData(float[] vertices, short[] indices) {
       
        Mesh mesh = new Mesh(true, vertices.length / 3, indices.length,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));

        mesh.setVertices(vertices);
        mesh.setIndices(indices);
       
        System.out.println("Num vertices: " + vertices.length);
        
        return mesh;
    }
    public static void optimize(IRNode inNode) {
        AbstractList<Mesh> meshes = new ArrayList<Mesh>();
        AbstractList<Matrix4> transforms = new ArrayList<Matrix4>();
        gatherMeshes(inNode, meshes);
        gatherTransforms(inNode, transforms);
        Mesh m = mergeMeshes(meshes, transforms);
        IRNode outNode = new IRNode();
        outNode.mesh = m;
        outNode.meshPartOffset = inNode.meshPartOffset;
        outNode.meshPartSize = m.getNumIndices();
        outNode.primitiveType = GL20.GL_TRIANGLES;
        inNode.detachAllChildren();
        inNode.attachChild(outNode);

    }

    public static void gatherMeshes(IRSpatial root, List<Mesh> outMeshes, List<Matrix4> outTransforms) {
        if (root.mesh != null && root.worldTransform != null) {
            outMeshes.add(root.mesh);
            outTransforms.add(root.worldTransform);
        }
        if (root instanceof IRNode) {
            IRNode irn = (IRNode) root;
            for (IRSpatial child : irn.getChildren()) {

                gatherMeshes(child, outMeshes, outTransforms);
            }
        }

    }

    public static void gatherMeshes(IRSpatial root, List<Mesh> outMeshes) {
        if (root.mesh != null && root.worldTransform != null) {
            outMeshes.add(root.mesh);

        }
        if (root instanceof IRNode) {
            IRNode irn = (IRNode) root;
            for (IRSpatial child : irn.getChildren()) {
                gatherMeshes(child, outMeshes);
            }
        }
    }

    public static void gatherTransforms(IRSpatial root, List<Matrix4> outTransforms) {
        //Matrix4 trn = new Matrix4();
        // trn.setTranslation(root.getWorldTranslation());
        if (root.mesh != null && root.worldTransform != null) {
            root.updateWorldMatrix();
            outTransforms.add(root.worldTransform);

        }
        if (root instanceof IRNode) {
            IRNode irn = (IRNode) root;
            for (IRSpatial child : irn.getChildren()) {
                    gatherTransforms(child, outTransforms);
            }
        }
    }

    public static Mesh mergeMeshes(AbstractList<Mesh> meshes, AbstractList<Matrix4> transformations) {
        if (meshes.size() == 0) {
            return null;
        }

        int vertexArrayTotalSize = 0;
        int indexArrayTotalSize = 0;

        VertexAttributes va = meshes.get(0).getVertexAttributes();
        int vaA[] = new int[va.size()];
        for (int i = 0; i < va.size(); i++) {
            vaA[i] = va.get(i).usage;
        }

        for (int i = 0; i < meshes.size(); i++) {
            Mesh mesh = meshes.get(i);
            if (mesh.getVertexAttributes().size() != va.size()) {
                meshes.set(i, copyMesh(mesh, true, false, vaA));
            }

            vertexArrayTotalSize += mesh.getNumVertices() * mesh.getVertexSize() / 4;
            indexArrayTotalSize += mesh.getNumIndices();
        }

        final float vertices[] = new float[vertexArrayTotalSize];
        final short indices[] = new short[indexArrayTotalSize];

        int indexOffset = 0;
        int vertexOffset = 0;
        int vertexSizeOffset = 0;
        int vertexSize = 0;

        for (int i = 0; i < meshes.size(); i++) {
            Mesh mesh = meshes.get(i);

            int numIndices = mesh.getNumIndices();
            int numVertices = mesh.getNumVertices();
            vertexSize = mesh.getVertexSize() / 4;
            int baseSize = numVertices * vertexSize;
            VertexAttribute posAttr = mesh.getVertexAttribute(Usage.Position);
            int offset = posAttr.offset / 4;
            int numComponents = posAttr.numComponents;

            if (mesh.getVertexSize() == 48) {
                System.out.println("Break");
            }

            { //uzupelnianie tablicy indeksow
                mesh.getIndices(indices, indexOffset);
                for (int c = indexOffset; c < (indexOffset + numIndices); c++) {
                    indices[c] += vertexOffset;
                }
                indexOffset += numIndices;
            }

            mesh.getVertices(0, baseSize, vertices, vertexSizeOffset);
            Mesh.transform(transformations.get(i), vertices, vertexSize, offset, numComponents, vertexOffset, numVertices);
            vertexOffset += numVertices;
            vertexSizeOffset += baseSize;
        }

        Mesh result = new Mesh(true, vertexOffset, indices.length, meshes.get(0).getVertexAttributes());
        result.setVertices(vertices);
        result.setIndices(indices);
        return result;
    }

    public static Mesh copyMesh(Mesh meshToCopy, boolean isStatic, boolean removeDuplicates, final int[] usage) {
        // TODO move this to a copy constructor?
        // TODO duplicate the buffers without double copying the data if possible.
        // TODO perhaps move this code to JNI if it turns out being too slow.
        final int vertexSize = meshToCopy.getVertexSize() / 4;
        int numVertices = meshToCopy.getNumVertices();
        float[] vertices = new float[numVertices * vertexSize];
        meshToCopy.getVertices(0, vertices.length, vertices);
        short[] checks = null;
        VertexAttribute[] attrs = null;
        int newVertexSize = 0;
        if (usage != null) {
            int size = 0;
            int as = 0;
            for (int i = 0; i < usage.length; i++) {
                if (meshToCopy.getVertexAttribute(usage[i]) != null) {
                    size += meshToCopy.getVertexAttribute(usage[i]).numComponents;
                    as++;
                }
            }
            if (size > 0) {
                attrs = new VertexAttribute[as];
                checks = new short[size];
                int idx = -1;
                int ai = -1;
                for (int i = 0; i < usage.length; i++) {
                    VertexAttribute a = meshToCopy.getVertexAttribute(usage[i]);
                    if (a == null) {
                        continue;
                    }
                    for (int j = 0; j < a.numComponents; j++) {
                        checks[++idx] = (short) (a.offset / 4 + j);
                    }
                    attrs[++ai] = new VertexAttribute(a.usage, a.numComponents, a.alias);
                    newVertexSize += a.numComponents;
                }
            }
        }
        if (checks == null) {
            checks = new short[vertexSize];
            for (short i = 0; i < vertexSize; i++) {
                checks[i] = i;
            }
            newVertexSize = vertexSize;
        }

        int numIndices = meshToCopy.getNumIndices();
        short[] indices = null;
        if (numIndices > 0) {
            indices = new short[numIndices];
            meshToCopy.getIndices(indices);
            if (removeDuplicates || newVertexSize != vertexSize) {
                float[] tmp = new float[vertices.length];
                int size = 0;
                for (int i = 0; i < numIndices; i++) {
                    final int idx1 = indices[i] * vertexSize;
                    short newIndex = -1;
                    if (removeDuplicates) {
                        for (short j = 0; j < size && newIndex < 0; j++) {
                            final int idx2 = j * newVertexSize;
                            boolean found = true;
                            for (int k = 0; k < checks.length && found; k++) {
                                if (tmp[idx2 + k] != vertices[idx1 + checks[k]]) {
                                    found = false;
                                }
                            }
                            if (found) {
                                newIndex = j;
                            }
                        }
                    }
                    if (newIndex > 0) {
                        indices[i] = newIndex;
                    } else {
                        final int idx = size * newVertexSize;
                        for (int j = 0; j < checks.length; j++) {
                            tmp[idx + j] = vertices[idx1 + checks[j]];
                        }
                        indices[i] = (short) size;
                        size++;
                    }
                }
                vertices = tmp;
                numVertices = size;
            }
        }

        Mesh result;
        if (attrs == null) {
            result = new Mesh(isStatic, numVertices, indices == null ? 0 : indices.length, meshToCopy.getVertexAttributes());
        } else {
            result = new Mesh(isStatic, numVertices, indices == null ? 0 : indices.length, attrs);
        }
        result.setVertices(vertices, 0, numVertices * newVertexSize);
        result.setIndices(indices);
        return result;
    }

}
