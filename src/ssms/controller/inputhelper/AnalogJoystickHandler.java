package ssms.controller.inputhelper;

import org.lwjgl.util.vector.Vector2f;

public interface AnalogJoystickHandler {
    void performAction(float advance, Vector2f joystickVal);
}
