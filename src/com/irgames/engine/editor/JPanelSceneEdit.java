/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.editor;

import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.irgames.engine.components.GameComponent;
import com.irgames.engine.components.IRGeom;
import com.irgames.engine.components.IRNode;
import com.irgames.engine.components.IRObject;
import com.irgames.engine.components.IRSpatial;
import com.irgames.engine.components.listeners.IRListener;
import com.irgames.engine.components.listeners.IRObjectAddedListener;
import com.irgames.engine.game.Game;
import com.irgames.engine.game.TestGame;
import com.irgames.engine.maps.Map;
import com.irgames.engine.pagingengine.GrassPopulator;
import com.irgames.engine.pagingengine.TreePopulator;
import com.irgames.engine.terrain.ModelTerrainComponent;
import com.irgames.managers.RenderManager;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;


/**
 *
 * @author Andrew
 */
public class JPanelSceneEdit extends javax.swing.JPanel {

    TestGame testGame;
    LwjglAWTCanvas canvas1;
    IRObjectAddedListener listener;
    DefaultMutableTreeNode root;
    private final IRPropertyGrid inspectorScene;
    private List<IRSpatial> sceneSpatials = new ArrayList<>();

    /**
     * Creates new form JPanelSceneEdit
     */
    /* public void removeNode(String name) {
     DefaultMutableTreeNode child = nodeWithName(name);
     if (child != null) {
     DefaultMutableTreeNode parent = (DefaultMutableTreeNode) child.getParent();
     parent.remove(child);
     }
     }

     public void attachToNode(IRSpatial spat) {
     if (spat.getParent().getName().equals("root node")) {
     root.add(new DefaultMutableTreeNode(spat.getName()));
     } else {
     DefaultMutableTreeNode parentNode = nodeWithName(spat.getParent().getName());
     if (parentNode == null) {
     parentNode = new DefaultMutableTreeNode(spat.getParent().getName());

     root.add(parentNode);
     }
     parentNode.add(new DefaultMutableTreeNode(spat.getName()));
     }
     }
     */
    /*DefaultMutableTreeNode nodeWithName(String name) {
     DefaultTreeModel dtm = (DefaultTreeModel) sceneTree.getModel();
     DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot();
     for (int i = 0; i < root.getChildCount(); i++) {
     DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) root.getChildAt(i);

     if (dmtn.toString().equals(name)) {
     return (DefaultMutableTreeNode) root.getChildAt(i);
     }
     }
     return null;
     }*/

    /* public void addAllChildren(IRNode parent) {
     for (IRSpatial spat : parent.getChildren()) {
     DefaultMutableTreeNode dmtn = nodeWithName(parent.getName());
     if (dmtn == null) {
     dmtn = new DefaultMutableTreeNode(spat.getParent().getName());
     root.add(dmtn);
     }
     dmtn.add(new DefaultMutableTreeNode(spat.getName()));
     if (spat instanceof IRNode) {
     IRNode irn = (IRNode) spat;
     addAllChildren(irn);
     }
     }
     }
     */
    public JPanelSceneEdit() {

        initComponents();

        canvas1 = new LwjglAWTCanvas(testGame = new TestGame());

        canvas1.getCanvas().setSize(glPanel.getSize());

        Map.edit_mode = true;

        inspectorScene = new IRPropertyGrid();

        inspectorScene.setSize(500, 500);

        jPanel6.add(inspectorScene);

        glPanel.add(canvas1.getCanvas(), BorderLayout.CENTER);
        DefaultListModel dlm = new DefaultListModel();
        //    DefaultTreeModel dtm = (DefaultTreeModel) sceneTree.getModel();
        // root = (DefaultMutableTreeNode) dtm.getRoot();
        DefaultListModel mod = new DefaultListModel();
        listener = new IRObjectAddedListener() {
            @Override
            public void action(IRSpatial spat) {
                /// if (spat instanceof IRNode) {

                //   IRNode irn = (IRNode) spat;
                // for (IRSpatial spat : testGame.rootNode.getChildren()) {
               /* if (spat.getParent() != null) {
                 if (spat.attachedToRoot) {
                 if (spat instanceof IRNode) {
                 attachToNode(spat);
                 } else if (spat instanceof IRGeom) {

                 if (spat.mesh != null) {
                 //dlm.addElement(spat.getName());
                 //jListObjects.setModel(dlm);
                 // root.add(new DefaultMutableTreeNode(spat.getName()));
                 attachToNode(spat);
                 } else if (spat.mesh == null) {
                 removeNode(spat.getName());
                 }
                 }

                 if (spat.mesh == null) {
                 removeNode(spat.getName());
                 }

                 } else if (!spat.attachedToRoot) {
                 removeNode(spat.getName());
                 }
                 dtm.reload();
                 }*/
                if (spat != null) {
                    if (spat.hasParent()) {
                        mod.addElement(spat.getName());
                        sceneSpatials.add(spat);
                    }
                }
            }

            @Override
            public void action() {
               // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        lstScene.setModel(mod);
        testGame.componentAddedListener = new IRListener() {
            @Override
            public void action() {
                updateListHier();

            }
        };
        testGame.sceneFileListener = listener;
    }

    void updateListHier() {
        DefaultListModel dlm = new DefaultListModel();
        for (GameComponent gc : testGame.components) {
            String enabledText = "";
            if (!gc.isEnabled()) {
                enabledText = " (disabled)";
            }
            dlm.addElement(gc.getName() + enabledText);
        }
        this.lstHier.setModel(dlm);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jSplitPane4 = new javax.swing.JSplitPane();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstScene = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        lstHier = new javax.swing.JList();
        jPanel5 = new javax.swing.JPanel();
        jSplitPane3 = new javax.swing.JSplitPane();
        glPanel = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOneTouchExpandable(true);

        jSplitPane4.setDividerLocation(300);
        jSplitPane4.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel6.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jPanel6ComponentResized(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 192, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
        );

        jSplitPane4.setRightComponent(jPanel6);

        lstScene.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstScene.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                lstSceneComponentResized(evt);
            }
        });
        lstScene.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstSceneValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(lstScene);

