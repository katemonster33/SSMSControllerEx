package ssms.controller.inputhelper;

import ssms.controller.Buttons;
import ssms.controller.InputShim;

public class KeySender extends ButtonInputHandler {
    int keyCode;
    char keyChar;
    public KeySender(Buttons controllerButton, int keyCode) {
        this(controllerButton, ButtonInputHandler.ButtonMode.Down, keyCode, '\0');
    }
    public KeySender(Buttons controllerButton, ButtonInputHandler.ButtonMode buttonMode, int keyCode, char keyChar) {
        super(controllerButton, buttonMode);
        this.keyCode = keyCode;
        this.keyChar = keyChar;
    }

    @Override
    public void performAction() {
        InputShim.keyDownUp(keyCode, keyChar);
    }
}
