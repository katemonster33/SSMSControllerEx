package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.inputhelper.MapInputHandler;
import ssms.controller.reflection.MapReflector;

import java.util.ArrayList;
import java.util.List;

public class MapTabUI extends InputScreenBase {
    public final static String ID = "MapTab";
    ViewportAPI viewportAPI;
    MapInputHandler mapInputHandler;
    CampaignScope campaignScope;

    MapReflector mapReflector;

    public MapTabUI() {
        indicators = new ArrayList<>();
        indicators.add(new Pair<>(Indicators.LeftStick, "Move cursor"));
        indicators.add(new Pair<>(Indicators.LeftStick, "(press) Toggle map move"));
        indicators.add(new Pair<>(Indicators.A, "Select point"));
        indicators.add(new Pair<>(Indicators.B, "Close dialog"));
        indicators.add(new Pair<>(Indicators.LeftTrigger, "Open sector view"));
        indicators.add(new Pair<>(Indicators.B, "Open system view"));
        indicators.add(new Pair<>(Indicators.BumperLeft, "Select cargo tab"));
        indicators.add(new Pair<>(Indicators.BumperRight, "Select intel tab"));
    }

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
        mapInputHandler = new MapInputHandler(viewportAPI);
    }

    @Override
    public void preInput(float amount) {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.MAP) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
            return;
        }
        mapInputHandler.advance(amount);
        if(!mapInputHandler.getIsMovingMap()) {
            if (controller.getButtonEvent(Buttons.B) == 1) {
                InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
            } else if (controller.getButtonEvent(Buttons.LeftTrigger) == 1) {
                InputShim.keyDownUp(Keyboard.KEY_Q, 'q');
            } else if (controller.getButtonEvent(Buttons.RightTrigger) == 1) {
                InputShim.keyDownUp(Keyboard.KEY_W, 'w');
            } else if (controller.getButtonEvent(Buttons.BumperLeft) == 1) {
                InputShim.keyDownUp(Keyboard.KEY_I, 'i');
            } else if (controller.getButtonEvent(Buttons.BumperRight) == 1) {
                InputShim.keyDownUp(Keyboard.KEY_E, 'e');
            }
        }
    }
}
