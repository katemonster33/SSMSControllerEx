package ssms.controller.inputhelper;

import ssms.controller.InputShim;

public class KeySender implements ButtonPressHandler {
    int keyCode;
    char keyChar;
    public KeySender(int keyCode) {
        this.keyCode = keyCode;
        this.keyChar ='\0';
    }

    public KeySender(int keyCode, char keyChar) {
        this.keyCode = keyCode;
        this.keyChar = keyChar;
    }

    public void performAction(float advance) {
        InputShim.keyDownUp(keyCode, keyChar);
    }
}
