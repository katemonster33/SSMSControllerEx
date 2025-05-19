package ssms.controller.campaign;

import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CampaignEngine;
import com.fs.starfarer.campaign.comms.IntelTabData;
import ssms.controller.Indicators;
import ssms.controller.InputScreenBase;
import ssms.controller.reflection.IntelTabReflector;

import java.util.ArrayList;
import java.util.List;

public class IntelTabUI extends InputScreenBase {
    public static final String ID = "IntelTab";
    IntelTabReflector intelTabReflector;
    IntelTabData intelTabData;
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
            indicators = new ArrayList<>();
            switch (intelTabData.getSelectedTabIndex()) {
                case 0 -> indicators = getIntelIndicators();
                case 1 -> indicators = null;
                case 2 -> indicators = null;
            }
        }
        return indicators;
    }

    @Override
    public void activate(Object ... args) {
        intelTabReflector = (IntelTabReflector) args[0];
        intelTabData = CampaignEngine.getInstance().getUIData().getIntelData();
    }
}
