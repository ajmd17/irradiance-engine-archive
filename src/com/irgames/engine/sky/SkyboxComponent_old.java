/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.sky;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.GameComponent;
import com.irgames.engine.components.IRNode;

/**
 *
 * @author Andrew
 */
public class SkyboxComponent_old extends GameComponent {

    protected final Pixmap[] data = new Pixmap[6];
    protected ShaderProgram shader;

    protected int u_worldTrans;
    protected Mesh quad;
    protected String vertexShader = " attribute vec3 a_position; \n"
            + " attribute vec3 a_normal; \n"
            + " attribute vec2 a_texCoord0; \n"
            + " uniform mat4 u_worldTrans; \n"
            + " varying vec2 v_texCoord0; \n"
            + " varying vec3 v_cubeMapUV; \n"
            + " void main() { \n"
            + "     v_texCoord0 = a_texCoord0;     \n"
            + "     vec4 g_position = u_worldTrans * vec4(a_position, 1.0); \n"
            + "     v_cubeMapUV = normalize(g_position.xyz); \n"
            + "     gl_Position = vec4(a_position, 1.0); \n"
            + " } \n";

    protected String fragmentShader = "#ifdef GL_ES \n"
            + " precision mediump float; \n"
            + " #endif \n"
            + " uniform samplerCube u_environmentCubemap; \n"
            + " varying vec2 v_texCoord0; \n"
            + " varying vec3 v_cubeMapUV; \n"
            + " void main() {      \n"
            + "   gl_FragColor = vec4(textureCube(u_environmentCubemap, v_cubeMapUV).rgb, 1.0);   \n"
            + " } \n";

    public String getDefaultVertexShader() {
        return vertexShader;
    }
    SkyBoxShader sbshader;

    public String getDefaultFragmentShader() {
        return fragmentShader;
    }
    IRNode skybox;

    @Override
    public void init() {
        shader = new ShaderProgram(vertexShader, fragmentShader);
        if (!shader.isCompiled()) {
            throw new GdxRuntimeException(shader.getLog());
        }

        u_worldTrans = shader.getUniformLocation("u_worldTrans");

        quad = createQuad();

        worldTrans = new Matrix4();
        fakeCam = new Matrix4();
        fakeCam.setTranslation(0, 0, 0f);
    }

    public SkyboxComponent_old(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ) {
        data[0] = positiveX;
        data[1] = negativeX;

        data[2] = positiveY;
        data[3] = negativeY;

        data[4] = positiveZ;
        data[5] = negativeZ;

        //String vert = Gdx.files.internal("shaders/cubemap.vertex.glsl").readString();
        //String frag = Gdx.files.internal("shaders/cubemap.fragment.glsl").readString();
    }

    public SkyboxComponent_old(FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ, FileHandle negativeZ) {
        this(new Pixmap(positiveX), new Pixmap(negativeX), new Pixmap(positiveY), new Pixmap(negativeY), new Pixmap(positiveZ), new Pixmap(negativeZ));
    }

    Matrix4 worldTrans, fakeCam;

    @Override
    public void update() {

    }
    private Quaternion tempQ = new Quaternion();
    float globalTime;

    @Override
    public void render(Camera camm, RenderContext ctxx) {
        globalTime += Gdx.graphics.getDeltaTime();
        tempQ.set(Vector3.Y, globalTime);
        render(tempQ);

    }
    float updateTime = Float.MAX_VALUE;
    float maxUpdateTime = 0.5f;

    public void render(Quaternion quaternion) {

        worldTrans.idt();
        cam.view.getRotation(quaternion, true);
        quaternion.conjugate();
        worldTrans.rotate(quaternion);
        shader.begin();
        shader.setUniformMatrix(u_worldTrans, worldTrans.cpy().mul(fakeCam));

        //bind cubemap
        Gdx.gl20.glBindTexture(GL20.GL_TEXTURE_CUBE_MAP, 0);
        Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL20.GL_RGB, data[0].getWidth(), data[0].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[0].getPixels());
        Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL20.GL_RGB, data[1].getWidth(), data[1].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[1].getPixels());

        Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL20.GL_RGB, data[2].getWidth(), data[2].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[2].getPixels());
        Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL20.GL_RGB, data[3].getWidth(), data[3].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[3].getPixels());

        Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL20.GL_RGB, data[4].getWidth(), data[4].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[4].getPixels());
        Gdx.gl20.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL20.GL_RGB, data[5].getWidth(), data[5].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[5].getPixels());

        Gdx.gl20.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
        Gdx.gl20.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
        Gdx.gl20.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
        Gdx.gl20.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);

        //Gdx.gl20.glGenerateMipmap(GL20.GL_TEXTURE_CUBE_MAP);
        quad.render(shader, GL20.GL_TRIANGLES);

        shader.end();
    }

    public Mesh createQuad() {
        Mesh mesh = new Mesh(true, 4, 6, VertexAttribute.Position(), VertexAttribute.ColorUnpacked(), VertexAttribute.TexCoords(0));
        mesh.setVertices(new float[]{-1f, -1f, 0, 1, 1, 1, 1, 0, 1,
            1f, -1f, 0, 1, 1, 1, 1, 1, 1,
            1f, 1f, 0, 1, 1, 1, 1, 1, 0,
            -1f, 1f, 0, 1, 1, 1, 1, 0, 0});
        mesh.setIndices(new short[]{0, 1, 2, 2, 3, 0});

        return mesh;
    }

}
