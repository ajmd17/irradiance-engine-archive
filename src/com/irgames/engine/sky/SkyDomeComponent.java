/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.sky;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.components.GameComponent;
import com.irgames.utils.RenderUtils.Bucket;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.shaders.CloudShader;
import com.irgames.utils.MathUtil;
import com.irgames.engine.game.TestShader;
import com.irgames.managers.LightingManager;

/**
 *
 * @author Andrew
 */
public class SkyDomeComponent extends GameComponent {

    Model mod;

    IRNode skyNode;
    AtmosphereShader shader;
    CloudShader cloudShader;
    IRNode skyDome;
    float hour;
    final private static float radiansPerHour
            = (float) Math.PI * 2 / Constants.hoursPerDay;
    private float observerLatitude = Constants.defaultLatitude;
    final private static float obliquity = 23.44f * com.badlogic.gdx.math.MathUtils.degreesToRadians;
    private float solarLongitude = 0f;
    private float solarRaHours = 0f;
    float timeScale = 3.7f;

    public void setTimeScale(float ts) {
        timeScale = ts;
    }

    public void setCloudColor(Color col) {
        cloudShader.cloudColor = new Vector3(col.r, col.g, col.b);
    }
    float globalTime;

    private void updateShaders() {
        this.globalTime += Gdx.graphics.getDeltaTime();
        this.cloudShader.globalTime = globalTime;
        this.shader.globalTime = globalTime;
    }

    public SkyDomeComponent() {
        this.setName("SkyDome");
    }

    @Override
    public void init() {
        skyNode = new IRNode("sky node");

        Texture cloudTex = new Texture(Gdx.files.internal("data/textures/noise/tex16.png"));
        cloudTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        cloudTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        cloudShader = new CloudShader(cloudTex);
        cloudShader.init();

        mod = Assets.loadObjModel(Gdx.files.internal("data/sky/dome.obj"));
        NodePart np = mod.nodes.get(0).parts.get(0);
        skyDome = new IRNode("sky dome");
        np.setRenderable(skyDome);
        shader = new AtmosphereShader();
        shader.init();
        skyDome.shader = shader;
        skyDome.setBucket(Bucket.sky);
        skyDome.scale(new Vector3(80, 80, 80));

        IRNode cloudNode = new IRNode("cloud node");
        cloudNode.setBucket(Bucket.sky);
        Model cloudLayer = Assets.loadObjModel(Gdx.files.internal("data/sky/cloudlayer.obj"));
        NodePart cnp = cloudLayer.nodes.get(0).parts.get(0);
        cnp.setRenderable(cloudNode);
        cloudNode.setLocalTranslation(new Vector3(0, 170, 0));
        cloudNode.scale(new Vector3(1500, 1500, 1500));

        cloudNode.shader = cloudShader;

        skyNode.attachChild(skyDome);
        skyNode.attachChild(cloudNode);

        this.rootNode.attachChild(skyNode);
    }

    private Vector3 skyLoc = new Vector3();

    @Override
    public void update() {
        skyLoc.set(cam.position.x, cam.position.y - 10f, cam.position.z);

        skyDome.setLocalTranslation(skyLoc);
        updateHour();
        shader.setSunDirection(getSunDirection());
        this.onTimeChanged();
        this.updateShaders();
        //System.out.println("sun dir: " + getSunDirection());
    }
    public void setTime(float newHour) {
        hour = newHour;
    }
    public void updateHour() {
        float tpf = Gdx.graphics.getDeltaTime();
        float newHour = hour + (tpf * 0.1f * timeScale);
        if (!(newHour >= 0f && newHour <= Constants.hoursPerDay)) {
            this.hour = 0f;
        } else {
            hour += (tpf * 0.1f * timeScale);
        }
    }

    float sunUpdateTime = Float.MAX_VALUE;
    float maxUpdateTime = 1f;
    Color mainColor = new Color(), twilight = new Color(), blue = new Color(), white = new Color(), moonCol = new Color(), beige = new Color();
    
    public void onTimeChanged() {
        if (sunUpdateTime >= maxUpdateTime) {
            // update
            mainColor.set(128.0f / 255.0f, 166.0f / 255.0f, 208.0f / 255.0f, 1.0f);
            twilight.set(204.0f / 255.0f, 135.0f / 255.0f, 93.0f / 255.0f, 1.0f);
            beige.set(226.0f / 255.0f, 214.0f / 255.0f, 186.0f / 255.0f, 1.0f);
            blue.set(168f/255f, 177f/255f, 204f/255f, 1.0f);
            white.set(1.0f, 1.0f, 1.0f, 1.0f);//new ColorRGBA(226.0f / 255.0f, 214.0f / 255.0f, 186.0f / 255.0f, 1.0f);
            moonCol.set(0.2f, 0.2f, 0.2f, 1.0f);
            if (getSunDirection().y > 0.0f) {
                beige.mul(MathUtil.clamp(getSunDirection().y * 5.0f, 0.4f, 1.0f));
                white.mul(MathUtil.clamp(getSunDirection().y * 5.0f, 0.1f, 1.0f));
                twilight.lerp(beige, MathUtil.clamp(getSunDirection().y * 5.0f, 0.0f, 1.0f));
                white.lerp(beige, 1.0f - MathUtil.clamp(getSunDirection().y * 5.0f, 0.6f, 1.0f));

                LightingManager.setSunDirection(MathUtil.multVector3(this.getSunDirection(), 1f));
            } else {
                blue.mul(0.4f);
                twilight.lerp(blue, MathUtil.clamp(-getSunDirection().y * 5.0f, 0.0f, 1.0f));
                white.lerp(blue, 1.0f - MathUtil.clamp(-getSunDirection().y * 5.0f, 0.3f, 0.7f));
                //white.mul(0.6f);

                LightingManager.setSunDirection(MathUtil.multVector3(this.getSunDirection(), -1f));
            }
            //LightingManager.setSunDirection(getSunDirection());
            LightingManager.setAmbientColor(twilight.cpy().mul(0.4f));
            LightingManager.setBackgroundColor(twilight);
            LightingManager.setFog(LightingManager.getAmbientColor(), LightingManager.getFogStart(), LightingManager.getFogEnd());
            this.setCloudColor(white);
            LightingManager.setSunColor(white);
            sunUpdateTime = 0f;
        } else {
            sunUpdateTime += Gdx.graphics.getDeltaTime() * 10;
        }
    }

