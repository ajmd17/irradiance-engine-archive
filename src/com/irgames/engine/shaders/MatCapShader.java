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
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.irgames.engine.components.IRMat;
import com.irgames.managers.EnvironmentMapper;
import com.irgames.managers.LightingManager;
import com.irgames.utils.RenderUtils;
import com.irgames.utils.RenderUtils.BackfaceCullMode;
import com.irgames.utils.RenderUtils.DepthTestMode;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 *
 * @author Xoppa
 */
public class MatCapShader extends LightShader {

    
    Camera camera;
    RenderContext context;
    int u_projTrans;
    int u_worldTrans;
    Texture leaf, normalMap;
    public boolean flipY = true;
    public DepthTestMode depthTestMode = DepthTestMode.on;
    public BackfaceCullMode faceCullMode = BackfaceCullMode.back;
    String texPath, texPathNorm;

    public MatCapShader() {

    }

    @Override
    public void init() {
        String vert = " attribute vec3 a_position; \n"
                + " attribute vec3 a_normal; \n"
                + " attribute vec2 a_texCoord0; \n"
                + " uniform mat4 u_worldTrans; \n"
                + " varying vec2 v_texCoord0; \n"
                + " varying vec3 v_cubeMapUV; \n"
                + " uniform mat4 u_projViewTrans;\n"
                + " uniform vec3 u_fresnel;\n"
                + " varying vec4 v_position;\n"
                + " uniform vec3 u_lightDirection;\n"
                + " uniform vec3 u_cameraPosition;\n"
                + " varying vec4 norm;\n"
                + " varying vec4 refVec;\n"
                + " varying vec4 lightVec;\n"
                + " varying vec4 vert;\n"
                + ""
                + ""
                + " void main() { \n"
                + "     v_texCoord0 = a_texCoord0;     \n"
                + "     vec4 g_position = u_worldTrans * vec4(a_position, 1.0); \n"
                + "     v_cubeMapUV = normalize(a_position); \n"
                + "     //computeRef(vec4(a_position, 1.0));\n"
                + "     norm = vec4(a_normal, 0.0);\n"
                + "     lightVec = vec4(u_lightDirection, 1.0)*u_worldTrans;\n"
                + "     gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0); \n"
                + "     vert = u_worldTrans * vec4(a_position, 1.0);\n"
           
                + "     v_position = vec4(a_position, 1.0);\n"
                + " } \n";

        String frag = "#ifdef GL_ES \n"
                + " precision mediump float; \n"
                + " #endif \n"
                + " varying vec2 v_texCoord0; \n"
                + " varying vec3 v_cubeMapUV; \n"
                + " varying vec4 refVec;\n"
                + " varying vec4 norm;\n"
                + " varying vec4 lightVec;\n"
                + " varying vec4 vert;\n"
                + " uniform float u_specularFactor;\n"
                + " uniform vec4 u_albedo;\n"
                + " uniform vec3 u_fresnel;\n"
                + " uniform float u_roughness;\n"
                + " uniform sampler2D u_matCapTexture;\n"
                + " uniform sampler2D u_diffuseTexture;\n"
                + " uniform vec3 u_cameraPosition;\n"
                + " varying vec4 v_position;\n"
                + " vec4 ref;\n"
                + " void computeRef(in vec4 modelSpacePos){\n"
                + "        vec3 worldPos = vert.xyz;\n"
                + "\n"
                + "        vec3 I = normalize( u_cameraPosition - worldPos  ).xyz;\n"
                + "        vec3 N = normalize( norm).xyz ;\n"
                + "\n"
                + "        ref.xyz = -reflect(I, N);\n"
                + "        ref.w   = u_fresnel.x + u_fresnel.y * pow(1.0 + dot(I, N), u_fresnel.z);\n"
                + "}\n"
                + "vec2 Optics_SphereCoord(in vec3 dir){\n"
                + "    float dzplus1 = dir.z + 1.0;\n"
                + "\n"
                + "    // compute 1/2p\n"
                + "    // NOTE: this simplification only works if dir is normalized.\n"
                + "    float inv_two_p = 1.414 * sqrt(dzplus1);\n"
                + "    //float inv_two_p = sqrt(dir.x * dir.x + dir.y * dir.y + dzplus1 * dzplus1);\n"
                + "    inv_two_p *= 2.0;\n"
                + "    inv_two_p = 1.0 / inv_two_p;\n"
                + "\n"
                + "    // compute texcoord\n"
                + "    return (dir.xy * vec2(inv_two_p)) + vec2(0.5);\n"
                + "}\n"
                + ""
                + " void main() {      \n"
                + "   float roughness = u_roughness;\n"
                + "   gl_FragColor = vec4(1.0);//u_albedo;\n"
                + "   #ifdef DIFFUSE_MAP\n"
                + "     gl_FragColor = texture2D(u_diffuseTexture, v_texCoord0);\n"
                + "   #endif\n"
                + "   vec4 lighting;\n"
                + "   vec3 n = normalize(norm.xyz);\n"
                + "   vec3 l = normalize(-lightVec.xyz);\n"
                + "   vec3 v = normalize(vert.xyz);\n"
                + "   vec3 h = normalize(n-l);\n"
                + "   float nDotl = max(0.0, dot(n, l));"
                + "   float nDotv = max(0.0, dot(n, v));"
                + "   vec3 reflectDir = reflect(lightVec, n);\n"
                + "   float specAngle = max(dot(reflectDir, v), 0.0);\n"
                + "   float specular = pow(dot(n, h), u_specularFactor);\n"
                + "   lighting = vec4(nDotl);\n"
                + "   computeRef(v_position);\n"
                + "   #ifdef DIFFUSE_MAP\n"
                + "   gl_FragColor *= texture2D(u_matCapTexture, Optics_SphereCoord(normalize(ref)));\n"
                + "   #endif\n"
                + "   #ifndef DIFFUSE_MAP\n"
                + "   gl_FragColor = texture2D(u_matCapTexture, Optics_SphereCoord(normalize(ref)));\n"
                + "   #endif\n"
                + ""
                + " } \n";
        vert = this.format(vert);
        frag = this.format(frag);
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
    Vector3 fresnel = new Vector3();

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        program.begin();
        context.setDepthTest(GL20.GL_LEQUAL);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);

