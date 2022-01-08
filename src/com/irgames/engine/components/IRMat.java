/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.components;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.math.Vector3;
import com.irgames.utils.RenderUtils.BackfaceCullMode;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class IRMat implements IRObject {
    List<IRProperty> properties = new ArrayList<>();
    @Override
    public List<IRProperty> getProperties() {
        return properties;
    }
    @Override
    public final void setProperty(String name, Object value) {
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
    public IRMat setPropertyC(String name, Object val) {
        setProperty(name, val);
        return this;
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
   
    public Color getColor(String name) {
        Object obj = getProperty(name);
        if (obj instanceof Color) {
            return (Color) obj;
        }
        return null;
    }
    
    @Override
    public void setUpdateNeeded() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    public static class val {

        public val(Object obj, String name) {
            this.obj = obj;
            this.name = name;
        }
        public Object obj;
       public  String name;
    }

    public static class texDef {

        public texDef(String name, Texture tex) {
            this.name = name;
            this.texture = tex;
        }
        public String name;
        public Texture texture;
    }
    public List<val> Vals = new ArrayList<>();
    public List<texDef> texDefs = new ArrayList<>();
    public List<Texture> textures = new ArrayList<>();
    public List<Pixmap> pixmaps = new ArrayList<>();
    public Texture normalMap;
    public Color color = Color.WHITE;
    public Shader shader;
    public BackfaceCullMode cullMode = BackfaceCullMode.back;
    public float alphaDiscard = 0.0f;

    public Shader getShader() {
        return shader;
    }

   
    public IRMat cpy() {
        IRMat cloned = new IRMat();
        for (val Value : Vals) {
            cloned.Vals.add(Value);

        }
        cloned.cullMode = this.cullMode;
        cloned.color = this.color;
        cloned.shader = this.shader;
        cloned.alphaDiscard = this.alphaDiscard;
        cloned.texDefs = this.texDefs;
        cloned.textures = this.textures;
        cloned.pixmaps = this.pixmaps;
        return cloned;
    }

    

    public Texture getTexture(String name) {
        for (texDef td : texDefs) {
            if (td.name.equals(name)) {
                return td.texture;
            }
        }
        return null;
    }
    public texDef getTextureDef(String name) {
        for (texDef td : texDefs) {
            if (td.name.equals(name)) {
                return td;
            }
        }
        return null;
    }
    public Pixmap getPixmap(int index) {
        return pixmaps.get(index);
    }

    public void addPixmap(Pixmap p) {
        if (pixmaps.size() < 6) {
            pixmaps.add(p);
        }
    }

    public void setTexture(String name, Texture tex) {
        tex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        texDef td = getTextureDef(name);
        if (td == null) {
            this.texDefs.add(new texDef(name, tex));
        } else {
            td.texture = tex;
        }
    }

    public IRMat(Texture tex) {
        this(Color.WHITE, tex);
    }

    public IRMat(Color color, Texture tex) {
        this.textures.add(tex);
        tex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        this.color = color;
    }

    public IRMat(FileHandle fileHandle) {
        this(Color.WHITE, fileHandle);
    }

    public IRMat(Color color, FileHandle fileHandle) {
        this(color, new Texture(fileHandle));
    }

    public IRMat(Color color, Texture diffuseMap, Texture normalMap) {
        this(color, diffuseMap);
        this.normalMap = normalMap;
    }

    public IRMat() {
        setProperty("roughness", 0.4f);
        setProperty("F0", 0.3f);
        setProperty("specular", 20.0f);
        setProperty("albedo", Color.WHITE);
        setProperty("drawsShadows", true);
    }

    public IRMat(Shader sh) {
        shader = sh;
    }

}
