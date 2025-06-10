package ssms.controller.inputhelper;

import ssms.controller.enums.LogicalButtons;
import ssms.controller.InputShim;

public class KeySender extends AbstractButtonInputHandler {
    int keyCode;
    char keyChar;
    public KeySender(int keyCode) {
        this(keyCode, '\0');
    }

    public KeySender(int keyCode, char keyChar) {
        super();
        this.keyCode = keyCode;
        this.keyChar = keyChar;
    }

    @Override
    public void performAction(float advance) {
        InputShim.keyDownUp(keyCode, keyChar);
    }
}
