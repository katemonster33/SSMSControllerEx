package ssms.controller.inputhelper;

import ssms.controller.enums.LogicalButtons;
import ssms.controller.SSMSControllerModPluginEx;

public abstract class AbstractButtonInputHandler {
    ButtonMode buttonMode;
    boolean buttonState = false;
    public AbstractButtonInputHandler() {
        this(ButtonMode.Down);
    }

    public AbstractButtonInputHandler(ButtonMode buttonMode) {
        this.buttonMode = buttonMode;
    }

    public LogicalButtons getButtons() {
        return logicalButtons;
    }

    public ButtonMode getButtonMode() {
        return buttonMode;
    }

    void handleDownHold(float advance) {

    }

    public void advance(float advance) {
    }

    public void handleEvent(boolean newState) {

        switch(buttonMode) {
            case Up: if(!newState) performAction(0.f); break;
            case DownAndHold: handleDownHold(0.f);
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
