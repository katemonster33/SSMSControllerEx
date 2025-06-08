package ssms.controller.inputhelper;

import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.Buttons;
import ssms.controller.InputShim;
import ssms.controller.SSMSControllerModPluginEx;

public class MapInputHandler{
    final float mouseMoveFactor = 4.f;
    boolean isMovingMap = false;
    Vector2f desiredMousePos;
    ViewportAPI viewportAPI;
    public MapInputHandler(ViewportAPI viewportAPI) {
        this.viewportAPI = viewportAPI;
    }

    boolean isStickActive(ReadableVector2f stick) {
        return stick.getX() != 0 || stick.getY() != 0;
    }

    void centerMousePos() {
        desiredMousePos = new Vector2f(viewportAPI.convertWorldXtoScreenX(viewportAPI.getCenter().getX()), viewportAPI.convertWorldYtoScreenY(viewportAPI.getCenter().getY()));
        InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
    }

    public boolean getIsMovingMap() {
        return isMovingMap;
    }

    public void advance(float advance) {
        var controller = SSMSControllerModPluginEx.controller;
        ReadableVector2f leftStick = controller.getLeftStick(), rightStick = controller.getRightStick();
        boolean leftStickActive = isStickActive(leftStick), rightStickActive = isStickActive(rightStick);
        if (leftStickActive && !rightStickActive) {
            if(isMovingMap) {
                InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
                centerMousePos();
                isMovingMap = false;
            } else {
                desiredMousePos.set(desiredMousePos.getX() + (leftStick.getX() * mouseMoveFactor), desiredMousePos.getY() + (leftStick.getY() * mouseMoveFactor));
                InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
            }
        } else if(rightStickActive && !leftStickActive) {
            if(!isMovingMap) {
                centerMousePos();
                InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
                isMovingMap = true;
            } else {
                desiredMousePos.set(desiredMousePos.getX() - (rightStick.getX() * mouseMoveFactor), desiredMousePos.getY() - (rightStick.getY() * mouseMoveFactor));
                InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
            }
        }
        if(controller.getButtonEvent(Buttons.LeftTrigger) == 1) {
            InputShim.mouseWheel((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), 1);
        } else if(controller.getButtonEvent(Buttons.RightTrigger) == 1) {
            InputShim.mouseWheel((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), -1);
        } else if (controller.getButtonEvent(Buttons.A) == 1) {
            InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
        } else if (controller.getButtonEvent(Buttons.A) == -1) {
            InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.LEFT);
        }
    }
}
