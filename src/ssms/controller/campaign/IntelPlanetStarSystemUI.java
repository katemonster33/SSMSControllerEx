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
import ssms.controller.inputhelper.ButtonPressOrHoldHandler;
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
    IntelPlanetTabUi.PlanetTabReflector planetTabReflector;
    DirectionalUINavigator directionalUINavigator;
    UIComponentAPI mapComponent;
    List<ButtonAPI> planetAttributeButtons;
    List<ButtonAPI> planetListButtons;

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
    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = new ArrayList<>();
            if(directionalUINavigator == null) {
                planetAttributeButtons = UIPanelReflector.getChildButtons(planetTabReflector.getPlanetInfoPanel());
                planetListButtons = UIPanelReflector.getChildButtons(planetTabReflector.getStarSystemDisplay());
                List<UIComponentAPI> buttonList = new ArrayList<>(planetListButtons);
                //buttonList.addAll(planetAttributeButtons);
                buttonList.add(mapComponent);
                List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
                for(var btn : buttonList) {
                    directionalObjects.add(new Pair<>(btn, null));
                }
                directionalUINavigator = new DirectionalUINavigator(directionalObjects) {
                    @Override
                    public void onSelect(Pair<UIComponentAPI, Object> obj) {
                        super.onSelect(obj);
                        if (obj.one == mapComponent && currentTabFocus != StarSystemTabFocusMode.Map) {
                            currentTabFocus = StarSystemTabFocusMode.Map;
                            clearHandlers();
                            InputScreenManager.getInstance().refreshIndicators();
                        } else if (currentTabFocus != StarSystemTabFocusMode.PlanetAttributes && obj.one != mapComponent) {
                            currentTabFocus = StarSystemTabFocusMode.PlanetAttributes;
                            clearHandlers();
                            InputScreenManager.getInstance().refreshIndicators();
                        }
                    }
                };
            }
            addDigitalJoystickHandler("Navigate", Joystick.DPad, directionalUINavigator);
            indicators.add(new Pair<>(Indicators.LeftStick, "Navigate"));
            if(currentTabFocus == StarSystemTabFocusMode.Map) {
                mapInputHandler = addMapHandler(planetTabReflector.getMap());
            } else {
                addButtonPressHandler("Select intel tab", LogicalButtons.LeftTrigger, new KeySender(Keyboard.KEY_1, '1'));
                addButtonPressHandler("Select faction tab", LogicalButtons.RightTrigger, new KeySender(Keyboard.KEY_3, '3'));
            }
            addButtonPressHandler("Show on map", LogicalButtons.X, new KeySender(Keyboard.KEY_S, 's'));
            addButtonPressHandler("Lay in course", LogicalButtons.Y, new KeySender(Keyboard.KEY_A, 'a'));
            if(currentTabFocus == StarSystemTabFocusMode.PlanetAttributes) {
                addButtonPressOrHoldHandler("Select", "Open Codex", LogicalButtons.A, new ButtonPressOrHoldHandler() {
                    @Override
                    public void performHoldAction(float advance) {
                        InputShim.keyDownUp(Keyboard.KEY_F2, '\0');
                    }

                    @Override
                    public void performPressAction(float advance) {
                        if(InputShim.getMouseX() != null && InputShim.getMouseY() != null) {
                            InputShim.mouseDownUp(InputShim.getMouseX(), InputShim.getMouseY(), InputEventMouseButton.LEFT);
                        }
                    }
                });
            } else {
                addButtonPressHandler("Select", LogicalButtons.A, (float advance) -> {
                    if(InputShim.getMouseX() != null && InputShim.getMouseY() != null) {
                        InputShim.mouseDownUp(InputShim.getMouseX(), InputShim.getMouseY(), InputEventMouseButton.LEFT);
                    }
                });
            }
            addButtonPressHandler("Return to planets list", LogicalButtons.B, new KeySender(Keyboard.KEY_Q, 'q'));
            addButtonPressHandler("Select map tab", LogicalButtons.BumperLeft, new KeySender(Keyboard.KEY_TAB));
            addButtonPressHandler("Select command tab", LogicalButtons.BumperRight, new KeySender(Keyboard.KEY_D, 'd'));
        }
        return indicators;
    }

    @Override
    public UIPanelAPI getPanelForIndicators() {
        return planetTabReflector.planetTabData();
    }

    @Override
    public void activate(Object ... args) throws Throwable {
        intelTabReflector = (IntelTabReflector) args[0];
        intelTabData = CampaignEngine.getInstance().getUIData().getIntelData();
        currentTabFocus = StarSystemTabFocusMode.PlanetAttributes;
        planetTabReflector = new IntelPlanetTabUi.PlanetTabReflector(intelTabReflector.getPlanetTabData());
        planetListButtons = new ArrayList<>();
        planetAttributeButtons = new ArrayList<>();
        mapComponent = planetTabReflector.getMap();
        indicators = null;
        directionalUINavigator = null;
    }

    @Override
    public void preInput(float amount) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.INTEL) InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
        else if(intelTabData.getSelectedTabIndex() == 0) InputScreenManager.getInstance().transitionDelayed(IntelTabUI.ID, intelTabReflector);
        else if(intelTabData.getSelectedTabIndex() == 1 && planetTabReflector.getStarSystem() == null) InputScreenManager.getInstance().transitionDelayed(IntelPlanetTabUi.ID, intelTabReflector);
        else if(intelTabData.getSelectedTabIndex() == 2) InputScreenManager.getInstance().transitionDelayed(IntelFactionTabUi.ID, intelTabReflector);

//        if(directionalUINavigator == null) return;
//        planetAttributeButtons = UIPanelReflector.getChildButtons(planetTabReflector.getPlanetInfoPanel());
//        planetListButtons = UIPanelReflector.getChildButtons(planetTabReflector.getStarSystemDisplay());
//        List<UIComponentAPI> buttonList = new ArrayList<>(planetListButtons);
//        //buttonList.addAll(planetAttributeButtons);
//        buttonList.add(mapComponent);
//        List<Pair<UIComponentAPI, Object>> directionalObjects = new ArrayList<>();
//        for(var btn : buttonList) {
//            directionalObjects.add(new Pair<>(btn, null));
//        }
//        directionalUINavigator.setNavigationObjects(directionalObjects);

        if(mapInputHandler != null) {
            mapInputHandler.advance(amount);
        }
    }
}
