package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.input.InputEventMouseButton;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CampaignEngine;
import com.fs.starfarer.campaign.comms.IntelTabData;
import com.fs.starfarer.campaign.ui.MarketConditionsWidget;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.inputhelper.DirectionalUINavigator;
import ssms.controller.inputhelper.KeySender;
import ssms.controller.inputhelper.MapInputHandler;
import ssms.controller.reflection.*;

import java.util.ArrayList;
import java.util.List;

public class IntelPlanetStarSystemUI extends InputScreenBase {
    public static final String ID = "IntelPlanetStarSystem";
    IntelTabReflector intelTabReflector;
    IntelTabData intelTabData;
    int lastFrameSelectedIndex = -1;
    CampaignScope campaignScope;
    IntelPlanetTabUi.PlanetTabReflector planetTabReflector;
    DirectionalUINavigator directionalUINavigator;
    UIComponentAPI mapComponent;
    List<ButtonAPI> planetAttributeButtons;
    List<ButtonAPI> planetListButtons;
    int selectedIndex = -1;
    Vector2f desiredMousePos = null;

    enum StarSystemTabFocusMode {
        PlanetAttributes,
        Map,
        PlanetList
    };
    StarSystemTabFocusMode currentTabFocus = StarSystemTabFocusMode.PlanetAttributes;
    MapInputHandler mapInputHandler;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String[] getScopes() {
        return new String[]{CampaignScope.ID};
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = new ArrayList<>();
            directionalUINavigator = new DirectionalUINavigator(new ArrayList<>()) {
                @Override
                public void onSelect(Pair<UIComponentAPI, Object> obj) {
                    super.onSelect(obj);
                    if(obj.one == mapComponent && currentTabFocus != StarSystemTabFocusMode.Map) {
                        currentTabFocus = StarSystemTabFocusMode.Map;
                        clearHandlers();
                    } else if(currentTabFocus != StarSystemTabFocusMode.PlanetAttributes && obj.one instanceof ButtonAPI buttonAPI && planetAttributeButtons.contains(buttonAPI)) {
                        currentTabFocus = StarSystemTabFocusMode.PlanetAttributes;
                        clearHandlers();
                    } else if(currentTabFocus != StarSystemTabFocusMode.PlanetList && obj.one instanceof ButtonAPI buttonAPI && planetListButtons.contains(buttonAPI)) {
                        currentTabFocus = StarSystemTabFocusMode.PlanetAttributes;
                        clearHandlers();
                    }
                }
            };
            addDigitalJoystickHandler("Navigate", Joystick.DPad, directionalUINavigator);
            indicators.add(new Pair<>(Indicators.LeftStick, "Navigate"));
            if(currentTabFocus == StarSystemTabFocusMode.Map) {
                mapInputHandler = addMapHandler(Global.getSector().getViewport());
            }
            addButtonPressHandler("Select intel tab", LogicalButtons.LeftTrigger, new KeySender(Keyboard.KEY_1, '1'));
            addButtonPressHandler("Select faction tab", LogicalButtons.RightTrigger, new KeySender(Keyboard.KEY_3, '3'));
            addButtonPressHandler("Show on map", LogicalButtons.X, new KeySender(Keyboard.KEY_S, 's'));
            addButtonPressHandler("Lay in course", LogicalButtons.Y, new KeySender(Keyboard.KEY_A, 'a'));
            if(currentTabFocus == StarSystemTabFocusMode.PlanetAttributes) {
                addButtonPressHandler("Open Codex", LogicalButtons.A, new KeySender(Keyboard.KEY_F2));
            } else {
                addButtonPressHandler("Select", LogicalButtons.A, (float advance) -> {
                    if(InputShim.getMouseX() != null && InputShim.getMouseY() != null) {
                        InputShim.mouseDownUp(InputShim.getMouseX(), InputShim.getMouseY(), InputEventMouseButton.LEFT);
                    }
                });
            }
            addButtonPressHandler("Return to planets list", LogicalButtons.B, new KeySender(Keyboard.KEY_Q, 'q'));
            indicators.addAll(campaignScope.getIndicators());
        }
        return indicators;
    }

    @Override
    public void activate(Object ... args) throws Throwable {
        intelTabReflector = (IntelTabReflector) args[0];
        intelTabData = CampaignEngine.getInstance().getUIData().getIntelData();
        lastFrameSelectedIndex = intelTabData.getSelectedTabIndex();
        currentTabFocus = StarSystemTabFocusMode.PlanetAttributes;
        desiredMousePos = null;
        campaignScope = (CampaignScope) InputScreenManager.getInstance().getCurrentScope();
        planetTabReflector = new IntelPlanetTabUi.PlanetTabReflector(intelTabReflector.getPlanetTabData());
        planetListButtons = new ArrayList<>();
        planetAttributeButtons = new ArrayList<>();
        mapComponent = planetTabReflector.getMap();
    }

    @Override
    public void preInput(float amount) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.INTEL) InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
        else if(intelTabData.getSelectedTabIndex() == 0) InputScreenManager.getInstance().transitionDelayed(IntelTabUI.ID, intelTabReflector);
        else if(intelTabData.getSelectedTabIndex() == 1 && planetTabReflector.getStarSystem() == null) InputScreenManager.getInstance().transitionDelayed(IntelPlanetTabUi.ID, intelTabReflector);
        else if(intelTabData.getSelectedTabIndex() == 2) InputScreenManager.getInstance().transitionDelayed(IntelFactionTabUi.ID, intelTabReflector);
        lastFrameSelectedIndex = intelTabData.getSelectedTabIndex();

        planetAttributeButtons = UIPanelReflector.getChildButtons(planetTabReflector.getPlanetInfoPanel());
        planetListButtons = UIPanelReflector.getChildButtons(planetTabReflector.getStarSystemDisplay());
        switch(currentTabFocus) {
            case PlanetAttributes -> preInputPlanetList(amount);
            case Map -> preInputMap(amount);
            case PlanetList -> preInputPlanetAttributes(amount);
        }
        campaignScope.handleInput(amount, true);
    }
}
