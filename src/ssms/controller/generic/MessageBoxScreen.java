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
    MessageBoxReflector dialogToReturnTo;
    List<ButtonAPI> dialogOptions;
    DirectionalUINavigator directionalUINavigator;

    public MessageBoxScreen() {
    }

    @Override
    public void activate(Object ...args) {
        controller = SSMSControllerModPluginEx.controller;
        dialogReflector = (MessageBoxReflector) args[0];
        uiToReturnTo = (String) args[1];
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        indicators = new ArrayList<>();
        dialogOptions = dialogReflector.getDialogButtons();
        if(!dialogOptions.isEmpty()) {
            //dialogOptions.get(0).highlight();
            directionalUINavigator = new DirectionalUINavigator(dialogOptions.stream().map(DirectionalUINavigator.NavigationObject::new).toList());
            addDigitalJoystickHandler("Navigate items", Joystick.DPad, directionalUINavigator);
            addButtonPressHandler("Select option", LogicalButtons.A, (float advance) -> clickButton());
        }
        addButtonPressHandler("Confirm", LogicalButtons.Y, new KeySender(Keyboard.KEY_RETURN));
        addButtonPressHandler("Dismiss", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
        return indicators;
    }

    public void clickButton()
    {
        if(directionalUINavigator != null && directionalUINavigator.getSelected() != null) {
            var btn = new ButtonReflector((ButtonAPI) directionalUINavigator.getSelected().component);

            if(btn.isCheckbox()) {
                btn.getButton().setChecked(!btn.getButton().isChecked());
            } else {
                dialogReflector.doActionPerformed(null, btn.getButton());
            }
        }
    }

    @Override
    public void preInput(float advance) {
        if(dialogReflector.isBeingDismissed()) {
            dialogOptions = null;
            if(Global.getCurrentState() == GameState.COMBAT) {
                InputScreenManager.getInstance().transitionToScope(BattleScope.ID, Global.getCombatEngine());
            } else if(dialogToReturnTo != null) {
                dialogReflector = dialogToReturnTo;
                dialogToReturnTo = null;
                refreshIndicators();
            } else {
                InputScreenManager.getInstance().transitionToScreen(uiToReturnTo);
            }
        }
        for(var child : dialogReflector.getChildPanels()) {
            if(MessageBoxReflector.isMsgBox(child)) {
                dialogToReturnTo = dialogReflector;
                dialogReflector = new MessageBoxReflector(child);
                refreshIndicators();
                return;
            }
        }
    }

    @Override
    public String getId() {
        return ID;
    }
}
