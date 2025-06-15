package ssms.controller.generic;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.combat.BattleScope;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.ButtonReflector;
import ssms.controller.reflection.MessageBoxReflector;

import java.util.ArrayList;
import java.util.List;

public class MessageBoxScreen extends InputScreenBase {
    public static final String ID = "MessageBox";
    String uiToReturnTo;
    HandlerController controller;
    MessageBoxReflector dialogReflector;
    List<ButtonAPI> dialogOptions;
    DirectionalUINavigator directionalUINavigator;
    Object getButtonCheckboxRenderer;

    public MessageBoxScreen() {
    }

    @Override
    public void activate(Object ...args) {
        controller = SSMSControllerModPluginEx.controller;
        dialogReflector = (MessageBoxReflector) args[0];
        uiToReturnTo = (String) args[1];
        dialogOptions = dialogReflector.getDialogButtons();

        indicators = new ArrayList<>();
        if(!dialogOptions.isEmpty()) {
            dialogOptions.get(0).highlight();
            List<Pair<UIComponentAPI, Object>> directionalUiElements = new ArrayList<>();
            for(var btn : dialogOptions) {
                directionalUiElements.add(new Pair<>(btn, null));
            }
            directionalUINavigator = new DirectionalUINavigator(directionalUiElements);
            addDigitalJoystickHandler("Navigate items", Joystick.DPad, directionalUINavigator);
            addButtonPressHandler("Select option", LogicalButtons.A, (float advance) -> clickButton());
        }
        addButtonPressHandler("Confirm", LogicalButtons.Y, new KeySender(Keyboard.KEY_RETURN));
        addButtonPressHandler("Dismiss", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
    }

    public void clickButton()
    {
        if(directionalUINavigator != null && directionalUINavigator.getSelected() != null) {
            var btn = (ButtonAPI) directionalUINavigator.getSelected().one;

            if(ButtonReflector.isCheckbox(btn)) {
                btn.setChecked(!btn.isChecked());
            } else {
                dialogReflector.doActionPerformed(null, btn);
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
    }

    @Override
    public String getId() {
        return ID;
    }
}
