/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.irgames.engine.components;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.irgames.engine.components.GameCam.CameraMode;
import static com.irgames.engine.components.GameCam.CameraMode.chase;
import com.irgames.engine.controls.CameraControl;
import com.irgames.engine.game.Game;
import com.irgames.utils.MathUtil;
import java.awt.MouseInfo;
import java.awt.Point;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;

/**
 *
 * @author Andrew
 */
public class GameCam extends PerspectiveCamera {

    private float camFov = 65;
    private float camNear = 1f;
    private float camFar = 300;
    private Vector3 pos = new Vector3();
    private boolean mouseDragging = false;
    private float mouseSensitivity = 0.75f;
    private float delta;
    private boolean enabled = true;
    private boolean cursorVisible = true;
    public Vector3 point;
    float zoomTo = 7, zoomOld = 7, zoomSpeed = 5;
    float zoom = 7;
    List<CameraControl> controls = new ArrayList<>();
    /*
    
    
    * @return butthole 
    
    
    */
    
    private void huh() {
        
    }
    
    public void addControl(CameraControl ctrl) {
        this.hu
        if (!controls.contains(ctrl)) {
            controls.add(ctrl);
            ctrl.init();
        }
    }
    public void removeControl(CameraControl ctrl) {
         if (controls.contains(ctrl)) {
            controls.remove(ctrl);
        }
    }
    public void setSensitivity(float sensitivity) {
        mouseSensitivity = sensitivity;
    }
    
    public void setSmoothing(float smoothingVal) {
        smoothing = ((1.0f - smoothingVal) + 1.0f);
    }

    public enum CameraMode {

        drag,
        firstperson,
        chase
    }
    private CameraMode camMode = CameraMode.drag;

    public GameCam setDragMode() {
        camMode = CameraMode.drag;
        oldMouseX = Gdx.input.getX();
        oldMouseY = Gdx.input.getY();
        return this;
    }

    public GameCam setFPSMode() {
        Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        camMode = CameraMode.firstperson;
        oldMouseX = Gdx.input.getX();
        oldMouseY = Gdx.input.getY();
        return this;
    }

    public GameCam setChaseMode(Vector3 point) {
        this.point = point;
        camMode = CameraMode.chase;
        return this;
    }

    public void resize(int width, int height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
    }
    static org.lwjgl.input.Cursor emptyCursor;

    public static void setHWCursorVisible(boolean visible) throws LWJGLException {
        if (Gdx.app.getType() != ApplicationType.Desktop && Gdx.app instanceof LwjglApplication) {
            return;
        }
        if (emptyCursor == null) {
            if (Mouse.isCreated()) {
                int min = org.lwjgl.input.Cursor.getMinCursorSize();
                IntBuffer tmp = BufferUtils.createIntBuffer(min * min);
                emptyCursor = new org.lwjgl.input.Cursor(min, min, min / 2, min / 2, 1, tmp, null);
            } else {
                throw new LWJGLException(
                        "Could not create empty cursor before Mouse object is created");
            }
        }
        if (Mouse.isInsideWindow()) {
            Mouse.setNativeCursor(visible ? null : emptyCursor);
        }
    }

    public void onLeftClick(int screenX, int screenY) {

    }

    public void onRightClick(int screenX, int screenY) {

    }

    public void onDragLeft() {

    }

    public void onDragRight() {

    }

    public void onTouchUp() {

    }

    public void onScroll(int amt) {

    }

    public GameCam(float fov) {
        
        super(fov, Gdx.graphics.getWidth(), Gdx.graphics.getWidth());
        
        
        
        
        position.set(pos);
        // lookAt(0, 0, 0);
        near = camNear;
        far = camFar;

        update();

        Gdx.input.setInputProcessor(new InputAdapter() {
            int btn = 0;
            Vector3 tmpVector = new Vector3();
            Quaternion q = new Quaternion();

            @Override
            public boolean touchUp(int x, int y, int pointer, int button) {
                boolean res = super.touchUp(x, y, pointer, button);
                if (camMode == CameraMode.drag || camMode == CameraMode.chase) {
                    mouseDragging = false;
                    if (camMode == CameraMode.chase) {
                        Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
                    }
                }
                onTouchUp();
                return res;
            }

            @Override
            public boolean scrolled(int amount) {
                if (camMode == CameraMode.chase) {
                    zoomTo += amount;
                    zoomTo = MathUtils.clamp(zoomTo, 2.5f, 25);
                }
                onScroll(amount);
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (btn == Input.Buttons.LEFT) {

                    if (camMode == CameraMode.drag || camMode == CameraMode.chase) {
                        mouseDragging = true;
                    }

                    onDragLeft();
                } else if (btn == Input.Buttons.RIGHT) {
                    onDragRight();
                }

                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                btn = button;
                if (button == Input.Buttons.LEFT) {
                    if (camMode == chase) {
                        Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
                    }
                    onLeftClick(screenX, screenY);

                    return true;
                } else if (button == Input.Buttons.RIGHT) {
                    onRightClick(screenX, screenY);
                    return true;
                }
                return false;
            }
        });
    }
    int oldX = 0;
    int oldY = 0;
    float distance = 35.0f;
    boolean zooming = false;
    float maxFov = 55;
    float minFov = 45;

    Quaternion camRot = new Quaternion();

