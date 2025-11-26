package ssms.controller.combat;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import ssms.controller.*;
import ssms.controller.campaign.MainCampaignUI;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.CodexUI;
import ssms.controller.generic.MessageBoxScreen;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.reflection.*;

import java.util.ArrayList;

public class BattleDeploymentScreen extends InputScreenBase {
    public static final String ID = "BattleDeployment";
    DeploymentUiReflector dui; // hehe, dui
    DirectionalUINavigator directionalUINavigator;
    UIPanelReflector widgetPanelReflector;

    public BattleDeploymentScreen() {
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object ...args) {
        if(args.length > 0) {
            CombatStateReflector csr = (CombatStateReflector) args[0];
            dui = new DeploymentUiReflector(csr.getDeploymentDialog());
            widgetPanelReflector = new UIPanelReflector(CombatStateReflector.GetInstance().getWidgetPanel());
        }
        indicators = new ArrayList<>();
        directionalUINavigator = new DirectionalUINavigator(dui.getAllButtons().stream().map(DirectionalUINavigator.NavigationObject::new).toList());
        addDigitalJoystickHandler("Navigate", Joystick.DPad, directionalUINavigator);
        addButtonPressHandler("Select", LogicalButtons.A, (float advance) -> clickButton());
    }

    public void clickButton() {
        if(directionalUINavigator.getSelected() != null) {
            var sel = directionalUINavigator.getSelected();
            InputShim.mouseMove((int) sel.getCenterX(), (int) sel.getCenterY());
            InputShim.mouseDownUp((int) sel.getCenterX(), (int) sel.getCenterY(), InputEventMouseButton.LEFT);
        }
    }

    UIComponentAPI lastUIOnTop = null;
    @Override
    public void preInput(float advance) {
        if(Global.getCurrentState() == GameState.CAMPAIGN) {
            InputScreenManager.getInstance().transitionToScreen(MainCampaignUI.ID);
            return;
        }
        if(Global.getCombatEngine().getCombatUI().isShowingDeploymentDialog()) {
            var lastChild = widgetPanelReflector.getLastChild();
            if(lastChild != lastUIOnTop && lastChild != dui.getDialogObject() && lastChild instanceof UIPanelAPI lastChildPanel) {
                lastUIOnTop = lastChild;
                // this nonsense tries to tell if the topmost UI element is the message box that shows up the first time we enter combat
                if (MessageBoxReflector.isMsgBox(lastChildPanel)) {
                    InputScreenManager.getInstance().transitionToScreen(MessageBoxScreen.ID, new MessageBoxReflector(lastChildPanel), getId());
                    return;
                }
            }
        } else {
            InputScreenManager.getInstance().transitionToScope(BattleScope.ID, Global.getCombatEngine());
            return;
        }
        if(isCodexOpen()) {
            InputScreenManager.getInstance().transitionToScreen(CodexUI.ID, getId());
        }
        directionalUINavigator.setNavigationObjects(dui.getAllButtons().stream().map(DirectionalUINavigator.NavigationObject::new).toList());
    }
}
