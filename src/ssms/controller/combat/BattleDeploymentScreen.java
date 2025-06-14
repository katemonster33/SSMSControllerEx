package ssms.controller.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import ssms.controller.*;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.MessageBoxScreen;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.reflection.*;

import java.util.ArrayList;
import java.util.List;

public class BattleDeploymentScreen extends InputScreenBase {
    public static final String ID = "BattleDeployment";
    DeploymentUiReflector dui; // hehe, dui
    DirectionalUINavigator directionalUINavigator;

    public BattleDeploymentScreen() {
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object ...args) {
        CombatStateReflector csr = (CombatStateReflector) args[0];
        dui = new DeploymentUiReflector(csr.getDeploymentDialog());
        indicators = new ArrayList<>();
        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
        for(var btn : dui.getAllButtons()) {
            directionalObjects.add(new Pair<>(btn, null));
        }
        directionalUINavigator = new DirectionalUINavigator(directionalObjects);
        addDigitalJoystickHandler("Navigate", Joystick.DPad, directionalUINavigator);
        addButtonPressHandler("Select", LogicalButtons.A, (float advance) -> clickButton());
    }

    public void clickButton() {
        if(directionalUINavigator.getSelected() != null) {
            var pos = directionalUINavigator.getSelected().one.getPosition();
            InputShim.mouseMove((int) pos.getCenterX(), (int) pos.getCenterY());
            InputShim.mouseDownUp((int) pos.getCenterX(), (int) pos.getCenterY(), InputEventMouseButton.LEFT);
        }
    }

    Object getUiOnTop() {
        var panel = CombatStateReflector.GetInstance().getWidgetPanel();
        if (panel != null) {
            var items = UIPanelReflector.getChildItems(panel);
            if (!items.isEmpty()) {
                var lastChild = items.get(items.size() - 1);
                if(InputScreenManager.getInstance().getDisplayPanel() != null && lastChild == InputScreenManager.getInstance().getDisplayPanel().getSubpanel()) {
                    lastChild = items.get(items.size() - 2);
                }
                if(lastChild == dui.getDialogObject()) {
                    return null;
                }
                return lastChild;
            }
        }
        return null;
    }

    Object lastUIOnTop = null;
    @Override
    public void preInput(float advance) {
        if(Global.getCombatEngine().getCombatUI().isShowingDeploymentDialog()) {
            var lastChild = getUiOnTop();
            if(lastChild != lastUIOnTop && lastChild instanceof UIPanelAPI lastChildPanel) {
                lastUIOnTop = lastChild;
                // this nonsense tries to tell if the topmost UI element is the message box that shows up the first time we enter combat
                MessageBoxReflector dr = MessageBoxReflector.TryGet(lastChildPanel);
                if (dr != null) {
                    InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, MessageBoxScreen.ID, new Object[]{ dr, getId() });
                    return;
                }
            }
        } else {
            InputScreenManager.getInstance().transitionToScope(BattleScope.ID, Global.getCombatEngine());
            return;
        }
        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
        for(var btn : dui.getAllButtons()) {
            directionalObjects.add(new Pair<>(btn, null));
        }
        directionalUINavigator.setNavigationObjects(directionalObjects);
    }
}
