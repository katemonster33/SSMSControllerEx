package ssms.controller.inputhelper;

import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.InputShim;
import ssms.controller.SSMSControllerModPluginEx;

public class MapInputHandler{
    final float mouseMoveFactor = 4.f;
    boolean isMovingMap = false;
    Vector2f desiredMousePos;
    ViewportAPI viewportAPI;
    boolean leftStickActive = false, rightStickActive = false;
    Vector2f leftStick, rightStick;
    boolean handledJoystickEvent = false;
    public MapInputHandler(ViewportAPI viewportAPI) {
        this.viewportAPI = viewportAPI;
    }

    boolean isStickActive(ReadableVector2f stick) {
        return stick.getX() != 0 || stick.getY() != 0;
    }

    public void centerMousePos() {
        desiredMousePos = new Vector2f(viewportAPI.convertWorldXtoScreenX(viewportAPI.getCenter().getX()), viewportAPI.convertWorldYtoScreenY(viewportAPI.getCenter().getY()));
        InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
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

        desiredMousePos.set(desiredMousePos.getX() + (joystickVal.getX() * mouseMoveFactor), desiredMousePos.getY() - (joystickVal.getY() * mouseMoveFactor));
        InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
        handledJoystickEvent = true;
    }

    public void handleRightJoystick(float advance, Vector2f joystickVal) {
        rightStickActive = isStickActive(joystickVal);
        rightStick = joystickVal;
        if (leftStickActive) return;
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
        if (buttonVal) {
            InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
        } else {
            InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
        }
    }

    public void advance(float advance) {
        var controller = SSMSControllerModPluginEx.controller;
        if(controller.getButtonEvent(LogicalButtons.LeftTrigger) == 1) {
            InputShim.mouseWheel((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), 1);
        } else if(controller.getButtonEvent(LogicalButtons.RightTrigger) == 1) {
            InputShim.mouseWheel((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), -1);
        }
        if(!handledJoystickEvent) {
            if (leftStickActive) handleLeftJoystick(advance, leftStick);
            if (rightStickActive) handleRightJoystick(advance, rightStick);
        }
        handledJoystickEvent = false;
    }
}
