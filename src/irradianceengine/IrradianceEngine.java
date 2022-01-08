/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irradianceengine;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.irgames.engine.game.TestGame;

/**
 *
 * @author Andrew
 */
public class IrradianceEngine {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.foregroundFPS = 1000;
        new LwjglApplication(new TestGame(), config);
    }

}