        program.setUniformMatrix(u_projTrans, camera.combined);
        fresnel.set(0.5f, 0.8f, 0.11f);
        program.setUniformf("u_fresnel", fresnel);
        program.setUniformf("u_cameraPosition", camera.position);
        program.setUniformMatrix("u_viewTrans", camera.view);
    }

    @Override
    public void render(Renderable renderable) {
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        program.setUniformf("u_lightDirection", lightDirection);

        //program.setUniformf("u_specularFactor", irmat.getFloat("specular"));
        //program.setUniformf("u_albedo", irmat.getColor("albedo"));
        //program.setUniformf("u_roughness", irmat.getFloat("roughness"));
        // program.setUniformf("u_fogStart", fogStart);
        //program.setUniformf("u_fogEnd", fogEnd);
        // program.setUniformf("u_fogColor", fogColor);
        //  program.setUniformf("u_lightColor", lightColor);
        if (this.getBoolean("DIFFUSE_MAP")) {
            Texture diffuseTex = this.irmat.getTexture("diffuse");
            if (diffuseTex != null) {
                diffuseTex.bind(4);
                program.setUniformi("u_diffuseTexture", 4);
            }
        }
        Texture mcTex = this.irmat.getTexture("matcap");
        // if (mcTex != null) {
        mcTex.bind(8);
        program.setUniformi("u_matCapTexture", 8);
        // }
        /*if (this.properties.getPropertyValue("NORMAL_MAP")) {
         Texture normalTex = this.irmat.getTexture("normal");
         if (normalTex != null) {
         normalTex.bind(1);
         program.setUniformi("u_normalMap", 1);
         }
         }*/

        super.render(renderable);
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
