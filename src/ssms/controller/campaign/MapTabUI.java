package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.inputhelper.MapInputHandler;
import ssms.controller.reflection.MapReflector;

import java.util.ArrayList;

public class MapTabUI extends InputScreenBase {
    public final static String ID = "MapTab";
    ViewportAPI viewportAPI;
    MapInputHandler mapInputHandler;
    CampaignScope campaignScope;

    MapReflector mapReflector;

    @Override
    public String getId() {
        return ID;
    }

    public String[] getScopes() {
        return new String[] { CampaignScope.ID };
    }

    @Override
    public void activate(Object ... args) {
        viewportAPI = Global.getSector().getViewport();
        mapReflector = (MapReflector) args[0];
        campaignScope = (CampaignScope) InputScreenManager.getInstance().getCurrentScope();
        indicators = new ArrayList<>();
        mapInputHandler = addMapHandler(viewportAPI);

        addButtonPressHandler("Close dialog", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
        addButtonPressHandler("Select cargo tab", LogicalButtons.BumperLeft, new KeySender(Keyboard.KEY_I, 'i'));
        addButtonPressHandler("Select intel tab", LogicalButtons.BumperRight, new KeySender(Keyboard.KEY_E, 'e'));
    }

    @Override
    public void preInput(float amount) {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.MAP) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
            return;
        }
        mapInputHandler.advance(amount);
        if(!mapInputHandler.getIsMovingMap()) {
            if (controller.getButtonEvent(LogicalButtons.B) == 1) {
                InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
            } else if (controller.getButtonEvent(LogicalButtons.LeftTrigger) == 1) {
                InputShim.keyDownUp(Keyboard.KEY_Q, 'q');
            } else if (controller.getButtonEvent(LogicalButtons.RightTrigger) == 1) {
                InputShim.keyDownUp(Keyboard.KEY_W, 'w');
            } else if (controller.getButtonEvent(LogicalButtons.BumperLeft) == 1) {
                InputShim.keyDownUp(Keyboard.KEY_I, 'i');
            } else if (controller.getButtonEvent(LogicalButtons.BumperRight) == 1) {
                InputShim.keyDownUp(Keyboard.KEY_E, 'e');
            }
        }
    }
}
