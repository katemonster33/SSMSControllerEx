package ssms.controller.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CampaignEngine;
import com.fs.starfarer.campaign.comms.IntelTabData;
import ssms.controller.Indicators;
import ssms.controller.InputScreenBase;
import ssms.controller.InputScreenManager;
import ssms.controller.reflection.IntelTabReflector;

import java.util.ArrayList;
import java.util.List;

public class IntelTabUI extends InputScreenBase {
    public static final String ID = "IntelTab";
    IntelTabReflector intelTabReflector;
    IntelTabData intelTabData;
    IntelManagerAPI intelManagerAPI;
    List<Pair<Indicators, String>> indicators = null;
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
        return output;
    }

    @Override
    public List<Pair<Indicators, String>> getIndicators() {
        if(indicators == null) {
            indicators = switch (intelTabData.getSelectedTabIndex()) {
                case 0 -> getIntelIndicators();
                default -> new ArrayList<>();
            };
        }
        return indicators;
    }

    @Override
    public void activate(Object ... args) {
        intelTabReflector = (IntelTabReflector) args[0];
        intelTabData = CampaignEngine.getInstance().getUIData().getIntelData();
    }

    @Override
    public void preInput(float amount) {
        if(Global.getSector().getCampaignUI().getCurrentCoreTab() != CoreUITabId.INTEL) {
            InputScreenManager.getInstance().transitionDelayed(MainCampaignUI.ID);
            return;
        }
    }
}
