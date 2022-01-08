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
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;
import com.irgames.managers.LightingManager;
import com.irgames.utils.RenderUtils;
import com.irgames.utils.RenderUtils.BackfaceCullMode;
import com.irgames.utils.RenderUtils.DepthTestMode;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 *
 * @author Xoppa
 */
public class NewShadowShader2 extends LightShader {

    Camera camera;
    RenderContext context;
    int u_projTrans;
    int u_worldTrans;
    Texture leaf, normalMap;
    public boolean flipY = true;
    public DepthTestMode depthTestMode = DepthTestMode.on;
    public BackfaceCullMode faceCullMode = BackfaceCullMode.back;
    String texPath, texPathNorm;
    public float near, far;
    public NewShadowShader2() {

    }

    @Override
    public void init() {
        String vert = Gdx.files.internal("data/shaders/depth/shadow_vert.glsl").readString();
        String frag = Gdx.files.internal("data/shaders/depth/shadow_frag.glsl").readString();
        //this.setProperty("TURN_RED", false);
       // vert = this.format(vert);
        //frag = this.format(frag);
        program = new ShaderProgram(vert, frag);
        if (!program.isCompiled()) {
            throw new GdxRuntimeException(program.getLog());
        }
        u_projTrans = program.getUniformLocation("u_projViewTrans");
        u_worldTrans = program.getUniformLocation("u_worldTrans");
        program.pedantic = false;
        LightingManager.addShader(this);
    }

    @Override
    public void dispose() {
        program.dispose();
    }
    Matrix4 camComb;
    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        program.begin();
        context.setDepthTest(GL20.GL_LEQUAL);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);

        program.setUniformMatrix(u_projTrans, camera.combined);
        camComb = camera.combined;
        
        program.setUniformf("u_near", near);
        program.setUniformf("u_far", far);
        Gdx.gl20.glEnable(GL20.GL_POLYGON_OFFSET_FILL);
        Gdx.gl20.glPolygonOffset(2.f, 100.f);
        Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl20.glCullFace(GL20.GL_FRONT);
        //program.setUniformf("u_cameraPosition", camera.position);
    }

    @Override
    public void render(Renderable renderable) {
        super.preRender();
        
        
        
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        program.setUniformMatrix("u_projViewWorldTrans", camComb.cpy().mul(renderable.worldTransform));
      //  program.setUniformf("u_lightDirection", lightDirection);
        //    program.setUniformf("u_fogStart", fogStart);
        //    program.setUniformf("u_fogEnd", fogEnd);
        //   program.setUniformf("u_fogColor", fogColor);
        //   program.setUniformf("u_lightColor", lightColor);
        //   program.setUniformf("u_albedo", irmat.getColor("albedo"));
        /*program.setUniformf("u_alphaDiscard", this.alphaDiscard);
       
        if (this.properties.getPropertyValue("DIFFUSE_MAP")) {
            Texture diffuseTex = this.irmat.getTexture("diffuse");
            if (diffuseTex != null) {
                diffuseTex.bind(0);
                program.setUniformi("u_diffuseTexture", 0);
            }
        }
        Boolean FlipY = this.irmat.getBoolean("flip_y");
        if (FlipY != null) {
            if (FlipY == false) {
                program.setUniformi("u_flipY", 0);
            } else {
                program.setUniformi("u_flipY", 1);
            }
        }
        Boolean Discard = this.irmat.getBoolean("discard");
        if (Discard != null) {
            if (Discard == false) {
                program.setUniformi("u_discard", 0);
            } else if (Discard == true) {
                program.setUniformi("u_discard", 1);
            }
        } else {
            program.setUniformi("u_discard", 0);
        }*/
        //if (this.properties.getPropertyValue("NORMAL_MAP")) {
        //  Texture normalTex = this.irmat.getTexture("normal");
        //  if (normalTex != null) {
        //     normaslTex.bind(1);
        //    program.setUniformi("u_normalMap", 1);
        // }
        //}
        Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl20.glCullFace(GL20.GL_FRONT);
        renderable.mesh.bind(program);
        renderable.mesh.render(program,
                renderable.primitiveType,
                renderable.meshPartOffset,
                renderable.meshPartSize, false);
        Gdx.gl20.glDisable(GL20.GL_CULL_FACE);
    }

    @Override
    public void end() {
        Gdx.gl20.glDisable(GL20.GL_POLYGON_OFFSET_FILL);
        Gdx.gl20.glDisable(GL20.GL_CULL_FACE);
        program.end();
    }
    private final IntArray tempArray = new IntArray();
    private Attributes combinedAttributes = new Attributes();
    private final IntIntMap attributes = new IntIntMap();
    private final int[] getAttributeLocations (final VertexAttributes attrs) {
		tempArray.clear();
		final int n = attrs.size();
		for (int i = 0; i < n; i++) {
			tempArray.add(attributes.get(attrs.get(i).getKey(), -1));
		}
		return tempArray.items;
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
