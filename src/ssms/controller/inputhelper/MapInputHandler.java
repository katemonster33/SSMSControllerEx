package ssms.controller.inputhelper;

import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIComponentAPI;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.CrosshairRenderer;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.InputShim;
import ssms.controller.SSMSControllerModPluginEx;
import ssms.controller.reflection.MapReflector;

public class MapInputHandler{
    final float mouseMoveFactor = 4.f;
    boolean isMovingMap = false;
    Vector2f desiredMousePos;
    UIComponentAPI mapComponent;
    boolean leftStickActive = false, rightStickActive = false;
    Vector2f leftStick, rightStick;
    boolean handledJoystickEvent = false;
    CrosshairRenderer headingIndicator;
    public MapInputHandler(UIComponentAPI mapComponent) {
        this.mapComponent = mapComponent;

        headingIndicator = new CrosshairRenderer();
        headingIndicator.setSize(32, 32);
    }

    boolean isStickActive(ReadableVector2f stick) {
        return stick.getX() != 0 || stick.getY() != 0;
    }

    public void centerMousePos() {
        desiredMousePos = new Vector2f(mapComponent.getPosition().getCenterX(), mapComponent.getPosition().getCenterY());
        InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
    }

    public boolean isFocused() {
        return desiredMousePos != null &&
                desiredMousePos.x >= mapComponent.getPosition().getX() && desiredMousePos.x < mapComponent.getPosition().getX() + mapComponent.getPosition().getWidth() &&
                desiredMousePos.y >= mapComponent.getPosition().getY() && desiredMousePos.y < mapComponent.getPosition().getY() + mapComponent.getPosition().getHeight();
    }

    public boolean getIsMovingMap() {
        return isMovingMap;
    }

    public Vector2f getDesiredMousePos() {
        return desiredMousePos;
    }

    public void handleLeftJoystick(float advance, Vector2f joystickVal) {
        leftStickActive = isStickActive(joystickVal);
        leftStick = joystickVal;
        if(rightStickActive || isMovingMap) return;
        if(!isFocused()) {
            return;
        }

        desiredMousePos.set(desiredMousePos.getX() + (joystickVal.getX() * mouseMoveFactor), desiredMousePos.getY() - (joystickVal.getY() * mouseMoveFactor));
        InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
        handledJoystickEvent = true;
    }

    public void handleRightJoystick(float advance, Vector2f joystickVal) {
        rightStickActive = isStickActive(joystickVal);
        rightStick = joystickVal;
        if (leftStickActive) return;
        if(!isFocused()) {
            return;
        }
        if (rightStickActive) {
            if (!isMovingMap) {
                centerMousePos();
                InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
                isMovingMap = true;
            } else {
                desiredMousePos.set(desiredMousePos.getX() - (joystickVal.getX() * mouseMoveFactor), desiredMousePos.getY() + (joystickVal.getY() * mouseMoveFactor));
                InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
            }
        } else if (isMovingMap) {
            InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
            centerMousePos();
            isMovingMap = false;
        }
        handledJoystickEvent = true;
    }

    public void handleAButton(float advance, boolean buttonVal) {
        if(InputShim.getMouseX() != null && InputShim.getMouseY() != null) {
            if (buttonVal) {
                InputShim.mouseDown(InputShim.getMouseX(), InputShim.getMouseY(), InputEventMouseButton.LEFT);
            } else {
                InputShim.mouseUp(InputShim.getMouseX(), InputShim.getMouseY(), InputEventMouseButton.LEFT);
            }
        }
    }

    public void advance(float advance) {
        var controller = SSMSControllerModPluginEx.controller;
        if(controller.getButtonEvent(LogicalButtons.LeftTrigger) == 1) {
            InputShim.mouseWheel((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), -5);
        } else if(controller.getButtonEvent(LogicalButtons.RightTrigger) == 1) {
            InputShim.mouseWheel((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), 5);
        }
        if(!handledJoystickEvent) {
            if (leftStickActive) handleLeftJoystick(advance, leftStick);
            if (rightStickActive) handleRightJoystick(advance, rightStick);
        }
        handledJoystickEvent = false;
    }

    public void render() {
        if(InputShim.hasMouseControl() && !isMovingMap && desiredMousePos != null) {
            headingIndicator.setMousePos(desiredMousePos.x, desiredMousePos.y);
            headingIndicator.render();
        }
    }
}
