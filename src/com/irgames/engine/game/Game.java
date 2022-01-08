/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.game;

import com.irgames.engine.components.GameCam;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.irgames.engine.assets.Assets;
import com.irgames.engine.physics.Physics;
import com.irgames.engine.components.GameComponent;
import com.irgames.engine.components.IRMat;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.components.IRSpatial;
import com.irgames.engine.components.listeners.IRListener;
import com.irgames.engine.components.listeners.IRObjectAddedListener;
import com.irgames.engine.controls.RigidBodyControl;
import com.irgames.engine.maps.Map;
import com.irgames.engine.maps.ModelInfo;
import com.irgames.engine.pbr.Presets;
import com.irgames.engine.shaders.BRDFShader;
import com.irgames.engine.shaders.components.ShaderProperties;
import com.irgames.managers.DepthTextureManager;
import com.irgames.managers.EnvironmentMapper;
import com.irgames.managers.LightingManager;
import com.irgames.managers.PostProcessManager;
import com.irgames.managers.RenderManager;
import com.irgames.managers.ShaderManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 *
 * @author Andrew
 */
public class Game extends InputAdapter implements ApplicationListener {

    public GameCam cam;
    protected RenderManager renderManager;
    public static RenderContext context;
    public static IRNode rootNode;
    protected int width = 1080, height = 720;
    protected PostProcessManager processor;
    protected TestShader testShader;
    protected float deltaTime;
    public Physics physics;
    public List<GameComponent> components = new ArrayList<>();
    protected SpriteBatch spriteBatch;
    public List<ModelInfo> sceneFile = new ArrayList<>();
    public IRNode mapNode;
    public boolean loadScene = false;
    public IRListener componentAddedListener;
    private boolean loadModel;
    private FileHandle loadModelFH;
    List<IRRunnable> runnables = new ArrayList<>();
    public IRObjectAddedListener sceneFileListener;
    private String title = "";

    public void invoke(IRRunnable runnable) {
        runnables.add(runnable);
    }

    public void saveComponents() {
        PrintWriter writer = null;
        try {
            String text = "";
            for (GameComponent comp : components) {
                text += "#START " + comp.getName() + "\n"
                        + "type: " + comp.getClass().getTypeName() + "\n"
                        + "enabled: " + comp.isEnabled() + "\n"
                        + "#END\n";
            }
            writer = new PrintWriter(ProjectProperties.projectPath + File.separator + "data" + File.separator + "components.ird", "UTF-8");
            writer.println(text);
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            writer.close();
        }

    }

