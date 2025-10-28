package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.coreui.P;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.CodexUI;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.inputhelper.MapInputHandler;
import ssms.controller.reflection.MapReflector;
import ssms.controller.reflection.UIPanelReflector;

import java.util.ArrayList;
import java.util.List;

public class MapTabUI extends InputScreenBase {
    public final static String ID = "MapTab";
    DirectionalUINavigator directionalUINavigator;

    MapReflector mapReflector;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void activate(Object ... args) {
        if(args.length > 0) {
            mapReflector = (MapReflector) args[0];
        }
        indicators = null;
        List<DirectionalUINavigator.NavigationObject> directionalObjects = new ArrayList<>(mapReflector.getButtons().stream().map(DirectionalUINavigator.NavigationObject::new).toList());
        directionalUINavigator = new DirectionalUINavigator(directionalObjects);
        directionalUINavigator.setMapComponent(mapReflector.getScroller());
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if (indicators == null) {
            indicators = new ArrayList<>();
            addDirectionalUINavigator(directionalUINavigator);
            var buttons = mapReflector.getChildButtons();
            if(buttons.size() == 4) {
                addButtonPressHandler("Go back", LogicalButtons.B, new KeySender(Keyboard.KEY_S, 's'));
            } else {
                addButtonPressHandler("Close dialog", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
            }
            addButtonPressHandler("Select cargo tab", LogicalButtons.BumperLeft, new KeySender(Keyboard.KEY_I, 'i'));
            addButtonPressHandler("Select intel tab", LogicalButtons.BumperRight, new KeySender(Keyboard.KEY_E, 'e'));
            if(directionalUINavigator.getCurContext() != DirectionalUINavigator.DirectionalUIContext.Map) {
                addButtonPressHandler("Select sector view", LogicalButtons.LeftTrigger, new KeySender(Keyboard.KEY_Q, 'q'));
                addButtonPressHandler("Select system view", LogicalButtons.RightTrigger, new KeySender(Keyboard.KEY_W, 'w'));
            }
        }
        return indicators;
    }

    @Override
    public void preInput(float amount) {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.MAP) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
            return;
        }
        if(isCodexOpen()) {
            InputScreenManager.getInstance().transitionDelayed(CodexUI.ID, getId());
        }
        if(directionalUINavigator != null) {
            directionalUINavigator.advance(amount);
        }
    }
}
