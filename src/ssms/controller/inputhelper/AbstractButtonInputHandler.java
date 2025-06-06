package ssms.controller.inputhelper;

import ssms.controller.Buttons;
import ssms.controller.SSMSControllerModPluginEx;

public abstract class AbstractButtonInputHandler {
    Buttons buttons;
    ButtonMode buttonMode;
    boolean buttonState = false;
    public AbstractButtonInputHandler(Buttons buttons) {
        this(buttons, ButtonMode.Down);
    }

    public AbstractButtonInputHandler(Buttons buttons, ButtonMode buttonMode) {
        this.buttons = buttons;
        this.buttonMode = buttonMode;
    }

    public Buttons getButtons() {
        return buttons;
    }

    public ButtonMode getButtonMode() {
        return buttonMode;
    }

    void handleDownHold(float advance) {

    }

    public void advance(float advance) {
        switch(buttonMode) {
            case Up: if(SSMSControllerModPluginEx.controller.getButtonEvent(buttons) == -1) performAction(advance); break;
            case Down: if(SSMSControllerModPluginEx.controller.getButtonEvent(buttons) == 1) performAction(advance); break;
            case UpOrDown: if(SSMSControllerModPluginEx.controller.getButtonEvent(buttons) != 0) performAction(advance); break;
            case DownAndHold: handleDownHold(advance);
        }
    }

    public abstract void performAction(float advance);

    public enum ButtonMode {
        Down,
        Up,
        UpOrDown,
        DownAndHold
    }
}
