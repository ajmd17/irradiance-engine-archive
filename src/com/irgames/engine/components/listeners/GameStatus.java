/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.components.listeners;

/**
 *
 * @author Andrew
 */
public class GameStatus {
    public static String GameStatus_String = "Ready";
    static IRStatusChangedListener lstr;
    public static void set(String str) {
        GameStatus_String = str;
        if (lstr != null) {
            lstr.action(str);
        }
    }
    public static void setListener(IRStatusChangedListener irlstr) {
        lstr = irlstr;
    }
}
