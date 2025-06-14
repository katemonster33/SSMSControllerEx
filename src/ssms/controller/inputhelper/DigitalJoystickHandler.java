package ssms.controller.inputhelper;

public interface DigitalJoystickHandler {
    void performUpAction(float advance);
    void performDownAction(float advance);
    void performLeftAction(float advance);
    void performRightAction(float advance);
}
