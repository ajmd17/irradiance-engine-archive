/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.files.FileHandle;
import com.irgames.engine.assets.MTLtoIRMAT;

import com.irgames.engine.game.ProjectProperties;
import com.irgames.engine.components.IRShader;
import com.irgames.engine.game.Game;
import com.irgames.engine.game.IRRunnable;
import com.irgames.engine.game.TestGame;
import com.irgames.engine.game.tests.TestFXAA;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.AbstractRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
import static org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority.MEDIUM;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;

/**
 *
 * @author Andrew
 */
public class JRFrame extends JRibbonFrame {

    static JPanelSceneEdit sceneEditor;

    public static ResizableIcon getResizableIconFromResource(String resource) throws FileNotFoundException {
        InputStream is = new FileInputStream(resource);
        return ImageWrapperResizableIcon.getIcon(is, new Dimension(32, 32));
    }

    public static void copyFile(File from, File to) throws IOException {
        Files.copy(from.toPath(), to.toPath(), REPLACE_EXISTING);
    }

    private static void changeHeight(RibbonTask task) {
        List<AbstractRibbonBand<?>> bands = task.getBands();
        for (AbstractRibbonBand arb : bands) {
            arb.setPreferredSize(new Dimension(40, 80));
        }
    }

    public static String removeExt(String str) {
        return str.substring(0, str.lastIndexOf("."));
    }

