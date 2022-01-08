/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.pagingengine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.IRMat;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.shaders.GrassShader;
import com.irgames.managers.LightingManager;
import com.irgames.utils.NodeUtils;
import com.irgames.engine.terrain.TerrainComponent;
import com.irgames.engine.game.TreeModel;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.managers.ShaderManager;
import com.irgames.utils.MathUtil;
import com.irgames.utils.MyRandom;
import com.irgames.utils.RenderUtils;
import com.irgames.utils.RenderUtils.BackfaceCullMode;
import com.irgames.utils.RenderUtils.Bucket;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Andrew
 */
public class GrassPopulator extends EntityPopulator {
    Model grassModel;
    IRMat grassMat;
    public GrassPopulator(IRNode rootNode) {
        super("grass", rootNode);
        try {
            this.useBatching = true;
            this.chunkAmount = 8;
            this.entityPerPatch = 4;
            this.patchCount = 6;
            this.spread = 3;
            // this.patchSpread = 30;
            this.tolerance = 0.1f;
            this.bucket = Bucket.transparent;
            this.mainNode.setBucket(Bucket.transparent);
            Texture grassTex = new Texture(Gdx.files.internal("data/textures/vegetation/grass.png"), true);
            grassTex.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
            
            Texture noiseMap = new Texture(Gdx.files.internal("data/textures/noise/tex10.png"));
            noiseMap.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            GrassShader myShader = (GrassShader) ShaderManager.getShader(GrassShader.class, new ShaderProperties());
            myShader.yFlipped = true;
            this.shader = myShader;
            LightingManager.addShader(myShader);
            //this.shader.init();
            myShader.setWindAmount(1.0f);
            myShader.setWindSpeed(7f);
            myShader.setFade(60.0f, 70.0f);
            ShaderManager.addGrassShader(myShader);
            grassMat = new IRMat();
            grassMat.alphaDiscard = 0.5f;
            grassMat.setTexture("diffuse", grassTex);
            grassMat.setProperty("flip_y", true);
            grassMat.setProperty("discard", false);
            grassMat.setProperty("drawsShadows", false);
            grassMat.shader = myShader;
            grassMat.cullMode = BackfaceCullMode.off;
            grassModel = Assets.loadObjModel(Gdx.files.internal("data/vegetation/grass.obj"));
        } catch (Exception ex) {
            Logger.getLogger(GrassPopulator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    @Override
    public IRNode setupEntityNode(Vector3 loc) {
        
       // IRNode boxtest = NodeUtils.createBox(loc, testShader);
        IRNode myNode = new IRNode("grass");
        grassModel.nodes.get(0).parts.get(0).setRenderable(myNode);
        myNode.setLocalTranslation(loc);
        float randScale = (float)MathUtil.randomInRange(0.75f, 1.4f);
        myNode.scale(new Vector3(randScale, randScale, randScale));
        myNode.setMaterial(grassMat);
        return myNode;
    }
    
}
