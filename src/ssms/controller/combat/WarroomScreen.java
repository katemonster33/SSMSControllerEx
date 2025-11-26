package ssms.controller.combat;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.campaign.MainCampaignUI;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.CodexUI;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.inputhelper.MapInputHandler;
import ssms.controller.reflection.*;

import java.util.ArrayList;

public class WarroomScreen extends InputScreenBase {
    public static final String ID = "Warroom";
    CombatStateReflector csr;
    WarroomReflector warroomReflector;
    DirectionalUINavigator directionalUINavigator;
    MapInputHandler mapInputHandler;
    ViewportAPI viewportAPI;

    @Override
    public void activate(Object ... args) {
        csr = CombatStateReflector.GetInstance();
        warroomReflector = new WarroomReflector(csr.getWarroom());
        viewportAPI = Global.getCombatEngine().getViewport();

        indicators = new ArrayList<>();
        addButtonPressHandler("Pause", LogicalButtons.Start, (float advance) -> Global.getCombatEngine().setPaused(!Global.getCombatEngine().isPaused()));
        addButtonPressHandler("Show video feed", LogicalButtons.Select, (float advance) -> {
            csr.HideWarroom();
            csr.SetVideoFeedToPlayerShip();
        });
        mapInputHandler = addMapHandler(csr.getWarroom());
        addButtonPressHandler("Target point/object", LogicalButtons.X, (float advance) -> {
            var desiredMousePos = mapInputHandler.getDesiredMousePos();
            if(desiredMousePos != null) {
                InputShim.mouseDownUp((int) desiredMousePos.getX(), (int) desiredMousePos.getY(), InputEventMouseButton.RIGHT);
            }
        });
        addButtonPressHandler("Return to deployment screen", LogicalButtons.Y, new KeySender(Keyboard.KEY_G, 'G'));
        directionalUINavigator = new DirectionalUINavigator(warroomReflector.getChildButtons(true).stream().map(DirectionalUINavigator.NavigationObject::new).toList());
        addDigitalJoystickHandler("Select buttons", Joystick.DPad, directionalUINavigator);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String[] getScopes() {
        return new String[]{ BattleScope.ID };
    }

    @Override
    public void preInput(float advance) {
        if(Global.getCurrentState() == GameState.CAMPAIGN) {
            InputScreenManager.getInstance().transitionToScreen(MainCampaignUI.ID);
            return;
        }
        if(!Global.getCombatEngine().getCombatUI().isShowingCommandUI()) {
            InputScreenManager.getInstance().transitionDelayed(BattleSteeringScreen.ID);
        } else if(Global.getCombatEngine().getCombatUI().isShowingDeploymentDialog()) {
            InputScreenManager.getInstance().transitionToScope(InputScopeBase.ID, new Object[]{}, BattleDeploymentScreen.ID, new Object[]{ csr });
        }
        if(isCodexOpen()) {
            InputScreenManager.getInstance().transitionToScreen(CodexUI.ID, getId());
        }

        directionalUINavigator.setNavigationObjects(warroomReflector.getChildButtons(true).stream().map(DirectionalUINavigator.NavigationObject::new).toList());
        mapInputHandler.advance(advance);
    }
}
