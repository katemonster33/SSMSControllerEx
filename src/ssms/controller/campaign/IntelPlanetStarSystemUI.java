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
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.enums.Indicators;
import ssms.controller.enums.Joystick;
import ssms.controller.enums.LogicalButtons;
import ssms.controller.generic.CodexUI;
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

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = new ArrayList<>();
            if(directionalUINavigator == null) {
                var planetAttributeButtons = new UIPanelReflector(planetTabReflector.getPlanetInfoPanel()).getChildButtons(true);
                var planetListButtons = new UIPanelReflector(planetTabReflector.getStarSystemDisplay()).getChildButtons();
                List<DirectionalUINavigator.NavigationObject> buttonList = new ArrayList<>(planetListButtons.stream().map(DirectionalUINavigator.NavigationObject::new).toList());
                buttonList.addAll(planetAttributeButtons.stream().map(DirectionalUINavigator.NavigationObject::new).toList());
                buttonList.add(new DirectionalUINavigator.NavigationObject(mapComponent));
                directionalUINavigator = new DirectionalUINavigator(buttonList);
                directionalUINavigator.setMapComponent(mapComponent);
            }
            addDirectionalUINavigator(directionalUINavigator);
            indicators.add(new Pair<>(Indicators.LeftStick, "Navigate"));
            if(directionalUINavigator.getCurContext() != DirectionalUINavigator.DirectionalUIContext.Map) {
                addButtonPressHandler("Select intel tab", LogicalButtons.LeftTrigger, new KeySender(Keyboard.KEY_1, '1'));
                addButtonPressHandler("Select faction tab", LogicalButtons.RightTrigger, new KeySender(Keyboard.KEY_3, '3'));
            }
            addButtonPressOrHoldHandler("More Info", "Open Codex", LogicalButtons.Y, new ButtonPressOrHoldHandler() {
                @Override
                public void performHoldAction(float advance) {
                    InputShim.keyDownUp(Keyboard.KEY_F2, '\0');
                }

                @Override
                public void performPressAction(float advance) {
                    InputShim.keyDownUp(Keyboard.KEY_F1, '\0');
                }
            });
            addButtonPressHandler("Return to planets list", LogicalButtons.B, new KeySender(Keyboard.KEY_Q, 'q'));
            addButtonPressHandler("Select map tab", LogicalButtons.BumperLeft, new KeySender(Keyboard.KEY_TAB));
            addButtonPressHandler("Select command tab", LogicalButtons.BumperRight, new KeySender(Keyboard.KEY_D, 'd'));
        }
        return indicators;
    }

    @Override
    public UIPanelAPI getPanelForIndicators() {
        return planetTabReflector.getPanel();
    }

    @Override
    public void activate(Object ... args) throws Throwable {
        if(args.length > 0) {
            intelTabReflector = (IntelTabReflector) args[0];
            intelTabData = CampaignEngine.getInstance().getUIData().getIntelData();
            planetTabReflector = new IntelPlanetTabUi.PlanetTabReflector(intelTabReflector.getPlanetTabData().getPanel());
            mapComponent = planetTabReflector.getMap();
        }
        indicators = null;
        directionalUINavigator = null;
    }

    @Override
    public void preInput(float amount) {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.INTEL) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
        } else if (intelTabData.getSelectedTabIndex() == 0) {
            InputScreenManager.getInstance().transitionDelayed(IntelTabUI.ID, intelTabReflector);
        } else if (intelTabData.getSelectedTabIndex() == 1 && planetTabReflector.getStarSystem() == null) {
            InputScreenManager.getInstance().transitionDelayed(IntelPlanetTabUi.ID, intelTabReflector);
        } else if (intelTabData.getSelectedTabIndex() == 2) {
            InputScreenManager.getInstance().transitionDelayed(IntelFactionTabUi.ID, intelTabReflector);
        }
        if(isCodexOpen()) {
            InputScreenManager.getInstance().transitionDelayed(CodexUI.ID, getId());
        }

        // intel tab was recreated due to user clicking the map, this is dumb and I hate it
        if (intelTabReflector.getBorderedPanel().getParent() == null) {
            var coreUi = intelTabReflector.getBorderedPanel().getCoreUIAPI();
            for(var coreUiChild : new UIPanelReflector((UIPanelAPI) coreUi).getChildPanels()) {
                var newBorderedPanel = BorderedPanelReflector.TryGet(coreUi, coreUiChild);
                if (newBorderedPanel != null) {
                    intelTabReflector = IntelTabReflector.TryGet(newBorderedPanel.getCoreUIAPI(), newBorderedPanel);
                    if (intelTabReflector != null) {
                        planetTabReflector = IntelPlanetTabUi.PlanetTabReflector.tryGet(intelTabReflector);
                        refreshIndicators();
                        directionalUINavigator = null;
                    }
                }
            }
        }
        if(directionalUINavigator != null) {
            directionalUINavigator.advance(amount);
        }
    }
}