    @Override
    public final void create() {
        Gdx.graphics.setDisplayMode(width, height, false);
        Gdx.graphics.setTitle("Game");

        cam = new GameCam(55) {
            @Override
            public void onLeftClick(int screenX, int screenY) {
                onClickLeft(screenX, screenY);
            }

            @Override
            public void onRightClick(int screenX, int screenY) {
                onClickRight(screenX, screenY);
            }

            @Override
            public void onDragLeft() {
                onMouseDragLeft();
            }

            @Override
            public void onDragRight() {
                onMouseDragRight();
            }

            @Override
            public void onTouchUp() {
                onMouseTouchUp();
            }

            @Override
            public void onScroll(int amt) {
                onMouseScroll(amt);
            }
        };
        spriteBatch = new SpriteBatch();
        renderManager = new RenderManager(cam);
        processor = new PostProcessManager();
        context = new RenderContext(new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED, 1));
        rootNode = new IRNode("root node");
        rootNode.attachChild(mapNode = new IRNode());
        ShaderManager.rootNode = rootNode;
        this.setBackgroundColor(Color.BLACK);
        Bullet.init();
        testShader = new TestShader();
        testShader.init();
        physics = new Physics(false);
        LightingManager.setSunColor(Color.WHITE);
        LightingManager.setFog(Color.GRAY, 100, 200);
        LightingManager.setSunDirection(new Vector3(-1f, -1f, -1));
        init();
    }

    public void addComponent(GameComponent component) {
        component.rootNode = new IRNode();
        rootNode.attachChild(component.rootNode);
        component.cam = cam;
        component.context = context;
        component.setEnabled(true);

        components.add(component);
        if (componentAddedListener != null) {
            componentAddedListener.action();
        }
    }
    Vector3 center = new Vector3();
    protected Vector3[] points = new Vector3[8];
    Vector3 lightDir = new Vector3(0.0f, -1f, 0.0f);

    /*protected void renderDepthMap() {
     Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
     Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
     DepthTextureManager.capture();
     // shadowBatch.begin(cam);
     // shadowBatch.render(RenderManager.instances);
     // shadowBatch.end();
     renderManager.renderDepthTexture(cam, context);
     DepthTextureManager.release();
     }*/
    public void removeComponent(GameComponent component) {
        components.remove(component);
        if (componentAddedListener != null) {
            componentAddedListener.action();
        }
    }

    public void disableComponent(GameComponent component) {

        rootNode.detachChild(component.rootNode);
        component.disable();
        if (componentAddedListener != null) {
            componentAddedListener.action();
        }
    }

    public void enableComponent(GameComponent component) {

        rootNode.attachChild(component.rootNode);
        component.enable();
        if (componentAddedListener != null) {
            componentAddedListener.action();
        }
    }

    public void loadModel(FileHandle fh) {
        loadModel = true;
        this.loadModelFH = fh;
    }

    protected void setBackgroundColor(Color color) {
        LightingManager.setBackgroundColor(color);
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    public void init() {

    }

    @Override
    public void resize(int i, int i1) {
        width = i;
        height = i1;
        Gdx.graphics.setDisplayMode(width, height, true);
        invoke(new IRRunnable() {

            @Override
            public void run() {
                processor.resize();
            }
        });
            
        
        if (cam != null) {
            this.cam.resize(width, height);
        }
        try {
            PostProcessManager.rebuild(width, height);
            DepthTextureManager.rebuild(width, height);
            cam.update();
        } catch (Exception ex) {

        }

    }

    public void onMouseDragLeft() {

    }

    public void onMouseDragRight() {

    }

    public void onMouseScroll(int amt) {

    }

    public void onMouseTouchUp() {

    }

    public void onClickLeft(int screenX, int screenY) {

    }

    public void onClickRight(int screenX, int screenY) {

    }

    public void preUpdate() {

    }

    public static void copyFile(File from, File to) throws IOException {
        Files.copy(from.toPath(), to.toPath(), REPLACE_EXISTING);
    }

    private static String remExtension(String name) {
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex >= 0) { // to prevent exception if there is no dot
            return name.substring(0, dotIndex);
        }
        return name;
    }

    public static void copyFolder(File source, File destination) {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }

            String files[] = source.list();

            for (String file : files) {
                File srcFile = new File(source, file);
                File destFile = new File(destination, file);

                copyFolder(srcFile, destFile);
            }
        } else {
            InputStream in = null;
            OutputStream out = null;

            try {
                in = new FileInputStream(source);
                out = new FileOutputStream(destination);

                byte[] buffer = new byte[1024];

                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            } catch (Exception e) {

                e.printStackTrace();

            }
        }
    }
    protected void moveWorld() { // reposition world to avoid floating point precision issues
        float maxDist = 200;
        if (cam.position.x > maxDist) {

            rootNode.setLocalTranslation(rootNode.getLocalTranslation().cpy().add(new Vector3(-maxDist, 0, 0)));
            cam.position.set(0, cam.position.y, cam.position.z);
            System.out.println("move");
            cam.update();
        }
        if (cam.position.z > maxDist) {

            rootNode.setLocalTranslation(rootNode.getLocalTranslation().cpy().add(new Vector3(0, 0, -maxDist)));
            cam.position.set(cam.position.x, cam.position.y, 0);
            System.out.println("move");
            cam.update();
        }
        if (cam.position.x < -maxDist) {

            rootNode.setLocalTranslation(rootNode.getLocalTranslation().cpy().add(new Vector3(maxDist, 0, 0)));
            cam.position.set(0, cam.position.y, cam.position.z);
            System.out.println("move");
            cam.update();
        }
        if (cam.position.z < -maxDist) {

            rootNode.setLocalTranslation(rootNode.getLocalTranslation().cpy().add(new Vector3(0, 0, maxDist)));
            cam.position.set(cam.position.x, cam.position.y, 0);
            System.out.println("move");
            cam.update();
        }
    }
    public void loadScene(String path) {
        File file = new File(path);
        if (path.startsWith(ProjectProperties.projectPath + File.separator + "assets" + File.separator + "scenes")) {

            sceneFile.clear();
            mapNode.detachAllChildren();
            try {
                Map.loadScene(path + File.separator + file.getName(), cam, mapNode, sceneFile);
            } catch (IOException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {

            File newFile = new File(ProjectProperties.projectPath + File.separator + "assets" + File.separator + "scenes" + File.separator + file.getName() + File.separator);
            newFile.mkdirs();
            File oldParentDir = file.getParentFile();
            copyFolder(oldParentDir, newFile);
            String apath = newFile.getAbsolutePath() + File.separator + file.getName();
            sceneFile.clear();
            mapNode.detachAllChildren();
            try {
                Map.loadScene(apath, cam, mapNode, sceneFile);
            } catch (IOException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void loadScene() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            Physics.purge();
            File file = fileChooser.getSelectedFile();
            File newFile = new File(ProjectProperties.projectPath + File.separator + "assets" + File.separator + "scenes" + File.separator + remExtension(file.getName()));
            try {
                copyFile(file, newFile);
            } catch (IOException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
            String path = newFile.getAbsolutePath();
            sceneFile.clear();
            mapNode.detachAllChildren();

            try {
                Map.loadScene(path, cam, mapNode, sceneFile);

                /*DefaultListModel lm;
                 if (objs.getList().getModel() instanceof DefaultListModel) {
                 lm = (DefaultListModel) objs.getList().getModel();
                 } else {
                 lm = new DefaultListModel();
                
                
                 }
                 for (int i = 0; i < sceneFile.size(); i++) {
                 lm.addElement(i);
                 }
                 objs.getList().setModel(lm);*/
            } catch (IOException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }

            /*  gt.sceneLoaded = false;
             gt.treeControl.clear();
             String filePath;
             if (path.contains(File.separator)) {
             filePath = path.
             substring(0, path.lastIndexOf(File.separator));
             } else {
             filePath = "";
             }
             assetManager.registerLocator(filePath, FileLocator.class);
             gt.mapPath = filePath;
             gt.removeAllTiles();
             gt.treeControl.mapPath = filePath;

             gt.boulderControl.clear();

             gt.boulderControl.mapPath = filePath;
             gt.generate();
             gt.sceneLoaded = true;*/
        }
    }

    public void saveScene() throws FileNotFoundException, UnsupportedEncodingException {

        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();

            System.out.println("Saving scene...");
            Map.saveScene(path, cam, sceneFile);

        }
    }

    public ModelInfo getSceneModel(int id) {
        if (sceneFile.size() - 1 >= (id)) {
            return sceneFile.get(id);
        }
        return null;
    }

    private void updateLoadModel() {
        if (loadModel) {

            try {
                Model model = null;
                IRNode mod = null;
                if (loadModelFH.toString().endsWith("obj")) {
                    // model = Assets.loadObjModel(loadModelFH);
                    mod = Assets.loadObjNode(loadModelFH);

                } else if (loadModelFH.toString().endsWith("g3db") || loadModelFH.toString().endsWith("g3dj")) {
                    model = Assets.loadG3dModel(loadModelFH);
                    mod = new IRNode(model);
                    for (IRSpatial irs : mod.getChildren()) {
                        irs.rotate(Vector3.X, -90);
                    }

                }

                //  mod = new IRNode(model);
                // mod = NewObjLoader.loadModelData(Gdx.files.absolute(loadModelFH.path()), false);//new IRNode(model);
                //   mod.scale(new Vector3(0.3f, 0.3f, 0.3f));
                mod.setID(Integer.toString(sceneFile.size()));
                try {
                    RigidBodyControl rbc = new RigidBodyControl(mod, 0);
                    rbc.rigidBody.userData = "model";
                    mod.addControl(rbc);
                } catch (Exception ex) {

                }
                //if (mod.shader == null) {
                try {

                    IRMat irm = new Presets().PBR_RedPaint;
                    mod.setMaterial(irm);
                    BRDFShader sh = (BRDFShader) ShaderManager.getShader(BRDFShader.class, new ShaderProperties().setProperty("ENV_MAP", false));

                    mod.setShader(sh);

                } catch (Exception ex) {
                    Logger.getLogger(ModelEditorGame.class.getName()).log(Level.SEVERE, null, ex);
                }
                //}
                /*IRMat irmat = new IRMat();
                 irmat.setTexture("matcap", Assets.loadTexture(Gdx.files.internal("data/materials/matcaps/zbrush-mat3.png")));
                 mod.setMaterial(irmat);
                 try {
                 mod.shader = ShaderManager.getShader(MatCapShader.class, new ShaderProperties().setProperty("ENV_MAP", true));
                 } catch (Exception ex) {
                 Logger.getLogger(TestSkyBox.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 mod.setMaterial(irmat);*/

                rootNode.attachChild(mod);
                System.out.println("size: " + mod.getChildren().size());
                String content;

                // content = new String(Files.readAllBytes(Paths.get("C:\\Users\\Andrew\\Documents\\NetBeansProjects\\IrradianceEngine3\\IrradianceEngine\\data\\models\\sapling\\sapling.mtl")));
                //   MTLtoIRMAT.convert(content, "C:\\Users\\Andrew\\Documents\\NetBeansProjects\\IrradianceEngine3\\IrradianceEngine\\data\\models\\sapling\\sydacad.irmat");
                //   IRMatSaveLoad.load(mod, "C:\\Users\\Andrew\\Documents\\NetBeansProjects\\IrradianceEngine3\\IrradianceEngine\\data\\models\\sapling\\sydacad.irmat");
                mod.setLocalTranslation(new Vector3(cam.position.x, cam.position.y, cam.position.z - 5));
                sceneFile.add(new ModelInfo(loadModelFH.name(), Vector3.Zero.toString(), mod.getLocalScale().toString(), new Quaternion().toString()));
                if (sceneFileListener != null) {
                    sceneFileListener.action(mod);
                }
            } catch (Exception ex) {
                System.out.println("Error: ");
                ex.printStackTrace();
            }

            loadModel = false;
        }
    }

    private void updateRunnables() {
        for (int i = (runnables.size() - 1); i > -1; i--) {
            IRRunnable run = runnables.get(i);
            if (run.ran == false) {
                run.run();
                run.ran = true;
            } else if (run.ran == true) {
                runnables.remove(run);
            }
        }
    }
    String memUsage;

    @Override
    public final void render() {
        memUsage = (float) Runtime.getRuntime().freeMemory() / (float) Runtime.getRuntime().maxMemory() * 100.0f + "%";
        Gdx.graphics.setTitle(this.title + "    -   " + Gdx.graphics.getFramesPerSecond() + " FPS" + "    Mem: " + memUsage);
        moveWorld();
        updateRunnables();
        EnvironmentMapper.update();
        deltaTime = Gdx.graphics.getDeltaTime();
        if (loadScene) {
            loadScene = false;
            loadScene();
        }
        updateLoadModel();
        
        preUpdate();
        for (GameComponent component : components) {

            if (!component.added) {
                component.init();

                component.added = true;
            }

            if (component.isEnabled()) {
                component.preUpdate();
            }
        }

        ShaderManager.updateTime(deltaTime);
        rootNode.draw(cam, context);

        processor.begin();
        RenderManager.render(cam, context);
        physics.renderDebug(cam);

        renderManager.renderTranslucent(cam, context);
        processor.end(cam);

        cam.updateCam();

        update();

        for (GameComponent component : components) {
            if (component.isEnabled()) {
                component.update();
                component.render(cam, context);
            }
        }

        drawSpriteBatch();
    }

    public void drawSpriteBatch() {

    }

    public void update() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

}
