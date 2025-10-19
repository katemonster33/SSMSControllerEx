package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
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

    @Override
    public void activate(Object ... args) {
        viewportAPI = Global.getSector().getViewport();
        mapReflector = (MapReflector) args[0];
        indicators = new ArrayList<>();
        mapInputHandler = addMapHandler(mapReflector.getPanel());

        List<DirectionalUINavigator.NavigationObject> directionalObjects = new ArrayList<>(mapReflector.getButtons().stream().map(DirectionalUINavigator.NavigationObject::new).toList());
        addDigitalJoystickHandler("Navigate", Joystick.DPad, new DirectionalUINavigator(directionalObjects));
        var buttons = mapReflector.getChildButtons();
        if(buttons.size() == 4) {
            addButtonPressHandler("Go back", LogicalButtons.B, new KeySender(Keyboard.KEY_S, 's'));
        } else {
            addButtonPressHandler("Close dialog", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
        }
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
