package ssms.controller.inputhelper;

public abstract class ButtonPressOrHoldHandler implements ButtonChangeHandler {
    boolean buttonInHeldState = false;
    final float buttonHoldTime = 2.f;
    float buttonDownTime = -1.f;
    boolean buttonState = false;
    @Override
    public void performAction(float advance, boolean buttonState) {
        this.buttonState = buttonState;
        if(!buttonState) {
            if(!buttonInHeldState) {
                performPressAction(advance);
            }
        }
        buttonDownTime = 0.f;
        buttonInHeldState = false;
    }

    public void advance(float amount) {
        if (buttonState && !buttonInHeldState) {
            buttonDownTime += amount;
            if(buttonDownTime > buttonHoldTime) {
                performHoldAction(amount);
                buttonInHeldState = true;
                buttonDownTime = -1.f;
            }
        }
    }

    public abstract void performHoldAction(float advance);

    public abstract void performPressAction(float advance);
}
