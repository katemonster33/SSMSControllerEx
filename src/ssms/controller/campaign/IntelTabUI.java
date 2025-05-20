package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CampaignEngine;
import com.fs.starfarer.campaign.comms.IntelTabData;
import org.lwjgl.input.Keyboard;
import ssms.controller.*;
import ssms.controller.reflection.ClassReflector;
import ssms.controller.reflection.FieldReflector;
import ssms.controller.reflection.IntelTabReflector;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class IntelTabUI extends InputScreenBase {
    public static final String ID = "IntelTab";
    IntelTabReflector intelTabReflector;
    IntelTabData intelTabData;
    IntelManagerAPI intelManagerAPI;
    List<Pair<Indicators, String>> indicators = null;
    int lastFrameSelectedIndex = -1;
    CampaignScope campaignScope;
    HandlerController controller;
    int planetSelected = -1;
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String[] getScopes() {
        return new String[] { CampaignScope.ID };
    }

    List<Pair<Indicators, String>> getIntelIndicators() {
        List<Pair<Indicators, String>> output = new ArrayList<>();
        output.add(new Pair<>(Indicators.LeftStick, "Navigate"));
        output.add(new Pair<>(Indicators.LeftTrigger, "Select prev mission"));
        output.add(new Pair<>(Indicators.RightTrigger, "Select next mission"));
        output.add(new Pair<>(Indicators.B, "Close"));
        return output;
    }

    List<Pair<Indicators, String>> getPlanetIndicators() {
        List<Pair<Indicators, String>> output = new ArrayList<>();
        output.add(new Pair<>(Indicators.LeftStick, "Navigate"));
        output.add(new Pair<>(Indicators.A, "Select"));
        output.add(new Pair<>(Indicators.B, "Close"));
        return output;
    }

    List<Pair<Indicators, String>> getFactionIndicators() {
        List<Pair<Indicators, String>> output = new ArrayList<>();
        output.add(new Pair<>(Indicators.LeftStick, "Navigate"));
        output.add(new Pair<>(Indicators.A, "Select"));
        output.add(new Pair<>(Indicators.B, "Close"));
        return output;
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = switch (intelTabData.getSelectedTabIndex()) {
                case 0 -> getIntelIndicators();
                case 1 -> getPlanetIndicators();
                case 2 -> getFactionIndicators();
                default -> new ArrayList<>();
            };
        }
        return indicators;
    }

    @Override
    public void activate(Object ... args) {
        intelTabReflector = (IntelTabReflector) args[0];
        intelTabData = CampaignEngine.getInstance().getUIData().getIntelData();
        lastFrameSelectedIndex = intelTabData.getSelectedTabIndex();
        campaignScope = (CampaignScope) InputScreenManager.getInstance().getCurrentScope();
        controller = (HandlerController) SSMSControllerModPluginEx.controller;
    }

    void hoverSelectedPlanet() {

    }

    void preInputIntelTab(float amount) {

    }

    void preInputPlanetTab(float amount) {
        var planets = intelTabReflector.getPlanetsTableRows();

        if(!planets.isEmpty()) {
            try {
                if (controller.getButtonEvent(HandlerController.Buttons.LeftStickUp) == 1) {
                    if (planetSelected == -1 || planetSelected > planets.size()) {
                        planetSelected = 0;
                    } else if (planetSelected < (planets.size() - 1)) {
                        planetSelected++;
                    }

                    intelTabReflector.ensurePlanetVisible(planets.get(planetSelected));
                } else if (controller.getButtonEvent(HandlerController.Buttons.LeftStickDown) == 1) {
                    if (planetSelected == -1 || planetSelected > planets.size()) {
                        planetSelected = 0;
                    } else if (planetSelected > 0) {
                        planetSelected--;
                    }
                    intelTabReflector.ensurePlanetVisible(planets.get(planetSelected));
                }
            } catch(Throwable ex) {
                Global.getLogger(getClass()).warn("Error!", ex);
            }
            //intelTabReflector.setSelectPlanetTableRow(planets.get(0), null, true);
        } else planetSelected = -1;
    }

    void preInputFactionTab(float amount) {

    }

    @Override
    public void preInput(float amount) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.INTEL) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
            return;
        }
        switch(intelTabData.getSelectedTabIndex()) {
            case 0 -> preInputIntelTab(amount);
            case 1 -> preInputPlanetTab(amount);
            case 2 -> preInputFactionTab(amount);
        }
        if(intelTabData.getSelectedTabIndex() != lastFrameSelectedIndex) {
            indicators = null;
            InputScreenManager.getInstance().refreshIndicators();
        }
        lastFrameSelectedIndex = intelTabData.getSelectedTabIndex();
        if(controller.getButtonEvent(HandlerController.Buttons.B) == 1) {
            InputShim.keyDownUp(Keyboard.KEY_ESCAPE, '\0');
        }
        campaignScope.handleInput(amount, true);
    }
}