    private void handleInput() {

        if (camMode == CameraMode.firstperson) {
            //if (Gdx.input.getX() != oldMouseX || Gdx.input.getY() != oldMouseY) {
            mouseMoved(Gdx.input.getX(), Gdx.input.getY(), Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
            //}
        } else if (camMode == CameraMode.drag || camMode == CameraMode.chase) {
            if (mouseDragging) {
                mouseMoved(Gdx.input.getX(), Gdx.input.getY(), Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
            }
        }
        direction.y = MathUtils.clamp(direction.y, -0.95f, 0.95f);
  
        if (camMode != CameraMode.chase) {
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                
                position.x -= delta * distance * (float) Math.sin(Math.toRadians(view.getRotation(camRot).getYaw() - 90));
                position.z += delta * distance * (float) Math.cos(Math.toRadians(view.getRotation(camRot).getYaw() - 90));
            }
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                position.x -= delta * distance * (float) Math.sin(Math.toRadians(view.getRotation(camRot).getYaw() + 90));
                position.z += delta * distance * (float) Math.cos(Math.toRadians(view.getRotation(camRot).getYaw() + 90));

            }
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {

                position.sub(MathUtil.multVector3(direction.cpy(), delta * distance));
            }
            if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {

                position.add(MathUtil.multVector3(direction.cpy(), delta * distance));
            } else {
            }

        }
        if (camMode == CameraMode.chase) {

            if (zoomTo != zoom) {
                zoom = MathUtils.lerp(zoomOld, zoomTo, delta * zoomSpeed);
                zoomOld = zoom;
            }

            position.set(point.cpy().sub(new Vector3(direction).scl(zoom)));
        }
        update();

        oldMouseX = Gdx.input.getX();
        oldMouseY = Gdx.input.getY();
    }
    float oldMagX, oldMagY;
    float rotSpeed;
    public float smoothing = 1.0f;
    float globalTime = 0f;

    private void doRotateCam(float sensitivity, int mouseX, int mouseY, int screenX, int screenY) {
        if (globalTime < 1) {
            globalTime += delta*1.3f;
        }
        if (camMode == CameraMode.drag || camMode == CameraMode.chase) {
            rotSpeed = delta * 30.0f;
        } else if (camMode == CameraMode.firstperson) {
            rotSpeed = (1.0f - Gdx.graphics.getDeltaTime()) * 0.1f;
        }
        float magX, magY;

        if (camMode == CameraMode.firstperson) {
            float xdiff = oldMagX - mouseX;
            float ydiff = oldMagY - mouseY;
            
            if (xdiff < 0.001f && xdiff > -0.001f || (globalTime < 1) || smoothing == 0.0f) { // add a slight delay until smoothing begins, else the camera flips out
                magX = mouseX;
            } else {

                magX = MathUtils.lerp(oldMagX, (float) mouseX, rotSpeed * smoothing);

            }
            if (ydiff < 0.001f && ydiff > -0.001f || (globalTime < 1) || smoothing == 0.0f) {
                magY = mouseY;
            } else {

                magY = MathUtils.lerp(oldMagY, (float) mouseY, rotSpeed * smoothing);

            }

            oldMagX = magX;
            oldMagY = magY;
            magX -= (float) Gdx.graphics.getWidth() / 2f;
            magY -= (float) Gdx.graphics.getHeight() / 2f;
            magY = -magY;

        } else {
            magX = (float) (mouseX) / (float) Gdx.graphics.getWidth();
            magY = (float) (mouseY) / (float) Gdx.graphics.getHeight();
            magX -= 0.5f;
            magY -= 0.5f;
            magY = -magY;

        }
   
        rotate(Vector3.Y, -magX * sensitivity * rotSpeed);
        rotate(direction.cpy().crs(Vector3.Y), 1 * magY * sensitivity * rotSpeed);
        if (direction.y > 0.95f || direction.y < -0.95f) { // negate and re-rerotate 
            magY *= -1f;
            rotate(direction.cpy().crs(Vector3.Y), 1 * magY * sensitivity * rotSpeed);
        }

    }

    int oldMouseX = 0;
    int oldMouseY = 0;

    public boolean mouseMoved(int mouseX, int mouseY, int screenX, int screenY) {

        if (camMode != CameraMode.drag && camMode != CameraMode.chase) {
            this.doRotateCam(mouseSensitivity, mouseX, mouseY, screenX, screenY);
            Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        } else if (camMode == CameraMode.drag || camMode == CameraMode.chase) {
            if (this.mouseDragging) {
                this.doRotateCam(mouseSensitivity * 7.5f, mouseX, mouseY, screenX, screenY);
            }

        }

        return true;
    }

    public void disable() {
        enabled = false;
    }

    public void enable() {
        enabled = true;
    }

    public void updateCam() {
        delta = Gdx.graphics.getDeltaTime();
        
        for (CameraControl ctrl : controls) {
            ctrl.update();
        }
        
        if (camMode == CameraMode.firstperson) {
            cursorVisible = false;
        } else {
            if (!mouseDragging) {
                cursorVisible = true;
            } else {
                cursorVisible = false;
            }
        }
        try {
            setHWCursorVisible(cursorVisible);
        } catch (LWJGLException e) {

        }

        if (enabled) {
            handleInput();
        }
    }

}
