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

    public void advance(float advance) {
        var controller = SSMSControllerModPluginEx.controller;
        ReadableVector2f leftStick = controller.getLeftStick(), rightStick = controller.getRightStick();
        if((isMovingMap && (leftStick.getX() != 0 || leftStick.getY() != 0)) ||
            (!isMovingMap && (rightStick.getX() != 0 || rightStick.getY() != 0))) {
            desiredMousePos = new Vector2f(viewportAPI.convertWorldXtoScreenX(viewportAPI.getCenter().getX()), viewportAPI.convertWorldYtoScreenY(viewportAPI.getCenter().getY()));
        }
        if (leftStick.getX() != 0 || leftStick.getY() != 0) {
            if (desiredMousePos == null) {
                desiredMousePos = new Vector2f(viewportAPI.convertWorldXtoScreenX(viewportAPI.getCenter().getX()), viewportAPI.convertWorldYtoScreenY(viewportAPI.getCenter().getY()));
            } else {
                desiredMousePos.set(desiredMousePos.getX() + (leftStick.getX() * mouseMoveFactor), desiredMousePos.getY() + (leftStick.getY() * mouseMoveFactor));
            }
            InputShim.mouseMove((int) desiredMousePos.getX(), (int) desiredMousePos.getY());
        }
        if(!mouseDown && controller.getButtonEvent(Buttons.LeftStickButton) == 1) {
            if(!movingMap) {
                InputShim.mouseDown((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
            } else {
                InputShim.mouseUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
            }
            movingMap = !movingMap;
        }
    }
}
