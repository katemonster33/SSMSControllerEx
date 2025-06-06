package ssms.controller.inputhelper;

import ssms.controller.Buttons;

public class ButtonInputHandler extends AbstractButtonInputHandler {
    Callback callback;
    public ButtonInputHandler(Buttons buttons, Callback callback) {
        super(buttons);
        this.callback = callback;
    }

    public ButtonInputHandler(Buttons buttons, ButtonMode buttonMode, Callback callback) {
        super(buttons, buttonMode);
        this.callback = callback;
    }

    @Override
    public void performAction(float advance) {
        if(callback != null) {
            callback.performAction(advance);
        }
    }

    public static interface Callback {
        void performAction(float advance);
    }
}
