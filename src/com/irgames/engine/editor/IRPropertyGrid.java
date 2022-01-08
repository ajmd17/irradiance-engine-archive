/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.irgames.engine.components.IRObject;
import com.irgames.engine.components.IRProperty;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Andrew
 */
public class IRPropertyGrid extends JTable {

    IRObject obj;

    class IRPropInfo {

        IRProperty prop;
        int row;

        public IRPropInfo(IRProperty prop, int row) {
            this.prop = prop;
            this.row = row;
        }
    }
    List<IRPropInfo> propInfo = new ArrayList<>();
    private static final long serialVersionUID = 1L;
    private Class editingClass;
    private static String[] columnNames = {"Type", "Value"};
    private static Object[][] data = {
        {"String", "I'm a string"}};

    public IRPropertyGrid() {
        //super(data, columnNames);
        // setData(data);DefaultTableModel model = new DefaultTableModel();

    }

    public void setProperties(List<IRProperty> properties) {

        Object[][] object = new Object[properties.size()][2];
        for (int i = 0; i < properties.size(); i++) {
            IRProperty prop = properties.get(i);
            propInfo.add(new IRPropInfo(prop, i));

            object[i] = new Object[]{prop.name, prop.value.toString()};
        }
        DefaultTableModel model = new DefaultTableModel(object, columnNames);

        this.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {

            }
        });
        setModel(model);
    }

    public String objToString(Object obj) {
        if (obj instanceof Vector3) {
            Vector3 v3 = (Vector3) obj;
            return v3.x + "," + v3.y + "," + v3.z;
        } else if (obj instanceof Color) {
            Color v3 = (Color) obj;
            return v3.r + "," + v3.g + "," + v3.b + "," + v3.a;
        } else {
            return obj.toString();
        }
    }

    public void setProperties(IRObject obj) {
        this.obj = obj;
        propInfo.clear();

        List<IRProperty> properties = obj.getProperties();
        List<Object> list = new ArrayList<>();
        Object[][] object = new Object[properties.size()][2];
        for (int i = 0; i < properties.size(); i++) {
            IRProperty prop = properties.get(i);
            propInfo.add(new IRPropInfo(prop, i));

            object[i] = new Object[]{prop.name, objToString(prop.value)};
            //list.add(prop.name);
            //list.add(prop.value);
        }
        DefaultTableModel model = new DefaultTableModel(object, columnNames);

        this.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {

            }
        });

        model.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {

                try {
                    IRProperty prop = getProp(e.getFirstRow());

                    String strVal = IRPropertyGrid.this.getValueAt(e.getFirstRow(), 1).toString();
                    if (prop.value instanceof Vector3) {
                        obj.setProperty(prop.name, toVector3(strVal), true);
                    } else if (prop.value instanceof Float) {
                        obj.setProperty(prop.name, toFloat(strVal), true);
                    } else if (prop.value instanceof Boolean) {
                        obj.setProperty(prop.name, toBoolean(strVal), true);
                    } else if (prop.value instanceof Color) {
                        obj.setProperty(prop.name, toColor(strVal), true);
                    } else {
                        obj.setProperty(prop.name, strVal, true);
                    } 
                    obj.setUpdateNeeded();
                    System.out.println("Set value of " + prop.name + " to " + prop.value);
                } catch (Exception ex) {

                }
            }

        });
        setModel(model);
    }
    private Color toColor(String value) {
        Color parsed = new Color();

        String txt = value;
        txt = txt.replace("[", "");
        txt = txt.replace("]", "");
        String[] spl = txt.split(",");
        parsed.set(Float.parseFloat(spl[0]), Float.parseFloat(spl[1]), Float.parseFloat(spl[2]), Float.parseFloat(spl[3]));

        return parsed;
    }
    private Vector3 toVector3(String value) {
        Vector3 parsed = new Vector3();

        String txt = value;
        txt = txt.replace("[", "");
        txt = txt.replace("]", "");
        String[] spl = txt.split(",");
        parsed.set(Float.parseFloat(spl[0]), Float.parseFloat(spl[1]), Float.parseFloat(spl[2]));

        return parsed;
    }

    private Float toFloat(String value) {
        return Float.parseFloat(value);
    }

    private Boolean toBoolean(String value) {
        return Boolean.parseBoolean(value);
    }

    private IRProperty getProp(String name) {
        for (IRPropInfo prop : this.propInfo) {
            if (prop.prop.name.equals(name)) {
                return prop.prop;
            }
        }
        return null;
    }

    private IRProperty getProp(int row) {
        for (IRPropInfo prop : this.propInfo) {
            if (prop.row == row) {
                return prop.prop;
            }
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (column == 0) {
            return false;
        } else {
            IRProperty prop = getProp(row);
            return prop.editable;
        }
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column
    ) {
        editingClass = null;
        int modelColumn = convertColumnIndexToModel(column);
        if (modelColumn == 1) {
            Class rowClass = getModel().getValueAt(row, modelColumn).getClass();
            return getDefaultRenderer(rowClass);
        } else {
            return super.getCellRenderer(row, column);
        }
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column
    ) {
        editingClass = null;
        int modelColumn = convertColumnIndexToModel(column);
        if (modelColumn == 1) {
            editingClass = getModel().getValueAt(row, modelColumn).getClass();
            return getDefaultEditor(editingClass);
        } else {
            return super.getCellEditor(row, column);
        }
    }
    //  This method is also invoked by the editor when the value in the editor
    //  component is saved in the TableModel. The class was saved when the
    //  editor was invoked so the proper class can be created.

    @Override
    public Class getColumnClass(int column
    ) {
        return editingClass != null ? editingClass : super.getColumnClass(column);
    }

}
