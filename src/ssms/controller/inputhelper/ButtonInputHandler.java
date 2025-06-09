package ssms.controller.inputhelper;

import ssms.controller.enums.LogicalButtons;

public class ButtonInputHandler extends AbstractButtonInputHandler {
    Callback callback;
    public ButtonInputHandler(LogicalButtons logicalButtons, Callback callback) {
        super(logicalButtons);
        this.callback = callback;
    }

    public ButtonInputHandler(LogicalButtons logicalButtons, ButtonMode buttonMode, Callback callback) {
        super(logicalButtons, buttonMode);
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
