/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.irgames.managers.LightingManager;
import com.irgames.utils.RenderUtils;
import com.irgames.utils.RenderUtils.BackfaceCullMode;
import com.irgames.utils.RenderUtils.DepthTestMode;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 *
 * @author Xoppa
 */
public class BlackShader extends LightShader {

    Camera camera;
    RenderContext context;
    int u_projTrans;
    int u_worldTrans;
    Texture leaf, normalMap;
    public boolean flipY = true;
    public DepthTestMode depthTestMode = DepthTestMode.on;
    public BackfaceCullMode faceCullMode = BackfaceCullMode.back;
    String texPath, texPathNorm;
    public boolean debugView = false;
    public Color color = Color.BLACK;
    public BlackShader() {

    }

    @Override
    public void init() {
        String vert;
        String frag;
        //this.setProperty("TURN_RED", false);
        vert = " attribute vec3 a_position; \n"
                + " attribute vec2 a_texCoord0; \n"
                + " uniform mat4 u_worldTrans;\n"
                + " uniform mat4 u_projViewTrans;\n"
                + " void main() { \n"
                + "     gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0); \n"
                + " } \n"
                + "";
        frag = "uniform vec4 u_color;\n"
                + "void main() {\n"
                + ""
                + "gl_FragColor = u_color;\n"
                + "}\n";
        program = new ShaderProgram(vert, frag);
        if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
        }
        u_projTrans = program.getUniformLocation("u_projViewTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
        program.pedantic = false;
    }

    @Override
    public void dispose() {
        program.dispose();
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        program.begin();
        context.setDepthTest(GL20.GL_LEQUAL);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);

        program.setUniformMatrix("u_projViewTrans", camera.combined);
        program.setUniformf("u_cameraPosition", camera.position);
    }

    @Override
    public void render(Renderable renderable) {
        super.preRender();
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        
        if (color == Color.BLACK) {
        Gdx.gl.glDepthMask(true);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        } else if (color == Color.WHITE){
            Gdx.gl.glDepthMask(false);
            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        }
       
        program.setUniformf("u_color", color);
        super.render(renderable);
        Gdx.gl.glDepthMask(true);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    @Override
    public void end() {
        program.end();
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return true;
    }
}
