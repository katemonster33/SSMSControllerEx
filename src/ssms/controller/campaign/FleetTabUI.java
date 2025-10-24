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
import ssms.controller.generic.CodexUI;
import ssms.controller.inputhelper.ButtonPressOrHoldHandler;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.reflection.FleetTabReflector;
import ssms.controller.reflection.UIPanelReflector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            addDirectionalUINavigator(directionalUINavigator);
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

        List<DirectionalUINavigator.NavigationObject> directionalObjects = fleetTabReflector.getButtons().stream().map(DirectionalUINavigator.NavigationObject::new).toList();
        directionalUINavigator = new DirectionalUINavigator(directionalObjects){
            @Override
            public void onSelect(NavigationObject obj) {
                super.onSelect(obj);
                mousePos.set(obj.getCenterX(), obj.getCenterY());
            }
        };
        indicators = null;
    }

    @Override
    public void preInput(float amount) {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.FLEET) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
        }
        var codex = tryGetCodexDialog();
        if(codex != null) {
            InputScreenManager.getInstance().transitionToScreen(CodexUI.ID, getId());
        }

        var directionalObjects = fleetTabReflector.getButtons().stream().map(DirectionalUINavigator.NavigationObject::new).collect(Collectors.toList());
        for(var shipItem : fleetTabReflector.getItems()) {
            if(shipItem instanceof UIPanelAPI shipPanel) {
                UIPanelReflector shipPanelReflector = new UIPanelReflector(shipPanel);
                var children = shipPanelReflector.getChildItems();
                if(children.get(0) instanceof UIPanelAPI fleetMemberViewPanel) {
                    UIPanelReflector fleetMemberReflector = new UIPanelReflector(fleetMemberViewPanel);
                    var fleetMemberComponents = fleetMemberReflector.getChildItems();
                    if(fleetMemberComponents.size() > 2) {
                        var fleetMemberView = (UIComponentAPI) fleetMemberComponents.get(2);
                        float newX1 = fleetMemberView.getPosition().getCenterX() - 50.f, newY1 = fleetMemberView.getPosition().getCenterY() - 50.f;
                        directionalObjects.add(new DirectionalUINavigator.NavigationObject(fleetMemberView, newX1, newX1 + 100.f, newY1, newY1 + 100.f));
                    }
                }
                for(int i = 2; i < children.size(); i++) {
                    if(children.get(i) instanceof UIPanelAPI uiPanelAPI) {
                        for(var btn : new UIPanelReflector(uiPanelAPI).getChildButtons(true)) {
                            directionalObjects.add(new DirectionalUINavigator.NavigationObject(btn));
                        }
                    }
                }
            }
        }
        directionalUINavigator.setNavigationObjects(directionalObjects);
        directionalUINavigator.advance(amount);
    }
}