    public static void centerWindow(JRFrame frame) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
    }

    public static void main(String[] args) {

        JFrame.setDefaultLookAndFeelDecorated(true);
        try {
            UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(JRFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(JRFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(JRFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(JRFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        // SubstanceLookAndFeel.setSkin("org.pushingpixels.substance.api.skin.BusinessSkin");
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                JRFrame frame = new JRFrame();
                frame.setTitle("Irradiance Engine");
                frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
                frame.pack();

                frame.setVisible(true);
                frame.setSize(1080, 720);
                sceneEditor = new JPanelSceneEdit();
                sceneEditor.setSize(300, 300);
                frame.add(sceneEditor);
                centerWindow(frame);
                IRNewProj irnew = new IRNewProj(sceneEditor.testGame) {
                    @Override
                    public void onOK() {

                        try {

                            JRibbonBand band1 = new JRibbonBand("GameComponents", null);
                            JRibbonBand band2 = new JRibbonBand("Sun", null);
                            JRibbonBand band3 = new JRibbonBand("Fog", null);
                            JRibbonBand band4 = new JRibbonBand("Transform", null);
                            JRibbonBand band5 = new JRibbonBand("Import Assets", null);
                            JRibbonBand band6 = new JRibbonBand("View", null);
                            JRibbonBand band7 = new JRibbonBand("Planting", null);
                            JRibbonBand band8 = new JRibbonBand("Post Process", null);
                            JRibbonBand band9 = new JRibbonBand("Game", null);
                            JCommandButton button1 = new JCommandButton("Manage", getResizableIconFromResource("resources/icon_gear.png"));
                            button1.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    new IRComponents(sceneEditor.testGame).show();
                                }

                            });
                            JCommandButton btnAddComponent = new JCommandButton("Add", getResizableIconFromResource("resources/icon_add.png"));

                            JCommandButton btnLightColor = new JCommandButton("Color", null);

                            JCommandButton btnFogColor = new JCommandButton("Color", null);

                            JCommandButton btnTranslate = new JCommandButton("Translate", getResizableIconFromResource("resources/icon_translate.png"));
                            JCommandButton btnRotate = new JCommandButton("Rotate", null);
                            JCommandButton btnWireframe = new JCommandButton("Wireframe", getResizableIconFromResource("resources/icon_wireframe.png"));

                            JCommandButton btnTree = new JCommandButton("Tree", getResizableIconFromResource("resources/icon_tree.png"));

                            btnTree.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    sceneEditor.testGame.selectedTool = sceneEditor.testGame.treeTool;

                                }

                            });

                            JCommandButton btnPlay = new JCommandButton("Play", getResizableIconFromResource("resources/icon_tree.png"));

                            btnPlay.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {

                                    JFrame jrf = new JFrame();
                                    jrf.setTitle("Irradiance Engine");
                                    jrf.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                                    jrf.pack();

                                    jrf.setVisible(true);
                                    jrf.setSize(1080, 720);

                                    LwjglAWTCanvas canvas1 = new LwjglAWTCanvas(new TestFXAA());

                                    canvas1.getCanvas().setSize(jrf.getSize());
                                    JPanel pnl = new JPanel();
                                    pnl.add(canvas1.getCanvas(), BorderLayout.CENTER);
                                    jrf.add(pnl);
                                   // Game.cam.setFPSMode();
                                }

                            });

                            JCommandButton btnTeapot = new JCommandButton("Teapot", null);

                            btnTeapot.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {

                                    sceneEditor.testGame.modelTool.itemName = "Teapot";
                                    sceneEditor.testGame.selectedTool = sceneEditor.testGame.modelTool;

                                }

                            });

                            btnWireframe.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    if (IRShader.wireframe == false) {
                                        IRShader.EnableWireframe();
                                    } else if (IRShader.wireframe == true) {
                                        IRShader.DisableWireframe();
                                    }
                                }

                            });

                            JCommandButton btnImportModel = new JCommandButton("Model", getResizableIconFromResource("resources/icon_importmodel.png"));
                            btnImportModel.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    JFileChooser fileChooser = new JFileChooser();
                                    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                        FileHandle fh = Gdx.files.absolute(fileChooser.getSelectedFile().getAbsolutePath());
                                        if (!ProjectProperties.projectPath.equals("")) {
                                            try {
                                                copyFile(new File(fileChooser.getSelectedFile().getAbsolutePath()), new File(ProjectProperties.projectPath + "\\assets\\models\\" + fh.name()));
                                                File mtlfile = new File(fileChooser.getSelectedFile().getParent() + File.separator + removeExt(fileChooser.getSelectedFile().getName()) + ".mtl");
                                                if (mtlfile.exists()) {

                                                    String content;

                                                    content = new String(Files.readAllBytes(Paths.get(mtlfile.getAbsolutePath())));

                                                    try {
                                                        MTLtoIRMAT.convert(content, ProjectProperties.projectPath + "\\assets\\models\\" + removeExt(fh.name()) + ".irmat");
                                                    } catch (Exception ex) {
                                                        JOptionPane.showMessageDialog(null, "Error loading model materials:\n" + ex);
                                                    }
                                                }
                                                sceneEditor.testGame.loadModel(Gdx.files.absolute(ProjectProperties.projectPath + "\\assets\\models\\" + fh.name()));
                                            } catch (IOException ex) {
                                                Logger.getLogger(JRFrame.class.getName()).log(Level.SEVERE, null, ex);
                                            }

                                        } else if (ProjectProperties.projectPath.equals("")) {
                                            sceneEditor.testGame.loadModel(fh);
                                        }

                                    }
                                }

                            });
                            JCommandButton btnImportTexture = new JCommandButton("Texture", getResizableIconFromResource("resources/image-x-generic.png"));
                            btnImportTexture.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    JFileChooser fileChooser = new JFileChooser();
                                    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                        FileHandle fh = Gdx.files.absolute(fileChooser.getSelectedFile().getAbsolutePath());
                                        if (!ProjectProperties.projectPath.equals("")) {
                                            try {
                                                copyFile(new File(fileChooser.getSelectedFile().getAbsolutePath()), new File(ProjectProperties.projectPath + "\\assets\\textures\\" + fh.name()));
                                            } catch (IOException ex) {
                                                Logger.getLogger(JRFrame.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        }

                                    }
                                }

                            });
                            band1.addCommandButton(button1, MEDIUM);

                            band1.addCommandButton(btnAddComponent, MEDIUM);
                            band2.addCommandButton(btnLightColor, MEDIUM);
                            band3.addCommandButton(btnFogColor, MEDIUM);
                            band4.addCommandButton(btnTranslate, MEDIUM);
                            band4.addCommandButton(btnRotate, MEDIUM);
                            band5.addCommandButton(btnImportModel, MEDIUM);
                            band5.addCommandButton(btnImportTexture, MEDIUM);
                            band6.addCommandButton(btnWireframe, MEDIUM);
                            band7.addCommandButton(btnTree, MEDIUM);
                            band7.addCommandButton(btnTeapot, MEDIUM);
                            band9.addCommandButton(btnPlay, MEDIUM);
                            band9.setResizePolicies((List) Arrays.asList(new CoreRibbonResizePolicies.None(band5.getControlPanel()), new IconRibbonBandResizePolicy(band5.getControlPanel())));

                            band8.setResizePolicies((List) Arrays.asList(new CoreRibbonResizePolicies.None(band5.getControlPanel()), new IconRibbonBandResizePolicy(band5.getControlPanel())));

                            band7.setResizePolicies((List) Arrays.asList(new CoreRibbonResizePolicies.None(band5.getControlPanel()), new IconRibbonBandResizePolicy(band5.getControlPanel())));
                            band6.setResizePolicies((List) Arrays.asList(new CoreRibbonResizePolicies.None(band6.getControlPanel()), new IconRibbonBandResizePolicy(band6.getControlPanel())));
                            band5.setResizePolicies((List) Arrays.asList(new CoreRibbonResizePolicies.None(band5.getControlPanel()), new IconRibbonBandResizePolicy(band5.getControlPanel())));
                            band4.setResizePolicies((List) Arrays.asList(new CoreRibbonResizePolicies.None(band4.getControlPanel()), new IconRibbonBandResizePolicy(band4.getControlPanel())));
                            band3.setResizePolicies((List) Arrays.asList(new CoreRibbonResizePolicies.None(band3.getControlPanel()), new IconRibbonBandResizePolicy(band2.getControlPanel())));
                            band2.setResizePolicies((List) Arrays.asList(new CoreRibbonResizePolicies.None(band2.getControlPanel()), new IconRibbonBandResizePolicy(band2.getControlPanel())));
                            band1.setResizePolicies((List) Arrays.asList(new CoreRibbonResizePolicies.None(band1.getControlPanel()),
                                    new IconRibbonBandResizePolicy(band2.getControlPanel())));

                            RibbonTask task1 = new RibbonTask("Home", band1, band4, band5, band6, band7, band9);
                            RibbonTask task2 = new RibbonTask("Environment", band2, band3);
                            RibbonTask task3 = new RibbonTask("Effects", band8);

                            changeHeight(task1);
                            changeHeight(task2);
                            changeHeight(task3);
                            frame.getRibbon().addTask(task1);
                            frame.getRibbon().addTask(task2);
                            frame.getRibbon().addTask(task3);
                            RibbonApplicationMenu menu = new RibbonApplicationMenu();

                            RibbonApplicationMenuEntryPrimary btnNew = new RibbonApplicationMenuEntryPrimary(null, "New...", null, CommandButtonKind.ACTION_ONLY) {

                            };
                            menu.addMenuEntry(btnNew);
                            ActionListener saveSceneListener = new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    IRSaveScene irss = new IRSaveScene(sceneEditor.testGame);
                                    irss.setModal(true);
                                    irss.show();
                                }

                            };

                            ActionListener loadSceneListener = new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {

                                    // sceneEditor.testGame.loadScene = true;
                                    IRLoadScene irls = new IRLoadScene(sceneEditor.testGame);
                                    irls.setModal(true);
                                    irls.show();
                                }

                            };

                            RibbonApplicationMenuEntryPrimary btnSaveScene = new RibbonApplicationMenuEntryPrimary(null, "Save Scene", saveSceneListener, CommandButtonKind.ACTION_ONLY);

                            menu.addMenuEntry(btnSaveScene);

                            RibbonApplicationMenuEntryPrimary btnSaveProj = new RibbonApplicationMenuEntryPrimary(null, "Save Project", null, CommandButtonKind.ACTION_ONLY);
                            menu.addMenuEntry(btnSaveProj);

                            RibbonApplicationMenuEntryPrimary btnOpenProj = new RibbonApplicationMenuEntryPrimary(null, "Open Project...", null, CommandButtonKind.ACTION_ONLY);
                            menu.addMenuEntry(btnOpenProj);

                            RibbonApplicationMenuEntryPrimary btnOpenScene = new RibbonApplicationMenuEntryPrimary(null, "Open Scene...", loadSceneListener, CommandButtonKind.ACTION_ONLY);
                            menu.addMenuEntry(btnOpenScene);

                            frame.getRibbon().setApplicationMenu(menu);
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(JRFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };

                irnew.setModal(true);
                irnew.show();

            }
        });

    }
}
