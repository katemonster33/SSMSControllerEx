package ssms.controller.inputhelper;

import org.lwjgl.util.vector.Vector2f;

public interface JoystickHandler {
    void performUpAction(float advance);
    void performDownAction(float advance);
    void performLeftAction(float advance);
    void performRightAction(float advance);
    void performAction(float advance, Vector2f stickVal);
}
