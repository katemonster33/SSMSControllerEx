package ssms.controller.generic;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.combat.BattleScope;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.reflection.ClassReflector;
import ssms.controller.reflection.MessageBoxReflector;
import ssms.controller.reflection.MethodReflector;

import java.util.ArrayList;
import java.util.List;

public class MessageBoxScreen extends InputScreenBase {
    public static final String ID = "MessageBox";
    List<Pair<Indicators, String>> indicators;
    String uiToReturnTo;
    HandlerController controller;
    MessageBoxReflector dialogReflector;
    List<ButtonAPI> dialogOptions;
    int selectedButton = -1;
    Object getButtonCheckboxRenderer;

    public MessageBoxScreen() {
    }

    @Override
    public void activate(Object ...args) {
        selectedButton = -1;
        controller = SSMSControllerModPluginEx.controller;
        dialogReflector = (MessageBoxReflector) args[0];
        uiToReturnTo = (String) args[1];
        dialogOptions = dialogReflector.getDialogButtons();

        indicators = new ArrayList<>();
        if(!dialogOptions.isEmpty()) {
            selectedButton = 0;
            dialogOptions.get(0).highlight();
            indicators.add(new Pair<>(Indicators.LeftStick, "Navigate items"));
            indicators.add(new Pair<>(Indicators.A, "Confirm option"));
        }
        indicators.add(new Pair<>(Indicators.B, "Dismiss"));
    }


    public void selectNextButton()
    {
        if(dialogOptions != null && !dialogOptions.isEmpty()) {
            int oldSelectedButton = selectedButton;
            if(selectedButton == -1) {
                selectedButton = 0;
            } else if(selectedButton < (dialogOptions.size() - 1)) {
                selectedButton++;
            }
            if(!dialogOptions.get(selectedButton).isEnabled()) {
                selectedButton++;
            }
            if(selectedButton >= dialogOptions.size()) {
                selectedButton = 0;
            }
            if(selectedButton != oldSelectedButton && oldSelectedButton != -1) {
                dialogOptions.get(oldSelectedButton).unhighlight();
            }
            dialogOptions.get(selectedButton).highlight();
        }
    }

    public void selectPrevButton()
    {
        if(dialogOptions != null && !dialogOptions.isEmpty()) {
            int oldSelectedButton = selectedButton;
            if(selectedButton == -1) {
                selectedButton = 0;
            } else if(selectedButton > 0) {
                selectedButton--;
            }
            if(!dialogOptions.get(selectedButton).isEnabled()) {
                selectedButton--;
            }
            if(selectedButton < 0) {
                selectedButton = dialogOptions.size() - 1;
            }
            if(selectedButton != oldSelectedButton && oldSelectedButton != -1) {
                dialogOptions.get(oldSelectedButton).unhighlight();
            }
            dialogOptions.get(selectedButton).highlight();
        }
    }

    public void clickButton()
    {
        if(selectedButton != -1 && dialogOptions != null && selectedButton < dialogOptions.size()) {
            var btn = dialogOptions.get(selectedButton);
            if(getButtonCheckboxRenderer == null) {
                try {
                    getButtonCheckboxRenderer = ClassReflector.GetInstance().findDeclaredMethod(btn.getClass(), "getRendererCheckbox");
                } catch(Throwable ex) {
                    Global.getLogger(getClass()).warn("Couldn't infer checkbox rendering method from button class!", ex);
                }
            }
            boolean isCheckbox = false;
            try {
                isCheckbox = MethodReflector.GetInstance().invoke(getButtonCheckboxRenderer, btn) != null;
            } catch(Throwable ex) {
                Global.getLogger(getClass()).warn("Couldn't tell if button is a check box! :(", ex);
            }
            if(isCheckbox) {
                btn.setChecked(!btn.isChecked());
            } else {
                dialogReflector.doActionPerformed(null, dialogOptions.get(selectedButton));
            }
        }
    }

    @Override
    public void preInput(float advance) {
        if(dialogReflector.isBeingDismissed()) {
            dialogOptions = null;
            if(Global.getCurrentState() == GameState.COMBAT) {
                InputScreenManager.getInstance().transitionToScope(BattleScope.ID, Global.getCombatEngine());
            } else {
                InputScreenManager.getInstance().transitionToScreen(uiToReturnTo);
            }
        }
        if(controller.getButtonEvent(LogicalButtons.LeftStickDown) == 1) {
            selectNextButton();
        } else if(controller.getButtonEvent(LogicalButtons.LeftStickUp) == 1) {
            selectPrevButton();
        } else if(controller.getButtonEvent(LogicalButtons.A) == 1) {
            clickButton();
        } else if(controller.getButtonEvent(LogicalButtons.B) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
        } else if(controller.getButtonEvent(LogicalButtons.Y) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_RETURN, '\0');
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        return indicators;
    }
}