        jSplitPane4.setLeftComponent(jScrollPane1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Objects", jPanel1);

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        lstHier.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstHier.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstHierValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(lstHier);

        jSplitPane2.setLeftComponent(jScrollPane3);

        jPanel5.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jPanel5ComponentResized(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 192, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 476, Short.MAX_VALUE)
        );

        jSplitPane2.setRightComponent(jPanel5);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Hierarchy", jPanel2);

        jSplitPane1.setLeftComponent(jTabbedPane1);

        jSplitPane3.setDividerLocation(640);
        jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane3.setResizeWeight(1.0);
        jSplitPane3.setOneTouchExpandable(true);

        glPanel.addHierarchyBoundsListener(new java.awt.event.HierarchyBoundsListener() {
            public void ancestorMoved(java.awt.event.HierarchyEvent evt) {
            }
            public void ancestorResized(java.awt.event.HierarchyEvent evt) {
                glPanelAncestorResized(evt);
            }
        });
        glPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                glPanelComponentResized(evt);
            }
        });

        javax.swing.GroupLayout glPanelLayout = new javax.swing.GroupLayout(glPanel);
        glPanel.setLayout(glPanelLayout);
        glPanelLayout.setHorizontalGroup(
            glPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 749, Short.MAX_VALUE)
        );
        glPanelLayout.setVerticalGroup(
            glPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 465, Short.MAX_VALUE)
        );

        jSplitPane3.setTopComponent(glPanel);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 744, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab("Assets", jPanel3);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 744, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab("tab2", jPanel4);

        jSplitPane3.setRightComponent(jTabbedPane2);

        jSplitPane1.setRightComponent(jSplitPane3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void glPanelAncestorResized(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_glPanelAncestorResized
        // TODO add your handling code here:

    }//GEN-LAST:event_glPanelAncestorResized

    private void glPanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_glPanelComponentResized
        // TODO add your handling code here:
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                testGame.resize(glPanel.getWidth(), glPanel.getHeight());
                canvas1.getCanvas().setSize(glPanel.getSize());
            }
        });
    }//GEN-LAST:event_glPanelComponentResized

    private void lstHierValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstHierValueChanged
        

    }//GEN-LAST:event_lstHierValueChanged

    private void jPanel5ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel5ComponentResized
        
    }//GEN-LAST:event_jPanel5ComponentResized

    private void jPanel6ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel6ComponentResized
        // TODO add your handling code here:

    }//GEN-LAST:event_jPanel6ComponentResized
    private IRSpatial spatialWithName(String name) {
        for (IRSpatial spat : sceneSpatials) {
            if (spat.getName().equals(name)) {
                return spat;
            }
        }
        return null;
    }
    private void lstSceneValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstSceneValueChanged
        // TODO add your handling code here:
        IRSpatial spat = spatialWithName(lstScene.getSelectedValue().toString());
        if (spat != null) {
            this.inspectorScene.setProperties(spat);
        }

    }//GEN-LAST:event_lstSceneValueChanged

    private void lstSceneComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_lstSceneComponentResized
        // TODO add your handling code here:
        inspectorScene.setSize(lstScene.getSize());
    }//GEN-LAST:event_lstSceneComponentResized

    private GameComponent gcWithName(String name) {
        for (GameComponent gc : testGame.components) {
            if (gc.getName().equals(name)) {
                return gc;
            }
        }
        return null;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel glPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JSplitPane jSplitPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JList lstHier;
    private javax.swing.JList lstScene;
    // End of variables declaration//GEN-END:variables
}
