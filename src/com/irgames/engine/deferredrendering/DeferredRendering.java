package com.irgames.engine.deferredrendering;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.deferredrendering.DeferredRenderManager;
import com.irgames.engine.shaders.post.PostFilter;
import com.irgames.managers.DepthTextureManager;

/**
 *
 * @author Andrew
 */
public class DeferredRendering extends PostFilter {

    Texture normalMap, diffuseMap;
    public Vector3 lightDirection = new Vector3(-0.8f, -1, 0);

    @Override
    public void onRender(Camera cam) {
        normalMap = DeferredRenderManager.getNormalMap();
        diffuseMap = DeferredRenderManager.getDiffuseMap();
        DepthTextureManager.depthTex.bind(8);
        shaderProgram.setUniformi("u_depthMap", 8);
        if (normalMap != null && diffuseMap != null) {
            normalMap.bind(9);
            shaderProgram.setUniformi("u_normalMap", 9);
            diffuseMap.bind(10);
            shaderProgram.setUniformi("u_diffuseMap", 10);
        }
        shaderProgram.setUniformf("u_lightDirection", lightDirection);
        shaderProgram.setUniformMatrix("u_invProjView", cam.invProjectionView);
        shaderProgram.setUniformMatrix("u_projViewMat", cam.combined);
        shaderProgram.setUniformMatrix("u_viewMatrix", cam.view);
    }

    public DeferredRendering() {
        this.createShader();
    }

    private void createShader() {
        String vertexShader = "attribute vec4 a_Position;    \n"
                + "attribute vec4 a_Color;\n"
                + "attribute vec2 a_texCoords;\n"
                + "varying vec4 v_Color;"
                + "varying vec2 v_texCoords; \n"
                + "void main()                  \n"
                + "{                            \n"
                + "   v_Color = a_Color;"
                + "   v_texCoords = a_texCoords;\n"
                + "   gl_Position =   a_Position;  \n"
                + "}                            \n";
        String fragmentShader = ""
                + "precision mediump float;\n"
                + "varying vec4 v_Color;\n"
                + "varying vec2 v_texCoords; \n"
                + "uniform sampler2D u_depthMap;\n"
                + "uniform sampler2D u_normalMap;\n"
                + "uniform sampler2D u_diffuseMap;\n"
                + "uniform mat4 u_invProjView;\n"
                + "uniform vec2 frustCorner;\n"
                + "uniform vec3 u_lightDirection;\n"
                + "uniform mat4 u_projViewMat;\n"
                + "uniform mat4 u_viewMatrix;\n"
                + "float unpack_depth(const in vec4 rgba_depth){\n"
                + "    const vec4 bit_shift =\n"
                + "        vec4(1.0/(256.0*256.0*256.0)\n"
                + "            , 1.0/(256.0*256.0)\n"
                + "            , 1.0/256.0\n"
                + "            , 1.0);\n"
                + "    float depth = dot(rgba_depth, bit_shift);\n"
                + "    return depth;\n"
                + "}\n"
                + "vec3 getPosition(vec2 uv, float depth) {\n"
                + "     vec4 pos = vec4(uv.x, uv.y, depth, 1.0) * 2.0 - 1.0;\n"
                + "     pos = u_invProjView * pos;\n"
                + "     pos = pos/pos.w;\n"
                + ""
                + ""
                + "     return pos.xyz;\n"
                + "}\n"
                + "vec3 getNormal(vec2 uv, float tex) {\n"
                + "     vec4 pos = vec4(uv.x * 2.0 - 1.0, (uv.y * 2.0 - 1.0), tex * 2.0 - 1.0, 1.0);\n"
                + "     "
                + ""
                + "     return pos.xyz;\n"
                + "}\n"
                + "vec3 decode (vec4 enc)\n"
                + "{\n"
                + "    float scale = 1.7777;\n"
                + "    vec3 nn =\n"
                + "        enc.xyz*vec3(2.*scale,2.*scale,0.) +\n"
                + "        vec3(-scale,-scale,1.);\n"
                + "    float g = 2.0 / dot(nn.xyz,nn.xyz);\n"
                + "    vec3 n;\n"
                + "    n.xy = g*nn.xy;\n"
                + "    n.z = g-1.;\n"
                + "    return n;\n"
                + "}"
                + "void main()                                  \n"
                + "{                                            \n"
                + "  vec4 finalSum;\n"
                + "  vec4 lighting;\n"
                + "  vec3 normal = normalize(decode(texture2D(u_normalMap, v_texCoords)));\n"
                + "  vec3 diffuse = texture2D(u_diffuseMap, v_texCoords).rgb;\n"
                + "  float depth = unpack_depth(texture2D(u_depthMap, v_texCoords));\n"
                + "  gl_FragColor = vec4(diffuse, 1.0);\n"
                + "  finalSum = vec4(diffuse, 1.0);\n"
                + "  float numLights = 4.0;\n"
                + "  vec3 position = getPosition(v_texCoords, depth);\n"
                + "  vec4 lightDir = normalize(vec4(u_lightDirection, 1.0));\n"
                + "  float nDotl = dot(normal, -lightDir.xyz);\n"
                + "  lighting += vec4(nDotl);\n"
                /*      + "  for (int i = -3; i < 3; i++) {\n"
                 + "     vec3 light = vec3(i*25.0,2.0,i*25.0);\n"
                 + "     vec3 lightDir = normalize(light-position);\n"
                 + "     float nDotl = dot(normal, normalize(position));\n"
                 + "     lighting += vec4(nDotl);\n"
                 + "  }\n"
                 */
                + "  finalSum = clamp(lighting, 0.0, 1.0);\n"
                + "  if (depth < 1.0) {\n"
                + "     gl_FragColor *= vec4(finalSum);\n"
                + "  }\n"
                + "}";

        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (shaderProgram.isCompiled() == false) {
            throw new IllegalStateException(shaderProgram.getLog());
        }
    }

}
