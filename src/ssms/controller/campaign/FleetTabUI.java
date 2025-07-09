package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.ButtonPressOrHoldHandler;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.FleetTabReflector;
import ssms.controller.reflection.UIPanelReflector;

import java.util.ArrayList;
import java.util.List;

public class FleetTabUI extends InputScreenBase {
    public static final String ID = "FleetTab";
    FleetTabReflector fleetTabReflector;
    DirectionalUINavigator directionalUINavigator;
    Vector2f mousePos;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if (indicators == null) {
            indicators = new ArrayList<>();
            addDigitalJoystickHandler("Navigate", Joystick.DPad, directionalUINavigator);
            addButtonPressHandler("Select item", LogicalButtons.A, (float advance) ->
                    InputShim.mouseDownUp((int) mousePos.x, (int) mousePos.y, InputEventMouseButton.LEFT));
            addButtonPressHandler("Close Dialog", LogicalButtons.B, new KeySender(Keyboard.KEY_ESCAPE));
            addButtonPressHandler("Open Codex", LogicalButtons.Y, new KeySender(Keyboard.KEY_F2));
            addButtonPressHandler("More Info", LogicalButtons.X, new KeySender(Keyboard.KEY_F1));
            addButtonPressHandler("Select character tab", LogicalButtons.BumperLeft, new KeySender(Keyboard.KEY_C, 'c'));
            addButtonPressHandler("Select refit tab", LogicalButtons.BumperRight, new KeySender(Keyboard.KEY_R, 'r'));

        }
        return indicators;
    }

    @Override
    public void activate(Object... args) {
        this.fleetTabReflector = (FleetTabReflector) args[0];

        mousePos = new Vector2f(0.f, 0.f);

        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
        for(var btn : fleetTabReflector.getButtons()) {
            directionalObjects.add(new Pair<>(btn, null));
        }
        directionalUINavigator = new DirectionalUINavigator(directionalObjects){
            @Override
            public void onSelect(Pair<UIComponentAPI, Object> obj) {
                super.onSelect(obj);
                mousePos.set(obj.one.getPosition().getCenterX(), obj.one.getPosition().getCenterY());
            }
        };
        ControllerCrosshairRenderer.getControllerRenderer().enable();
        indicators = null;
    }

    @Override
    public void preInput(float amount) {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.FLEET) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
        }

        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
        for(var btn : fleetTabReflector.getButtons()) {
            directionalObjects.add(new Pair<>(btn, null));
        }
        directionalUINavigator.setNavigationObjects(directionalObjects);
    }
}
