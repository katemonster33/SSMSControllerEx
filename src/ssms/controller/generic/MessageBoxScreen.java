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
import ssms.controller.reflection.ScrollPanelReflector;

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
        if(args.length > 0) {
            dialogReflector = (MessageBoxReflector) args[0];
            uiToReturnTo = (String) args[1];
            directionalUINavigator = new DirectionalUINavigator(new ArrayList<>());
        }
        indicators = null;
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = new ArrayList<>();
            List<DirectionalUINavigator.NavigationObject> navigationObjects = new ArrayList<>();
            List<ScrollPanelReflector> scrollPanelReflectors = new ArrayList<>();
            getPanelNavigatables(dialogReflector.getInnerPanel(), navigationObjects, scrollPanelReflectors);
            if (!navigationObjects.isEmpty()) {
                //dialogOptions.get(0).highlight();
                directionalUINavigator.setNavigationObjects(navigationObjects);
                addDirectionalUINavigator(directionalUINavigator);
            }
            addButtonPressHandler("Confirm", LogicalButtons.Y, new KeySender(Keyboard.KEY_RETURN));
            addButtonPressHandler("Dismiss", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
        }
        return indicators;
    }

    @Override
    public void preInput(float advance) {
        if (dialogReflector.isBeingDismissed()) {
            dialogOptions = null;
            if (Global.getCurrentState() == GameState.COMBAT) {
                InputScreenManager.getInstance().transitionToScope(BattleScope.ID, Global.getCombatEngine());
            } else if (dialogToReturnTo != null) {
                dialogReflector = dialogToReturnTo;
                dialogToReturnTo = null;
                refreshIndicators();
            } else {
                InputScreenManager.getInstance().transitionToScreen(uiToReturnTo);
            }
        }
        if (isCodexOpen()) {
            InputScreenManager.getInstance().transitionDelayed(CodexUI.ID, getId());
        }
        for (var child : dialogReflector.getChildPanels()) {
            if (MessageBoxReflector.isMsgBox(child)) {
                dialogToReturnTo = dialogReflector;
                dialogReflector = new MessageBoxReflector(child);
                refreshIndicators();
                return;
            }
        }
        if (directionalUINavigator != null) {
            directionalUINavigator.advance(advance);
        }
    }

    @Override
    public String getId() {
        return ID;
    }
}
