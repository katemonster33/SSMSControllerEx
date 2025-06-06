package ssms.controller.inputhelper;

import ssms.controller.Buttons;
import ssms.controller.InputShim;

public class KeySender extends AbstractButtonInputHandler {
    int keyCode;
    char keyChar;
    public KeySender(Buttons controllerButton, int keyCode) {
        this(controllerButton, AbstractButtonInputHandler.ButtonMode.Down, keyCode, '\0');
    }

    public KeySender(Buttons controllerButton, int keyCode, char keyChar) {
        this(controllerButton, ButtonMode.Down, keyCode, keyChar);
    }

    public KeySender(Buttons controllerButton, AbstractButtonInputHandler.ButtonMode buttonMode, int keyCode, char keyChar) {
        super(controllerButton, buttonMode);
        this.keyCode = keyCode;
        this.keyChar = keyChar;
    }

    @Override
    public void performAction(float advance) {
        InputShim.keyDownUp(keyCode, keyChar);
    }
}
