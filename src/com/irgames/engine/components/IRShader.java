/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.IRMat;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.engine.shaders.components.ShaderProperty;
import com.irgames.engine.stats.Stats;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.RenderUtils.BackfaceCullMode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class IRShader implements Shader, IRObject {

    public static boolean wireframe = false;
    public ShaderProgram program;
    public BackfaceCullMode cullMode = BackfaceCullMode.back;
    protected float alphaDiscard = 0.0f;
    protected List<Texture> textures = new ArrayList<>();
    protected IRMat irmat;
    public IRShader depthShader;
    public boolean updateNeeded = false;
    public IRShader getDepthCounterpart() {
        return depthShader;
    }

    public IRShader() {

    }

    //List<ShaderProperty> shaderProperties = new ArrayList<>();
    //public ShaderProperties properties = new ShaderProperties();
    public static void EnableWireframe() {
        wireframe = true;
    }

    public static void DisableWireframe() {
        wireframe = false;
    }

    public void applyMaterial(IRMat mat) {
        this.cullMode = mat.cullMode;
        this.alphaDiscard = mat.alphaDiscard;
        this.textures = mat.textures;
        this.irmat = mat;
    }

    protected String format(String shaderText) {
        String[] lines = shaderText.split("\n");
        boolean inIfStatement = false;
        String ifStatementText = "";
        boolean removing = false;
        String finalString = "";
        for (int i = 0; i < lines.length; i++) {

            if (lines[i].trim().startsWith("#ifdef")) {
                inIfStatement = true;
                ifStatementText = lines[i].trim().substring(7);
                if (getBoolean(ifStatementText) != null) {
                    if (getBoolean(ifStatementText) == false) {
                        removing = true;
                    }
                } else {
                    removing = true;
                }
                lines[i] = "";

            } else if (lines[i].trim().startsWith("#ifndef")) {
                inIfStatement = true;
                ifStatementText = lines[i].trim().substring(8);
                if (getBoolean(ifStatementText) == null) {
                    removing = false;
                } else {
                    if (getBoolean(ifStatementText) == true) {
                        removing = true;
                    }

                }
                lines[i] = "";

            } else if (lines[i].trim().startsWith("#endif")) {
                if (inIfStatement) {
                    inIfStatement = false;
                    removing = false;
                }
                lines[i] = "";
            }
            if (inIfStatement && removing) {
                lines[i] = "";
            }
            finalString += lines[i] + "\n";
        }
        return finalString;
    }

    public void preCompile(String vertShader, String fragShader) {
        vertShader = format(vertShader);
        fragShader = format(fragShader);
    }

    public void preRender() {
        if (cullMode != BackfaceCullMode.off) {
            Gdx.gl.glEnable(GL20.GL_CULL_FACE);
            if (cullMode == BackfaceCullMode.back) {
                Gdx.gl.glCullFace(GL20.GL_BACK);
            } else if (cullMode == BackfaceCullMode.front) {
                Gdx.gl.glCullFace(GL20.GL_FRONT);
            } else if (cullMode == BackfaceCullMode.both) {
                Gdx.gl.glCullFace(GL20.GL_FRONT_AND_BACK);
            }
        } else {
            Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        }
    }

    @Override
    public void init() {

    }

    @Override
    public int compareTo(Shader shader) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable rndrbl) {
        return true;
    }

    @Override
    public void begin(Camera camera, RenderContext rc) {

    }

    @Override
    public void render(Renderable renderable) {

        int primType = renderable.primitiveType;
        if (wireframe) {
            primType = GL20.GL_LINES;
        }
        Stats.NUM_VERTICES += renderable.mesh.getNumVertices();
        
        renderable.mesh.render(program,
                primType,
                renderable.meshPartOffset,
                renderable.meshPartSize);
    }

    @Override
    public void end() {

    }

    @Override
    public void dispose() {

    }

    public List<IRProperty> properties = new ArrayList<>();

    @Override
    public List<IRProperty> getProperties() {
        return properties;
    }

    @Override
    public void setProperty(String name, Object value) {
        setProperty(name, value, true);
    }

    @Override
    public void setProperty(String name, Object value, Boolean bool) {
        for (IRProperty property : properties) {
            if (property.name.equals(name)) {
                property.value = value;
                property.editable = bool;
                return;
            }
        }
        IRProperty prop = new IRProperty(name, value, bool);
        properties.add(prop);

    }
    public IRProperty getProperty(List<IRProperty> props, String name) {
        for (IRProperty property : props) {
            if (property.name.equals(name)) {
                return property;
            }
        }
        return null;
    }
    @Override
    public Object getProperty(String name) {
        for (IRProperty property : properties) {
            if (property.name.equals(name)) {
                return property.value;
            }
        }
        return null;
    }

    @Override
    public Vector3 getVector3(String name) {
        Object obj = getProperty(name);
        if (obj instanceof Vector3) {
            return (Vector3) obj;
        }
        return null;
    }

    @Override
    public Boolean getBoolean(String name) {
        Object obj = getProperty(name);
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        return false;
    }

    @Override
    public Float getFloat(String name) {
        Object obj = getProperty(name);
        if (obj instanceof Float) {
            return (Float) obj;
        }
        return 0f;
    }

    @Override
    public String getString(String name) {
        Object obj = getProperty(name);
        if (obj instanceof String) {
            return (String) obj;
        }
        return "";
    }

    public int compareProperties(List<IRProperty> props) {
        for (IRProperty prop : props) {
            IRProperty sp = getProperty(properties, prop.name);
            if (sp != null) {
                if (!sp.value.equals(prop.value)) {
                    return 0;
                }
            } else if (sp == null) {
                return 0;
            }
        }
        for (IRProperty prop : properties) {
            IRProperty sp = getProperty(props, prop.name);
            if (sp != null) {
                if (!sp.value.equals(prop.value)) {
                    return 0;
                }
            } else if (sp == null) {
                return 0;
            }
        }
        return 1;
    }

    @Override
    public void setUpdateNeeded() {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        this.updateNeeded = true;
    }
}
