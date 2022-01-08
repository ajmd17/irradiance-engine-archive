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
import java.io.File;
import java.io.PrintWriter;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 *
 * @author Xoppa
 */
public class BRDFShader extends LightShader {

    Camera camera;
    RenderContext context;
    int u_projTrans;
    int u_worldTrans;
    Texture leaf, normalMap;
    public boolean flipY = true;
    public DepthTestMode depthTestMode = DepthTestMode.on;
    public BackfaceCullMode faceCullMode = BackfaceCullMode.back;
    String texPath, texPathNorm;
    public Pixmap[] data = new Pixmap[6];
    boolean envMapSetup = false;

    public BRDFShader() {

    }

    public void setupEnvMap(IRMat irm2) {
        if (!envMapSetup) {
            for (int i = 0; i < 6; i++) {
                this.data[i] = irm2.getPixmap(i);
            }
            EnvironmentMapper.setEnvironmentMap(data);
            envMapSetup = true;
        }
    }

    public void updateData(Pixmap[] data) {
        this.data = data;
        //   EnvironmentMapper.setEnvironmentMap(data);
        Gdx.gl.glBindTexture(GL20.GL_TEXTURE_CUBE_MAP, 0);
        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL20.GL_RGB, data[0].getWidth(), data[0].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[0].getPixels());
        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL20.GL_RGB, data[1].getWidth(), data[1].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[1].getPixels());

        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL20.GL_RGB, data[2].getWidth(), data[2].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[2].getPixels());
        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL20.GL_RGB, data[3].getWidth(), data[3].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[3].getPixels());

        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL20.GL_RGB, data[4].getWidth(), data[4].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[4].getPixels());
        Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL20.GL_RGB, data[5].getWidth(), data[5].getHeight(), 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, data[5].getPixels());

        Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR_MIPMAP_LINEAR);
        Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR_MIPMAP_LINEAR);
        Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
        Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_CUBE_MAP, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
        Gdx.gl.glGenerateMipmap(GL20.GL_TEXTURE_CUBE_MAP);
    }

    @Override
    public void init() {
        /* String vert = " attribute vec3 a_position; \n"
         + " attribute vec3 a_normal; \n"
         + " attribute vec2 a_texCoord0; \n"
         + " uniform mat4 u_worldTrans; \n"
         + " varying vec2 v_texCoord0; \n"
         + " varying vec3 v_cubeMapUV; \n"
         + " uniform mat4 u_projViewTrans;\n"
         + " uniform vec3 u_fresnel;\n"
         + " uniform vec3 u_lightDirection;\n"
         + " uniform vec3 u_cameraPosition;\n"
         + " varying vec4 norm;\n"
         + " varying vec4 wNorm;\n"
         + " varying vec4 refVec;\n"
         + " varying vec4 lightVec;\n"
         + " varying vec4 vert;\n"
         + " void computeRef(in vec4 modelSpacePos){\n"
         + "        vec3 worldPos = (u_worldTrans * modelSpacePos).xyz;\n"
         + "\n"
         + "        vec3 I = normalize( u_cameraPosition - worldPos  ).xyz;\n"
         + "        vec3 N = normalize( (u_worldTrans * vec4(a_normal, 0.0)).xyz );\n"
         + "\n"
         + "        refVec.xyz = -reflect(I, N);\n"
         + "        refVec.w   = u_fresnel.x + u_fresnel.y * pow(1.0 + dot(I, N), u_fresnel.z);\n"
         + "}\n"
         + ""
         + ""
         + " void main() { \n"
         + "     v_texCoord0 = a_texCoord0;     \n"
         + "     vec4 g_position = u_worldTrans * vec4(a_position, 1.0); \n"
         + "     v_cubeMapUV = normalize(a_position); \n"
         + "     computeRef(vec4(a_position, 1.0));\n"
         + "     wNorm = normalize(inverse(transpose(u_worldTrans))*vec4(a_normal, 0.0));\n"
         + "     norm = normalize(vec4(a_normal, 0.0));\n"
         + "     lightVec = normalize(vec4(u_lightDirection, 1.0));\n"
         + "     gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0); \n"
         + "     vert = vec4(u_cameraPosition, 1.0);\n"
         + "     //vert /= vert.w;\n"
         + " } \n";

         String frag = "#ifdef GL_ES \n"
         + " precision mediump float; \n"
         + " #endif \n"
         + " uniform samplerCube u_environmentCubemap; \n"
         + " varying vec2 v_texCoord0; \n"
         + " varying vec3 v_cubeMapUV; \n"
         + " varying vec4 refVec;\n"
         + " varying vec4 norm;\n"
         + " varying vec4 wNorm;\n"
         + " varying vec4 lightVec;\n"
         + " varying vec4 vert;\n"
         + " uniform float u_specularFactor;\n"
         + " uniform vec4 u_albedo;\n"
         + " uniform float u_roughness;\n"
         + " uniform sampler2D u_diffuseTexture;\n"
         + " uniform vec3 u_cameraPosition;\n"
         + "vec4 desaturate(vec4 color, float amount)\n"
         + "{\n"
         + "    vec4 gray = vec4(dot(vec4(0.2125,0.7154,0.0721, 1.0), color));\n"
         + "    return vec4(mix(color, gray, amount));\n"
         + "}\n"
         + " vec4 blurTexCube() {\n"
         + "   vec4 finTex;"
         + "   "
         + "   float dist = gl_FragCoord.z/gl_FragCoord.w;\n"
         + "   if (dist < 100.0) {\n"
         + "   for (int x = -2; x < 2; x++) {\n"
         + "     for (int y = -2; y < 2; y++) {\n"
         + "     //vec4 texCoord = mix(refVec, refVec + vec4(x*0.015*u_roughness, 0.0, y*0.01*u_roughness, 0.0), clamp(1.0-dist, 0.3, 0.7));\n"
         + "     vec4 texCoord = refVec + vec4(x*0.005, y*0.008, y*0.0011, 0.0);\n"
         + "     finTex += vec4(textureCube(u_environmentCubemap, texCoord).rgb, 1.0);\n"
         + "    }\n"
         + "   }\n"
         + "   finTex /= 16.0;\n"
         + "   return desaturate(finTex, clamp(u_roughness*2.0, 0.5, 1.0));"
         + " } else {\n"
         + "   return vec4(1.0);\n"
         + " }\n"
         + " }\n"
         + ""
         + ""
         + " void main() {      \n"
         + "   float roughness = u_roughness;\n"
         + "   gl_FragColor = u_albedo;\n"
         + "   #ifdef DIFFUSE_MAP\n"
         + "     gl_FragColor *= texture2D(u_diffuseTexture, v_texCoord0);\n"
         + "   #endif\n"
         + "   vec4 lighting;\n"
         + "   vec3 n = normalize(norm.xyz);\n"
         + "   vec3 l = normalize(-lightVec.xyz);\n"
         + "   vec3 v = normalize(vert.xyz);\n"
         + "   vec3 h = normalize(v+l);\n"
         + "   float nDotl = max(0.0, dot(n, l));"
         + "   float nDotv = max(0.0, dot(n, v));"
         + "   vec3 reflectDir = reflect(-lightVec, n);\n"
         + "   float specAngle = max(dot(reflectDir, v), 0.0);\n"
         + "   float specular = pow(dot(n, h), u_specularFactor);\n"
         + "   lighting = vec4(nDotl);\n"
         + "   #ifdef ENV_MAP\n"
         + "   vec4 reflection = blurTexCube()*1.0-roughness;\n"
         + "     gl_FragColor *= mix(reflection, lighting, roughness);   \n"
         + "   #endif\n"
         + "   #ifndef ENV_MAP\n"
         + "     gl_FragColor *= lighting + vec4(specular*(1.0-roughness));   \n"
         + "   #endif\n"
         + "   gl_FragColor += clamp(vec4(specular),0.0, 1.0);\n"
         + ""
         + ""
         + " } \n";
        
         try {
         PrintWriter vwriter = new PrintWriter("data/shaders/pbr/pbr.vert.glsl", "UTF-8");
         vwriter.print(vert);
         vwriter.close();
         } catch (Exception ex) {
         System.out.println(ex);
         }
         try {
         PrintWriter fwriter = new PrintWriter("data/shaders/pbr/pbr.frag.glsl", "UTF-8");
         fwriter.print(frag);
         fwriter.close();
         } catch (Exception ex) {
         System.out.println(ex);
         }
         */
        String vert = Gdx.files.internal("data/shaders/pbr/pbr.vert.glsl").readString();
        String frag = Gdx.files.internal("data/shaders/pbr/pbr.frag.glsl").readString();
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
    public void begin(Camera camera, RenderContext context
    ) {
        this.camera = camera;
        this.context = context;
        program.begin();
        context.setDepthTest(GL20.GL_LEQUAL);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        program.setUniformMatrix("u_viewTrans", camera.view);
        program.setUniformMatrix(u_projTrans, camera.combined);
        fresnel.set(0.0f, 0.0f, 0.98f);
        program.setUniformf("u_fresnel", fresnel);
        program.setUniformf("u_cameraPosition", camera.position);
        program.setUniformf("u_cameraDirection", camera.direction);
    }

    @Override
    public void render(Renderable renderable
    ) {
        Gdx.gl.glDepthMask(true);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        program.setUniformf("u_lightDirection", lightDirection);
        program.setUniformf("u_lightColor", lightColor);
        program.setUniformf("u_specularFactor", irmat.getFloat("specular"));
        program.setUniformf("u_albedo", irmat.getColor("albedo"));
        program.setUniformf("u_F0", irmat.getFloat("F0"));
        program.setUniformf("u_roughness", irmat.getFloat("roughness"));
        program.setUniformf("u_metallic", irmat.getFloat("metallic"));
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
        if (this.getBoolean("NOISE_MAP")) {
            program.setUniformf("u_grimeAmount", irmat.getFloat("grime"));
            Texture nTex = this.irmat.getTexture("noise");
            if (nTex != null) {
                nTex.bind(5);
                program.setUniformi("u_noiseMap", 5);
            }
        }
        if (this.getBoolean("NORMAL_MAP")) {
            Texture normalTex = this.irmat.getTexture("normal");
            if (normalTex != null) {
                normalTex.bind(1);
                program.setUniformi("u_normalMap", 1);
            }
        }
        //Gdx.gl.glBindTexture(GL20.GL_TEXTURE_CUBE_MAP, 0);
        if (this.getBoolean("ENV_MAP")) {
            if (EnvironmentMapper.probes.size() > 0) {

                if (EnvironmentMapper.probes.get(0) != null) {
                    if (EnvironmentMapper.probes.get(0).rendered) {
                        program.setUniformf("u_envMapPos", EnvironmentMapper.probes.get(0).position);
                        program.setUniformf("u_envMapBound", 10f);
                        EnvironmentMapper.probes.get(0).cubemap.bind(0);
                        program.setUniformi("u_environmentCubemap", 0);
                    }
                }

            } else {
                if (EnvironmentMapper.cubeMaps.size() > 0) {
                    if (EnvironmentMapper.cubeMaps.get(0) != null) {
                        program.setUniformf("u_envMapPos", Vector3.Zero);
                        program.setUniformf("u_envMapBound", 0f);
                        EnvironmentMapper.cubeMaps.get(0).bind(0);
                        program.setUniformi("u_environmentCubemap", 0);
                    }
                }
            }

        }
        super.render(renderable);
    }

    @Override
    public void end() {
        program.end();
    }

    @Override
    public int compareTo(Shader other
    ) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance
    ) {
        return true;
    }
}
