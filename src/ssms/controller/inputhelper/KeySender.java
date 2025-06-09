package ssms.controller.inputhelper;

import ssms.controller.enums.LogicalButtons;
import ssms.controller.InputShim;

public class KeySender extends AbstractButtonInputHandler {
    int keyCode;
    char keyChar;
    public KeySender(LogicalButtons controllerButton, int keyCode) {
        this(controllerButton, AbstractButtonInputHandler.ButtonMode.Down, keyCode, '\0');
    }

    public KeySender(LogicalButtons controllerButton, int keyCode, char keyChar) {
        this(controllerButton, ButtonMode.Down, keyCode, keyChar);
    }

    public KeySender(LogicalButtons controllerButton, AbstractButtonInputHandler.ButtonMode buttonMode, int keyCode, char keyChar) {
        super(controllerButton, buttonMode);
        this.keyCode = keyCode;
        this.keyChar = keyChar;
    }

    @Override
    public void performAction(float advance) {
        InputShim.keyDownUp(keyCode, keyChar);
    }
}
