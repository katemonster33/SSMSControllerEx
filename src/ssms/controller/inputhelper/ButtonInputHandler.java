package ssms.controller.inputhelper;

import ssms.controller.Buttons;
import ssms.controller.SSMSControllerModPluginEx;

public abstract class ButtonInputHandler {
    Buttons buttons;
    ButtonMode buttonMode;
    boolean buttonState = false;
    public ButtonInputHandler(Buttons buttons, ButtonMode buttonMode) {
        this.buttons = buttons;
        this.buttonMode = buttonMode;
    }

    void handleDownHold(float advance) {

    }

    public void advance(float advance) {
        switch(buttonMode) {
            case Up: if(SSMSControllerModPluginEx.controller.getButtonEvent(buttons) == -1) performAction(); break;
            case Down: if(SSMSControllerModPluginEx.controller.getButtonEvent(buttons) == 1) performAction(); break;
            case UpOrDown: if(SSMSControllerModPluginEx.controller.getButtonEvent(buttons) != 0) performAction(); break;
            case DownAndHold:
        }
    }

    public abstract void performAction();

    public enum ButtonMode {
        Down,
        Up,
        UpOrDown,
        DownAndHold
    }
}