    /* public void attachToNode(IRNode rootNode) {
     rootNode.attachChild(skyNode);
     }*/
    public Vector3 getSunDirection() {
        Vector3 result = convertToWorld(0f, solarLongitude);
        assert result.isUnit();
        return result;
    }

    public Vector3 convertToWorld(float latitude, float longitude) {
        if (!(latitude >= (-Math.PI / 2) && latitude <= (Math.PI / 2))) {

            throw new IllegalArgumentException(
                    "latitude should be between -Pi/2 and Pi/2, inclusive");
        }
        if (!(longitude >= 0f && longitude <= Math.PI * 2)) {

            throw new IllegalArgumentException(
                    "longitude should be between 0 and 2*Pi, inclusive");
        }

        Vector3 equatorial = convertToEquatorial(latitude, longitude);
        Vector3 world = convertToWorld(equatorial);

        //assert world.isUnit();
        return world;
    }

    public float getSiderealHour() {
        float noon = 12f;
        float siderealHour = hour - noon - solarRaHours;
        siderealHour = MathUtil.modulo(siderealHour, Constants.hoursPerDay);

        return siderealHour;
    }

    public float getSiderealAngle() {
        float siderealHour = getSiderealHour();
        float siderealAngle = siderealHour * radiansPerHour;

        assert siderealAngle >= 0f : siderealAngle;
        assert siderealAngle < Math.PI * 2 : siderealAngle;
        return siderealAngle;
    }

    public static Vector3 convertToEquatorial(Vector3 ecliptical) {
        //  Validate.nonNull(ecliptical, "coordinates");
        /*
         * The conversion consists of a rotation about the +X
         * (vernal equinox) axis.
         */
        Quaternion rotate;
        rotate = fromAngleNormalAxis(obliquity, Vector3.X);
        Vector3 equatorial = ecliptical.cpy().mul(rotate);
        //rotate.mult(ecliptical);

        return equatorial;
    }

    public static Vector3 convertToEquatorial(float latitude,
            float longitude) {
        if (!(latitude >= -Math.PI / 2 && latitude <= Math.PI / 2)) {

            throw new IllegalArgumentException(
                    "latitude should be between -Pi/2 and Pi/2, inclusive");
        }
        if (!(longitude >= 0f && longitude <= Math.PI * 2)) {
            throw new IllegalArgumentException(
                    "longitude should be between 0 and 2*Pi, inclusive");
        }
        /*
         * Convert angles to Cartesian ecliptical coordinates.
         */

        float cosLat = (float) Math.cos(latitude);
        float sinLat = (float) Math.sin(latitude);
        float cosLon = (float) Math.cos(longitude);
        float sinLon = (float) Math.sin(longitude);
        Vector3 ecliptical
                = new Vector3(cosLat * cosLon, cosLat * sinLon, sinLat);
        assert ecliptical.isUnit();
        /*
         * Convert to equatorial coordinates.
         */

        Vector3 equatorial = convertToEquatorial(ecliptical);

        //assert equatorial.isUnit();
        return equatorial;
    }

    public static Quaternion fromAngleNormalAxis(float angle, Vector3 axis) {
        Quaternion q = new Quaternion();
        float halfAngle = 0.5f * angle;
        float sin = (float) Math.sin(halfAngle);
        q.w = (float) Math.cos(halfAngle);
        q.x = sin * axis.x;
        q.y = sin * axis.y;
        q.z = sin * axis.z;

        return q;
    }

    public float getSolarLongitude() {
        assert solarLongitude <= Math.PI * 2 : solarLongitude;
        assert solarLongitude >= 0f : solarLongitude;

        return solarLongitude;
    }

    public Vector3 convertToWorld(Vector3 equatorial) {
        //Validate.nonNull(equatorial, "coordinates");

        float siderealAngle = getSiderealAngle();
        /*
         * The conversion consists of a (-siderealAngle) rotation about the Z
         * (north celestial pole) axis followed by a (latitude - Pi/2) rotation
         * about the Y (east) axis followed by a permutation of the axes.
         */
        Quaternion zRotation;
        zRotation = fromAngleNormalAxis(-siderealAngle, Vector3.Z);
        Vector3 rotated = equatorial.cpy().mul(zRotation);
        // rotated.mul(zRotation);

        float coLatitude = (float) Math.PI / 2 - observerLatitude;
        Quaternion yRotation;
        yRotation = fromAngleNormalAxis(-coLatitude, Vector3.Y);
        rotated.mul(yRotation);

        Vector3 world = new Vector3(-rotated.x, rotated.z, rotated.y);

        return world;
    }

}
