package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.inputhelper.MapInputHandler;
import ssms.controller.reflection.MapReflector;
import ssms.controller.reflection.UIPanelReflector;

import java.util.ArrayList;
import java.util.List;

public class MapTabUI extends InputScreenBase {
    public final static String ID = "MapTab";
    ViewportAPI viewportAPI;
    MapInputHandler mapInputHandler;

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
        indicators = new ArrayList<>();
        mapInputHandler = addMapHandler(mapReflector.getMap());

        addButtonPressHandler("Close dialog", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
        for(var btn : mapReflector.getButtons()) {
            directionalObjects.add(new Pair<>(btn, null));
        }
        addDigitalJoystickHandler("Navigate", Joystick.DPad, new DirectionalUINavigator(directionalObjects));
        addButtonPressHandler("Select cargo tab", LogicalButtons.BumperLeft, new KeySender(Keyboard.KEY_I, 'i'));
        addButtonPressHandler("Select intel tab", LogicalButtons.BumperRight, new KeySender(Keyboard.KEY_E, 'e'));
        addButtonPressHandler("Select sector view", LogicalButtons.Y, new KeySender(Keyboard.KEY_Q, 'q'));
        addButtonPressHandler("Select system view", LogicalButtons.X, new KeySender(Keyboard.KEY_W, 'w'));
    }

    @Override
    public void preInput(float amount) {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.MAP) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
            return;
        }
        mapInputHandler.advance(amount);
    }
}
